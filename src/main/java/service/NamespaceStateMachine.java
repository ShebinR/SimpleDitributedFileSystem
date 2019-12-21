package service;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.alipay.sofa.jraft.util.Utils;
import namespace.DirInode;
import namespace.FileInode;
import namespace.Namespace;
import snapshot.NamespaceSnapshot;
import util.Constants;
import utilization.ClusterUtilization;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static service.NamespaceOperation.*;

public class NamespaceStateMachine extends StateMachineAdapter {

    private AtomicInteger value = new AtomicInteger(10);
    private AtomicLong leaderTerm = new AtomicLong(-1);
    private Namespace namespace = new Namespace();

    private boolean updateCluster = false;
    private boolean updateElectionTimeout = false;
    private boolean updatePrevElectionInfo = false;
    private long prevElectionTime;
    private int numberOfElections;
    private int currElectionTimeout;

    public long start;
    public boolean nodeStartup;
    public long snapshotLoadStart;
    public long snapshotLoadEnd;
    public long snapshotLoadCount;
    public long snapshotSaveStart;
    public long snapshotSaveEnd;
    public long snapshotSaveCount;
    public Map<String, Long> metrics = new HashMap<>();

    public String getMetrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Startup Time : " + metrics.get("StartupTime") + "\n");
        sb.append("SNAPSHOT LOAD : \n");
        for(Map.Entry<String, Long> entry: metrics.entrySet()) {
            if(entry.getKey().contains("Snapshot Load"))
                sb.append(entry.getKey() + " : " + entry.getValue() + " ms\n");
        }

        sb.append("\nSNAPSHOT SAVE : \n");
        for(Map.Entry<String, Long> entry: metrics.entrySet()) {
            if(entry.getKey().contains("Snapshot Save"))
                sb.append(entry.getKey() + " : " + entry.getValue() + " ms\n");
        }

        return sb.toString();
    }
    public boolean isUpdateElectionTimeout() {
        return updateElectionTimeout;
    }

    public void setUpdateElectionTimeout(boolean updateElectionTimeout) {
        this.updateElectionTimeout = updateElectionTimeout;
    }

    public boolean isUpdatePrevElectionInfo() {
        return updatePrevElectionInfo;
    }

    public void setUpdatePrevElectionInfo(boolean updatePrevElectionInfo) {
        this.updatePrevElectionInfo = updatePrevElectionInfo;
    }

    public long getPrevElectionTime() {
        return prevElectionTime;
    }

    public void setPrevElectionTime(long prevElectionTime) {
        this.prevElectionTime = prevElectionTime;
    }

    public int getNumberOfElections() {
        return numberOfElections;
    }

    public void setNumberOfElections(int numberOfElections) {
        this.numberOfElections = numberOfElections;
    }

    public int getCurrElectionTimeout() {
        return currElectionTimeout;
    }

    public void setCurrElectionTimeout(int currElectionTimeout) {
        this.currElectionTimeout = currElectionTimeout;
    }

    public boolean getUpdateCluster() {
        return updateCluster;
    }

    public void unsetUpdateCluster() {
        this.updateCluster = false;
    }

    public int getValue(){
        return this.value.getAndAdd(1);
    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    public Namespace getNamespace(){
        return this.namespace;
    }

    public void renewClusterUtilization(ClusterUtilization utilization) {
        namespace.traverseUpdateUtilization(utilization);
    }

    @Override
    public void onApply(Iterator iterator) {
//        System.out.println();
        boolean leader = false;
        while(iterator.hasNext()){
            int value = 0;

            NamespaceOperation namespaceOperation = null;
            NamespaceClosure closure = null;

//            System.out.println();

            if(iterator.done() != null){
                closure = (NamespaceClosure)iterator.done();
                namespaceOperation = closure.getNamespaceOperation();
                leader = true;
            }else{
                final ByteBuffer data = iterator.getData();

                try{
                    namespaceOperation = SerializerManager.getSerializer(SerializerManager.Hessian2)
                            .deserialize(data.array(), NamespaceOperation.class.getName());
                }catch (CodecException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("APPLY : " + Constants.NAMESPACE_OPN.get(namespaceOperation.getOp()));
            if(namespaceOperation != null){
                boolean status;
                int respCode;
//                System.out.println("OP : " + namespaceOperation.getOp());

                switch (namespaceOperation.getOp()) {
                    case GET:
//                        System.out.println("GET REQ");
                        value = this.value.getAndAdd(1);
                        System.out.printf("Get Value = {%d} at logIndex = {%d}", value, iterator.getIndex());
                        if(closure != null){
                            closure.success(value);
                            closure.run(Status.OK());
                        }
                        break;
                    case CREATE:
//                        System.out.println("CREATE REQ");
                        byte ret = this.namespace.create(
                                namespaceOperation.getPath(),
                                namespaceOperation.getFileType());

//                        //System.out.println("Create Response : " + namespace.printTree());
                        if(closure != null) {
                            String message = Constants.CREATE_MESSAGE.get(ret);
                            status = ret == Constants.CREATE_SUCCESS;
                            closure.success(status, message, ret);
//                            System.out.println("\nUpdating closure status!");
                            closure.run(Status.OK());
                        }
                        break;
                    case REMOVE:
//                        System.out.println("REMOVE REQ");
                        List<List<String>> blocks = new ArrayList<>();
                        status = this.namespace.remove(
                                namespaceOperation.getPath(),
                                namespaceOperation.getFileType(),
                                blocks);
//                        //System.out.println("Remove Response : " + namespace.printTree());
                        if(closure != null) {
                            byte retCode = status ? (namespaceOperation.getFileType() == Constants.TYPE_FILE ?
                                    Constants.REMOVE_FILE_SUCCESS : Constants.REMOVE_DIR_SUCCESS)
                                    : Constants.REMOVE_FAILURE;
                            closure.removeResponseMessage(status, Constants.REMOVE_MESSAGE.get(retCode), retCode, blocks);
//                            System.out.println("\nUpdating closure status!");
                            closure.run(Status.OK());
                        }
                        break;
                    case RENAME:
//                        System.out.println("RENAME REQ");
                        ret = this.namespace.rename(
                                namespaceOperation.getSrcPath(),
                                namespaceOperation.getDestPath(),
                                namespaceOperation.getFileType());
//                        //System.out.println("Rename Response : " + namespace.printTree());
                        if(closure != null) {
                            String message = Constants.RENAME_MESSAGE.get(ret);
                            status = ret == Constants.RENAME_SUCCESS;
                            closure.renameResponseMessage(message, status, (int)ret);
//                            System.out.println("\nUpdating closure status!");
                            closure.run(Status.OK());
                        }
                        break;
                    case WRITE:
//                        System.out.println("WRITE REQ");
                        FileInode fileHandle = this.namespace.write(
                                namespaceOperation.getPath(),
                                namespaceOperation.getBlockSize(),
                                namespaceOperation.getBlockLocations());
                        if(closure != null) {
                            respCode = (fileHandle == null) ? Constants.WRITE_UPDATE_FAILED : Constants.WRITE_SUCCESS;
//                            //System.out.println("Write response - " + Constants.WRITE_MESSAGE.get((byte)respCode));
//                            //System.out.println("Name space block size : " + namespaceOperation.getBlockSize());
                            String blockId = fileHandle.getAddress().get(fileHandle.getAddress().size()-1).get(0  ).getBlockId();
                            List<String> blockLocations = namespace.getBlockLocationsFromInode(fileHandle, fileHandle.getAddress().size()-1);
                            closure.writeResponseMessage(
                                    Constants.WRITE_MESSAGE.get((byte)respCode),
                                    !(fileHandle == null),
                                    respCode,
                                    blockLocations,
                                    blockId,
                                    namespaceOperation.getBlockSize());
//                            System.out.println("\nUpdating closure status!");
                            closure.run(Status.OK());
                        }
                        break;
                    case UPDATE_ELECTION_TIMEOUT:
//                        System.out.println("UPDATE_ELECTION_TIMEOUT REQ");
//                        if(currElectionTimeout == 0)
//                            currElectionTimeout = Constants.ELECTION_TIMEOUT_MILLIS;
//                        else
//                            currElectionTimeout = currElectionTimeout * 2;
                        currElectionTimeout = namespaceOperation.getCurrElectionTimeout();

                        prevElectionTime = namespaceOperation.getPrevElectionTime();
                        numberOfElections = namespaceOperation.getNumberOfElections();

//                        System.out.println("Updated!\n currElectionTimeout - " + currElectionTimeout);
//                        System.out.println("Previous Election time - " + prevElectionTime);
//                        System.out.println("Number of elections " + numberOfElections);

                        if(closure != null){
                             closure.run(Status.OK());
                        }
                        break;
                    case UPDATE_PREV_ELECTION_INFO:
//                        System.out.println("UPDATE_PREV_ELECTION_INFO REQ");
                        prevElectionTime = namespaceOperation.getPrevElectionTime();
                        numberOfElections = namespaceOperation.getNumberOfElections();

//                        System.out.println("Updated! \n Previous Election time - " + prevElectionTime);
//                        System.out.println("Number of elections " + numberOfElections);

                        if(closure != null){
                            closure.run(Status.OK());
                        }
                        break;
                    default:
//                        System.out.println("UNKNOWN REQ");
//                        System.out.println("Not implemented yet!");
                        break;
                }
            }
            iterator.next();
        }
        if(nodeStartup) {
//            System.out.println("==============================");
            long end = System.currentTimeMillis();
//            System.out.println("Time for STARTUP : " + ((end - start)) + " ms");
            metrics.put("StartupTime", (end - start));
            nodeStartup = false;
//            System.out.println("==============================");
        }
//        System.out.println();
    }

    @Override
    public void onLeaderStart(final long term) {
        this.leaderTerm.set(term);
        super.onLeaderStart(term);
//        System.out.println("Starting leader! Setting to update cluster utilization!");
        this.updateCluster = true;

        numberOfElections++;

        if(currElectionTimeout == 0){
//            System.out.println("First leader!");
            currElectionTimeout = Constants.ELECTION_TIMEOUT_MILLIS;
            prevElectionTime = System.currentTimeMillis();
//            System.out.println("prev election time " + prevElectionTime);
            //Setting updateElectionTimeout will update both the timeout and prev time in all nodes
            updateElectionTimeout = true;
        }else{
//            System.out.println("New leader!");
            updateElectionTimeout = checkIfNodesAreFlaky();
            if(!updateElectionTimeout){
                updatePrevElectionInfo = true;
            }
        }

    }

    private boolean checkIfNodesAreFlaky(){
//        System.out.println("Checking if the nodes are flaky");
//        System.out.println("Curr election timeout " + currElectionTimeout);
//        System.out.println("Previous election time" + prevElectionTime);
//        System.out.println("Number of elections " + numberOfElections);
        long currTime = System.currentTimeMillis();
        boolean result = false;
        do{
//            System.out.println("Time diff - " + (currTime - prevElectionTime));
            if(currTime - prevElectionTime < Constants.MAX_ELECTION_INTERVAL_MILLIS &&
                    numberOfElections > Constants.MAX_ELECTION_COUNT){
                if(currElectionTimeout * 2 < Constants.MAX_ELECTION_TIMEOUT_MILLIS) {
                    currElectionTimeout = currElectionTimeout * 2;
                    result = true;
                }
            }

            if(currTime - prevElectionTime > Constants.MAX_ELECTION_INTERVAL_MILLIS &&
                currElectionTimeout != Constants.ELECTION_TIMEOUT_MILLIS) {

                currElectionTimeout = currElectionTimeout / 2;
                result = true;
            }
        }while(false);
        prevElectionTime = currTime;
//        System.out.println("Result - " + result);
        return result;
    }

    @Override
    public void onLeaderStop(final Status status) {
        this.leaderTerm.set(-1);
        super.onLeaderStop(status);
    }

    @Override
    public void onSnapshotSave(final SnapshotWriter writer, final Closure done) {
        if(!Constants.SNAPSHOT_ON)
            return;
        snapshotSaveStart = System.currentTimeMillis();
//        System.out.println("Initiating snapshot!");
        final String serializedTree = this.namespace.serializeNameSpace();
        Utils.runInThread(() -> {
            final NamespaceSnapshot snapshot = new NamespaceSnapshot(writer.getPath() + File.separator + "data");
            if (snapshot.save(serializedTree)) {
                if (writer.addFile("data")) {
                    done.run(Status.OK());
                } else {
                    done.run(new Status(RaftError.EIO, "Fail to add file to writer"));
                }
            } else {
                done.run(new Status(RaftError.EIO, "Fail to save counter snapshot %s", snapshot.getPath()));
            }
        });
        snapshotSaveEnd = System.currentTimeMillis();
        snapshotSaveCount++;
        metrics.put("Snapshot Save " + snapshotSaveCount, (snapshotSaveEnd - snapshotSaveStart));
    }

    @Override
    public boolean onSnapshotLoad(final SnapshotReader reader) {
        if(!Constants.SNAPSHOT_ON)
            return true;
        snapshotLoadStart = System.currentTimeMillis();
        if (isLeader()) {
//            System.out.println("Leader is not supposed to load snapshot");
            return false;
        }
        if (reader.getFileMeta("data") == null) {
//            System.out.println("Fail to find data file in " +  reader.getPath());
            return false;
        }
        final NamespaceSnapshot snapshot = new NamespaceSnapshot(reader.getPath() + File.separator + "data");
        try {
            this.namespace.setRoot((DirInode) snapshot.load());
            snapshotLoadEnd = System.currentTimeMillis();
            snapshotLoadCount++;
            metrics.put("Snapshot Load " + snapshotLoadCount, (snapshotLoadEnd - snapshotLoadStart));
            return true;
        } catch (final IOException e) {
//            System.out.println("Fail to load snapshot from " + snapshot.getPath());
            return false;
        }
    }
}

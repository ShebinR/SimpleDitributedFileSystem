package service;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.rhea.StoreEngineHelper;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import namespace.BlockDetails;
import namespace.FileInode;
import namespace.Inode;
import util.Constants;
import utilization.ClusterUtilization;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

public class NamespaceServiceImpl implements NamespaceService {
    private final NamespaceServer namespaceServer;
    private final Executor readIndexExecutor;
    private int currentUpdates;

    public NamespaceServiceImpl(NamespaceServer namespaceServer) {
        this.namespaceServer = namespaceServer;
        this.readIndexExecutor = createReadIndexExecutor();
        this.currentUpdates = 0;
    }

    private Executor createReadIndexExecutor(){
        final StoreEngineOptions opts = new StoreEngineOptions();
        return StoreEngineHelper.createReadIndexExecutor(opts.getReadIndexCoreThreads());
    }

    private boolean isLeader() {
        return this.namespaceServer.getNsm().isLeader();
    }

    public int getCurrentUpdates() {
        return currentUpdates;
    }

    public void setCurrentUpdates(int currentUpdates) {
        this.currentUpdates = currentUpdates;
    }

    private void applyOperation(final NamespaceOperation op, final NamespaceClosure closure){
//        System.out.println("Apply operation requested : " + op.getOp());
        this.currentUpdates++;
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        try {
            closure.setNamespaceOperation(op);
            final Task task = new Task();
            task.setData(ByteBuffer.wrap(SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(op)));
            task.setDone(closure);
//            System.out.println("Created task. Cally onApply() on each raft server");
            this.namespaceServer.getNode().apply(task);
//            System.out.println("Returned from onApply()");
        } catch (CodecException e) {
            String errorMsg = "Fail to encode Namespace operation";
            closure.failure(errorMsg, Constants.RESP_CODE_ON_APPLY_FAILED);
            closure.run(new Status(RaftError.EINTERNAL, errorMsg));
        }
    }

    private void handlerNotLeaderError(final NamespaceClosure closure) {
        closure.failure("Not leader.", Constants.RESP_CODE_NOT_LEADER);
        closure.run(new Status(RaftError.EPERM, "Not leader"));
    }


    @Override
    public void create(String path, int fileType, NamespaceClosure closure) {
        System.out.println("REQ : create " + path + " [" + fileType + "]");
//        System.out.println("Inside Create of Service");
        applyOperation(NamespaceOperation.createMakedirOrfile(path, fileType), closure);
    }

    @Override
    public void remove(String path, int fileType, NamespaceClosure closure) {
        System.out.println("REQ : remove " + path + " [" + fileType + "]");
//        System.out.println("Inside Remove of Service");
        applyOperation(NamespaceOperation.createRemovedirOrfile(path, fileType), closure);
    }

    @Override
    public void getNameSpaceTree(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "NST" + "]");
//        System.out.println("Inside Namespace Tree Get of Service");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        String message = this.namespaceServer.getNsm().getNamespace().printTree();
        closure.success(true, message, 0);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    @Override
    public void getClusterUtil(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "CU" + "]");
//        System.out.println("Inside Cluster Util Get of Service");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        String message = this.namespaceServer.getClusterUtilization().toString();
        closure.success(true, message, 0);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    @Override
    public void getClusterUtilUpdate(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "CUU" + "]");
//        System.out.println("Inside Cluster Util Update Get of Service");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        this.namespaceServer.getClusterUtilization().clearUtilization();
        this.namespaceServer.getNsm().renewClusterUtilization(
                this.namespaceServer.getClusterUtilization());
        String message = this.namespaceServer.getClusterUtilization().toString();
        closure.success(true, message, 0);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    @Override
    public void getCacheUtil(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "CACHE" + "]");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
//        System.out.println("Inside Cluster Cache Get of Service");
        String message = this.namespaceServer.getNsm().getNamespace().getCache().toString();
        closure.success(true, message, 0);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    @Override
    public void getServerMetaUtil(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "SMD" + "]");
//        System.out.println("Inside Cluster metadata of Service");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        String message = getServerMetaData();
        closure.success(true, message, 0);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    String getServerMetaData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cache : " + Constants.CACHE_ON + "\n");
        sb.append("Snapshot ON : " + Constants.SNAPSHOT_ON + "\n");
        sb.append("Snapshot Interval : " + Constants.SNAPSHOT_INTERVAL + "\n");
        sb.append("Cache Capacity : " + Constants.CACHE_CAPACITY + "\n");
        sb.append("Block Size : " + Constants.BLOCK_SIZE + "\n");
        sb.append("Replication Factor : " + Constants.REPLICATION_FACTOR + "\n");
        sb.append("Max Updates between snapshots : " + Constants.MAX_UPDATES_BETWEEN_SNAPSHOTS + "\n");
        sb.append("Default election timeout : " + Constants.ELECTION_TIMEOUT_MILLIS + "\n");
        sb.append("Current election timeout : " + this.namespaceServer.getNsm().getCurrElectionTimeout() + "\n");
        sb.append("Last election time : " + this.namespaceServer.getNsm().getPrevElectionTime() + "\n");
        sb.append("Total elections so far : " + this.namespaceServer.getNsm().getNumberOfElections() + "\n");

        return sb.toString();
    }

    @Override
    public void getServerMetricsUtil(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "MU" + "]");
//        System.out.println("Inside Cluster metadata of Service");
//        if(!isLeader()){
////            System.out.println("Not leader");
//            handlerNotLeaderError(closure);
//            return;
//        }
        String message = this.namespaceServer.getNsm().getMetrics();
        closure.success(true, message, 0);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    @Override
    public void getClusterElectionTimeout(NamespaceClosure closure) {
        System.out.println("REQ : get" + " [" + "ETO" + "]");
//        System.out.println("Inside Cluster Election timeout!");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        String message = "Election time : " + this.namespaceServer.getNode().getOptions().getElectionTimeoutMs();
        closure.success(true, message, 0);
        closure.run(Status.OK());
    }

    @Override
    public void rename(String srcPath, String destPath, int fileType, NamespaceClosure closure) {
        System.out.println("REQ : rename " + srcPath + " -> " + destPath + " [" + fileType + "]");
//        System.out.println("Inside Rename of Service");
        applyOperation(NamespaceOperation.createRenameFileOrDir(srcPath, destPath, fileType), closure);
    }

    @Override
    public void write(final String path, final int writeBlockSize, final NamespaceClosure closure) {
        System.out.println("REQ : write " + path + " [" + writeBlockSize + "]");
//        System.out.println("Inside Write of Service");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        Inode inode = this.namespaceServer.getNsm().getNamespace().getInodeForPath(path);
        if(inode == null){
            //Err! file doesn't exist
//            System.out.println("File does not exist!");
            closure.writeResponseMessage(Constants.WRITE_MESSAGE.get(Constants.WRITE_PATH_INVALID), false, Constants.WRITE_PATH_INVALID,
                    null, null, 0);
            closure.run(Status.OK());
        }else if(inode.getType() != Constants.TYPE_FILE){
//            System.out.println("Path refers to directory!");
            //Err! the path refers to a directory
            closure.writeResponseMessage(Constants.WRITE_MESSAGE.get(Constants.WRITE_FILETYPE_INVALID), false, Constants.WRITE_FILETYPE_INVALID,
                    null, null, 0);
            closure.run(Status.OK());
        }else{
//            System.out.println("Valid write request!");
            FileInode file = (FileInode)inode;
            List<List<BlockDetails>> blocks = file.getAddress();
//            System.out.println("Found block details");
            //Check if the last block for the file still has some space left.
            //If so, return the last block details from here and replicate the size changes
            //If not, find new nodes to allocate the block
            List<String> dataNodes = null;
//            System.out.println("Blocks size : " + blocks.size());
            List<BlockDetails> lastBlock = (blocks.size() > 0 ) ? blocks.get(blocks.size()-1) : null;
//            System.out.println("Found last block");
            int lastBlockSize = (lastBlock != null) ? (lastBlock.isEmpty() ? 0 : lastBlock.get(0).getSize()) : 0;
            int sizeToWrite = writeBlockSize;

//            System.out.println("Last block size : " + lastBlockSize);

            if(lastBlockSize % Constants.BLOCK_SIZE == 0){
//                System.out.println("Need to create new block");
                ClusterUtilization obj = this.namespaceServer.getClusterUtilization();
                dataNodes = obj.allocateNodesForABlock();
//                System.out.println("Allocated nodes: " + dataNodes);
                if(dataNodes.size() == 0){
                    //No space in the cluster. Return failure
                    closure.writeResponseMessage(Constants.WRITE_MESSAGE.get(Constants.WRITE_FAIL_CLUSTER_FULL), false, Constants.WRITE_FILETYPE_INVALID,
                            null, null, 0);
                    closure.run(Status.OK());
                }

                //Update the utilization info here itself since we don't have block reports to update them
                // Applying before calling the namespace change replication since that call doesn't return here
                obj.updateUtilization(dataNodes, true);
//                System.out.println("Updated cluster utilization:\n" + obj);

            } else{
//                System.out.println("Using existing last block");
                sizeToWrite = Math.min(Constants.BLOCK_SIZE - lastBlockSize, writeBlockSize);
            }
//            System.out.println("Next block size to write : " + sizeToWrite);
            applyOperation(NamespaceOperation.createWrite(path, sizeToWrite, dataNodes),closure);

        }
    }

    @Override
    public void read(String path, NamespaceClosure closure) {
        System.out.println("REQ : read " + path);
//        System.out.println("Inside Read of Service");
        if(!isLeader()){
//            System.out.println("Not leader");
            handlerNotLeaderError(closure);
            return;
        }
        List<List<BlockDetails>> result = this.namespaceServer.getNsm().getNamespace().read(path);
        byte retCode = result == null ? Constants.READ_PATH_INVALID : Constants.READ_SUCCESS;
        closure.readResponseMessage(Constants.READ_MESSAGE.get(retCode),!(result == null),retCode, result);
//        System.out.println("\nUpdating closure status!");
        closure.run(Status.OK());
    }

    @Override
    public ClusterUtilization getClusterUtilizationObject() {
        return this.namespaceServer.getClusterUtilization();
    }
}

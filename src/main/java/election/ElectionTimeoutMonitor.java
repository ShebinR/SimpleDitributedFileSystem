package election;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import service.NamespaceClosure;
import service.NamespaceOperation;
import service.NamespaceStateMachine;

import java.nio.ByteBuffer;

public class ElectionTimeoutMonitor implements Runnable {
    NamespaceStateMachine nsm;
    Node node;

    public ElectionTimeoutMonitor(NamespaceStateMachine nsm, Node node) {
        this.nsm = nsm;
        this.node = node;
    }

    @Override
    public void run() {
        while(true){
            //System.out.println("Election timeout monitor started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!nsm.isLeader()){
//                int prevElectionTimeout = this.node.getOptions().getElectionTimeoutMs();
//                int currElectionTimeout = this.nsm.getCurrElectionTimeout();
//                //System.out.println("Prev timeout: "+ prevElectionTimeout + "\t Curr Timeout: " + currElectionTimeout );
//                if(currElectionTimeout != 0 && prevElectionTimeout != currElectionTimeout){
//                    System.out.println("ElectionTimeoutMonitor - Updating node options election timeout");
//                    System.out.println("Prev timeout: "+ prevElectionTimeout + "\t Curr Timeout: " + currElectionTimeout);
//                    this.node.resetElectionTimeoutMs(currElectionTimeout);
//                }
                continue;
            }
            if(!nsm.isUpdatePrevElectionInfo() && !nsm.isUpdateElectionTimeout()){
                continue;
            }

            System.out.println("Inside run of ElectionTimeoutMonitor");

            final Task task = new Task();
            System.out.println("PET in thread : " + this.nsm.getPrevElectionTime());
            NamespaceOperation op = nsm.isUpdatePrevElectionInfo()
                    ? NamespaceOperation.createUpdatePrevElectionInfo(this.nsm.getPrevElectionTime(), this.nsm.getNumberOfElections())
                    : NamespaceOperation.createUpdateElectionTimeout(this.nsm.getPrevElectionTime(),
                    this.nsm.getNumberOfElections(), this.nsm.getCurrElectionTimeout());

            System.out.println("OP obj : " + op.getPrevElectionTime());
            try {
                task.setData(ByteBuffer.wrap(SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(op)));
                NamespaceClosure closure = new NamespaceClosure() {
                    @Override
                    public void run(Status status) {
                        System.out.println("updated election timeout");
                        //Need to update nodeoption's election timeout
                    }
                };
                task.setDone(closure);
                System.out.println("Created task. Calling onApply() on each raft server");
                this.node.apply(task);
            } catch (CodecException e) {
                e.printStackTrace();
            }
            if(nsm.isUpdatePrevElectionInfo()){
                this.nsm.setUpdatePrevElectionInfo(false);
            }else{
                System.out.println("Updating election timeout in node options");
                this.node.resetElectionTimeoutMs(this.nsm.getCurrElectionTimeout());
                this.nsm.setUpdateElectionTimeout(false);
                this.nsm.setNumberOfElections(0);
            }
        }
    }
}

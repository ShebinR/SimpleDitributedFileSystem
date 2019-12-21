package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import service.NamespaceClosure;
import service.NamespaceService;
import utilization.ClusterUtilization;

import java.util.List;

public class RemoveRequestProcessor extends AsyncUserProcessor<RemoveRequest> {

    private final NamespaceService namespaceService;

    public RemoveRequestProcessor(NamespaceService namespaceService) {
        super();
        this.namespaceService = namespaceService;
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, RemoveRequest request) {
//        System.out.println("---------------------------------");
//        System.out.println("Received remove request");
//        System.out.println(request);
        final NamespaceClosure closure = new NamespaceClosure() {
            @Override
            public void run(Status status) {
                ClusterUtilization util = namespaceService.getClusterUtilizationObject();
                //Update utilization
                for( List<String> block : getRemoveResponse().removedBlocks){
                    util.updateUtilization(block,  false);
                }
//                System.out.println("Sending remove response");
//                System.out.println(getRemoveResponse());
                asyncContext.sendResponse(getRemoveResponse());
//                System.out.println("---------------------------------");
            }
        };

        this.namespaceService.remove(request.getPath(), request.getFileType(), closure);
    }

    @Override
    public String interest() {
        return RemoveRequest.class.getName();
    }
}

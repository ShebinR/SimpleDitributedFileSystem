package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import service.NamespaceClosure;
import service.NamespaceService;

public class RenameRequestProcessor extends AsyncUserProcessor<RenameRequest> {
    private final NamespaceService namespaceService;

    public RenameRequestProcessor(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, RenameRequest renameRequest) {
//        System.out.println("---------------------------------");
//        System.out.println("Received remove request");
//        System.out.println(renameRequest);
        final NamespaceClosure closure = new NamespaceClosure() {
            @Override
            public void run(Status status) {
//                System.out.println("Sending remove response");
//                System.out.println(getRenameResponse());
                asyncContext.sendResponse(getRenameResponse());
//                System.out.println("---------------------------------");
            }
        };
        this.namespaceService.rename(
                renameRequest.getSrcPath(),
                renameRequest.getDstPath(),
                renameRequest.getFileType(),
                closure);
    }

    @Override
    public String interest() {
        return RenameRequest.class.getName();
    }
}

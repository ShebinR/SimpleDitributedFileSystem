package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import service.NamespaceClosure;
import service.NamespaceService;

public class WriteRequestProcessor extends AsyncUserProcessor<WriteRequest> {
    private final NamespaceService namespaceService;

    public WriteRequestProcessor(NamespaceService namespaceService) {
        super();
        this.namespaceService = namespaceService;
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, WriteRequest request) {
//        System.out.println("---------------------------------");
//        System.out.println("Received write request");
//        System.out.println(request);
        final NamespaceClosure closure = new NamespaceClosure() {
            @Override
            public void run(Status status) {
//                System.out.println("Sending write response");
//                System.out.println(getWriteResponse());
                asyncContext.sendResponse(getWriteResponse());
//                System.out.println("---------------------------------");
            }
        };

        this.namespaceService.write(request.getPath(), request.getBlockSize(), closure);
    }

    @Override
    public String interest() {
        return WriteRequest.class.getName();
    }
}

package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import service.NamespaceClosure;
import service.NamespaceService;

public class CreateRequestProcessor extends AsyncUserProcessor<CreateRequest> {
    private final NamespaceService namespaceService;

    public CreateRequestProcessor(NamespaceService namespaceService) {
        super();
        this.namespaceService = namespaceService;
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, CreateRequest request) {
//        System.out.println("---------------------------------");
//        System.out.println("Received create request");
//        System.out.println(request);
        final NamespaceClosure closure = new NamespaceClosure() {
            @Override
            public void run(Status status) {
//                System.out.println("Sending create response");
//                System.out.println(getResponse());
                asyncContext.sendResponse(getResponse());
//                System.out.println("---------------------------------");
            }
        };

        this.namespaceService.create(request.getPath(), request.getFileType(), closure);
    }

    @Override
    public String interest() {
        return CreateRequest.class.getName();
    }
}

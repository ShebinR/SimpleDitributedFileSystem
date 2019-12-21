package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import service.NamespaceClosure;
import service.NamespaceService;

public class ReadRequestProcessor extends AsyncUserProcessor<ReadRequest> {

    private final NamespaceService namespaceService;

    public ReadRequestProcessor(NamespaceService namespaceService) {
        super();
        this.namespaceService = namespaceService;
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, ReadRequest request) {
//        System.out.println("---------------------------------");
//        System.out.println("Received read request");
//        System.out.println(request);
        final NamespaceClosure closure = new NamespaceClosure() {
            @Override
            public void run(Status status) {
//                System.out.println("Sending read response");
                // Handle failures
                if(getReadResponse() == null) {
//                    System.out.println(getResponse());
                    asyncContext.sendResponse(getResponse());
                }
                else {
//                    System.out.println(getReadResponse());
                    asyncContext.sendResponse(getReadResponse());
                }
//                System.out.println("---------------------------------");
            }
        };

        this.namespaceService.read(request.getPath(), closure);
    }

    @Override
    public String interest() {
        return ReadRequest.class.getName();
    }


}

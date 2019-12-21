package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import service.NamespaceClosure;
import service.NamespaceService;
import util.Constants;

public class GetStatsRequestProcessor extends AsyncUserProcessor<GetStatsRequest>{
    private final NamespaceService namespaceService;

    public GetStatsRequestProcessor(NamespaceService namespaceService) {
        super();
        this.namespaceService = namespaceService;
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, GetStatsRequest getStatsRequest) {
//        System.out.println("---------------------------------");
//        System.out.println("Received namespace tree get request");
//        System.out.println(getStatsRequest);
        final NamespaceClosure closure = new NamespaceClosure() {
            @Override
            public void run(Status status) {
//                System.out.println("Sending namespace tree response");
//                System.out.println(getResponse());
                asyncContext.sendResponse(getResponse());
//                System.out.println("---------------------------------");
            }
        };

        if(getStatsRequest.getCategory() == Constants.STATS_NAMESPACE_TREE)
            this.namespaceService.getNameSpaceTree(closure);
        else if(getStatsRequest.getCategory() == Constants.STATS_CLUSTER_UTIL)
            this.namespaceService.getClusterUtil(closure);
        else if(getStatsRequest.getCategory() == Constants.STATS_CLUSTER_UTIL_UPDATE)
            this.namespaceService.getClusterUtilUpdate(closure);
        else if(getStatsRequest.getCategory() == Constants.STATS_CLUSTER_CACHE)
            this.namespaceService.getCacheUtil(closure);
        else if(getStatsRequest.getCategory() == Constants.STATS_CLUSTER_ELECTION_TO)
            this.namespaceService.getClusterElectionTimeout(closure);
        else if(getStatsRequest.getCategory() == Constants.STATS_CLUSTER_META)
            this.namespaceService.getServerMetaUtil(closure);
        else if(getStatsRequest.getCategory() == Constants.STATS_NODE_METRICS)
            this.namespaceService.getServerMetricsUtil(closure);
    }

    @Override
    public String interest() {
        return GetStatsRequest.class.getName();
    }
}

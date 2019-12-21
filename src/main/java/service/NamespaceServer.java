package service;

import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.codahale.metrics.Metric;
import election.ElectionTimeoutMonitor;
import org.apache.commons.io.FileUtils;
import rpc.*;
import snapshot.SnapshotMonitor;
import util.Constants;
import utilization.ClusterUtilization;
import utilization.UtilizationInitializer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class NamespaceServer {

    private RaftGroupService raftGroupService;
    private Node node;
    private NamespaceStateMachine nsm;
    private ClusterUtilization clusterUtilization;
    private Thread utilizationMonitorThread;
    private Thread intermediateSnapshotThread;
    private UtilizationInitializer monitor;

    public Node getNode() {
        return node;
    }

    public NamespaceServer(final String dataPath, final String groupId, final PeerId serverId,
                           final NodeOptions nodeOptions, final String clusterConfig) throws IOException {

        FileUtils.forceMkdir(new File(dataPath));
        final RpcServer rpcServer = new RpcServer(serverId.getPort());
        RaftRpcServerFactory.addRaftRequestProcessors(rpcServer);

        NamespaceService namespaceService = new NamespaceServiceImpl(this);
        rpcServer.registerUserProcessor(new GetStatsRequestProcessor(namespaceService));
        rpcServer.registerUserProcessor(new CreateRequestProcessor(namespaceService));
        rpcServer.registerUserProcessor(new RemoveRequestProcessor(namespaceService));
        rpcServer.registerUserProcessor(new RenameRequestProcessor(namespaceService));
        rpcServer.registerUserProcessor(new WriteRequestProcessor(namespaceService));
        rpcServer.registerUserProcessor(new ReadRequestProcessor(namespaceService));

        this.nsm = new NamespaceStateMachine();
        this.clusterUtilization = new ClusterUtilization(clusterConfig);
        this.monitor = new UtilizationInitializer(this.nsm, this.clusterUtilization);
        System.out.println("Starting a thread to initialize utilization!");
        utilizationMonitorThread = new Thread(this.monitor);
        utilizationMonitorThread.start();

        //set Node options
        nodeOptions.setElectionTimeoutMs(Constants.ELECTION_TIMEOUT_MILLIS);

        nodeOptions.setFsm(this.nsm);
        nodeOptions.setLogUri(dataPath + File.separator + "log");
        nodeOptions.setRaftMetaUri(dataPath + File.separator + "raft_meta");
        if(Constants.SNAPSHOT_ON)
            nodeOptions.setSnapshotUri(dataPath + File.separator + "snapshot");
        nodeOptions.setSnapshotIntervalSecs(Constants.SNAPSHOT_INTERVAL);

        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions, rpcServer);
        this.node = this.raftGroupService.start();
        this.nsm.nodeStartup = true;
        this.nsm.start = System.currentTimeMillis();
        System.out.println("Raft Server started");
        System.out.println("Block Size : " + Constants.BLOCK_SIZE);

        SnapshotMonitor obj = new SnapshotMonitor(this.node, namespaceService);
        intermediateSnapshotThread = new Thread(obj);
        intermediateSnapshotThread.start();

        System.out.println("Starting a thread to monitor election timeout!");
        Thread electionMonitorThread = new Thread(new ElectionTimeoutMonitor(this.nsm, node));
        electionMonitorThread.start();

        System.out.println("--------------------------");
        for(Map.Entry<String, Metric> entry: node.getNodeMetrics().getMetrics().entrySet()) {
            System.out.println(entry.getKey());
        }
        System.out.println("--------------------------");
    }

    public NamespaceStateMachine getNsm() {
        return nsm;
    }

    public void setNsm(NamespaceStateMachine nsm) {
        this.nsm = nsm;
    }

    public ClusterUtilization getClusterUtilization() {
        return clusterUtilization;
    }
}

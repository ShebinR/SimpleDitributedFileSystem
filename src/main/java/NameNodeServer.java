import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import service.NamespaceServer;

import java.io.IOException;

public class NameNodeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        final String dataPath = args[0];
        final String groupId = args[1];
        final String serverIdStr = args[2];
        final String initConfStr = args[3];
        final String clusterConfig = args[4];

        final NodeOptions nodeOptions = new NodeOptions();

        nodeOptions.setDisableCli(false);
        final PeerId serverId = new PeerId();
        if (!serverId.parse(serverIdStr)) {
            throw new IllegalArgumentException("Fail to parse serverId:" + serverIdStr);
        }
        final Configuration initConf = new Configuration();
        if (!initConf.parse(initConfStr)) {
            throw new IllegalArgumentException("Fail to parse initConf:" + initConfStr);
        }
        nodeOptions.setInitialConf(initConf);

        NamespaceServer namespaceServer = new NamespaceServer(dataPath, groupId, serverId, nodeOptions, clusterConfig);
        System.out.println("Started Name node server at port:"
                + namespaceServer.getNode().getNodeId().getPeerId().getPort());
        System.out.println("Snapshot Interval : " + nodeOptions.getSnapshotIntervalSecs());
    }
}

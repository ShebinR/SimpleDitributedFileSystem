package snapshot;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.Status;
import service.NamespaceService;
import util.Constants;

public class SnapshotMonitor implements Runnable {
    Node node;
    NamespaceService service;

    public SnapshotMonitor(Node node, NamespaceService service) {
        this.node = node;
        this.service = service;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(this.service.getCurrentUpdates() < Constants.MAX_UPDATES_BETWEEN_SNAPSHOTS)
                continue;

            System.out.println("Initiating snapshot! Updates has gone high!");
            node.snapshot(new Closure() {
                @Override
                public void run(Status status) {
                    System.out.println(status);
                    System.out.println("Snapshot done!");
                }
            });
            this.service.setCurrentUpdates(0);
        }


    }
}

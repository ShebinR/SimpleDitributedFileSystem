package utilization;

import service.NamespaceStateMachine;

public class UtilizationInitializer implements Runnable {
    NamespaceStateMachine nsm;
    ClusterUtilization util;

    public UtilizationInitializer(NamespaceStateMachine nsm, ClusterUtilization util) {
        this.nsm = nsm;
        this.util = util;
    }

    @Override
    public void run() {
        System.out.println("Thread to update utilization started!");
        while(true) {
            if(!nsm.isLeader()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if(!nsm.getUpdateCluster()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            System.out.println("Refreshing cluster utilization at the leader!");
            nsm.renewClusterUtilization(this.util);
            nsm.unsetUpdateCluster();
            System.out.println(this.util);
        }
    }
}

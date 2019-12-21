package service;

import utilization.ClusterUtilization;

public interface NamespaceService {

    void getNameSpaceTree(final NamespaceClosure closure);
    void getClusterUtil(final NamespaceClosure closure);
    void getClusterUtilUpdate(final NamespaceClosure closure);
    void getClusterElectionTimeout(final NamespaceClosure closure);
    void create(final String path, int fileType, final NamespaceClosure closure);
    void remove(final String path, int fileType, final NamespaceClosure closure);
    void rename(final String srcPath, final String destPath, final int fileType, final NamespaceClosure closure);
    void write(final String path, final int blockSize, final NamespaceClosure closure);
    void read(final String path, final NamespaceClosure closure);
    void getServerMetaUtil(NamespaceClosure closure);
    void getServerMetricsUtil(NamespaceClosure closure);
    ClusterUtilization getClusterUtilizationObject();
    int getCurrentUpdates();
    void setCurrentUpdates(int currentUpdates);
    void getCacheUtil(NamespaceClosure closure);
}

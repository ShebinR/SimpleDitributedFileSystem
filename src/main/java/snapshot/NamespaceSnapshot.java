package snapshot;

import namespace.Inode;
import namespace.Namespace;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class NamespaceSnapshot {
    private String path;

    public NamespaceSnapshot(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean save(String serializedTree) {
        try {
            FileUtils.writeStringToFile(new File(path), serializedTree);
            return true;
        } catch (IOException e) {
            System.out.println("Failed saving snapshot");
            return false;
        }
    }

    public Inode load() throws IOException {
        final String serializedTree = FileUtils.readFileToString(new File(path));
        if (!serializedTree.equals("")) {
            return Namespace.deserializeNameSpaceTree(serializedTree);
        }
        throw new IOException("Fail to load snapshot from " + path + ",content: " + serializedTree);
    }
}

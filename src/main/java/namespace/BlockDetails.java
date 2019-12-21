package namespace;

import java.io.Serializable;

public class BlockDetails implements Serializable {
    String hostName;
    String blockId;
    int size;

    public BlockDetails(String hostName, String blockId, int size) {
        this.hostName = hostName;
        this.blockId = blockId;
        this.size = size;
    }

    public String getHostName() {
        return hostName;
    }

    public String getBlockId() {
        return blockId;
    }

    public int getSize() {
        return size;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String toString() {
        return hostName + " : " + blockId + " : " + size;
    }

    public String serialize() {
        return hostName + "-" + blockId + "-" + size;
    }

    static public BlockDetails deserialize(String blockDetails) {
        String fields[] = blockDetails.split("-");
        return new BlockDetails(fields[0], fields[1], Integer.parseInt(fields[2]));
    }
}

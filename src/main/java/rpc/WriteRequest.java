package rpc;

import java.io.Serializable;

public class WriteRequest implements Serializable {
    private String path;
    private int blockSize;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockId) {
        this.blockSize = blockId;
    }

    @Override
    public String toString() {
        return "WriteRequest { " +
                "path='" + path + '\'' +
                ", blockSize=" + blockSize +
                " } ";
    }
}


package rpc;

import java.io.Serializable;

public class RemoveRequest implements Serializable {
    String path;
    int fileType;

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "RemoveRequest { " +
                "path='" + path + '\'' +
                ", fileType=" + fileType +
                " }";
    }
}

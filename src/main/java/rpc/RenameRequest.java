package rpc;

import java.io.Serializable;

public class RenameRequest implements Serializable {
    String srcPath;
    String dstPath;
    int fileType;

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getDstPath() {
        return dstPath;
    }

    public void setDstPath(String dstPath) {
        this.dstPath = dstPath;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "RenameRequest { " +
                "srcPath='" + srcPath + '\'' +
                ", dstPath='" + dstPath + '\'' +
                ", fileType=" + fileType +
                " } ";
    }
}

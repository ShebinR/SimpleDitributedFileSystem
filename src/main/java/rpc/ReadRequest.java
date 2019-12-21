package rpc;

import java.io.Serializable;

public class ReadRequest implements Serializable {
    String path;
    public ReadRequest(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ReadRequest { " +
                "path='" + path + '\'' +
                " }";
    }
}

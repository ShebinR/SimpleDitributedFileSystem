package rpc;

import java.util.List;

public class WriteResponse extends Response {
    List<String> blockLocations;
    String blockId;
    int size;

    public WriteResponse(int respCode, boolean success, String errMsg,
                         List<String> locations, String blockId, int size){
        super(respCode,success,errMsg);
        this.blockLocations = locations;
        this.blockId = blockId;
        this.size = size;
    }

    public List<String> getBlockLocations() {
        return blockLocations;
    }

    public void setBlockLocations(List<String> blockLocations) {
        this.blockLocations = blockLocations;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "WriteResponse { " +
                this.responseCode + ", success=" + this.success + ", errorMsg=" + this.errMsg +
                "blockLocations=" + blockLocations +
                ", blockId='" + blockId + '\'' +
                ", size=" + size +
                " } ";
    }
}

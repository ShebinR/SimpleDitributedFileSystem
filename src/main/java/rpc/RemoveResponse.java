package rpc;

import java.util.List;

public class RemoveResponse extends Response {
    List<List<String>> removedBlocks;

    public RemoveResponse(int respCode, boolean success, String errMsg, List<List<String>> blocks){
        super(respCode, success, errMsg);
        this.removedBlocks = blocks;
    }

    @Override
    public String toString() {
        return "RemoveResponse{" + responseCode + ", success=" + this.success + ", errorMsg=" + this.errMsg + " ]"+
                "removedBlocks=" + removedBlocks +
                '}';
    }
}

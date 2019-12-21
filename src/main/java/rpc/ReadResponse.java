package rpc;

import namespace.BlockDetails;

import java.util.List;

public class ReadResponse extends Response {
    List<List<BlockDetails>> blockDetails;

    public ReadResponse(int respCode, boolean success, String errMsg, List<List<BlockDetails>> blockDetails){
        super(respCode, success, errMsg);
        this.blockDetails = blockDetails;
    }

    public List<List<BlockDetails>> getBlockDetails() {
        return blockDetails;
    }
}

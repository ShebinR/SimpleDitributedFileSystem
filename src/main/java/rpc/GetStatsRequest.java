package rpc;

import java.io.Serializable;

public class GetStatsRequest implements Serializable {
    private byte category;

    public byte getCategory() {
        return category;
    }

    public void setCategory(byte category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "GetStatsRequest {" +
                "category = " + category +
                " }";
    }
}

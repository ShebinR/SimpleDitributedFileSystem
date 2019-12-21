package rpc;

import java.io.Serializable;

public class ValueResponse implements Serializable {
    private int value;
    private boolean success;

    private String errMsg;

    public ValueResponse(){
        super();
    }

    public ValueResponse(int value, boolean success, String errMsg) {
        super();
        this.value = value;
        this.success = success;
        this.errMsg = errMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String toString(){
        return "ValueResponse [value=" + this.value + ", success=" + this.success + ", errorMsg=" + this.errMsg + "]";    }


}

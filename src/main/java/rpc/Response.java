package rpc;

import java.io.Serializable;

public class Response implements Serializable {
    protected int responseCode;
    protected boolean success;
    protected String errMsg;

    public Response(){
        super();
    }

    public Response(int respCode, boolean success, String errMsg) {
        super();
        this.responseCode = respCode;
        this.success = success;
        this.errMsg = errMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int value) {
        this.responseCode = value;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String toString(){
        return "ValueResponse [ value=" + this.responseCode + ", success=" + this.success + ", errorMsg=" + this.errMsg + " ]";    }

}

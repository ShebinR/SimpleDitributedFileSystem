package rpc;

import java.io.Serializable;

public class RenameResponse implements Serializable {
    int responseCode;
    boolean success;
    String message;

    public RenameResponse() {super();}

    public RenameResponse(int responseCode, boolean success, String message) {
        this.responseCode = responseCode;
        this.success = success;
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RenameResponse{" +
                "responseCode=" + responseCode +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}

package service;

import com.alipay.sofa.jraft.Closure;
import namespace.BlockDetails;
import rpc.*;

import java.util.List;

public abstract class NamespaceClosure implements Closure {
    private NamespaceOperation namespaceOperation;
    private ValueResponse valueResponse;
    private Response response;
    private RenameResponse renameResponse;
    private WriteResponse writeResponse;
    private ReadResponse readResponse;
    private RemoveResponse removeResponse;

    public NamespaceOperation getNamespaceOperation() {
        return namespaceOperation;
    }

    public void setNamespaceOperation(NamespaceOperation namespaceOperation) {
        this.namespaceOperation = namespaceOperation;
    }

    public ValueResponse getValueResponse() {
        return valueResponse;
    }

    public void setValueResponse(ValueResponse valueResponse) {
        this.valueResponse = valueResponse;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response createResponse) {
        this.response = createResponse;
    }

    public RenameResponse getRenameResponse() {
        return renameResponse;
    }

    public void setRenameResponse(RenameResponse renameResponse) {
        this.renameResponse = renameResponse;
    }

    public WriteResponse getWriteResponse() {
        return writeResponse;
    }

    public void setWriteResponse(WriteResponse writeResponse) {
        this.writeResponse = writeResponse;
    }

    public ReadResponse getReadResponse() {
        return readResponse;
    }

    public void setReadResponse(ReadResponse readResponse) {
        this.readResponse = readResponse;
    }

    public RemoveResponse getRemoveResponse() {
        return removeResponse;
    }

    public void setRemoveResponse(RemoveResponse removeResponse) {
        this.removeResponse = removeResponse;
    }

    protected void failure(final String errMsg, int code){
        final Response resp = new Response();
        resp.setSuccess(false);
        resp.setErrMsg(errMsg);
        resp.setResponseCode(code);
        setResponse(resp);
    }

    protected void success(final int value){
        final ValueResponse resp = new ValueResponse();
        resp.setSuccess(true);
        resp.setValue(value);
        setValueResponse(resp);
    }

    protected void success(boolean status, String errMsg) {
        final Response resp = new Response();
        resp.setResponseCode(status ? 0 : -1);
        resp.setSuccess(status);
        resp.setErrMsg(errMsg);
        setResponse(resp);
//        System.out.println("Closure : " + this.getResponse());
    }

    protected void success(boolean status, String errMsg, int responseCode) {
        final Response resp = new Response();
        resp.setResponseCode(responseCode);
        resp.setSuccess(status);
        resp.setErrMsg(errMsg);
        setResponse(resp);
//        System.out.println("Closure : " + this.getResponse());
    }

    protected void removeResponseMessage(boolean status, String errMsg, int responseCode, List<List<String>> blocks){
        final RemoveResponse response = new RemoveResponse(responseCode, status,errMsg, blocks);
        setRemoveResponse(response);
//        System.out.println("Closure : " + this.getRemoveResponse());
    }

    protected void renameResponseMessage(final String msg, boolean status, int responseCode) {
        final RenameResponse response = new RenameResponse();
        response.setMessage(msg);
        response.setSuccess(status);
        response.setResponseCode(responseCode);
        setRenameResponse(response);
//        System.out.println("Closure : " + this.getRenameResponse());
    }

    protected void writeResponseMessage(final String msg, boolean status, int responseCode,
                                        List<String> result, String blockId, int size) {
        final WriteResponse response = new WriteResponse(responseCode, status, msg,
                result, blockId, size);

        setWriteResponse(response);
//        System.out.println("Closure : " + this.getWriteResponse());
    }

    void readResponseMessage(final String msg, boolean status, int responseCode,
                             List<List<BlockDetails>> result){
        final ReadResponse response = new ReadResponse(responseCode, status, msg,result);
        setReadResponse(response);
//        System.out.println("Closure : " + this.getReadResponse());
    }
}

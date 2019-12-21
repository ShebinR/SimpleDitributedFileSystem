package Client;

import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import com.alipay.sofa.jraft.util.Endpoint;
import namespace.BlockDetails;
import rpc.*;
import util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import static util.FileOperations.readFileAsBytes;
import static util.FileSystemUtilities.refactorBlocks;

public class ClientLibraryInterface {
    Map<String, Long> metrics = new HashMap<>();
    Map<String, Integer> count = new HashMap<>();
    long writeSize = 0;
    String groupId;
    String confStr;
    BoltCliClientService clientService;
    RouteTable routeTable;
    Configuration conf;
    PeerId leader;

    public ClientLibraryInterface(String[] args) {
        this.groupId = args[0];
        this.confStr = args[1];
    }

    private static List<byte[]> readFileInBlocks(String filePath) {
        List<byte[]> blocks = new ArrayList<>();

        byte[] contents = readFileAsBytes(filePath);
        int k = 0;
        byte[] currentBlock = new byte[Constants.BLOCK_SIZE];
        for (int i = 0; i < contents.length; i++) {
            if (k % Constants.BLOCK_SIZE == 0) {
                blocks.add(currentBlock);
                k = 0;
                currentBlock = new byte[Constants.BLOCK_SIZE];
            }

            currentBlock[k++] = contents[i];
        }
        if (k != 0)
            blocks.add(currentBlock);
        return blocks;
    }

    public void init() throws TimeoutException, InterruptedException {
        conf = new Configuration();
        if (!conf.parse(confStr)) {
            throw new IllegalArgumentException("Fail to parse conf:" + confStr);
        }
        RouteTable.getInstance().updateConfiguration(groupId, conf);
        clientService = new BoltCliClientService();
        clientService.init(new CliOptions());
        this.routeTable = RouteTable.getInstance();
        if (!RouteTable.getInstance().refreshLeader(clientService, groupId, 1000).isOk()) {
            throw new IllegalStateException("Refresh leader failed");
        }
        leader = RouteTable.getInstance().selectLeader(groupId);
        System.out.println("Leader is " + leader.toString());
    }

    public void refreshLeader() throws TimeoutException, InterruptedException {
        boolean success = routeTable.refreshLeader(this.clientService, this.groupId, 10000).isOk();
        if(success){
            // Get the leader information of the cluster, and return null if the leader is unknown.
            this.leader = routeTable.selectLeader(this.groupId);
            System.out.println("Leader is " + leader.toString());
        }
    }

    public Object sendCommandToServerAsync(Object request, CountDownLatch latch) throws RemotingException, InterruptedException {
        this.clientService.getRpcClient().invokeWithCallback(this.leader.getEndpoint().toString(), request, new InvokeCallback() {
            @Override
            public void onResponse(Object o) {
                latch.countDown();
                System.out.println("Result : " + o.toString());
            }

            @Override
            public void onException(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        }, 5000);
        return new Object();
    }

    public Object sendCommandToServer(Object request, String name) throws TimeoutException, InterruptedException {
        int retry = 0;
        Object reply = null;
        long start = System.currentTimeMillis();
        while(retry <= Constants.RETRY_LIMIT) {
            try {
                reply = this.clientService.getRpcClient()
                        .invokeSync(this.leader.getEndpoint().toString(), request, 5000);
                if(reply == null) {
                    System.out.println("ERROR : Reply is null!");
                    return reply;
                }
                //System.out.println(request.getClass());
                //System.out.println(reply.getClass());
                Response r = (Response) reply;
                if(!r.isSuccess() && r.getResponseCode() == Constants.RESP_CODE_NOT_LEADER) {
                    retry++;
                    if(retry <= Constants.RETRY_LIMIT) {
                        System.out.println("Refreshing leader & Sending 2nd Request!");
                        refreshLeader();
                    } else {
                        System.out.println("Reached retry limit!");
                    }
                } else {
                    break;
                }
            } catch (RemotingException e) {
                System.out.println("Exception : " + e);
                retry++;
                if(retry <= Constants.RETRY_LIMIT) {
                    System.out.println("Refreshing leader & Sending 2nd Request!");
                    refreshLeader();
                } else {
                    System.out.println("Reached retry limit!");
                }
            }
        }
        long tt = (System.currentTimeMillis() - start);
        metrics.put(name, metrics.getOrDefault(name, 0L) + tt);
        count.put(name, count.getOrDefault(name, 0) + 1);
        return reply;
    }

    public Object sendCommandToServer(Object request, Endpoint endpoint, String name) throws TimeoutException, InterruptedException {
        int retry = 0;
        Object reply = null;
        long start = System.currentTimeMillis();
        while (retry <= Constants.RETRY_LIMIT) {
            try {
                reply = this.clientService.getRpcClient()
                        .invokeSync(endpoint.toString(), request, 5000);
                if (reply == null) {
                    System.out.println("ERROR : Reply is null!");
                    return reply;
                }
                //System.out.println(request.getClass());
                //System.out.println(reply.getClass());
                Response r = (Response) reply;
                if (!r.isSuccess() && r.getResponseCode() == Constants.RESP_CODE_NOT_LEADER) {
                    retry++;
                    if (retry <= Constants.RETRY_LIMIT) {
                        System.out.println("Refreshing leader & Sending 2nd Request!");
                        refreshLeader();
                    } else {
                        System.out.println("Reached retry limit!");
                    }
                } else {
                    break;
                }
            } catch (RemotingException e) {
                retry++;
                if (retry <= Constants.RETRY_LIMIT) {
                    System.out.println("Refreshing leader & Sending 2nd Request!");
                    refreshLeader();
                } else {
                    System.out.println("Reached retry limit!");
                }
            }
        }
        long tt = (System.currentTimeMillis() - start);
        metrics.put(name, metrics.getOrDefault(name, 0L) + tt);
        count.put(name, count.getOrDefault(name, 0) + 1);
        return reply;
    }

    public void getMetricStats(String fields[]) throws InterruptedException, TimeoutException {
        GetStatsRequest request = new GetStatsRequest();
        request.setCategory(Constants.STATS_COMMAND_LINE.get(fields[1]));
        List<PeerId> peers = routeTable.getConfiguration(groupId).getPeers();
        for(PeerId peer: peers) {
            System.out.println("For " + peer.getEndpoint());
            Response response = (Response) sendCommandToServer(request, peer.getEndpoint(), "Stats");
            if (response != null)
                System.out.println(response.getErrMsg());
        }
    }

    public void getStats(String fields[]) throws InterruptedException, TimeoutException {
        GetStatsRequest request = new GetStatsRequest();
        if(Constants.STATS_COMMAND_LINE.get(fields[1]) == Constants.STATS_NODE_METRICS) {
            getMetricStats(fields);
            return;
        }
        request.setCategory(Constants.STATS_COMMAND_LINE.get(fields[1]));
        Response response = (Response) sendCommandToServer(request, "Stats");
        if(response != null)
            System.out.println(response.getErrMsg());

        /*this.clientService.getRpcClient().invokeWithCallback(this.leader.getEndpoint().toString(), request, new InvokeCallback() {
            @Override
            public void onResponse(Object o) {
                latch.countDown();
                System.out.println("Result : " + o.toString());
            }

            @Override
            public void onException(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        }, 5000); */
    }

    public void sendCreateCommandsAsync(String commands[]) throws RemotingException, InterruptedException {

        CountDownLatch latch = new CountDownLatch(commands.length);
        for(String command: commands) {
            String fields[] = command.split(" ");
            CreateRequest request = new CreateRequest();
            request.setPath(fields[1]);
            request.setFileType(fields[0].equals("mkdir") ? Constants.TYPE_DIR : Constants.TYPE_FILE);
            this.clientService.getRpcClient().invokeWithCallback(this.leader.getEndpoint().toString(), request, new InvokeCallback() {
                @Override
                public void onResponse(Object o) {
                    latch.countDown();
                    System.out.println("Result : " + o.toString());
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                    latch.countDown();
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            }, 5000);
        }
    }

    public Response sendCreateCommand(String path, int fileType) throws InterruptedException, TimeoutException {
        CreateRequest request = new CreateRequest();
        request.setPath(path);
        request.setFileType(fileType);
        Response response = (Response) sendCommandToServer(request, "CREATE_" + fileType);
        if(response != null)
            System.out.println(response);
        return response;
    }

    public void sendRemoveCommand(String path, int fileType) throws InterruptedException, TimeoutException {
        RemoveRequest request = new RemoveRequest();
        request.setPath(path);
        request.setFileType(fileType);
        RemoveResponse response = (RemoveResponse) sendCommandToServer(request, "REMOVE_" + fileType) ;
        if(response != null)
            System.out.println(response);
    }

    public void sendRenameCommand(String[] fields) throws InterruptedException, TimeoutException {
        RenameRequest request = new RenameRequest();
        request.setSrcPath(fields[1]);
        request.setDstPath(fields[2]);
        request.setFileType(1);
        RenameResponse response = (RenameResponse) sendCommandToServer(request, "RENAME");
        if(response != null)
            System.out.println(response);
    }

    public void createFileCommand(String[] fields) throws RemotingException, InterruptedException, TimeoutException {
        /* Send Create Request */
        System.out.println("CREATING FILE REQUEST");
        Response createResponse = this.sendCreateCommand(fields[2], Constants.TYPE_FILE);

        if (createResponse.getResponseCode() == Constants.CREATE_PATH_OCCUPIED ||
            createResponse.getResponseCode() == Constants.CREATE_SUCCESS) {

            System.out.println("SENDING REQUEST FOR WRITE");
            byte[] contents = readFileAsBytes(fields[1]);
            System.out.println("TOTAL SIZE OF FILE : " + contents.length);

            // Check for 1st block
            int requestSize = Math.min(Constants.BLOCK_SIZE, contents.length);
            System.out.println("SENDING REQ : size of " + requestSize);
            WriteResponse response = getWriteBlocks(fields[2], requestSize);
            System.out.println("SAVING Write  : "
                    + response.getBlockLocations()
                    + " with ID " + response.getBlockId()
                    + " of size " + response.getSize());
            List<byte[]> blocks = refactorBlocks(contents,response.getSize());

            // For 2 .. N blocks
            //System.out.println("Number of blocks : " + blocks.size());
            for (int i = 1; i < blocks.size(); i++) {
                requestSize = blocks.get(i).length;
                System.out.println("SENDING REQ : size of " + requestSize);
                WriteResponse writeResponse = getWriteBlocks(fields[2], requestSize);
                System.out.println("SAVING Write  : "
                        + writeResponse.getBlockLocations()
                        + " with ID " + writeResponse.getBlockId()
                        + " of size " + writeResponse.getSize());

                if(!writeResponse.isSuccess() && writeResponse.getResponseCode() == Constants.WRITE_FAIL_CLUSTER_FULL) {
                    System.out.println(writeResponse.getErrMsg());
                    break;
                }
            }
            blocks = null;
            contents = null;
            System.gc();
        } else {
            System.out.println("Create Failed!");
        }
    }

    public WriteResponse getWriteBlocks(String path, int size) throws InterruptedException, TimeoutException {
        WriteRequest writeRequest = new WriteRequest();
        writeRequest.setPath(path);
        writeRequest.setBlockSize(size);
        WriteResponse writeResponse = (WriteResponse) sendCommandToServer(writeRequest, "WRITE");
        if(writeResponse != null) {
            System.out.println(writeResponse);
            writeSize += writeResponse.getSize();
        }
        else
            System.out.println("Write failed!");
        return writeResponse;
    }

    public void readCommand(String path) throws InterruptedException, TimeoutException {
        ReadRequest readRequest = new ReadRequest(path);
        ReadResponse readResponse = (ReadResponse) sendCommandToServer(readRequest, "READ");
        //System.out.println(readResponse);
        if(readResponse.getResponseCode() == Constants.READ_SUCCESS)
            printBlockDetails(path, readResponse.getBlockDetails());
        else
            System.out.println((Response)readResponse);
    }

    public void printBlockDetails(String path, List<List<BlockDetails>> blockDetails) {
        System.out.println(path);
        int index = 0;
        for(List<BlockDetails> replicas : blockDetails) {
            System.out.print(index + " | ");
            for(BlockDetails r: replicas) {
                System.out.print(r + " ");
            }
            System.out.println();
            index++;
        }
    }

    public String dumpMetrics() {
        StringBuilder sb = new StringBuilder();
        Map<String, Double> result = new HashMap<>();
        for(Map.Entry<String, Long> entry: metrics.entrySet()) {
            int currCount = count.get(entry.getKey());
            Double rate = currCount * 1.0 / (entry.getValue() / 1000);
            result.put(entry.getKey(), rate);
            sb.append(entry.getKey() + " : " + String.format("%.3f" , rate) + " req/sec" + "\n");
        }
        return metrics.toString() + "\n" + count.toString() + "\n" + result.toString() + "\n" + sb.toString()
                + "Write Size : " + (writeSize / 1024 * 1024 ) + " MB";
    }
}

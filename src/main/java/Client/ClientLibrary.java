package Client;

import com.alipay.remoting.exception.RemotingException;
import util.Constants;
import util.FileOperations;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class ClientLibrary {

    private static Map<String, Integer> requestCount = new HashMap<>();
    private static Map<String, Long> requestTime = new HashMap<>();
    private static Set<String> commands = new HashSet<>(Arrays.asList("create", "rm", "rename", "mkdir", "rmdir",
            "write", "read"));

    public static void main(String[] args)
            throws TimeoutException, InterruptedException, RemotingException {

        ClientLibraryInterface c = null;
        System.out.println("Client Library Interface");
        try {
            c = new ClientLibraryInterface(args);
            c.init();
        } catch (IllegalStateException e) {
            System.out.println("Remote unavailable!");
            System.exit(0);
        }

        for(String cmd : commands){
            requestCount.put(cmd, 0);
            requestTime.put(cmd, 0L);
        }


        if(args.length == 2)
            executeInterface(c);
        else
            executeTestCase(args, c);

        System.out.println("----------------------Statistics----------------------");
        System.out.println("Request Count : ");
        for(Map.Entry<String, Integer> entry : requestCount.entrySet()){
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        System.out.println("Request Time : ");
        for(Map.Entry<String, Long> entry : requestTime.entrySet()){
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        System.exit(0);
    }

    public static void executeTestCase(String args[], ClientLibraryInterface c) throws RemotingException, InterruptedException, TimeoutException {
        String testcaseDir = args[2] + "/";
        for(int i=3; i<args.length; i++){
            List<String> commands = FileOperations.readFileAsString(testcaseDir + args[i]);
            final long start = System.currentTimeMillis();
            int count = 0;
            for(String command: commands) {
                System.out.println("COMMAND INDEX : " + count);
                executeInput(command, c);
                count++;
            }
            executeInput("dm", c);
            System.out.println("Time Taken : " + (System.currentTimeMillis() - start) + " ms.");
        }
    }

    public static void executeInterface(ClientLibraryInterface c) throws RemotingException, InterruptedException, TimeoutException {
        while (true) {
            System.out.print("> ");
            Scanner s = new Scanner(System.in);
            String command = s.nextLine();
            int ret = executeInput(command, c);
            if(ret == 0)
                break;
        }
    }

    public static int executeInput(String command, ClientLibraryInterface c) throws RemotingException, InterruptedException, TimeoutException {
        if (command.equals("exit"))
            return 0;
        String fields[] = command.split(" ");
        final long start = System.currentTimeMillis();
        switch (fields[0]) {
            case "mkdir":
                if (fields.length != 2) {
                    System.out.println("Usage: mkdir <path>");
                    break;
                }
                c.sendCreateCommand(fields[1], Constants.TYPE_DIR);
                break;
            case "create":
                if (fields.length != 2) {
                    System.out.println("Usage: create <path>");
                    break;
                }
                c.sendCreateCommand(fields[1], Constants.TYPE_FILE);
                break;
            case "rmdir":
                if (fields.length != 2) {
                    System.out.println("Usage: rmdir <path>");
                    break;
                }
                c.sendRemoveCommand(fields[1], Constants.TYPE_DIR);
                break;
            case "rm":
                if (fields.length != 2) {
                    System.out.println("Usage: rm <path>");
                    break;
                }
                c.sendRemoveCommand(fields[1], Constants.TYPE_FILE);
                break;
            case "rename":
                if (fields.length != 3) {
                    System.out.println("Usage: rename <source_path> <destination_path>");
                    break;
                }
                c.sendRenameCommand(fields);
                break;
            case "write":
                if (fields.length != 3) {
                    System.out.println("Usage: write <local_fs_location> <dfs_location>");
                    break;
                }
                c.createFileCommand(fields);
                break;
            case "read":
                if (fields.length != 2) {
                    System.out.println("Usage: read <dfs_location>");
                    break;
                }
                c.readCommand(fields[1]);
                break;
            case "nn":
                Set<String> supported = new HashSet<>(Arrays.asList("tree", "util", "util_update",
                        "eto", "cache", "meta", "metrics"));
                if (fields.length != 2 ||
                        !(supported.contains(fields[1]))) {
                    System.out.println("Usage : nn <category>");
                    System.out.println("Supported Categories : tree, util, util_update");
                    break;
                }
                c.getStats(fields);
                break;
            case "dm":
                String output = c.dumpMetrics();
                System.out.println("METRICS FRM CLI");
                System.out.println("===========================");
                System.out.println(output);
                System.out.println("===========================");
                break;
            default:
                System.out.println("Unsupported command!");
                break;
        }
        final long end = System.currentTimeMillis();
        requestTime.put(fields[0], requestTime.getOrDefault(fields[0],0L) + (end-start));
        requestCount.put(fields[0], requestCount.getOrDefault(fields[0], 0)  + 1);
        return 1;
    }
}

package namespace;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInode extends Inode {
    int size;
    List<List<BlockDetails>> address;
    private final AtomicInteger blockIdCounter;

    FileInode(int size) {
        super(1);
        this.size = size;
        address = new ArrayList<List<BlockDetails>>();
        blockIdCounter = new AtomicInteger(0);
    }

    FileInode(int size, AtomicInteger counter) {
        super(1);
        this.size = size;
        address = new ArrayList<List<BlockDetails>>();
        blockIdCounter = counter;
    }

    void addNewBlock(){
        address.add(new ArrayList<>());
    }

    // Adds Block Details at the end
    void addEntry(BlockDetails b) {
        this.address.get(address.size() - 1).add(b);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<List<BlockDetails>> getAddress() {
        return address;
    }

    public int getBlockIdCounter() {
        return blockIdCounter.get();
    }

    public int incrementAndGet() {
        while(true) {
            int existingValue = getBlockIdCounter();
            int newValue = existingValue + 1;
            if(blockIdCounter.compareAndSet(existingValue, newValue)) {
                return newValue;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File Inode Info : \nSize : " + size).append("\n");
        sb.append("No of blocks : " + address.size()).append("\n");

        return sb.toString();
    }

    public String toStringOLD() {
        StringBuilder sb = new StringBuilder();
        sb.append("File Inode Info : \nSize : " + size).append("\n");
        for(List<BlockDetails> b: address) {
            sb.append("Block : ");
            for(BlockDetails d: b)
                sb.append("[").append(d.toString()).append("]");
            sb.append("\n");
        }

        return sb.toString();
    }

    public String getTreePrintFormat(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append("File Inode Info : \n");
        sb.append(prefix + "Size : " + size).append("\n");
        for(List<BlockDetails> b: address) {
            sb.append(prefix + "Block : ");
            for(BlockDetails d: b)
                sb.append("[").append(d.toString()).append("]");
            sb.append("\n");
        }

        return sb.toString();
    }

    public String getTreePrintFormatReduceInfo(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append("File Inode Info : \n");
        sb.append(prefix + "Size : " + size).append("\n");
        sb.append(prefix + "No of blocks : " + address.size()).append("\n");
//        for(List<BlockDetails> b: address) {
//            sb.append(prefix + "Block : ");
//            for(BlockDetails d: b)
//                sb.append("[").append(d.toString()).append("]");
//            sb.append("\n");
//        }

        return sb.toString();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("F").append(":");
        sb.append(this.size).append(":");
        sb.append(this.blockIdCounter).append(":");
        sb.append("[");
        if(this.address.size() != 0) {
            for (List<BlockDetails> blocks : this.address) {
                for (BlockDetails block : blocks)
                    sb.append("<").append(block.serialize()).append(">");
                sb.append(",");
            }
        } else {
            sb.append("X").append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]").append("}");

        return sb.toString();
    }

    static public FileInode deserialize(Queue<String> serializedTree) {
        //Format F:SIZE:COUNTER:[<BD1R1><BD1R2><BD1R2>,<><><>,..]
        String fileInodeData = serializedTree.poll();
       // System.out.println("FILE: " + fileInodeData);
        String fields[] = fileInodeData.split(":");
        String allBlocks[] = StringUtils.substringsBetween(fields[3],"[", "]");
        FileInode fileInode = new FileInode(
                Integer.parseInt(fields[1]),
                new AtomicInteger(Integer.parseInt(fields[2])));

        if(allBlocks[0].equals("X")) {
            // No blocks added
        } else {
            for (String perBlock : allBlocks[0].split(",")) {
                fileInode.addNewBlock();
                for (String replicas : StringUtils.substringsBetween(perBlock, "<", ">")) {
                   // System.out.println("Replica Info : " + replicas);
                    fileInode.addEntry(BlockDetails.deserialize(replicas));
                }
            }
        }

        return fileInode;
    }
}

package namespace;

import util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DirInode extends Inode {
    Map<String, Inode> dirEntries;

    public DirInode() {
        super(0);
        dirEntries = new HashMap<String, Inode>();
    }

    public void addDirEntry(String dirName, Inode inode) {
        dirEntries.put(dirName, inode);
    }

    Set<String> listDirEntries() {
        return dirEntries.keySet();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(String dirName: dirEntries.keySet()) {
            sb.append(dirName).append("\n");
        }

        return sb.toString();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("D").append(":");
        if(dirEntries.size() != 0) {
            for (String dirName : dirEntries.keySet()) {
                sb.append(dirName);
                sb.append("|").append(dirEntries.get(dirName).type);
                sb.append(",");
            }
        } else {
            sb.append("X");
            sb.append(",");
        }
        // Remove last added ","
        sb.setLength(sb.length() - 1);;
        sb.append(":");
        sb.append(this.dirEntries.size());
        sb.append("}");

        return sb.toString();
    }

    public static DirInode deserialize(Queue<String> serializeInfo) {
        String dirInodeInfo = serializeInfo.poll();
       // System.out.println("DIR " + dirInodeInfo);
        String fields[] = dirInodeInfo.split(":");
        int numberOfChildren = Integer.parseInt(fields[2]);
        String dirEntryNames[] = fields[1].split(",");

        DirInode dirInode = new DirInode();
        for(int i = 0; i < numberOfChildren; i++) {
            //System.out.println(dirEntryNames[i]);
            String entryValues[] = dirEntryNames[i].split("\\|");
            int type = Integer.parseInt(entryValues[1]);
            Inode inode = (type == Constants.TYPE_FILE) ?
                    FileInode.deserialize(serializeInfo) :
                    DirInode.deserialize(serializeInfo);
            dirInode.dirEntries.put(dirEntryNames[i].split("\\|")[0], inode);
        }

        return dirInode;
    }
}

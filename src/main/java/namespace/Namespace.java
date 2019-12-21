package namespace;

import org.apache.commons.lang.StringUtils;
import util.Cache;
import util.Constants;
import utilization.ClusterUtilization;

import java.util.*;

public class Namespace {
    DirInode root;
    Cache cache;
    public Namespace() {
        cache = new Cache(Constants.CACHE_CAPACITY);
        root = new DirInode();
        if(Constants.CACHE_ON)
            cache.put(Constants.ROOT_DIR, root);
    }

    public Cache getCache() {
        return cache;
    }

    Inode validatePath(String path, final String fields[], int start, int end) {
        Inode curr = root;
        Inode parent = null;
        if(fields.length == 0) {
//            System.out.println("Path is empty");
            return parent;
        }
        if(!fields[0].isEmpty()){
            //path doesn't start with "/"
            return parent;
        }

        int ind = path.lastIndexOf("/");
        String parentPath;
        if(ind == 0)
            parentPath = path.substring(0,ind + 1);
        else
            parentPath = path.substring(0,ind);
//        System.out.println("Parent path: " + parentPath);


        if(Constants.CACHE_ON){
            parent = cache.get(parentPath);
            if(parent != null) {
//                System.out.println("using parent from cache");
                return parent;
            }
        }

        for(int i = start; i <= end; i++) {
            parent = curr;
            if(((DirInode)curr).dirEntries.containsKey(fields[i])) {
                curr = ((DirInode)curr).dirEntries.get(fields[i]);
            } else {
                if(i == fields.length - 1)
                    break;
                return null;
            }
        }
        if(Constants.CACHE_ON)
            cache.put(parentPath, parent);
        return parent;
    }

    public void setRoot(DirInode root) {
        this.root = root;
        if(Constants.CACHE_ON)
            cache.put(Constants.ROOT_DIR, root);
    }

    public Inode getInodeForPath(String path){
//        System.out.println(cache);
        Inode result = null;
        result = cache.get(path);
        if(Constants.CACHE_ON && result != null){
            return result;
        }else{
            String fields[] = path.split("/");
            Inode parent = validatePath(path, fields, 1, fields.length-1);
            if (parent == null) {
                return result;
            }
            if (!((DirInode) parent).dirEntries.containsKey(fields[fields.length - 1])) {
                return result;
            } else {
                result = ((DirInode) parent).dirEntries.get(fields[fields.length - 1]);
            }
            if(Constants.CACHE_ON)
                cache.put(path,result);
            return result;
        }

    }

    public byte create(String path, int fileType) {
//        System.out.println("Namespace : Create -> " + path + " Type : " + fileType);
        byte result = Constants.CREATE_SUCCESS;
        do {
            String[] fields = path.split("/");
            Inode parent = validatePath(path, fields, 1, fields.length-1);
            if (parent == null) {
                result = Constants.CREATE_PATH_INVALID;
                break;
            }
            if (((DirInode) parent).dirEntries.containsKey(fields[fields.length - 1])) {
                result = Constants.CREATE_PATH_OCCUPIED;
                break;
            }

            Inode newNode;
            if(fileType == Constants.TYPE_FILE){
                newNode = new FileInode(0);
                ((DirInode) parent).dirEntries.put(fields[fields.length - 1], newNode);
            }else{
                newNode = new DirInode();
                ((DirInode) parent).dirEntries.put(fields[fields.length - 1], newNode);
            }
//            System.out.println("Parent :" + parent);
//            System.out.println("New Node : " + newNode);
            if(Constants.CACHE_ON)
                cache.put(path, newNode);
        } while (false);
        return result;
    }

    public boolean remove(String path, int fileType, List<List<String>> blocks) {
//        System.out.println("Namespace : Remove -> " + path + " Type : " + fileType);
        boolean result = true;
        do {
            String[] fields = path.split("/");
            Inode parent = validatePath(path, fields, 1, fields.length-1);
            if (parent == null) {
                result = false;
                break;
            }
            Inode nodeToDelete = null;
            if (!((DirInode) parent).dirEntries.containsKey(fields[fields.length - 1])) {
                result = false;
                break;
            } else {
                nodeToDelete = ((DirInode) parent).dirEntries.get(fields[fields.length - 1]);
            }
            //If the Inode of the specified path doean't match the type of the file mentioned,
            // then return false. i.e Asked for a file and found a directory or vice versa
            if (nodeToDelete.type != fileType) {
                result = false;
                break;
            }
            if (fileType == Constants.TYPE_DIR) {
                if (!((DirInode)nodeToDelete).dirEntries.isEmpty()) {
//                    System.out.println("Directory not empty");
                    result = false;
                    break;
                }
            }else{
                FileInode file = (FileInode)nodeToDelete;
                //fill in the blocks to update the cluster utilization info later
                for(List<BlockDetails> details : file.getAddress()){
                    List<String> block = new ArrayList<>();
                    blocks.add(block);
                    for(BlockDetails bd : details){
                        block.add(bd.getHostName());
                    }
                }

            }
//            System.out.println("Node to delete: " + fields[fields.length - 1]);
            ((DirInode) parent).dirEntries.remove(fields[fields.length - 1]);

            if(Constants.CACHE_ON)
                cache.delete(path);

        } while (false);
        return result;
    }


    public byte rename(final String srcPath, final String destPath, int fileType) {
//        System.out.println("Namespace : Rename -> src : " + srcPath + " dest : " + destPath + " Type : " + fileType);
        String[] srcField = srcPath.split("/");
        Inode srcParentNode = validatePath(srcPath, srcField, 1, srcField.length - 1);
//        //System.out.println(srcField[srcField.length - 1]);
//        //System.out.println(((DirInode)srcParentNode).dirEntries);
        if(srcParentNode == null ||
                !((DirInode)srcParentNode).dirEntries.containsKey(srcField[srcField.length - 1])) {
//            //System.out.println("Source Path is not valid!");
            return Constants.RENAME_SRC_INVALID;
        }

        String[] destField = destPath.split("/");
        Inode destParentNode = validatePath(destPath, destField, 1, destField.length - 1);
        if(destParentNode == null) {
//            //System.out.println("Dest Path is not valid! [" + destPath + "]");
            return Constants.RENAME_DEST_INVALID;
        }

        if(((DirInode)destParentNode).dirEntries.containsKey(destField[destField.length -1])) {
//            //System.out.println("Dest Path already occupied! [" + destPath + "]");
            return Constants.RENAME_DEST_OCCUPIED;
        }

        Inode subTree = ((DirInode)srcParentNode).dirEntries.get(srcField[srcField.length - 1]);
        ((DirInode)srcParentNode).dirEntries.remove(srcField[srcField.length - 1]);
        if(srcParentNode == destParentNode)
            ((DirInode)srcParentNode).dirEntries.put(destField[destField.length - 1], subTree);
        else
            ((DirInode)destParentNode).dirEntries.put(destField[destField.length - 1], subTree);

        if(Constants.CACHE_ON) {
            cache.invalidateKeyPrefix(srcPath);
            cache.put(destPath, subTree);
        }

        return Constants.RENAME_SUCCESS;
    }

    public List<List<BlockDetails>> read(String path){
        List<List<BlockDetails>> result = null;
        Inode inode = getInodeForPath(path);
        do{
            if(inode == null || inode.getType() != Constants.TYPE_FILE){
                //Err! file doesn't exist
                break;
            }
            result = ((FileInode)inode).getAddress();
        }while(false);
        return result;
    }

    public FileInode write(String path, int blockSize, List<String> blockLocations) {
//        System.out.println("Namespace : Write -> path : " + path + " Size : " + blockSize + " Locations : " + blockLocations);
        String blockId = "";
        FileInode file = null;
        do {
            Inode nodeToAppend = getInodeForPath(path);
            if(nodeToAppend == null || nodeToAppend.type != Constants.TYPE_FILE){
                break;
            }
            file = (FileInode)nodeToAppend;
//            System.out.println("File inode to append " + file);
            //Update the file size
            file.setSize(file.getSize() + blockSize);

            if(blockLocations == null){ //Implies - add size to the last existing block
//                System.out.println("Updating exiting block");
                List<BlockDetails> lastBlock = file.getAddress().get(file.getAddress().size()-1);
                for(BlockDetails replica : lastBlock){
                    replica.setSize(replica.getSize() + blockSize);
                }
                blockId = String.valueOf(file.getBlockIdCounter());
//                System.out.println("Last Block Id : " + blockId);
                break;
            }
            //Add a new block and the three entries for the same with the hostname of chosen datanodes
//            System.out.println("Adding new blocks");
            file.addNewBlock();
            blockId = "B" + file.incrementAndGet();
            for(String loc :blockLocations){
//                System.out.println("Location " + loc);
                file.addEntry(new BlockDetails(loc, blockId, blockSize));
            }
            if(Constants.CACHE_ON)
                cache.put(path, file);

        } while (false);
////        System.out.println("==================");
////        System.out.println("After adding blocks\n" + file);
////        System.out.println("==================");
        return file;
    }

    public List<String> getBlockLocationsFromInode(FileInode inode, int blockIndex){
        List<String> result = new ArrayList<>();
        List<BlockDetails> details = inode.getAddress().get(blockIndex);
        for(BlockDetails blk : details){
            result.add(blk.getHostName());
        }
        return result;
    }

    public String toString() {

        List<String> output = new LinkedList<>();
        output.add("/");
        helpToString(root,"",output);
        return output.toString();
    }

    private void helpToString(Inode curr, String path, List<String> output){
        if(((DirInode) curr).dirEntries.isEmpty() || curr.type == Constants.TYPE_FILE){
            output.add(path);
            return;
        }
        for (Map.Entry<String, Inode> e : ((DirInode) curr).dirEntries.entrySet()) {
            //path += ( "/" + e.getKey());
            helpToString(e.getValue(),path + "/" + e.getKey() , output);
        }
    }

    public String printTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        DFS("/", root, 0, sb);
        return sb.toString();
    }

    public void DFS(String parent, Inode curr, int level, StringBuilder sb) {
        sb.append(getIndent(level));
        sb.append("+--");
        sb.append(parent);
        sb.append("\n");
        if(curr.type == Constants.TYPE_FILE) {
            sb.append(getIndent(level + 1));
            sb.append("   ");
            sb.append(((FileInode)curr).getTreePrintFormatReduceInfo(getSpaceIndent(level + 1)));
            return;
        }
        for(Map.Entry<String, Inode> dir: ((DirInode)curr).dirEntries.entrySet()) {
            DFS(dir.getKey(), dir.getValue(), level + 1, sb);
        }
    }

    public void DFSReducedInfo(String parent, Inode curr, int level, StringBuilder sb) {
        sb.append(getIndent(level));
        sb.append("+--");
        sb.append(parent);
        sb.append("\n");
        if(curr.type == Constants.TYPE_FILE) {
            return;
        }
        for(Map.Entry<String, Inode> dir: ((DirInode)curr).dirEntries.entrySet()) {
            DFSReducedInfo(dir.getKey(), dir.getValue(), level + 1, sb);
        }
    }

    String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }

    String getSpaceIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("|  ");
        }
        sb.append("   ").append("   ");
        return sb.toString();
    }

    public void traverseUpdateUtilization(ClusterUtilization utilization) {
        traverseUpdateUtilizationRec(root, utilization);
    }

    void traverseUpdateUtilizationRec(Inode currInode, ClusterUtilization util) {
        if(currInode.type == Constants.TYPE_FILE) {
            FileInode fileInode = (FileInode)currInode;
            for(List<BlockDetails> block: fileInode.getAddress()) {
                List<String> blockLocations = new ArrayList<>();
                for(BlockDetails blockDetails: block) {
                    blockLocations.add(blockDetails.getHostName());
                }
                util.updateUtilization(blockLocations, true);
            }
            return;
        }

        DirInode dirInode = (DirInode)currInode;
        for(Map.Entry<String, Inode> dir: dirInode.dirEntries.entrySet()) {
            traverseUpdateUtilizationRec(dir.getValue(), util);
        }
    }

    public String serializeNameSpace() {
//        System.out.println("Received Serialize tree request!");
        return this.serializeNameSpaceRec(this.root);
    }

    public String serializeNameSpaceRec(Inode curr) {
        if(curr.type == Constants.TYPE_FILE) {
            return ((FileInode)curr).serialize();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(((DirInode)curr).serialize());
        for(Inode inode : ((DirInode) curr).dirEntries.values())
            sb.append(serializeNameSpaceRec(inode));

        return sb.toString();
    }

    public static Inode deserializeNameSpaceTree(String serializedTree) {
//        System.out.println("Received Deserialize tree request!");
        String inodeInfo[] = StringUtils.substringsBetween(serializedTree, "{", "}");
        Queue<String> serializedTreeInfo = new LinkedList<>(Arrays.asList(inodeInfo));
        return DirInode.deserialize(serializedTreeInfo);
    }
}

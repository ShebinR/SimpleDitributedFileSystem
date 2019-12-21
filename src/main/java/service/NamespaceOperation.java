package service;

import java.io.Serializable;
import java.util.List;

public class NamespaceOperation implements Serializable {

    static final byte GET = 0x01;
    static final byte CREATE = 0x02;
    static final byte REMOVE = 0x03;
    static final byte RENAME = 0x04;
    static final byte WRITE = 0x05;
    static final byte UPDATE_ELECTION_TIMEOUT = 0x06;
    static final byte UPDATE_PREV_ELECTION_INFO = 0x07;


    private byte op;
    private String path;
    private int fileType;
    private String srcPath;
    private String destPath;
    private int blockSize;
    private List<String> blockLocations;
    private long prevElectionTime;
    private int currElectionTimeout;
    private int numberOfElections;


    public NamespaceOperation(byte op) {
        this.op = op;
    }

    public NamespaceOperation(byte op, long prevTime, int electionCount, int electionTimeout){
        this.op = op;
        this.prevElectionTime = prevTime;
        this.numberOfElections  = electionCount;
        this.currElectionTimeout = electionTimeout;
    }

    public NamespaceOperation(byte op, long prevTime, int electionCount){
        this.op = op;
        this.prevElectionTime = prevTime;
        this.numberOfElections  = electionCount;
    }

    public NamespaceOperation(byte op, String path, int fileType) {
        this.op = op;
        this.path = path;
        this.fileType = fileType;
    }

    public NamespaceOperation(byte op, int fileType, String srcPath, String destPath) {
        this.op = op;
        this.fileType = fileType;
        this.srcPath = srcPath;
        this.destPath = destPath;
    }

    public NamespaceOperation(byte op, String path, int blockSize, List<String> locations){
        this.op = op;
        this.path = path;
        this.blockSize = blockSize;
        this.blockLocations = locations;
    }

    public static NamespaceOperation createUpdateElectionTimeout(long prevElectionTime, int numElections, int electionTimeout) {
        return new NamespaceOperation(UPDATE_ELECTION_TIMEOUT, prevElectionTime, numElections, electionTimeout);
    }

    public static NamespaceOperation createUpdatePrevElectionInfo(long prevElectionTime, int numElections) {
        return new NamespaceOperation(UPDATE_PREV_ELECTION_INFO, prevElectionTime, numElections);
    }

    public static NamespaceOperation createMakedirOrfile(String path, int type) {
        return new NamespaceOperation(CREATE, path, type);
    }

    public static NamespaceOperation createRemovedirOrfile(String path, int type) {
        return new NamespaceOperation(REMOVE, path, type);
    }

    public static NamespaceOperation createRenameFileOrDir(String srcPath, String destPath, int fileType) {
        return new NamespaceOperation(RENAME, fileType, srcPath, destPath);
    }

    public static NamespaceOperation createWrite(String path, int blockSize, List<String> locations){
        return new NamespaceOperation(WRITE, path, blockSize, locations);
    }

    public String getPath() {
        return this.path;
    }

    public byte getOp() {
        return this.op;
    }

    public int getFileType() {
        return fileType;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public List<String> getBlockLocations() {
        return blockLocations;
    }

    public long getPrevElectionTime() {
        return prevElectionTime;
    }

    public int getNumberOfElections() {
        return numberOfElections;
    }

    public int getCurrElectionTimeout() {
        return currElectionTimeout;
    }
}

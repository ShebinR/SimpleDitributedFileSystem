package util;

import com.google.common.collect.ImmutableMap;

public class Constants {

    /* Client Constants */
    // Should be changed to max number of servers in the system
    public static final int RETRY_LIMIT = 1;
    public static boolean CACHE_ON = true;
    public static boolean SNAPSHOT_ON = true;

    public static final int SNAPSHOT_INTERVAL = 300;
    public static final int MAX_UPDATES_BETWEEN_SNAPSHOTS = 10000;
    public static final int ELECTION_TIMEOUT_MILLIS = 1000;
    public static final int MAX_ELECTION_INTERVAL_MILLIS = 15000;
    public static final int MAX_ELECTION_COUNT = 2;
    public static final int MAX_ELECTION_TIMEOUT_MILLIS = 20000;


    public static final int TYPE_DIR = 0;
    public static final int TYPE_FILE = 1;
    public static final int REPLICATION_FACTOR = 3;
    public static final int MAX_NODE_UTILIZATION = 10000;
    public static final int BLOCK_SIZE = 1024 * 128;

    public static final String ROOT_DIR = "/";
    public static final int CACHE_CAPACITY = 100;
    public static final int RESP_CODE_NOT_LEADER = -2;
    public static final int RESP_CODE_ON_APPLY_FAILED = -3;

    /* Write Error code*/
    public static final byte WRITE_SUCCESS = 0x0;
    public static final byte WRITE_PATH_INVALID = 0x1;
    public static final byte WRITE_FILETYPE_INVALID = 0x2;
    public static final byte WRITE_UPDATE_FAILED = 0x3;
    public static final byte WRITE_FAIL_CLUSTER_FULL = 0x4;


    public static final ImmutableMap<Byte, String> WRITE_MESSAGE = ImmutableMap.of(
            WRITE_SUCCESS, "Write success!",
            WRITE_PATH_INVALID, "Invalid file path!",
            WRITE_FILETYPE_INVALID, "Invalid file type!",
            WRITE_UPDATE_FAILED, "Write operation failed!",
            WRITE_FAIL_CLUSTER_FULL, "Cluster is at max utilization! Write Failed!"
            );

    /* Rename Error Code */
    public static final byte RENAME_SUCCESS = 0x0;
    public static final byte RENAME_SRC_INVALID = 0x1;
    public static final byte RENAME_DEST_INVALID = 0x2;
    public static final byte RENAME_DEST_OCCUPIED = 0x3;
    public static final ImmutableMap<Byte, String> RENAME_MESSAGE = ImmutableMap.of(
            RENAME_SUCCESS, "Renamed successfully!",
            RENAME_SRC_INVALID, "Invalid source path!",
            RENAME_DEST_INVALID, "Invalid destination path!",
            RENAME_DEST_OCCUPIED, "Destination already filled!");

    /* Create Error Code */
    public static final byte CREATE_SUCCESS = 0x0;
    public static final byte CREATE_PATH_INVALID = 0x1;
    public static final byte CREATE_PATH_OCCUPIED = 0x2;
    public static final ImmutableMap<Byte, String> CREATE_MESSAGE = ImmutableMap.of(
            CREATE_SUCCESS, "File created successfully!",
            CREATE_PATH_INVALID, "Invalid path!",
            CREATE_PATH_OCCUPIED, "Path already taken!"
    );

    /* Create Error Code */
    public static final byte READ_SUCCESS = 0x0;
    public static final byte READ_PATH_INVALID = 0x1;
    public static final ImmutableMap<Byte, String> READ_MESSAGE = ImmutableMap.of(
            READ_SUCCESS, "Read successful!",
            READ_PATH_INVALID, "Invalid path!"
    );

    /* Stats Category */
    public static final byte STATS_NAMESPACE_TREE = 0x1;
    public static final byte STATS_CLUSTER_UTIL = 0x2;
    public static final byte STATS_CLUSTER_UTIL_UPDATE = 0x3;
    public static final byte STATS_CLUSTER_ELECTION_TO = 0x4;
    public static final byte STATS_CLUSTER_CACHE = 0x5;
    public static final byte STATS_CLUSTER_META = 0x06;
    public static final byte STATS_NODE_METRICS = 0x07;

    public static final ImmutableMap<String, Byte> STATS_COMMAND_LINE = ImmutableMap.<String, Byte>builder()
            .put("tree", STATS_NAMESPACE_TREE)
            .put("util", STATS_CLUSTER_UTIL)
            .put("util_update", STATS_CLUSTER_UTIL_UPDATE)
            .put("eto", STATS_CLUSTER_ELECTION_TO)
            .put("cache", STATS_CLUSTER_CACHE)
            .put("meta", STATS_CLUSTER_META)
            .put("metrics", STATS_NODE_METRICS)
            .build();

    /* Create Error Code */
    public static final byte REMOVE_FILE_SUCCESS = 0x0;
    public static final byte REMOVE_DIR_SUCCESS = 0x1;
    public static final byte REMOVE_FAILURE = 0x2;
    public static final ImmutableMap<Byte, String> REMOVE_MESSAGE = ImmutableMap.of(
            REMOVE_FILE_SUCCESS, "Removed file successfully!",
            REMOVE_DIR_SUCCESS, "Removed Directory successfully!",
            REMOVE_FAILURE, "Invalid path! Remove failed"
    );

    public static final byte RACK_ALIVE = 0x1;
    public static final byte RACK_DEAD = 0x0;
    public static final ImmutableMap<Byte, String> RACK_STATUS = ImmutableMap.of(
            RACK_ALIVE, "ALIVE",
            RACK_DEAD, "DEAD"
    );
}

package namespace;

public class Inode {
    int type;

    Inode(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}

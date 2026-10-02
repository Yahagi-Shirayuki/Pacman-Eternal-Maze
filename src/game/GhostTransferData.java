package game;

public class GhostTransferData {

    int type;
    boolean mortisBuffed;

    GhostTransferData(int type) {
        this(type, false);
    }

    GhostTransferData(int type, boolean mortisBuffed) {
        this.type = type;
        this.mortisBuffed = mortisBuffed;
    }
}

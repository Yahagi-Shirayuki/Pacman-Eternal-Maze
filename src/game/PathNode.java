package game;

public class PathNode {

    int x;
    int y;
    int costFromStart;
    int estimatedCostToGoal;
    PathNode parent;

    PathNode(int x, int y, int costFromStart, int estimatedCostToGoal, PathNode parent) {
        this.x = x;
        this.y = y;
        this.costFromStart = costFromStart;
        this.estimatedCostToGoal = estimatedCostToGoal;
        this.parent = parent;
    }

    int getTotalCost() {
        return costFromStart + estimatedCostToGoal;
    }
}

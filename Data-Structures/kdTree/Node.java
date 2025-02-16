package kdTree;

public class Node {
    private int value;
    private double x;  // 节点相对于root节点的坐标
    private double y;
    private boolean direction; // 此节点划分空间的方向， true：左右（x轴），false：上下（y轴）
    private Node left;   // 左子树和右子树
    private Node right;

    Node(int v, double x, double y, boolean dir) {
        this.value = v;
        this.x = x;
        this.y = y;
        this.direction = dir;
        this.left = null;
        this.right = null;
    }

    public int getValue() {
        return value;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isDirection() {
        return direction;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public void setDirection(boolean parentDir) {
        this.direction = !parentDir;
    }
}


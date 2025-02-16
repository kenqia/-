package kdTree;

public class KdTree {
    /* 2维kdTree
     */
    private Node root;

    KdTree(int value) {
        root = new Node(value, 0, 0, true); // root节点初始化
    }

    public void addNode(Node newNode) {
        /* 向kdTree添加新的节点
         */
        Node curNode = root;
        addNodeHelper(root, newNode);
    }

    public Node nearest(double x, double y) {
        /* 先从当前节点（最初是root节点）开始，计算它和A(x, y)的距离，将其和已知的最近节点进行比较
         * 找到当前节点左右子树中较好的一侧goodSide（A所在区域），较差的一侧badSide；
         * 在goodSide中进行search，更新最近节点；
         * 如果badSide中任何节点的距离都比最近节点大，则略过它，否则search badSide；
         */
        return nearestHelper(root, x, y, root);
    }

    private Node nearestHelper(Node curNode, double x, double y, Node nearNode) {
        if (distance(curNode, x, y) < distance(nearNode, x, y)) {
            nearNode = curNode;
        }

        Node[] sides = getSides(curNode, x, y, nearNode);
        Node goodSide = getGoodSide(sides);
        Node badSide = getBadSide(sides);

        if (goodSide != null) {
            nearNode = nearestHelper(goodSide, x, y, nearNode);
        }

        if (badSide != null) {
            if (curNode.isDirection() && Math.abs(badSide.getY() - y) < distance(nearNode, x, y)) {
                nearNode = nearestHelper(badSide, x, y, nearNode);
            } else if (!curNode.isDirection() && Math.abs(badSide.getX() - x) < distance(nearNode, x, y)) {
                nearNode = nearestHelper(badSide, x, y, nearNode);
            }
        }

        return nearNode;
    }

    private Node[] getSides(Node curNode, double x, double y, Node nearNode) {
        Node[] res = new Node[2];
        Node left = curNode.getLeft();
        Node right = curNode.getRight();
        if (curNode.isDirection()) {
            if (curNode.getX() > x) {
                getRes(left, right, res);
            } else {
                getRes(right, left, res);
            }
        } else {
            if (curNode.getY() > y) {
                getRes(right, left, res);
            } else {
                getRes(left, right, res);
            }
        }
        return res;
    }

    private void getRes(Node a, Node b, Node[] res) {
        res[0] = a;
        res[1] = b;
    }

    private Node getGoodSide(Node[] sides) {
        return sides[0];
    }

    private Node getBadSide(Node[] sides) {
        return sides[1];
    }

    private double distance(Node node, double x, double y) {
        double lenX = node.getX() - x;
        double lenY = node.getY() - y;
        return Math.sqrt(Math.pow(lenX, 2) + Math.pow(lenY, 2));
    }

    private void addNodeHelper(Node curNode, Node newNode) {
        /* 根据当前节点划分的区域添加newNode
         */
        if (curNode.isDirection()) {
            leftOrRight(curNode.getX(), newNode.getX(), curNode, newNode);
        } else {
            leftOrRight(newNode.getY(), curNode.getY(), curNode, newNode);
        }
    }

    private void leftOrRight(double a, double b, Node cur, Node newNode) {
        /* 判断newnode应该添加到cur节点的左子树还是右子树
         */
        if (a > b) {
            leftSide(cur, newNode);
        } else {
            rightSide(cur, newNode);
        }
    }

    private void leftSide(Node cur, Node newNode) {
        /* newNode应该添加到cur节点的左子树上
         * 如果cur节点左子树为空，直接setLeft为newNode
         * 否则，调用helper方法进行添加
         */
        if (checkLeftEmpty(cur)) {
            cur.setLeft(newNode);
            newNode.setDirection(cur.isDirection());
        } else {
            addNodeHelper(cur.getLeft(), newNode);
        }
    }

    private boolean checkLeftEmpty(Node node) {
        return node.getLeft() == null;
    }

    private void rightSide(Node cur, Node newNode) {
        /* newNode应该添加到cur节点的右子树上
         * 如果cur节点右子树为空，直接setRight为newNode
         * 否则，调用helper方法进行添加
         */
        if (checkRightEmpty(cur)) {
            cur.setRight(newNode);
            newNode.setDirection(cur.isDirection());
        } else {
            addNodeHelper(cur.getRight(), newNode);
        }
    }

    private boolean checkRightEmpty(Node node) {
        return node.getRight() == null;
    }
}


import java.util.Scanner;
//1
public class AdjacencyListGraph {
    private static final int MaxVnum = 100;
    private static Scanner sc = new Scanner(System.in);

    public static class AdjNode {
        int v;
        AdjNode next;
    }

    public static class VexNode {
        String data;
        AdjNode first;
    }

    public static class AlGraph {
        VexNode vex[] = new VexNode[MaxVnum];
        int vexnum, edgenum;
    }

    public static int locateVex(AlGraph g, String x) {
        for (int i = 0; i < g.vexnum; i++) {
            if (g.vex[i].data.equals(x)) {
                return i;
            }
        }
        return -1;
    }

    public static void insertEdge(AlGraph g, int i, int j) {
        AdjNode s = new AdjNode();
        s.v = j;
        s.next = g.vex[i].first;
        g.vex[i].first = s;
    }

    public static void createGraph(AlGraph g) {
        g.vexnum = sc.nextInt();
        g.edgenum = sc.nextInt();
        for (int i = 0; i < g.vexnum; i++) {
            g.vex[i] = new VexNode();
            g.vex[i].data = sc.next();
            g.vex[i].first = null;
        }
        String u, v;
        while (g.edgenum-- > 0) {
            u = sc.next();
            v = sc.next();
            int i = locateVex(g, u);
            int j = locateVex(g, v);
            if (i != -1 && j != -1) {
                insertEdge(g, i, j);
            } else {
                System.out.println("错误");
                g.edgenum++;
            }
        }
    }

    public static void print(AlGraph g) {
        for (int i = 0; i < g.vexnum; i++) {
            System.out.print(g.vex[i].data);
            AdjNode t = g.vex[i].first;
            while (t != null) {
                System.out.print("->[" + t.v + "]");
                t = t.next;
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        AdjacencyListGraph.AlGraph x = new AlGraph();
        AdjacencyListGraph.createGraph(x);
        AdjacencyListGraph.print(x);
    }

}
/*
4 5
a b c d
a b
a d
b c
d b
d c
 */

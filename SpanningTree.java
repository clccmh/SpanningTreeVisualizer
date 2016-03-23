import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Author Carter Hay 
 *  
 */
public class SpanningTree {

  public static void main (String[] args) {
    SpanningTree st = new SpanningTree();
    if (args.length == 1) {
      System.out.println("Opening file: " + args[0]);
      File file = new File(args[0]);
      if (file.exists()) {
        if (file.canRead()) {
          try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
              System.out.println(line);
              if (line.matches("[0-9]* R")) {
                System.out.println("Random");
                st.calculateRandomConnections(line);
              } else if (line.matches("[0-9]* ([0-9]*-[0-9]* )*")) {
                System.out.println("Connections list");
                System.out.println(st.calculateConnections(line));
              } else {
                System.out.println("Invalid line");
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          System.out.println("That file exists, but it cannot be read.");
        }
      } else {
        System.out.println("That file does not exist.");
      }
    } else {
      System.out.println("Usage: java SpanningTree [Filename]");
    }
  }

  /**
   * @param line The line to be calculated
   */
  private void calculateRandomConnections (String line) {

  }

  /**
   * @param line The line to be calculated
   */
  private ArrayList<Node> calculateConnections (String line) {
    String[] splitLine = line.split(" ");
    ArrayList<Node> nodes = new ArrayList<Node>();
    for (int i = 0; i < Integer.parseInt(splitLine[0]); i++) {
      nodes.add(new Node(i+1, i+1, 0));
    }
    for (int i = 1; i < splitLine.length; i++) {
      String[] connect = splitLine[i].split("-");
      nodes.get(Integer.parseInt(connect[0])-1).addConnection(nodes.get(Integer.parseInt(connect[1])-1));
      nodes.get(Integer.parseInt(connect[1])-1).addConnection(nodes.get(Integer.parseInt(connect[0])-1));
    }
    return nodes;
  }

  /**
   * @param nodes The nodes to calculate the tree for
   */
  private void calculateTree(ArrayList<Node> nodes) {
    for (Node node : nodes) {
      for (Node connection : node.getConnections()) {
        connection.calculateRoot(node);
      }
    }
  }

  private class Node {
    private ArrayList<Node> connections;
    private int myself;
    private int root;
    private int hops;

    public Node (int myself, int root, int hops) {
      this.connections = new ArrayList<Node>();
      this.myself = myself;
      this.root = root;
      this.hops = hops;
    }

    /**
     * @param node The node to be added to the connections
     */
    public void addConnection(Node node) {
      connections.add(node);
    }

    /**
     * @return The connections for the node
     */
    public ArrayList<Node> getConnections() {
      return this.connections;
    }

    /**
     * @return The root of the node
     */
    public int getRoot() {
      return this.root;
    }

    /**
     * @return The hops till the root of the node
     */
    public int getHops() {
      return this.hops;
    }

    /**
     * @return String that represents the node
     */
    public String toString() {
      return "[" + this.myself + " " + this.root + " " + this.hops + "]";
    }

    /**
     * @param node The node to compare with
     */
    public void calculateRoot(Node node) {
      if (node.getRoot() < this.root) {
        this.root = node.getRoot();
        this.hops = node.getHops() + 1;
      }
    }
  }

}

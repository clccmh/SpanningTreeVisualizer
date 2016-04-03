import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.lang.StringBuilder;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.Scanner;

/**
 * This program implements a SpanningTree as learned in Csci4311 and pertaining to 
 * networking connections between hubs. 
 *
 * @author Carter Hay 
 * @version 1.0
 *
 * TODO: Code review
 *
 */
public class SpanningTree {
  
  JFrame jFrame = new JFrame();

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
                ArrayList<Node> nodes = null;
                boolean invalid = true;
                while (invalid) {
                  line = st.generateRandomConnections(line);
                  System.out.println(line);
                  nodes = st.calculateConnections(line);
                  invalid = st.containsOrphan(nodes);
                }
                st.handleTree(nodes);
              } else if (line.matches("[0-9]* ([0-9]*-[0-9]* )*")) {
                ArrayList<Node> nodes = st.calculateConnections(line);
                if (!st.containsOrphan(nodes)) {
                  st.handleTree(nodes);
                } else {
                  System.out.println("Invalid line: Part of the tree is not attatched.");
                }
              } else {
                System.out.println("Invalid line: Invalid format.");
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
    System.exit(0);
  }

  /**
   * This is the method that is responsible for handling the tree, 
   * and calculating it step by step.
   *
   * @param nodes The nodes that make up the tree.
   */
  private void handleTree(ArrayList<Node> nodes) {
    this.findAllRoots(nodes);
    this.removeCycles(nodes);
    this.draw(nodes);
    System.out.println("Press any key to continue to the next tree.");
    try {
      System.in.read();
    } catch (IOException e) {
    }
  }

  /**
   * Calculates the roots for every node in the tree.
   *
   * @param nodes An ArrayList of nodes to calculate the Roots for
   */
  private void findAllRoots(ArrayList<Node> nodes) {
    String previous = "";
    for (int i = 0; i < nodes.size(); i++) {
      draw(nodes);
      System.out.println("Please type n to see the next step, or q to quit.");
      Scanner in = new Scanner(System.in);
      String choice = in.nextLine();
      if (choice.compareToIgnoreCase("q") == 0) {
        System.exit(0);
      }
      this.calculateTree(nodes);
      if (previous.equals(nodes.toString())) {
        break;
      }
      previous = nodes.toString();
    }
  }

  /**
   * Generates a string representing random connections between nodes
   *
   * @param line The line to be calculated
   * @return String representing random connections between nodes
   */
  private String generateRandomConnections (String line) {
    StringBuilder sb = new StringBuilder();
    Random rand = new Random();
    int numOfNodes = Integer.parseInt(line.split(" ")[0]);
    System.out.println("numOfNodes: " + numOfNodes);
    sb.append(line.split(" ")[0] + " ");
    for (int i = 1; i <= numOfNodes; i++) {
      int con = rand.nextInt(numOfNodes)+1;
      if (i == con && con < numOfNodes) {
        con++;
      } else if (con == numOfNodes) {
        con--;
      }
      sb.append(i + "-" + con + " ");
    }
    return sb.toString();
  }

  /**
   * Calculates the nodes from a string representing the connections
   *
   * @param line The line to be calculated
   * @return An ArrayList of nodes in the tree.
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
   * Determines if an arraylist representing a spanning tree contains 
   * any sections that are orphaned from the rest
   *
   * @return Whether or not a tree contains orphanned sections
   * @see canReachNode
   */
  private boolean containsOrphan (ArrayList<Node> nodes) {
    for (Node node : nodes) {
      for (Node node2 : nodes) {
        if (node2 != node) {
          if (!canReachNode(node2, node, new ArrayList<Node>())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Recursive helper method to dertermine if a node can reach another node
   *
   * @return Whether or not a node can reach another node
   */
  private boolean canReachNode (Node current, Node toReach, ArrayList<Node> alreadyChecked) {
    alreadyChecked.add(current);
    boolean reached = false;
    for (Node con : current.getConnections()) {
      if (con == toReach) {
        return true;
      } else if (!alreadyChecked.contains(con)) {
        reached = reached | canReachNode(con, toReach, alreadyChecked);
      }
    }
    return reached;
  }

  /**
   * @param nodes The nodes to calculate the tree for
   */
  private void calculateTree (ArrayList<Node> nodes) {
    ArrayList<Node> temp = new ArrayList<Node>();
    for (Node node : nodes) {
      temp.add(new Node(node.getAddress(), node.getRoot(), node.getHops(), node.getConnections()));
    }
    for (Node node : temp) {
      for (Node connection : node.getConnections()) {
        connection.calculateRoot(node);
      }
    }
  }

  /**
   * Removes all cycles from the tree
   *
   * @param nodes The nodes that make up the tree
   */
  private void removeCycles(ArrayList<Node> nodes) {
    for (Node node : nodes) {
      //Remove duplicate connections to the same node
      Set<Node> set = new HashSet<Node>();
      set.addAll(node.getConnections());
      node.setConnections(new ArrayList<Node>(set));

      //Remove duplicate paths to the root.
      //Each node should only have two connections at max and only one with a smaller number of hops.
      if (node.getConnections().size() > 1) {
        ArrayList<Node> temp = new ArrayList<Node>();
        Node currentSmallest = null;
        for (Node con : node.getConnections()) {
          if (con.getHops() <= node.getHops()) {
            if (currentSmallest == null) {
              currentSmallest = con;
            } else if (con.getHops() < currentSmallest.getHops()) {
              currentSmallest = con;
            } else if (con.getHops() == currentSmallest.getHops() && con.getAddress() < currentSmallest.getAddress()) {
              currentSmallest = con;
            }
          } else {
            temp.add(con);
          }
        }
        if (currentSmallest != null) {
          temp.add(currentSmallest);
        }
        node.setConnections(temp);
      }
    }

    // This is needed to make sure one connection does not exist where the reciprocal is removed
    for (Node node : nodes) {
      for (Node con : node.getConnections()) {
        if (!con.getConnections().contains(node)) {
          node.removeConnection(con);
        }
      }
    }
    
  }

  /**
   * Draws the tree
   *
   * @param nodes The nodes that make up the tree.
   * @see drawingBoard
   */
  private void draw (ArrayList<Node> nodes) {
    for (Node node : nodes) {
      System.out.print("\t" + node + ": ");
      for (Node con : node.getConnections()) {
        System.out.print(con + ", ");
      }
      System.out.println("");
    }
    jFrame.dispose();
    jFrame = new JFrame();
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setAlwaysOnTop(true);
    jFrame.setFocusableWindowState(false);
    jFrame.setSize(500, 500);
    jFrame.add(new drawingBoard(nodes));
    jFrame.setVisible(true);
    
  }

  /**
   * 
   */
  private class Node {
    private ArrayList<Node> connections;
    private int address;
    private int root;
    private int hops;
    private int x;
    private int y;

    public Node (int address, int root, int hops, ArrayList<Node> connections) {
      this.connections = connections;
      this.address = address;
      this.root = root;
      this.hops = hops;
    }

    public Node (int address, int root, int hops) {
      this.connections = new ArrayList<Node>();
      this.address = address;
      this.root = root;
      this.hops = hops;
    }

    /**
     * Sets the coordinates of the node
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public void setCoords (int x, int y) {
      this.x = x;
      this.y = y;
    }

    /**
     * @return The x coordinate of the node
     */
    public int getX() {
      return this.x;
    }

    /**
     * @return The y coordinate of the node
     */
    public int getY() {
      return this.y;
    }

    /**
     * @param node The node to be added to the connections
     */
    public void addConnection(Node node) {
      connections.add(node);
    }

    /**
     * Removes a connection
     *
     * @param node The node to remove from the connections
     */
    public void removeConnection(Node node) {
      connections.remove(node);
    }

    /**
     * @return The connections for the node
     */
    public ArrayList<Node> getConnections() {
      return this.connections;
    }

    /**
     * @param con The connection list to be set
     */
    public void setConnections(ArrayList<Node> con) {
      this.connections = con;
    }

    /**
     * @return Myself for the node
     */
    public int getAddress() {
      return this.address;
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
      return "[" + this.address + " " + this.root + " " + this.hops + "]";
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
  
  /**
   * This handles the pop up gui that draws the tree.
   */
  private class drawingBoard extends JPanel {
    private ArrayList<Node> nodes;

    public drawingBoard (ArrayList<Node> nodes) {
      this.nodes = nodes;
    }

    @Override
    public void paintComponent (Graphics g) {
      int rectWidth = 50;
      int rectHeight = 50;
      int rectX = (int)(super.getSize().getWidth()/2)-(rectWidth/2);
      int rectY = 10;
      int prevX = rectX;
      int prevY = rectY;
      int num = 0;

      // Loop through all of the nodes and draw the boxes and strings
      for (Node node : nodes) {
        node.setCoords(prevX, prevY);
        g.drawRect(prevX, prevY, rectWidth, rectHeight);
        g.drawString(node.toString(), prevX+5, prevY+(rectHeight/2)+5);
        if (num % 3 == 0) {
          prevX = rectX-rectWidth;
          prevY = prevY+(rectHeight*2);
        } else if (num % 3 == 1) {
          prevX = prevX+(rectWidth*2);
        } else if (num % 3 == 2) {
          prevX = prevX-rectWidth;
          prevY = prevY+(rectHeight*2);
        }
        num++;
      }

      // Loop through all of the nodes and draw the lines
      for (Node node : nodes) {
        ArrayList<int[]> lines = new ArrayList<int[]>();
        int nodeX = node.getX()+rectWidth/2;
        int nodeY = node.getY()+rectHeight/2;
        // Create a line between every connection and the node
        for (Node con : node.getConnections()) {
          int[] line = new int[4];
          line[0] = nodeX;
          line[1] = nodeY;
          line[2] = con.getX()+rectWidth/2;
          line[3] = con.getY()+rectHeight/2;
          lines.add(line);
        }

        // For each line, move it over by five if there is more than one line
        for (int[] line : lines) {
          int inc = 5;
          for (int[] ln : lines) {
            if (line != ln) {
              if (line[2] == ln[2] && line[3] == ln[3]) {
                ln[2] += inc;
                ln[0] += inc;
                if (inc > 0) {
                  inc = inc * -1;
                } else {
                  inc = inc * -2;
                }
              }
            }
          }
        }
        // Draw all of the lines
        for (int[] line : lines) {
          g.drawLine(line[0], line[1], line[2], line[3]);
        }
      }

    }
  }

}

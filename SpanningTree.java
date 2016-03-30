import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
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
 * @Author Carter Hay 
 *
 * TODO: Implement removal of the duplicate paths to the root
 * Find a good way to draw the tree
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
                line = st.calculateRandomConnections(line);
                System.out.println(line);
                ArrayList<Node> nodes = st.calculateConnections(line);
                st.findAllRoots(nodes);
                st.removeDuplicatePaths(nodes);
                st.draw(nodes);
                System.out.println("Press any key to continue to the next tree");
                System.in.read();
              } else if (line.matches("[0-9]* ([0-9]*-[0-9]* )*")) {
                ArrayList<Node> nodes = st.calculateConnections(line);
                st.findAllRoots(nodes);
                st.removeDuplicatePaths(nodes);
                st.draw(nodes);
                System.out.println("Press any key to continue to the next tree");
                System.in.read();
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
    System.exit(0);
  }

  /**
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
   * @param line The line to be calculated
   */
  private String calculateRandomConnections (String line) {
    StringBuilder sb = new StringBuilder();
    Random rand = new Random();
    int numOfNodes = Character.getNumericValue(line.charAt(0));
    sb.append(line.charAt(0) + " ");
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

  private void removeDuplicatePaths(ArrayList<Node> nodes) {
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
    
  }

  private void draw (ArrayList<Node> nodes) {
    jFrame.dispose();
    jFrame = new JFrame();
    jFrame.add(new drawingBoard(nodes));
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setAlwaysOnTop(true);
    jFrame.setFocusableWindowState(false);
    jFrame.setSize(500, 500);
    jFrame.setVisible(true);
    
  }

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

    public void setCoords (int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return this.x;
    }

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
     * 
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
  
  private class drawingBoard extends JPanel {
    private ArrayList<Node> nodes;

    public drawingBoard(ArrayList<Node> nodes) {
      this.nodes = nodes;
    }

    public void paintComponent (Graphics g) {
      int rectWidth = 50;
      int rectHeight = 50;
      int rectX = (int)(super.getSize().getWidth()/2)-(rectWidth/2);
      int rectY = 10;
      int prevX = rectX;
      int prevY = rectY;
      int num = 0;
      ArrayList<Node> alreadyLined = new ArrayList<Node>();

      for (Node node : nodes) {
        node.setCoords(prevX, prevY);
        g.drawRect(prevX, prevY, rectWidth, rectHeight);
        g.drawString(node.toString(), prevX+5, prevY+(rectHeight/2)+5);
        if (num % 3 == 0) {
          prevX = rectX-rectWidth;
          prevY = prevY+rectHeight*2*((num/3)+1);
        } else if (num % 3 == 1) {
          prevX = prevX+(rectWidth*2);
        } else if (num % 3 == 2) {
          prevX = prevX-rectWidth;
          prevY = prevY+rectHeight*2;
        }
        num++;
      }

      for (Node node : nodes) {
        for (Node con : node.getConnections()) {
          g.drawLine(node.getX()+rectWidth/2, node.getY()+rectHeight/2, con.getX()+rectWidth/2, con.getY()+rectHeight/2);
        }
      }

    }
  }

}

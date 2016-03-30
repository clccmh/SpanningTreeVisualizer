import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @Author Carter Hay
 *
 */
public class DrawingTest extends JPanel{
  private Graphics g;

  public static void main (String[] args) {
    Graphics g = new Graphics();
    g.drawRect(10, 10, 100, 100);
    JFrame jFrame = new JFrame();
    jFrame.add(new DrawingTest(g));
    jFrame.setSize(500, 500);
    jFrame.setVisible(true);
    
  }

  public DrawingTest(Graphics g) {
    this.g = g;
  }

  @Override
  public void paintComponent (Graphics g) {
    g = this.g;
    int rectX = (int)(super.getSize().getWidth()/2)-(50/2);
    int rectY = 10;
    int rectWidth = 50;
    int rectHeight = 50;
    g.drawRect(rectX, rectY, rectWidth, rectHeight);
    g.drawString("[1 1 0]", rectX+5, rectY+(rectHeight/2)+5);

    g.drawRect(rectX-(rectWidth), rectHeight*2, rectWidth, rectHeight);
    g.drawString("[3 1 1]", rectX-(rectWidth)+5, rectHeight*2+(rectHeight/2)+5);

    g.drawLine(rectX+rectWidth/2, rectY+rectHeight, rectX-rectWidth+rectWidth/2, rectHeight*2);

    g.drawRect(rectX+(rectWidth), rectHeight*2, rectWidth, rectHeight);
    g.drawString("[4 1 1]", rectX+(rectWidth)+5, rectHeight*2+(rectHeight/2)+5);

    g.drawLine(rectX+rectWidth/2, rectY+rectHeight, rectX+rectWidth+rectWidth/2, rectHeight*2);
  }
}

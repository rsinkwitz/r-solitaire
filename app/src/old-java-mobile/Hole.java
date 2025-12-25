package rainer.solitaire;

import javax.microedition.lcdui.*;

public class Hole {

  private boolean selected;
  private boolean hasPeg;
  private SolitaireCanvas canvas;

  public Hole(SolitaireCanvas canvas) {
    this.canvas = canvas;
    selected = false;
    hasPeg = true;
  }

  public Hole(SolitaireCanvas canvas, boolean hasPeg) {
    this.canvas = canvas;
    selected = false;
    this.hasPeg = hasPeg;
  }

  protected void paint(int x, int y, int width, int height, Graphics g) {

    int pegInset = 2;

    if (!hasPeg) {
      // Leeres Loch zeichnen
      g.  setColor(0, 0, 255);
      g.drawArc(x+pegInset, y+pegInset, width-2*pegInset, height-2*pegInset, 0, 360);
    } else {
      if(selected) {
        // Ausgewähltes Loch zeichnen
        g.setColor(255, 0, 0);
        g.fillArc(x+pegInset, y+pegInset, width-2*pegInset, height-2*pegInset, 0, 360);
      } else {
        // Normales Loch zeichnen, mit Stöpsel
        g.setColor(0, 0, 255);
        g.fillArc(x+pegInset, y+pegInset, width-2*pegInset, height-2*pegInset, 0, 360);

      }
    }
  }

  public boolean isHasPeg() {
    return hasPeg;
  }
  public void setHasPeg(boolean hasPeg) {
    this.hasPeg = hasPeg;
  }
  public boolean isSelected() {
    return selected;
  }
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

}

package rainer.solitaire;

import javax.microedition.lcdui.*;

public class SolitaireCanvas
    extends Canvas
    implements CommandListener {
  public static SolitaireCanvas instance;
  private Hole[][] holes;
  private int ofx, ofy, wx, wy;
  private int selx = -1, sely = -1;
  private int selxLast = -1, selyLast = -1;

//  int numKeyPressed = -1;
  private int cursorX = 0;
  private int cursorY = 0;
  private static final int cursorInset = 4;
  private Move[] moves;
  private int movestop;
  private boolean isBeforeFirst;

  public SolitaireCanvas() {
    try {
      jbInit();
      instance = this;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    setCommandListener(this);
    addCommand(new Command("Zurueck", Command.SCREEN, 1));
    addCommand(new Command("Neustart", Command.SCREEN, 1));
    addCommand(new Command("Beenden", Command.EXIT, 1));
    setupHoles();
    setupScaling();
  }

  public void commandAction(Command command, Displayable displayable) {
    if (command.getCommandType() == Command.EXIT) {
      Solitaire.quitApp();
    } else {
      if (command.getLabel().equals("Neustart")) {
        setupHoles();
        setupScaling();
        unSelect();
        repaint();
      }
      if (command.getLabel().equals("Zurueck")) {
        undoMove();
        repaint();
      }
    }
  }

  protected void paint(Graphics g) {
    g.setColor(255, 255, 255);
    g.fillRect(0, 0, getWidth(), getHeight());
//    if (numKeyPressed>-1) {
//      g.setColor(0, 255, 0);
//      g.fillRect(0, ofy + wy * numKeyPressed, wx*7, wy);
//    }
    for (int j = 0; j < 7; j++) {
      for (int i = 0; i < 7; i++) {
        if (holes[j][i] != null) {
          holes[j][i].paint(ofx + wx * i, ofy + wy * j, wx, wy, g);
        }
      }
    }
    g.setColor(255, 255, 0);
    g.fillArc(ofx + wx * cursorX + cursorInset,
              ofy + wy * cursorY + cursorInset, wx - 2 * cursorInset,
              wy - 2 * cursorInset, 0, 360);
    //g.setColor(0, 0, 0);
    //g.drawString("By Rainer", 0, 0, g.TOP | g.LEFT);
  }

  protected void setupScaling() {
    ofx = 0;
    ofy = 0;
    wx = getWidth() / 7;
    wy = getHeight() / 7;
    if (wy > wx) {
      wy = wx;
    }
    wx = wy;
    ofx = (getWidth() - 7 * wx) / 2;
    ofy = (getHeight() - 7 * wy) / 2;
  }

  protected void setupHoles() {
    holes = new Hole[7][];
    Hole[] row;
    int rowno = 0;
    int colno;

    row = new Hole[7];
    colno = 2;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    row = new Hole[7];
    colno = 2;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    row = new Hole[7];
    colno = 0;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    row = new Hole[7];
    colno = 0;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    row = new Hole[7];
    colno = 0;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    row = new Hole[7];
    colno = 2;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    row = new Hole[7];
    colno = 2;
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    row[colno++] = new Hole(this);
    holes[rowno++] = row;

    moves = new Move[100];
    movestop = 0;
    isBeforeFirst = true;
  }

  /**
   * pointerPressed
   *
   * @param x int
   * @param y int
   */
  protected void pointerPressed(int x, int y) {
    holeAction((x - ofx) / wx, (y - ofy) / wy);
  }

  protected void holeAction(int x, int y) {
    if (holes[y][x] != null) {
      if (isBeforeFirst) {
        holes[y][x].setHasPeg(false);
        isBeforeFirst = false;
      } else {
        if (holes[y][x].isSelected()) {
          unSelect();
        } else {
          setSelected(x, y);
          if (hasLastSelected()) {
            move(selxLast, selyLast, selx, sely);
            unSelect();
          }
        }
      }
      repaint();
    }
  }

  protected void setSelected(int x, int y) {
    selxLast = selx;
    selyLast = sely;
    selx = x;
    sely = y;
    if (hasLastSelected()) {
      holes[selyLast][selxLast].setSelected(false);
    }
    holes[sely][selx].setSelected(true);
  }

  protected void unSelect() {
    if (hasSelected()) {
      holes[sely][selx].setSelected(false);
    }
    selxLast = -1;
    selyLast = -1;
    selx = -1;
    sely = -1;
  }

  protected boolean hasSelected() {
    return selx != -1;
  }

  protected boolean hasLastSelected() {
    return selxLast != -1;
  }

  protected void move(int xfrom, int yfrom, int xto, int yto) {
    int xover = (xfrom + xto) / 2;
    int yover = (yfrom + yto) / 2;
    if ((xfrom == xto || yfrom == yto)
        && Math.abs(xfrom - xto) + Math.abs(yfrom - yto) == 2
        && holes[yfrom][xfrom].isHasPeg()
        && holes[yover][xover].isHasPeg()
        && !holes[yto][xto].isHasPeg()) {
      holes[yfrom][xfrom].setHasPeg(false);
      holes[yover][xover].setHasPeg(false);
      holes[yto][xto].setHasPeg(true);
      moves[movestop++] = new Move(xfrom, yfrom, xto, yto);
    }
  }

  protected void undoMove() {
    if (movestop > 0) {
      Move m = moves[--movestop];
      int xfrom = m.fromx;
      int yfrom = m.fromy;
      int xto = m.tox;
      int yto = m.toy;
      int xover = (xfrom + xto) / 2;
      int yover = (yfrom + yto) / 2;
      holes[yfrom][xfrom].setHasPeg(true);
      holes[yover][xover].setHasPeg(true);
      holes[yto][xto].setHasPeg(false);
    }
  }

  /**
   * keyPressed
   *
   * @param keyCode int
   */
  protected void keyPressed(int keyCode) {
    int gameAction = getGameAction(keyCode);
    /*
         if (keyCode >= KEY_NUM1 && keyCode <= KEY_NUM7) {
      int keyvalue = keyCode - KEY_NUM1;
      if (numKeyPressed == -1) {
        numKeyPressed = keyvalue;
        repaint();
      } else {
        holeAction(keyvalue, numKeyPressed);
        numKeyPressed=-1;
      }
         } */
    if (gameAction == LEFT) { //|| keyCode == -3  || keyCode == KEY_NUM4
      cursorX--;
      if (cursorX < 0)
        cursorX = 0;
      repaint();
    }
    if (gameAction == RIGHT) { //|| keyCode == -4  || keyCode == KEY_NUM6
      cursorX++;
      if (cursorX > 6)
        cursorX = 6;
      repaint();
    }
    if (gameAction == UP) { //|| keyCode == -1  || keyCode == KEY_NUM2
      cursorY--;
      if (cursorY < 0)
        cursorY = 0;
      repaint();
    }
    if (gameAction == DOWN) { //|| keyCode == -2  || keyCode == KEY_NUM8
      cursorY++;
      if (cursorY > 6)
        cursorY = 6;
      repaint();
    }
    if (gameAction == FIRE) { //|| keyCode == -5  || keyCode == KEY_NUM5
      holeAction(cursorX, cursorY);
    }
  }

  private class Move {
    public int fromx, fromy, tox, toy;

    public Move(int fromx, int fromy, int tox, int toy) {
      this.fromx = fromx;
      this.fromy = fromy;
      this.tox = tox;
      this.toy = toy;
    }
  }

}

package rainer.solitaire;

import javax.microedition.lcdui.*;

public class SplashScreen extends Canvas implements CommandListener {
  public SplashScreen() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    setCommandListener(this);
    addCommand(new Command("Weiter", Command.SCREEN, 1));
  }

  public void commandAction(Command command, Displayable displayable) {
    if (command.getLabel().equals("Weiter")) {
      Display.getDisplay(Solitaire.instance).setCurrent(SolitaireCanvas.instance);
    }

  }

  protected void paint(Graphics g) {
    g.setColor(255, 255, 255);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(0, 0, 0);
    g.drawString("By Rainer", getWidth()/2, getHeight()/2, g.HCENTER | g.BASELINE);
    //g.drawString("key="+kc, 0, 10, g.TOP | g.LEFT);
  }

}

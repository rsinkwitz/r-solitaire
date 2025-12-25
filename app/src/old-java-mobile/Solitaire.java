package rainer.solitaire;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class Solitaire extends MIDlet {
  static Solitaire instance;
  SolitaireCanvas displayable = new SolitaireCanvas();
  SplashScreen splash = new SplashScreen();
  public Solitaire() {
    instance = this;
  }

  public void startApp() {
    Display.getDisplay(this).setCurrent(splash);
    //Display.getDisplay(this).setCurrent(displayable);
  }

  public void pauseApp() {
  }

  public void destroyApp(boolean unconditional) {
  }

  public static void quitApp() {
    instance.destroyApp(true);
    instance.notifyDestroyed();
    instance = null;
  }

}

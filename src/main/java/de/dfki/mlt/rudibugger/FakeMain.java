package de.dfki.mlt.rudibugger;
public class FakeMain {

  /** This class and main only exists to trick the JavaFX application starter,
   *  which requires JavaFX to be a module in the final jar.
   */
  public static void main(String[] args) {
    MainApp.main(args);
  }

}

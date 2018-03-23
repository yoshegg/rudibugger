/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.DataModelAdditions;

import de.dfki.mlt.rudibugger.DataModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides additional functionality to start VOnDAs compiler.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class VondaCompilation {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("vondaConnect");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /**
   * Initializes this addition of <code>DataModel</code>.
   *
   * @param model  The current <code>DataModel</code>
   */
  public VondaCompilation(DataModel model) { _model = model; }

  /**
   * Starts the default compilation process as specified in the project's YAML
   * configuration file.
   *
   * @throws IOException
   * @throws InterruptedException
   */
  public void startDefaultCompile() throws IOException, InterruptedException {
    String compileScript = _model.getCompileFile().toString();
    startCompile(compileScript);
  }

  /**
   * Starts VOnDAs compilation process by using a given command.
   *
   * @param inputCmd  A custom command
   * @throws IOException
   * @throws InterruptedException
   */
  public void startCompile(String inputCmd) throws IOException, InterruptedException {
    _model.rudiSave.quickSaveAllFiles();

    String command = "bash -c '"
          + "cd " + _model.getRootFolder().toString() + ";"
          + inputCmd + ";"
          + "read -n1 -r -p \"Press any key to continue...\" key;'";

    log.info("Starting compilation...");
    File mateTerminal = new File("/usr/bin/mate-terminal");
    Process p;
    String windowTitle = "Compiling " + _model.getProjectName();
    String titleOpt = "-T";

    if ("Linux".equals(System.getProperty("os.name"))) {
      String[] cmd;
      String termString = "/usr/bin/xterm";
      if (mateTerminal.exists()) {
        termString = "/usr/bin/mate-terminal";
        titleOpt = "-t";
      }
      cmd = new String[] { termString, titleOpt, windowTitle, "-e", command};

      log.debug("Executing the following command: " + Arrays.toString(cmd));

      p = Runtime.getRuntime().exec(cmd);
    } else {
      p = Runtime.getRuntime().exec(_model.getCompileFile().toString());
    }
  }
}

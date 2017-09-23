/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger;

import de.dfki.rudibugger.project.Project;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Helper {

  static Logger log = Logger.getLogger("rudiLog");

  /**
   * This function asks the user explicitly to close the current project and
   * open another one.
   * @param proj
   * @return true if user accepted to overwrite currently opened project
   */
  public static boolean overwriteProjectCheck(Project proj) {
    log.debug("A project is already opened.");
    log.debug("Asking whether it should be replaced.");
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Open new project?");
    alert.setContentText(
            "There is already an opened project: \n" + proj.getProjectName()
            + "\n\nDo you want to close this project and \nopen another one?");
    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.OK;
  }

  // taken from https://stackoverflow.com/questions/17307761/is-there-a-java-equivalent-to-pythons-easy-string-splicing

  public static String slice_start(String s, int startIndex) {
    if (startIndex < 0) {
      startIndex = s.length() + startIndex;
    }
    return s.substring(startIndex);
  }

  public static String slice_end(String s, int endIndex) {
    if (endIndex < 0) {
      endIndex = s.length() + endIndex;
    }
    return s.substring(0, endIndex);
  }

  public static String slice_range(String s, int startIndex, int endIndex) {
    if (startIndex < 0) {
      startIndex = s.length() + startIndex;
    }
    if (endIndex < 0) {
      endIndex = s.length() + endIndex;
    }
    return s.substring(startIndex, endIndex);
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudibugger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Constants {

  /* the standard ports for rudibugger / rudimant connection */
  public static final int SERVER_PORT_RUDIBUGGER = 3665;
  public static final int SERVER_PORT_RUDIMANT = 3664;

  /* the standard values of some essential files and folders */
  public static String PATH_TO_RUDI_FILES = "src/main/rudi/";
  public static String COMPILE_FILE = "compile";
  public static String RUN_FILE = "run.sh";

  /* used to mark and signalize the user's request of a new project */
  public static final int OVERWRITE_CHECK_CANCEL = 0;
  public static final int OVERWRITE_CHECK_CURRENT_WINDOW = 1;
  public static final int OVERWRITE_CHECK_NEW_WINDOW = 2;

  /* used to mark and signalize the state of the rule model */
  public static final int RULE_MODEL_UNCHANGED = 0;
  public static final int RULE_MODEL_NEWLY_CREATED = 1;
  public static final int RULE_MODEL_CHANGED = 2;
  public static final int RULE_MODEL_REMOVED = 9;

  /* used to signalize the opening or closing of a project */
  public static final int PROJECT_OPEN = 1;
  public static final int PROJECT_CLOSED = 0;

  /* used to mark the usage state of a file in a project */
  public static final int FILE_USED = 1;
  public static final int FILE_NOT_USED = 0;
  public static final int FILE_IS_MAIN = 2;
  public static final int FILE_IS_WRAPPER = 3;
  public static final int IS_FOLDER = 9;

}

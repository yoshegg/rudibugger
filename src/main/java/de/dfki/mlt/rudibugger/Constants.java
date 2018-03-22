/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * For convenience this class contains constants that can be used anywhere in
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Constants {

  /* Default values of some essential files and folders. */
  public static String PATH_TO_RUDI_FILES = "src/main/rudi/";
  public static String COMPILE_FILE = "compile";
  public static String RUN_FILE = "run.sh";
  public static Path GLOBAL_CONFIG_FILE
    = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
      "rudibuggerConfiguration.yml");
  public static Path RECENT_PROJECTS_FILE
    = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
      "recentProjects.yml");
  public static Path GLOBAL_CONFIG_PATH
    = Paths.get(System.getProperty("user.home"), ".config", "rudibugger");

  /* Mark and signalize the user's request of a new project. */
  public static final int OVERWRITE_CHECK_CANCEL = 0;
  public static final int OVERWRITE_CHECK_CURRENT_WINDOW = 1;
  public static final int OVERWRITE_CHECK_NEW_WINDOW = 2;

  /* Mark and signalize the state of the RuleModel. */
  public static final int RULE_MODEL_UNCHANGED = 0;
  public static final int RULE_MODEL_NEWLY_CREATED = 1;
  public static final int RULE_MODEL_CHANGED = 2;
  public static final int RULE_MODEL_REMOVED = 9;

  /* Signalize the opening or closing of a project. */
  public static final int PROJECT_OPEN = 1;
  public static final int PROJECT_CLOSED = 0;

  /* Mark the usage state of a file in a project. */
  public static final int FILE_USED = 1;
  public static final int FILE_NOT_USED = 0;
  public static final int FILE_IS_MAIN = 2;
  public static final int FILE_IS_WRAPPER = 3;
  public static final int IS_FOLDER = 9;

  /* Compilation state of .rudi files. */
  public static final int COMPILATION_PERFECT = 1;
  public static final int COMPILATION_WITH_ERRORS = 2;
  public static final int COMPILATION_WITH_WARNINGS = 3;
  public static final int COMPILATION_FAILED = 4;
  public static final int COMPILATION_UNDEFINED = 5;

  /* Modification state of .rudi files. */
  public static final int FILES_SYNCED = 10;
  public static final int FILES_OUT_OF_SYNC = 20;
  public static final int FILES_SYNC_UNDEFINED = 30;

}

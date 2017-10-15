/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.rudibugger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Constants {

  public static String PATH_TO_RUDI_FILES = "src/main/rudi/";

  public static String COMPILE_FILE = "compile";
  public static String RUN_FILE = "run.sh";
  public static String RULE_LOCATION_SUFFIX = "RuleLocation.yml";

  public static final int OVERWRITE_CHECK_CANCEL = 0;
  public static final int OVERWRITE_CHECK_CURRENT_WINDOW = 1;
  public static final int OVERWRITE_CHECK_NEW_WINDOW = 2;

  public static final int STATE_NEVER = 0;
  public static final int STATE_IF_TRUE = 1;
  public static final int STATE_IF_FALSE = 2;
  public static final int STATE_ALWAYS = 3;
  public static final int STATE_PARTLY = 9;

  public static final int RULE_MODEL_UNCHANGED = 0;
  public static final int RULE_MODEL_NEWLY_CREATED = 1;
  public static final int RULE_MODEL_CHANGED = 2;
}

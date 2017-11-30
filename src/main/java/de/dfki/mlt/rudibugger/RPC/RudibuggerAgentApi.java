package de.dfki.mlt.rudibugger.RPC;

import de.dfki.mlt.rudimant.common.RuleInfo;

/**
 * the server API that is separated from the server;
 */
public class RudibuggerAgentApi {

  /**
   * Start logging a specific rule
   *
   * @param ruleId
   * @param result
   */
  public void printLog(RuleInfo ruleId, boolean[] result) {
    System.out.println("printing... " + ruleId);
  }
}
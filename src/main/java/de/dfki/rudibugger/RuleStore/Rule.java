/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RuleStore;

import java.nio.file.Path;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.log4j.Logger;
import static de.dfki.rudibugger.Constants.*;


/**
 * A Rule contains information concerning the rule's data.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Rule {

  /** the logger of Rules */
  static Logger log = Logger.getLogger("ruleLog");

  /**
   * Constructs a new Rule.
   *
   * @param name the rule's name
   */
  public Rule(String name) {
    _ruleName = new SimpleStringProperty(name);
    _line = new SimpleIntegerProperty();
    _ruleState = new SimpleIntegerProperty(STATE_NEVER);
    _ruleState.addListener(new ChangeListener() {

      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        log.debug("value changed:\n"
                + "  Rule " + _ruleName.getValue() + " from " + getSource().getFileName().toString() + ":\n"
                + "  From " + oldValue.toString() + " to " + newValue.toString());
      }
    });
    _subRules = FXCollections.observableArrayList();
  }

  /** The name of the rule */
  private final StringProperty _ruleName;
  public final StringProperty ruleNameProperty() { return _ruleName; }
  public final String getRuleName() { return ruleNameProperty().get(); }

  /** The line in which the rule appears */
  private final IntegerProperty _line;
  public final IntegerProperty lineProperty() { return _line; }
  public final Integer getLine() { return lineProperty().get(); }
  public final void setLine(Integer line) { _line.set(line); }

  /** The rule logging state */
  private final IntegerProperty _ruleState;
  public final IntegerProperty ruleStateProperty() { return _ruleState; }
  public final Integer getRuleState() {return ruleStateProperty().get(); }
  public final void setRuleState(final Integer ruleState) {
    this.ruleStateProperty().set(ruleState);
  }

  /** The (possible) subrules */
  private final ObservableList<Rule> _subRules;
  public final ObservableList<Rule> subRuleProperty() {return _subRules; }
  public void addSubRule(int position, Rule newRule) {
    _subRules.add(position, newRule);
  }

  /** The originating file */
  private final ObjectProperty<Path> source = new SimpleObjectProperty<>(null);
  public final ObjectProperty<Path> sourceProperty() { return source; }
  public final Path getSource() { return this.sourceProperty().get(); }
  public final void setSource(final Path source) {
    this.sourceProperty().set(source);
  }


  /** The errors that rudimant warned us about */
  private final ObservableMap<String, String> _errors =
          FXCollections.observableHashMap();
  public final ObservableMap errorProperty() { return _errors; }
  public void addError(Integer position, String message) {
    String lineNumber = Integer.toString(position);

    /* key should be unique */
    // TODO: rudimant does not save more than one error per line
    while (true) {
      if (_errors.keySet().contains(lineNumber)) {
        lineNumber += "%supp";
      } else {
        break;
      }

    }
    _errors.put(lineNumber, message);
  }

  /**
   * add Children of type Rule
   *
   * @param list
   * @param counter
   */
  public void addChildren(List<Object> list, int counter) {
    for (Object childRule : list) {
      if (childRule instanceof Rule) {
        addSubRule(counter, (Rule) childRule);
      } else {
        log.error("childRule is not of type Rule, but "
                + childRule.getClass());
      }
      counter += 1;
    }
  }

  @Override
  public String toString() {
    return getRuleName() + " (" + getLine() + ")";
  }

  public String toString(String prefix) {
    String newPrefix = prefix + "  ";
    String print = "";
    print += (getRuleName() + " (" + getLine() + ")");
    if (! _errors.isEmpty()) {
      for (String x : _errors.keySet()) {
        print += "[" + x + ", " + _errors.get(x) + "]";
      }
    }
    print += "\n";
    for (Rule child : _subRules) {
      print += newPrefix + child.toString(newPrefix);
    }
    return print;
  }



//  /** TODO: POSSIBLE ERRORS */
//
//
//  /* The path within the file */
//
//  private final ObjectProperty<ArrayList<String>> path = new SimpleObjectProperty<>(null);
//
//  public final ObjectProperty<ArrayList<String>> pathProperty() {
//    return this.path;
//  }
//
//  public final ArrayList<String> getPath() {
//    return this.pathProperty().get();
//  }
//
//  public void setPath(final ArrayList<String> path) {
//    this.path.set(path);
//  }
//


}

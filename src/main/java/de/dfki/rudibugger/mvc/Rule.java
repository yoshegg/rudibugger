/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.mvc;

import java.nio.file.Path;
import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Rule extends RudiComponent {

  /* The name of the rule */

  private final StringProperty ruleName = new SimpleStringProperty();

  public final StringProperty ruleNameProperty() {
    return this.ruleName;
  }

  public final String getRuleName() {
    return this.ruleNameProperty().get();
  }

  public final void setRuleName(String ruleName) {
    this.ruleName.set(ruleName);
  }


  /* The path within the file */

  private final ObjectProperty<ArrayList<String>> path = new SimpleObjectProperty<>(null);

  public final ObjectProperty<ArrayList<String>> pathProperty() {
    return this.path;
  }

  public final ArrayList<String> getPath() {
    return this.pathProperty().get();
  }

  public void setPath(final ArrayList<String> path) {
    this.path.set(path);
  }


  /* The rule logging state */

  private final IntegerProperty ruleState = new SimpleIntegerProperty();

  public final IntegerProperty ruleStateProperty() {
    return this.ruleState;
  }

  public final Integer getRuleState() {
    return this.ruleStateProperty().get();
  }

  public final void setRuleState(final Integer ruleState) {
    this.ruleStateProperty().set(ruleState);
  }

  /* The line */

  private final IntegerProperty line = new SimpleIntegerProperty();

  public final IntegerProperty lineProperty() {
    return this.line;
  }

  public final Integer getLine() {
    return this.lineProperty().get();
  }

  public final void setLine(final Integer line) {
    this.lineProperty().set(line);
  }


  /* The subrules */

  private final ObservableList<Rule> subrules
          = FXCollections.observableArrayList();

  public ObservableList<Rule> getSubrules() {
    return subrules;
  }



  /* The originating file */

  private final ObjectProperty<Path> source = new SimpleObjectProperty<>(null);

  public final ObjectProperty<Path> sourceProperty() {
    return this.source;
  }

  public final Path getSource() {
    return this.sourceProperty().get();
  }

  public final void setSource(final Path source) {
    this.sourceProperty().set(source);
  }

}

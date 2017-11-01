/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Import {


  /** the logger of Imports */
  static Logger log = Logger.getLogger("importLog");

  /**
   * Constructs a new Import.
   *
   * @param name the import's name
   */
  public Import(String name) {
    _importName = new SimpleStringProperty(name);
    _line = new SimpleIntegerProperty();
    _children = FXCollections.observableArrayList();
  }

  /** The name of the import */
  private final StringProperty _importName;
  public final StringProperty importNameProperty() { return _importName; }
  public final String getImportName() { return importNameProperty().get(); }

  /** The line in the parent file in which this file was imported */
  private final IntegerProperty _line;
  public final IntegerProperty lineProperty() { return _line; }
  public final Integer getLine() { return lineProperty().get(); }
  public final void setLine(Integer line) { _line.set(line); }

  /** The possible children (rules and other imports) */
  private final ObservableList _children;
  public final ObservableList childrenProperty() { return _children; }
  public void addImport(int position, Import newImport) {
    _children.add(position, newImport);
  }
  public void addRule(int position, Rule newRule) {
    _children.add(position, newRule);
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
   * add Children of type Rule or Import
   *
   * @param list
   * @param counter
   */
  public void addChildren(List<Object> list, int counter) {
    for (Object child : list) {
      if (child instanceof Import) {
        addImport(counter, (Import) child);
      }
      else if (child instanceof Rule) {
        addRule(counter, (Rule) child);
      } else {
        log.error("child is not of type Rule or Import, but "
                + child.getClass());
      }
      counter += 1;
    }
  }

  @Override
  public String toString() {
    return getImportName() + " (" + getLine() + ")";
  }

  public String toString(String prefix) {
    String newPrefix = prefix + "  ";
    String print = "";
    print += (getImportName() + " (" + getLine() + ")");
    if (! _errors.keySet().isEmpty()) {
      for (String x : _errors.keySet()) {
        print += "[" + x + ", " + _errors.get(x) + "]";
      }
    }
    print += "\n";
    for (Object child : _children) {
      if (child instanceof Rule) {
        print += newPrefix + ((Rule) child).toString(newPrefix);
      } else {
        print += newPrefix + ((Import) child).toString(newPrefix);
      }
    }
    return print;
  }
}

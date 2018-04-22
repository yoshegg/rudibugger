/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.RuleModel;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudimant.common.BasicInfo;
import de.dfki.mlt.rudimant.common.ErrorWarningInfo;
import de.dfki.mlt.rudimant.common.ImportInfo;
import de.dfki.mlt.rudimant.common.RuleInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * This class contains the data about the rule structure that is shown in
 * ruleTreeView.
 *
 * It contains the field <code>_rootImport</code> represents the root of the
 * tree-like rule structure.
 *
 * This class is also responsible for reading in the compiled rule structure
 * (<code>RuleLoc.yml</code>) and updating it. It also keeps track of
 *   - used Imports in the compiled project,
 *   - changes to the ruleLogging state of the different rules
 *   - warnings and errors that occured during compilation
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RuleModel {

  /** The Logger. */
  static Logger log = LoggerFactory.getLogger("RuleModel");

  /** The <code>DataModel</code> */
  private final DataModel _model;

  /** Contains all of the used Imports' paths. */
  private final HashSet<Path> _importSet = new HashSet();

  /** Maps rule ID's to their ruleLoggingState property. */
  private final ObservableMap<Integer, IntegerProperty> _idLoggingStateMap
          = FXCollections.observableMap(new LinkedHashMap<>());

  /** Maps ruleIds to paths and lines. */
  private final HashMap<Integer, RuleInfoExtended> _idRuleMap = new HashMap<>();

  /** Contains errors that occured during compilation. */
  private final LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> _errorInfos
    = new LinkedHashMap<>();

  /** Contains warnings that occurred during compilation. */
  private final LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> _warnInfos
    = new LinkedHashMap<>();

  /**
   * Contains parsing error that might have occurred during compilation
   * attempt. Should normally only contain one element.
   */
  private final LinkedHashMap<ErrorWarningInfo, ImportInfoExtended>
          _parsingFailure = new LinkedHashMap<>();

  /**
   * Represents the root of the tree-like structure used to represent
   * all the rules of the RuleModel.
   */
  private ImportInfoExtended _rootImport;

  /** Indicates that the ruleModel has been changed after various events. */
  private final IntegerProperty _changedState
          = new SimpleIntegerProperty(RULE_MODEL_UNCHANGED);


  /*****************************************************************************
   * INITIALIZERS AND RESETTER
   ****************************************************************************/

  /**
   * Initializes this project specific addition of <code>DataModel</code>.
   *
   * @param model The current <code>DataModel</code>
   */
  public RuleModel(DataModel model) {
    _model = model;
    log.debug("An empty RuleModel has been created");
  }

  /** Initializes the ruleModel. */
  public void init() {
    log.debug("Initializing the RuleModel...");
    _rootImport = defineRuleModel();
    if (_rootImport != null) _changedState.set(RULE_MODEL_NEWLY_CREATED);
  }

  /** Updates the ruleModel. */
  public void update() {
    log.debug("Updating the RuleModel...");

    /* Resetting compilation specific fields */
    _errorInfos.clear();
    _warnInfos.clear();
    _importSet.clear();
    _idRuleMap.clear();

    _rootImport = defineRuleModel();
    if (_rootImport != null) _changedState.set(RULE_MODEL_CHANGED);
  }

  /** Resets the ruleModel. */
  public void reset() {
    log.debug("Resetting the RuleModel...");

    _errorInfos.clear();
    _warnInfos.clear();
    _importSet.clear();
    _idRuleMap.clear();
    _rootImport = null;

    _changedState.set(RULE_MODEL_REMOVED);

  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /**
   * Reads in ruleModel and processes it to match rudibugger's requirements.
   *
   * @return <code>ImportInfoExtended</code> representing the rule structure
   */
  private ImportInfoExtended defineRuleModel() {

    BasicInfo basicRuleStructure = readInRuleLocationFile();
    if (basicRuleStructure == null) { return null; }

    BasicInfo processedRuleStructure
            = processInfos(basicRuleStructure, null, _model);

    if (! (processedRuleStructure instanceof ImportInfoExtended)) {
      log.error("Processing infos of RuleModel failed");
      return null;
    }

    /* Set compilation outcome state */
    if (! _parsingFailure.isEmpty())
      _model._compilationStateProperty().setValue(COMPILATION_FAILED);
    else if (! _errorInfos.isEmpty())
      _model._compilationStateProperty().setValue(COMPILATION_WITH_ERRORS);
    else if (! _warnInfos.isEmpty() && _errorInfos.isEmpty())
      _model._compilationStateProperty().setValue(COMPILATION_WITH_WARNINGS);
    else
      _model._compilationStateProperty().setValue(COMPILATION_PERFECT);

    return (ImportInfoExtended) processedRuleStructure;
  }

  /**
   * Needed to upgrade BasicInfos to include JavaFX properties. This step is
   * necessary because we did not want to include JavaFX dependencies in
   * VOnDA.
   *
   * At the same time
   *   - warnings and errors are being extracted,
   *   - listeners are being defined (to keep track of state changes of
   *     children),
   *   - used Imports of the last compilation step are stored
   *
   * @param current Checked BasicInfo
   * @param parent Parent BasicInfo
   * @param model DataModel
   * @return
   */
  private BasicInfo processInfos(BasicInfo current, BasicInfo parent,
                                 DataModel model) {

    if (current instanceof ImportInfo) {
      ImportInfoExtended ii
              = new ImportInfoExtended((ImportInfo) current, model, parent);
      extractWarnErrors(ii);
      _importSet.add(ii.getAbsolutePath());
      for (BasicInfo child : current.getChildren()) {
        ii.getChildren().add(processInfos(child, ii, model));
      }
      if (parent != null)
        ((ImportInfoExtended) ii.getParent()).addListener(ii);
      return ii;
    }

    else {
      RuleInfoExtended ri
              = new RuleInfoExtended((RuleInfo) current, model, parent);
      _idRuleMap.put(ri.getId(), ri);
      _idLoggingStateMap.put(ri.getId(), ri.stateProperty());
      for (BasicInfo child : current.getChildren()) {
        ri.getChildren().add(processInfos(child, ri, model));
      }
      ri.getParentImport().addListener(ri);
      return ri;
    }

  }

  /**
   * Reads in <code>RuleLoc.yml</code> and stores its structure.
   *
   * @param ruleLocFile the .yml file containing the map of all rules
   * @return <code>ImportInfo</code> containing rule structure
   */
  private ImportInfo readInRuleLocationFile() {
    File ruleLocFile = _model.project.getRuleLocationFile().toFile();
    ImportInfo ii;
    try {
      Yaml yaml = new Yaml();
      ii = (ImportInfo) yaml.load(new FileReader(ruleLocFile));
    } catch (FileNotFoundException e) {
      log.error("Could not read in RuleLoc.yml");
      _changedState.set(RULE_MODEL_REMOVED);
      return null;
    } catch (org.yaml.snakeyaml.error.YAMLException e) {
      log.error(e.getMessage());
      _changedState.set(RULE_MODEL_REMOVED);
      return null;
    }
    return ii;
  }

  /**
   * Extracts occurred warnings and errors and stores them explicitly.
   *
   * @param ii Import to extract infos from
   */
  private void extractWarnErrors(ImportInfoExtended ii) {
    ii.getWarnings().forEach((ewi) -> _warnInfos.put(ewi, ii) );
    ii.getErrors().forEach((ewi) -> _errorInfos.put(ewi, ii) );
    if (ii.getParsingFailure() != null)
      _parsingFailure.put(ii.getParsingFailure(), ii);
  }


  /*****************************************************************************
   * GETTERS AND SETTERS FOR PRIVATE FIELDS
   ****************************************************************************/

  /** @return Set of Paths of all used Imports */
  public HashSet<Path> getImportSet() { return _importSet; }

  /** @return Root Import of RuleModel */
  public ImportInfoExtended getRootImport() { return _rootImport; }

  /** @return Map containing errors that occured during compilation */
  public LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> getErrorInfos() {
    return _errorInfos;
  }

  /** @return Map containing warnings that occured during compilation */
  public LinkedHashMap<ErrorWarningInfo, ImportInfoExtended> getWarnInfos() {
    return _warnInfos;
  }

  /** @return Parsing failure that might have occurred */
  public LinkedHashMap<ErrorWarningInfo, ImportInfoExtended>
        getParsingFailure() { return _parsingFailure; }

  /** @Return ObservableMap containing the ruleLoggingStates of all rules */
  public ObservableMap<Integer, IntegerProperty> idLoggingStatesMap() {
    return _idLoggingStateMap;
  }

  /** @Return Property reflecting the ruleModel's state after compilation */
  public IntegerProperty changedStateProperty() { return _changedState; }

  /**
   * Sets the property reflecting the ruleModel's state after compilation.
   *
   * @param val New state
   */
  public void setChangedStateProperty(int val) {
    _changedState.set(val);
  }

  /**
   * Gets rule with given id.
   *
   * @param id  Id of rule
   * @return    Requested <code>RuleInfoExtended</code>
   */
  public RuleInfoExtended getRule(int id) { return _idRuleMap.get(id); }

}

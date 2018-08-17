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
package de.dfki.mlt.rudibugger.SearchAndFind;

import de.dfki.mlt.rudibugger.project.Project;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Controller manages the executeSearch dialog.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SearchController {

  /** The logger. */
  static Logger log = LoggerFactory.getLogger("SearchCont.");

  /** TODO */
  private Project _project;

  /** TODO */
  private Search _search;

//  /**
//   * Initializes the SearchController.
//   *
//   * @param model
//   */
//  public void initModel(DataModel model) {
//    if (_model != null) {
//      throw new IllegalStateException("Model can only be initialized once");
//    }
//    _model = model;
//
//    setUpTableView();
//    setUpSearchListeners();
//  }

  public void init(Project project) {
    _project = project;
    _search = new Search(_project);
    setUpTableView();
    setUpSearchListeners();
  }


  /*****************************************************************************
   * METHODS
   ****************************************************************************/

  /** Defines when to start a new executeSearch. */
  private void setUpSearchListeners() {
    searchTextField.textProperty().addListener(l -> executeSearch());
    ignoreCaseCheckBox.selectedProperty().addListener(l -> executeSearch());
    wholeWordCheckBox.selectedProperty().addListener(l -> executeSearch());
    regexCheckBox.selectedProperty().addListener(l -> executeSearch());
  }

  /**
   * Defines the TableView's content, columns, factories and when to open a
   * file.
   */
  private void setUpTableView() {
    /* Bind results from model to TableView */
    searchResultTable.setItems(_search.searchResults);

    /* Define columns. */
    TableColumn<String[], String> fileColumn
      = (TableColumn<String[], String>) searchResultTable.getColumns().get(0);
    TableColumn<String[], String> lineColumn
      = (TableColumn<String[], String>) searchResultTable.getColumns().get(1);
    TableColumn<String[], String> occurrenceColumn
      = (TableColumn<String[], String>) searchResultTable.getColumns().get(2);

    /* Set CellValueFactories */
    fileColumn.setCellValueFactory(value
      -> new SimpleStringProperty(value.getValue()[0]));
    lineColumn.setCellValueFactory(value
      -> new SimpleStringProperty(value.getValue()[1]));
    occurrenceColumn.setCellValueFactory(value
      -> new SimpleStringProperty(value.getValue()[2]));

    /* When to open a file. */
    searchResultTable.getSelectionModel().selectedItemProperty()
            .addListener(cl -> openLine());
  }

  /** Opens the file and line of the selected result. */
  private void openLine() {
    String[] selectedEntry = ((String[]) searchResultTable.getSelectionModel()
      .getSelectedItem());
    Path rudiFile = _project.getRudiFolder()
      .resolve(Paths.get(selectedEntry[0]));
    int lineNumber = Integer.decode(selectedEntry[1]);
    _project.openRule(rudiFile, lineNumber);
  }

  /** Runs a executeSearch with the current content of the searchTextField. */
  private void executeSearch() {
    _search.executeSearch(searchTextField.getText(),
                         ignoreCaseCheckBox.isSelected(),
                         wholeWordCheckBox.isSelected(),
                         regexCheckBox.isSelected()
    );
  }


  /*****************************************************************************
   * GUI ELEMENTS
   ****************************************************************************/

  /** Represents the TextField used to enter a executeSearch. */
  @FXML
  private TextField searchTextField;

  /** Contains the results of the executeSearch. */
  @FXML
  private TableView searchResultTable;

  /** Used to know if the case of the executeSearch is irrelevant. */
  @FXML
  private CheckBox ignoreCaseCheckBox;

  /** Used to know if whole words will be searched. */
  @FXML
  private CheckBox wholeWordCheckBox;

  /** Used to know if the entered executeSearch is a regular expression. */
  @FXML
  private CheckBox regexCheckBox;

}

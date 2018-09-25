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

package de.dfki.mlt.rudibugger.view.editor;

import static de.dfki.mlt.rudibugger.Constants.CONNECTED_TO_VONDA;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.editor.Editor;
import de.dfki.mlt.rudibugger.editor.RudibuggerEditor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudibuggerEditorController {

  static Logger log = LoggerFactory.getLogger("editorCon");

  /** The current <code>DataModel</code> */
  private DataModel _model;

  public void initModel(DataModel model) {
    if (_model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    _model = model;
    setEditor(_model.getEditor());
  }


  /* ***************************************************************************
   * METHODS
   * **************************************************************************/

  private void setEditor(Editor editor) {
    if (editor instanceof RudibuggerEditor) {
      TabPane tp = new TabPane();
      tp.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
      HBox.setHgrow(tp, Priority.ALWAYS);
      tabBox.getChildren().add(tp);
      ((RudibuggerEditor) editor).setTabPane(tp);
    }
  }


  /* ***************************************************************************
   * The different GUI elements
   * **************************************************************************/

  /** The HBox containing the tabPane(s) */
  @FXML
  private HBox tabBox;

}

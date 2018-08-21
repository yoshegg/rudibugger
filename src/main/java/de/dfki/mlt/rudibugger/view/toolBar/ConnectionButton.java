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
package de.dfki.mlt.rudibugger.view.toolBar;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.project.ruleModel.RuleModel;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class ConnectionButton extends Button {

  public ToolBar getParentToolBar() { return (ToolBar) super.getParent(); }

  public void reset() {
    setText("No project");
    setOnMouseEntered(e -> setText(null));
    setOnMouseExited(e -> setText(null));
    setDisable(true);
  }

  public void update(RuleModel rm, int connectionState) {
    if (rm == null) {
      setText("Not compiled");
      setOnMouseEntered(e -> setText(null));
      setOnMouseExited(e -> setText(null));
      setDisable(true);
      return;
    }
    switch(connectionState) {
      case CONNECTED_TO_VONDA:
        setText("Connected");
        setOnMouseEntered(e -> setText("Disconnect"));
        setOnMouseExited(e -> setText("Connected"));
        setDisable(false);
        break;
      case ESTABLISHING_CONNECTION:
        setText("Connecting");
        setOnMouseEntered(e -> setText("Disconnect"));
        setOnMouseExited(e -> setText("Connecting"));
        setDisable(false);
        break;
      case DISCONNECTED_FROM_VONDA:
        setText("Disconnected");
        setOnMouseEntered(e -> setText("Connect"));
        setOnMouseExited(e -> setText("Disconnected"));
        setDisable(false);
    }
  }



}

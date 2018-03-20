/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.StatusBar;

import static de.dfki.mlt.rudibugger.Constants.*;
import java.util.HashMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This class controls the behaviour of the sync indicator in the lower left
 * of rudibugger. It indicates if the current .java files are equivalent to the
 * .rudi files or not.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SyncIndicator {

  /** The Controller. */
  private StatusBarController _controller;

  /** An icon showing the current sync state. */
  private ImageView _indicator;

  /** Shows an explanation of the current sync state. */
  private Tooltip _tooltip;

  /** Path for icons describing sync status. */
  private static final String ICONS_PATH
          = "file:src/main/resources/icons/syncStatus/";

  /** Map of sync status icons. */
  private static final HashMap<Integer, Image> ICONS
    = new HashMap<Integer, Image>() {{
      put(FILES_SYNCED,         new Image(ICONS_PATH + "okay.png"));
      put(FILES_OUT_OF_SYNC,    new Image(ICONS_PATH + "out.png"));
      put(FILES_SYNC_UNDEFINED, new Image(ICONS_PATH + "undefined.png"));
   }};

  /** Map of sync status tooltip's texts. */
  private static final HashMap<Integer, String> MESSAGES
    = new HashMap<Integer, String>() {{
      put(FILES_SYNCED,         ".rudi files and compiled .java files "
                              + "up-to-date.");
      put(FILES_OUT_OF_SYNC,    ".rudi files out of sync.");
      put(FILES_SYNC_UNDEFINED, ".rudi and compiled .java compile status "
                              + "unknown. (Probably never compiled.)");
    }};

  /**
   * Creates a new instance of <code>SyncIndicator</code>, creates a tooltip
   * instance and links everything to the <code>StatusBarController</code>.
   *
   * @param indicator  Icon to be manipulated
   * @param controller Parent controller
   */
  public SyncIndicator(ImageView indicator, StatusBarController controller) {
    _indicator = indicator;
    _tooltip = new Tooltip();
    _controller = controller;

    /* Initializes the default look and behaviour if no project is loaded. */
    _indicator.setImage(ICONS.get(FILES_SYNC_UNDEFINED));
    _tooltip.setText(MESSAGES.get(FILES_SYNC_UNDEFINED));
    Tooltip.install(_indicator, _tooltip);
  }

  /** Responsible for updating tooltip and icon. */
  private final ChangeListener<Number> listener = ((cl, ov, nv) -> {
      int val = nv.intValue();
      String msg = MESSAGES.get(val);

      if (val != FILES_SYNCED)
        _controller.setStatusBar(msg);

      _tooltip.setText(msg);
      Tooltip.install(_indicator, _tooltip);

      _indicator.setImage(ICONS.get(val));
  });

  /**
   * Links a given property to the listener of <code>SyncIndicator</code>.
   *
   * @param property  Property to be linked to
   */
  public void linkListenerToProperty(IntegerProperty property) {
    property.addListener(listener);
  }

}
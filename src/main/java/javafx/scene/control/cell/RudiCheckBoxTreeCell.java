package javafx.scene.control.cell;

import de.dfki.rudibugger.project.RudiCheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import javafx.util.StringConverter;


public class RudiCheckBoxTreeCell<T> extends CheckBoxTreeCell<T> {

    private final CheckBox checkBox;

    private ObservableValue<Boolean> booleanProperty;

    private BooleanProperty indeterminateProperty;

    public RudiCheckBoxTreeCell() {
        // getSelectedProperty as anonymous inner class to deal with situation
        // where the user is using CheckBoxTreeItem instances in their tree
        this(item -> {
            if (item instanceof CheckBoxTreeItem<?>) {
                return ((CheckBoxTreeItem<?>)item).selectedProperty();
            }
            return null;
        });
    }

    public RudiCheckBoxTreeCell(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty) {
        this(getSelectedProperty, CellUtils.<T>defaultTreeItemStringConverter(), null);
    }

    public RudiCheckBoxTreeCell(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty,
            final StringConverter<TreeItem<T>> converter) {
        this(getSelectedProperty, converter, null);
    }

    private RudiCheckBoxTreeCell(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty,
            final StringConverter<TreeItem<T>> converter,
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getIndeterminateProperty) {
        this.getStyleClass().add("check-box-tree-cell");
        setSelectedStateCallback(getSelectedProperty);
        setConverter(converter);

        this.checkBox = new RudiCheckBox();
        this.checkBox.setAllowIndeterminate(true);

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);
    }


}

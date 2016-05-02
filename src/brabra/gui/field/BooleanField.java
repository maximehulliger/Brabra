package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import javafx.scene.control.CheckBox;

public class BooleanField extends ValueField.WithCustomModel<Boolean> {
	
	private final CheckBox cboxClosed = new CheckBox(), cboxOpen = new CheckBox();
	
	public BooleanField(Consumer<Boolean> setModelValue, Supplier<Boolean> getModelValue) {
		super(setModelValue, getModelValue, false);
		
		//--- Styles
		cboxClosed.getStyleClass().add("fields-cBoxClosed");
		cboxOpen.getStyleClass().add("fields-cBoxOpen");
		
		//--- View:
		contentOpen.getChildren().add(cboxOpen);
		contentClosed.getChildren().add(cboxClosed);
		cboxClosed.setDisable(true);
		setValue(getModelValue());

		//--- Control:
		cboxOpen.setOnAction(e -> onChange());
	}

	protected Boolean getNewValue() {
		return cboxOpen.isSelected();
	}

	protected void setDisplayValue(Boolean newVal) {
		cboxOpen.setSelected(newVal);
		cboxClosed.setSelected(newVal);
	}

	public static class Pro extends BooleanField {

		public Pro(Consumer<Boolean> setModelValue, Supplier<Boolean> getModelValue) {
			super(setModelValue, getModelValue);
		}

		protected void setModelValue(final Boolean val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}

		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}
}

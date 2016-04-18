package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import javafx.scene.control.CheckBox;

public class BooleanField extends ValueField.WithCustomValue<Boolean> {
	
	private CheckBox cboxClosed, cboxOpen;
	
	public BooleanField(String name, Consumer<Boolean> setModelValue, Supplier<Boolean> getModelValue) {
		super(name, setModelValue, getModelValue, true);
	}

	protected Boolean getNewValue() {
		return cboxOpen.isSelected();
	}

	protected void setDisplayValue(Boolean newVal) {
		if (notInitialized()) {
			//--- View:
			cboxClosed = new CheckBox();
			cboxOpen = new CheckBox();
			contentOpen.getChildren().add(cboxOpen);
			contentClosed.getChildren().add(cboxClosed);
			cboxClosed.setDisable(true);
			setDefaultValue(false);
			
			//--- Control:
			cboxOpen.setOnAction(e -> onChange());
		}
		cboxOpen.setSelected(newVal);
		cboxClosed.setSelected(newVal);
	}

	public static class Pro extends BooleanField implements Field.Pro {

		public Pro(String name, Consumer<Boolean> setModelValue, Supplier<Boolean> getModelValue) {
			super(name, setModelValue, getModelValue);
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

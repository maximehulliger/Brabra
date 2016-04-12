package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.control.CheckBox;

public class BooleanField extends ValueField<Boolean> {
	
	private final CheckBox cboxClosed = new CheckBox(), cboxOpen = new CheckBox();
	private final Consumer<Boolean> setModelValue;
	private final Supplier<Boolean> getModelValue;
	
	public BooleanField(String name, Consumer<Boolean> setModelValue, Supplier<Boolean> getModelValue) {
		super(name);
		this.setModelValue = setModelValue;
		this.getModelValue = getModelValue;
		
		//--- View:
		contentOpen.getChildren().add(cboxOpen);
		contentClosed.getChildren().add(cboxClosed);
		cboxClosed.setDisable(true);
		
		//--- Control:
		cboxOpen.setOnAction(e -> this.onChange());
		cboxClosed.setOnAction(e -> this.onChange());
	}

	protected void setModelValue(Boolean val) {
		setModelValue.accept(val);
	}

	protected Boolean getModelValue() {
		return getModelValue.get();
	}

	protected Boolean getNewValue() {
		return cboxOpen.isSelected();
	}

	protected void updateValue(Boolean newVal) {
		cboxOpen.setSelected(newVal);
		cboxClosed.setSelected(newVal);
	}
}

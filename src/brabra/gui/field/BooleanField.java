package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.control.CheckBox;

public class BooleanField extends ValueField<Boolean> {
	
	private final CheckBox cboxClosed = new CheckBox(), cboxOpen = new CheckBox();
	private final Consumer<Boolean> setModelValue;
	private final Supplier<Boolean> getModelValue;
	private boolean boolValue = false;
	private boolean clicked = false;
	
	public BooleanField(String name, Consumer<Boolean> setModelValue, Supplier<Boolean> getModelValue) {
		super(name);
		this.setModelValue = setModelValue;
		this.getModelValue = getModelValue;
		this.cboxClosed.setSelected(getModelValue.get());
		this.cboxOpen.setSelected(getModelValue.get());
		
		//--- View:
		contentOpen.getChildren().add(cboxOpen);
		contentClosed.getChildren().add(cboxClosed);
		cboxClosed.setDisable(true);
		
		//--- Control:
		cboxOpen.setOnAction(e -> this.clicked = true);
		this.setOnMouseExited(e-> {
			if (this.clicked) {
				this.onChange();
				this.clicked = false;
			}
		});
	}
	
	public void setOpen(boolean open) {
		super.setOpen(open);
	}

	protected void setModelValue(Boolean val) {
		boolValue = val;
		setModelValue.accept(val);
	}

	protected Boolean getModelValue() {
		return getModelValue.get();
	}

	protected Boolean getNewValue() {
		return cboxOpen.isSelected();
	}

	protected void updateValue(Boolean newVal) {
		cboxOpen.setSelected(boolValue);
		cboxClosed.setSelected(boolValue);	
	}
}

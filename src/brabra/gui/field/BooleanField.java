package brabra.gui.field;

import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.gui.field.Field;
import javafx.scene.control.CheckBox;

public class BooleanField extends Field {
	
	private final CheckBox cbox = new CheckBox();
	private final Consumer<Boolean> onChange;
	private final Supplier<Boolean> modelValue;
	
	public BooleanField(String name, Consumer<Boolean> onChange, Supplier<Boolean> onUpdate) {
		super.setName(name);
		this.onChange = onChange;
		this.modelValue = onUpdate;
		
		//--- View:
		contentOpen.getChildren().addAll(basicText, cbox);
		contentClose.getChildren().addAll(basicText, cbox);
		
		//--- Control:
		cbox.setOnAction(
			e -> { this.onChange();e.consume(); }
		);
	}

	public void onChange() {
		onChange.accept(cbox.isSelected());
	}

	public void update(Observable o, java.lang.Object arg) {
		cbox.setSelected(modelValue.get());
	}
	
	public void setOpen(boolean open) {
		super.setOpen(open);
		if (cbox != null)
			cbox.setDisable(!open);
	}
}

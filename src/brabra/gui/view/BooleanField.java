package brabra.gui.view;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.gui.view.Field;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

public class BooleanField extends Field {
	public CheckBox cbox = new CheckBox();
	private final Consumer<Boolean> onChange;
	private final Supplier<Boolean> modelValue;
	
	public BooleanField(GridPane root,Consumer<Boolean> onChange, Supplier<Boolean> onUpdate ,String name) {
		super(root);
		this.onChange = onChange;
		this.modelValue = onUpdate;
		
		super.setName(name);
		
		getChildren().add(cbox);
		
		// 2. Controller
		cbox.setOnMouseClicked(
			e ->{if(super.open) {this.onChange();e.consume();}}
		);
	}

	public void onChange() {
		onChange.accept(cbox.isSelected());
	}

	public void update() {
		cbox.setSelected(modelValue.get());
	}
	
	public void setOpen(boolean open) {
		super.setOpen(open);
		if (cbox != null)
			cbox.setDisable(false);
	}
}

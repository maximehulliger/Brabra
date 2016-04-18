package brabra.gui.field;

import java.util.Observable;

/** An abstract field that define how to deal with one value. */
public abstract class ValueField<T> extends Field {
	
	final String name; //TODO 4 debug only: to remove (when working)
	private T value = null;
	private Object triggerArg = null;
	
	public ValueField(String name, boolean withTriangle) {
		super(name, withTriangle);
		this.name = name; //TODO 4 debug only: to remove
	}
	
	public ValueField<T> respondingTo(Object triggerArg) {
		this.triggerArg = triggerArg;
		return this;
	}

	/** Set the model value. */
	protected abstract void setModelValue(T val);
	
	/** Return the value from the model. */
	protected abstract T getModelValue();
	
	/** Return the value from the gui. */
	protected abstract T getNewValue();
	
	/** Update the text and field value in the gui. Should be called. */
	protected void updateGUI(T newVal){
		this.value = newVal;
	}
	
	protected final T value() {
		return value;
	}
	
	protected final void onChange() {
		final T newValue = getNewValue();
		if ((this.value == null && newValue != this.value) || !this.value.equals(newValue)){
			this.value = newValue;
			setModelValue(value);
		}
		System.out.println(name + " changed"); //TODO 4 debug only: to remove
	}

	public final void update(Observable o, java.lang.Object arg) {
		if (isVisible()) {
			if (triggerArg == null || arg == triggerArg) {
				final T newVal = getModelValue();
				//if (newVal != value || !getNewValue().equals(newVal)){
					updateGUI(newVal);
					System.out.println(arg.toString() + " updated"); //TODO 4 debug
				//}
			}
		}
	}
}

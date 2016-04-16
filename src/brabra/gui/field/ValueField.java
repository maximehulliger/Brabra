package brabra.gui.field;

import java.util.Observable;

/** An abstract field that define how to deal with one value. */
public abstract class ValueField<T> extends Field {
	
	final String name; //TODO 4 debug only: to remove (when working)
	private Object triggerArg = null;
	
	public ValueField(String name) {
		super(name);
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
	
	/** Update the text and field value in the gui. */
	protected abstract void updateValue(T newVal);
	
	protected final void onChange() {
		setModelValue(getNewValue());
		System.out.println(name + " changed"); //TODO 4 debug only: to remove
	}

	public final void update(Observable o, java.lang.Object arg) {
		System.out.println(arg.toString() + "ready to update"); //TODO 4 debug
		if (triggerArg == null || arg == triggerArg) {
			final T newVal = getModelValue();
//			if (!getNewValue().equals(newVal))
				updateValue(newVal);
		}
	}
}

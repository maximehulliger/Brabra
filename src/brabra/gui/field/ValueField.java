package brabra.gui.field;

import java.util.ArrayList;
import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/** An abstract field that define how to deal with one value. */
public abstract class ValueField<T> extends Field {
	
	private T value = null, defaultValue = null;
	private Object triggerArg = null;
	private ArrayList<Runnable> onChange = new ArrayList<>(2);
	
	// --- let to children ---
	
	/** Set the model value. */
	protected abstract void setModelValue(T val);
	
	/** Return the value from the model. */
	protected abstract T getModelValue();
	
	/** Return the value from the gui. */
	protected abstract T getNewValue();
	
	/** Called to update the text and field values in the gui. */
	protected abstract void setDisplayValue(T newVal);

	/** Return the current last valid value of this field. */
	protected final T value() {
		return value;
	}
	
	public ValueField(T defaultValue) {
		this.value = this.defaultValue = defaultValue;
	}

	// --- Value field services ---

	/** To set the value of the field (what is null ?). */
	protected void setDefaultValue(T defaultValue) {
		// old was default
		if (value==null || value.equals(this.defaultValue))
			value = defaultValue;
		this.defaultValue = defaultValue;
			
	}
	
	/** Set the value of the field and update the gui (if changed). Return true if the value changed. */
	protected boolean setValue(T value) {
		value = value == null ? defaultValue : value;
		
		if (this.value == null ? this.value != value : !this.value.equals(value)){
			this.value = value;
			//System.out.println(name() + " updated to "+value);
			setDisplayValue(value);
			return true;
		} else
			return false;
	}
	
	public ValueField<T> respondingTo(Object triggerArg) {
		this.triggerArg = triggerArg;
		return this;
	}

	public Runnable addOnChange(Runnable onChange) {
		if (onChange != null)
			this.onChange.add(onChange);
		return onChange;
	}
	
	public void removeOnChange(Runnable onChange) {
		if (!this.onChange.remove(onChange))
			throw new IllegalArgumentException("onChange runnable wasn't to run !");
	}

	public void update(Observable o, java.lang.Object arg) {
		if (isVisible() && (triggerArg == null || arg == triggerArg)) {
			setValue(getModelValue());
		}
	}
	
	public void setOpen(boolean open) {
		super.setOpen(open);
		setDisplayValue(value());
	}

	protected final void onChange() {
		final T newValueRaw = getNewValue();
		final T newValue = newValueRaw == null ? defaultValue : newValueRaw;
		
		if (this.value == null ? this.value != newValue : !this.value.equals(newValue)){
			this.value = newValue;
			setModelValue(value);
			if (onChange != null)
				onChange.forEach(r -> r.run());
			//System.out.println(name() + " changed");
		}
	}

	/** Change listener that calls on change when se proprety is changed to false (for loose focus). */
	protected class FieldChangeListener implements ChangeListener<Boolean> {
	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
	    	if (oldPropertyValue!=newPropertyValue &&  !newPropertyValue)
	    		onChange();
	    }
	}
	
	// --- for the child fields

	public static abstract class WithCustomModel<T> extends ValueField<T> {
		
		private final Consumer<T> setModelValue;
		private final Supplier<T> getModelValue;

		protected WithCustomModel(Consumer<T> setModelValue, Supplier<T> getModelValue, T defaultValue) {
			super(defaultValue);
			this.setModelValue = setModelValue;
			this.getModelValue = getModelValue;
		}

		protected void setModelValue(T val) {
			setModelValue.accept(val);
		}

		protected final T getModelValue() {
			return getModelValue.get();
		}
	}
}

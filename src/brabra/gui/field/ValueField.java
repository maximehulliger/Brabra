package brabra.gui.field;

import java.util.ArrayList;
import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** An abstract field that define how to deal with one value. */
public abstract class ValueField<T> extends Field {
	
	private final String name; //TODO 4 debug only: to remove (when working)
	private T value = null, defaultValue = null;
	private Object triggerArg = null;
	private ArrayList<Runnable> onChange = new ArrayList<>(2);
	
	
	public ValueField(String name, T value, boolean withTriangle) {
		super(name, withTriangle);
		this.name = name; //TODO 4 debug only: to remove
		this.value = value;
		setDisplayValue(value);
	}
	
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

	// --- Value field services ---

	/** flag used to create the field on first setDisplayValue(). */
	private boolean initialized = false;
	
	/** flag used to create the field on first setDisplayValue(). */
	protected boolean notInitialized() {
		if (!initialized) {
			initialized = true;
			return true;
		} else
			return false;
	}
	
	/** To set the value of the field when the */
	protected void setDefaultValue(T defaultValue) {
		if (this.defaultValue != defaultValue) {
			final boolean oldValueWasDefault = value == this.defaultValue;
			this.defaultValue = defaultValue;
			if (oldValueWasDefault) {
				value = defaultValue;
				setDisplayValue(value);
			}
		}
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

	public ValueField<T> respondingTo(Object triggerArg) {
		this.triggerArg = triggerArg;
		return this;
	}

	public void update(Observable o, java.lang.Object arg) {
		if (isVisible()) {
			if (triggerArg == null || arg == triggerArg) {
				final T newValRaw = getModelValue();
				final T newVal = newValRaw == null ? defaultValue : newValRaw;
				
				if (newVal != value && (newVal == null || !newVal.equals(value))){
					value = newVal;
					setDisplayValue(newVal);
					System.out.println(arg.toString() + " updated"); //TODO 4 debug
				}
			}
		}
	}

	protected final void onChange() {
		final T newValueRaw = getNewValue();
		final T newValue = newValueRaw == null ? defaultValue : newValueRaw;
		
		if (this.value == null ? this.value != newValue : !this.value.equals(newValue)){
			this.value = newValue;
			setModelValue(value);
			if (onChange != null)
				onChange.forEach(r -> r.run());
			System.out.println(name + " changed"); //TODO 4 debug only: to remove
		}
	}

	public static abstract class WithCustomValue<T> extends ValueField<T> {
		
		private final Consumer<T> setModelValue;
		private final Supplier<T> getModelValue;
		
		public WithCustomValue(String name, Consumer<T> setModelValue, Supplier<T> getModelValue, boolean withTriangle) {
			super(name, getModelValue.get(), withTriangle);
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

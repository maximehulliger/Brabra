package brabra.gui.field;

import brabra.Brabra;
import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class VectorField extends ValueField<Vector> {

	private final Vector vector;
	private TextField[] valueFields;
	
	public VectorField(String name, Vector vector, boolean withTriangle) {
		super(name, vector.copy(), withTriangle);
		super.setDefaultValue(Vector.zero);
		this.vector = vector;
	}

	protected void setModelValue(Vector val) {
		vector.set(val);
	}

	protected Vector getModelValue() {
		return vector.copy();
	}

	protected Vector getNewValue() {
		final Float x = Master.getFloat(valueFields[0].getText(), true);
		final Float y = Master.getFloat(valueFields[1].getText(), true);
		final Float z = Master.getFloat(valueFields[2].getText(), true);
		return new Vector(x,y,z);
	}

	protected void setDisplayValue(Vector newVal) {
		if (notInitialized()) {
			valueFields = new TextField[] {new TextField(), new TextField(), new TextField()};
			
			for (TextField field : valueFields) {
				//--- View:
				field.setPrefWidth(55);
				//--- Control:
				field.setOnAction(e->this.onChange());
				field.focusedProperty().addListener(new ChangeListener<Boolean>()
				{
				    @Override
				    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
				    {
				        if (!newPropertyValue){
				            onChange();
				        }
				    }
				});
			}
			
			contentOpen.getChildren().addAll(valueFields);
			
		}
		if (newVal == null) {
			setValue("null");
			for (TextField field : valueFields)
				field.setText("0");
		} else {
			setValue(newVal.formated(Physic.epsilon));
			valueFields[0].setText(Master.formatFloat(newVal.x, Physic.epsilon));
			valueFields[1].setText(Master.formatFloat(newVal.y, Physic.epsilon));
			valueFields[2].setText(Master.formatFloat(newVal.z, Physic.epsilon));
		}
	}
	
	public static class Pro extends VectorField implements Field.Pro {

		public Pro(String name, Vector vector, boolean withTriangle) {
			super(name, vector, withTriangle);
		}

		protected void setModelValue(final Vector val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}

		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}
}

package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.control.*;
import brabra.game.Observable.NVector;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Vector;
import brabra.gui.field.BooleanField;
import brabra.gui.field.FloatField;
import brabra.gui.field.VectorField;
import brabra.gui.model.AppModel;


/** View for the parameters. Listen to the app model. */
public class ParametersView extends View implements Observer {
	
	public VectorField sampleField;			//this will be list of objectFields
	public VectorField sampleField2;			//this will be list of objectFields
	public FloatField sampleField3;
	public BooleanField sampleField4;
	
	public float sampleFloat = 3;
	public boolean sampleBoolean = false;
	
	
	public ParametersView(AppModel appModel) {
		appModel.addObserver(this);
    	
		int currentRow = 0;
		
		// title
		super.add(new Label("parameters:"), 0, currentRow++);
    	
		// running
		super.add(new BooleanField("running",
					run -> appModel.app.game.setRunning(run), 
					() -> appModel.app.game.running()), 
				0, currentRow++);
		
		// display all collider
		super.add(new BooleanField("display all collider", 
				dc -> {Collider.displayAllColliders=dc;}, 
				() -> Collider.displayAllColliders),
				0, currentRow++);
		
		
		//test data
    	NVector sample = new NVector(new Vector(1,2,3));
		sample.setOnChange(()->{System.out.println("new value 4 sample: "+sample);});
		super.add(new VectorField("Sample", sample), 0, currentRow++);
	}

	public void update(Observable o, java.lang.Object arg) {
		sampleField.onChange();
	}
}


package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import brabra.game.Observable.NVector;
import brabra.game.physic.geo.Vector;
import brabra.gui.model.AppModel;
import brabra.gui.view.VectorField;
import brabra.gui.view.FloatField;
import brabra.gui.view.BooleanField;


/** View for the parameters. Listen to the app model. */
public class ParametersView implements Observer {
	
	public VectorField sampleField;			//this will be list of objectFields
	public VectorField sampleField2;			//this will be list of objectFields
	public FloatField sampleField3;
	public BooleanField sampleField4;
	public List<Field> list;
	
	public float sampleFloat = 3;
	public boolean sampleBoolean = false;
	
	
	public ParametersView(Pane root, AppModel appModel) {
		appModel.addObserver(this);
    	root.getChildren().add(new Label("parameters tab"));
    	
    	GridPane grid = new GridPane();
		root.getChildren().add(grid);
    	
		list = new ArrayList<>();
		
		//test data
    	NVector sample = new NVector(new Vector(1,2,3));
		sample.setOnChange(()->{System.out.println("new value 4 sample: "+sample);});
		String sampleName = "Sample";
		
		NVector sample2 = new NVector(new Vector(3,2,1));
		sample2.setOnChange(()->{System.out.println("new value 4 sample: "+sample2);});
		String sampleName2 = "Sample";
		
		//set test data in VectorField
		sampleField = new VectorField(grid,sample,sampleName);
		list.add(sampleField);
		
		sampleField2 = new VectorField(grid,sample2,sampleName2);
		list.add(sampleField2);
		
		sampleField3 = new FloatField(grid, f -> {sampleFloat = f;}, () -> {return sampleFloat;}, "mass");
		list.add(sampleField3);
		
		sampleField4 = new BooleanField(grid, b -> {sampleBoolean = b;System.out.println(sampleBoolean);}, () -> {return sampleBoolean;}, "Visible");
		list.add(sampleField4);
		int i = 0;
		for (Field f:list){
			grid.add(f, 0, i);
			i++;
		}
	}

	public void update(Observable o, java.lang.Object arg) {
		sampleField.onChange();
		/*btn.setText("Say '"+((AppModel)o).textToPrint()+"'");*/
	}
}


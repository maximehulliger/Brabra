package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;
import java.util.*;

import brabra.gui.model.AppModel;
import brabra.gui.model.SceneModel;
import brabra.gui.TriangleButton;
import brabra.game.scene.Object;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

/** View for the parameters. Listen to the app model. */
public class ParametersView implements Observer {
	
	private List<Object> lists;	
	private int i;
	
	public TriangleButton[] btns;
	public Label[] labels;
	
	public ParametersView(GridPane root, AppModel appModel, SceneModel sceneModel) {
		appModel.addObserver(this);
		this.i = 0;
		this.lists = sceneModel.objects();
		this.labels = new Label[sceneModel.objectCount()];
		this.btns = new TriangleButton[sceneModel.objectCount()];
		root.setHgap(8);
		root.setVgap(8);
		
		for(Object obj : lists){
			btns[i] = new TriangleButton().createStartingTriangle(8d);
			root.setPadding(new Insets(2,0,2,4));
			root.add(btns[i],0,i);
			
			labels[i] = new Label();
			labels[i].setId("objectName");
			labels[i].setText(obj.toString());
			root.add(labels[i],1,i);
			
			i++;
		}
	}

	public void update(Observable o, java.lang.Object arg) {
		System.out.println("updated");

		/*btn.setText("Say '"+((AppModel)o).textToPrint()+"'");*/
	}
}


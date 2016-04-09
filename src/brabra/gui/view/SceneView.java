package brabra.gui.view;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import brabra.game.scene.Object;
import brabra.gui.TriangleButton;
import brabra.gui.model.SceneModel;

public class SceneView implements Observer {

	private List<Object> lists;
	
	public TriangleButton[] btns;
	public Label[] labels;
	
	public SceneView(Pane root, SceneModel sceneModel) {
		
		sceneModel.addObserver(this);
		GridPane grid = new GridPane();
		root.getChildren().add(grid);
		
		this.lists = sceneModel.objects();
		this.labels = new Label[sceneModel.objectCount()];
		this.btns = new TriangleButton[sceneModel.objectCount()];
		grid.setHgap(8);
		grid.setVgap(8);
		
		for (int i=0; i<lists.size(); i++) {
			Object obj = lists.get(i);
			btns[i] = new TriangleButton();
			grid.setPadding(new Insets(2,0,2,4));
			grid.add(btns[i],0,i);
			
			labels[i] = new Label();
			labels[i].setId("objectName");
			labels[i].setText(obj.toString());
			grid.add(labels[i],1,i);
		}
	}

	public void update(Observable o, java.lang.Object updated) {
		
		// TODO update object field with object == updated
		
	}
}

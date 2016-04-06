package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import brabra.gui.model.AppModel;

/** View for the parameters. Listen to the app model. */
public class ParametersView implements Observer {
	
	public ParametersView(Pane root, AppModel appModel) {
		appModel.addObserver(this);
    	root.getChildren().add(new Label("parameters tab"));
	}

	public void update(Observable o, java.lang.Object arg) {
		/*btn.setText("Say '"+((AppModel)o).textToPrint()+"'");*/
	}
}


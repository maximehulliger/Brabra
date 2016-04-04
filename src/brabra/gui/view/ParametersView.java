package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import brabra.gui.model.AppModel;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/** View for the parameters. Listen to the app model. */
public class ParametersView implements Observer {
	
	public final Button btn;
	
	public ParametersView(Pane root, AppModel appModel) {
		appModel.addObserver(this);
		this.btn = new Button();
		// init the button
    	btn.setText("Say '"+appModel.textToPrint()+"'");
        root.getChildren().add(btn);
	}

	public void update(Observable o, Object arg) {
		System.out.println("updated");
		btn.setText("Say '"+((AppModel)o).textToPrint()+"'");
	}
	
}

package brabra.gui.view;


import brabra.game.Observable.NVector;
import brabra.gui.view.Field;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class VectorField extends Field {

	private NVector vector;
	private TextField xValue;
	private TextField yValue;
	private TextField zValue;
	private Label value;
	
	//TODO: array of 3 num fields (editable)
	
	public VectorField(GridPane root, NVector vector,String name) {
		super(root);
		// 1. View
		this.vector = vector;
		this.xValue =new TextField(Float.toString(vector.x));
		this.yValue =new TextField(Float.toString(vector.y));
		this.zValue =new TextField(Float.toString(vector.z));
		xValue.setPrefWidth(55);
		yValue.setPrefWidth(55);
		zValue.setPrefWidth(55);
		
		super.setName(name);
		
		content.getChildren().add(this.value = new Label(this.vector.toString()));
		hidedcontent.getChildren().addAll(xValue, yValue, zValue);
		
		// 2. Controller
		xValue.setOnAction(
			e ->{
				this.onChange();
			});
		yValue.setOnAction(
			e ->{
				this.onChange();
			});
		zValue.setOnAction(
			e ->{
				this.onChange();
			});
	}
	
	private float checkFloat(String input){
		try {
	        Float.parseFloat(input);
	        	return Float.parseFloat(input);
	        } catch (NumberFormatException e) {
	        	return 0;
	    }
	}

	public void onChange() {
		final Float x = this.checkFloat(xValue.getText());
		final Float y = this.checkFloat(yValue.getText());
		final Float z = this.checkFloat(zValue.getText());
		
		vector.set(x,y,z);
	}

	public void update() {
		value.setText(vector.toString());
	}
	
//	public void setOpen() {
//		super.setOpen();
//
//		
//		//TODO: show/hide vector fields
//		
//	}
}

package com.jonahe.app;



import javafx.application.Application;

import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

/** 
 * @author Erik
 *
 * An application to extract and save .mp3 files from all .AVI files in a folder. Using VLC command line commands
 *
 */

public class App extends Application{
	


	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setTitle("Extract .mp3 from .avi");

		
		Parent root = FXMLLoader.load(getClass().getResource("ui.fxml"));
		
		
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		

	}




	public static void main(String[] args) {
		launch(args);
	}
}

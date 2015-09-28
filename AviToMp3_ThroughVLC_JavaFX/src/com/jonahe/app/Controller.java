package com.jonahe.app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;


public class Controller {

	@FXML
	private BorderPane rootPane;
	@FXML
	private FlowPane centerPane;
	@FXML
	private Button btnSelectFiles;
	@FXML
	private Button btnSelectDir;
	@FXML
	private TextField txtFldSourceFolder;
	@FXML
	private ListView<File> listviewFilesInSelectedFolder;
	@FXML
	private TextField txtFldTargetFolder;
	@FXML
	private Button btnExtractMP3;
	
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label progressInfo;
	
	private Model model = new Model();
	
	
	
	@FXML
	public void onClickSelectFiles(){
		System.out.println("Test!");
		FileChooser selectFiles = createFileChooser();
		Window currentWindow = btnExtractMP3.getScene().getWindow();
		List<File> sourceFiles = selectFiles.showOpenMultipleDialog(currentWindow);
		if(sourceFiles != null){
			model.trySetupUsingFileList(sourceFiles); // just copies the selected files to an array in the model
			populateListView(model.getSourceFiles());
			// set textfield to the path then update target path
			txtFldSourceFolder.setText(sourceFiles.get(0).getParent());
			updateTargetFolderText();
		}
		
	}
	
	private FileChooser createFileChooser(){
		FileChooser selectFiles = new FileChooser();
		selectFiles.getExtensionFilters().add(new FileChooser.ExtensionFilter("AVI-files", "*.avi", "*.AVI"));
		
		return selectFiles;
	}
	
	@FXML
	public void onClickSelectDirectory(){
		System.out.println("Dir selector!");
		Window currentWindow = btnExtractMP3.getScene().getWindow();
		DirectoryChooser chooseDir = new DirectoryChooser();
		File sourceFolder = chooseDir.showDialog(currentWindow);
		if(sourceFolder != null){
			// set the path in the text field and then run the same as if user typed it in there
			txtFldSourceFolder.setText(sourceFolder.getPath());
			onTxtFldSourceFolderChange();
			
		}
	}
	
	
	@FXML
	public void onTxtFldSourceFolderChange(){
		System.out.println("TextField change detected.");
		/*
		 * Check if path exists, if so, send it to model, to check if it has the right type of files in it
		 */
		boolean successfulSetup = model.trySetupUsingSourceDir(txtFldSourceFolder.getText());
		if(successfulSetup){
			populateListView(model.getSourceFiles());
			updateTargetFolderText();
		} else {
			System.out.println("Not a folder, or not containing AVI.");
		}
	}
	
	@FXML
	public void onClickExtract(){
		System.out.println("Extraction click!");
		File saveDir = new File(txtFldTargetFolder.getText().trim());
		
		if(model.createOutPutFolder(saveDir)){

			
			Task<Void> convertFilesTask = createConversionTask(model.getSourceFiles());
			// bind progress bar value to task progress value
			progressBar.progressProperty().unbind(); // if this is not first time, old binding should be ignored
			progressBar.progressProperty().bind(convertFilesTask.progressProperty());
			
			// first now we can execute the task..
			Thread convert = new Thread(convertFilesTask);
			convert.start();

					
		}		
		
		
		
		
	}
	
	private void populateListView(File[] filesArray){
		System.out.println("Trying to populate listview.");
		ObservableList<File> files = FXCollections.observableArrayList();
		files.addAll(Arrays.asList(filesArray));
		listviewFilesInSelectedFolder.setItems(files);
	}
	
	private void updateTargetFolderText(){
		System.out.println("Updating target folder.");
		txtFldTargetFolder.setText(txtFldSourceFolder.getText() + File.separator + "Extracted_MP3s" );
	}
	
	private Task<Void> createConversionTask(File[] sourceFiles){
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				for(int i = 0; i < sourceFiles.length; i++){
					try {
						model.convertFile(sourceFiles[i]);
						updateProgress(i+1, sourceFiles.length);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}	
				}
				return null;
			};
		};
	}
}

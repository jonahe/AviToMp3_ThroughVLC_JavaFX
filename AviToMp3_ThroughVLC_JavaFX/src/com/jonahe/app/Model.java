package com.jonahe.app;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

public class Model {
	private FilenameFilter filenameFilter;
	private File sourceFolder;
	private File[] sourceFiles;
	private File outputFolder;
	private double timeTaken;
	

	public Model(){
		filenameFilter = createFilter(".avi");
	}
	
	public File[] getSourceFiles(){
		return sourceFiles;
	}
	
	public boolean trySetupUsingSourceDir(String sourceDirPath){
		boolean success = false;
		File sourceDir = new File(sourceDirPath);
		
		if(sourceDir.isDirectory()){
			if(containsAVIFiles(sourceDir)){
				success = true;
			}
		}
		return success;
	}
	
	public boolean trySetupUsingFileList(List<File> selectedFiles){
		sourceFiles = selectedFiles.toArray(new File[selectedFiles.size()]);
		return true;
	}

	
	public boolean containsAVIFiles(File folderToCheck){
		sourceFiles  = folderToCheck.listFiles(filenameFilter);
		return sourceFiles.length > 0;
	}
	
	
	public boolean createOutPutFolder(File saveDir){
		boolean isDir = true;
		// if it doesn't exist, we create it. 
		// we then check if it's a directory
		if(!saveDir.exists()){
			saveDir.mkdirs();
		}
		isDir = saveDir.isDirectory();
		if(!isDir){
			saveDir.delete();
			System.out.println("savedir was not a dir. was deleted");
		} else {
			outputFolder = saveDir;
		}
		return isDir;
	}
	

	public void convertFiles() throws IOException, InterruptedException{
		System.out.println("Starting conversion..");
		String program = "vlc.exe";
		String sourceFilePath;
		String taskBeginning = "--sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100}:std{access=file,mux=raw,dst='";
		String outputFilePath;
		String taskClosing = "'}";
		String end = "vlc://quit";
		ProcessBuilder processBuild;
		Process process;
		
		for(int i = 0; i < sourceFiles.length; i++){
			File file = sourceFiles[i];
			sourceFilePath = file.getPath();
			outputFilePath = getOutputFilePath(file.getName());
			// insert the destination path into the task
			String task = taskBeginning + outputFilePath + taskClosing;
			
//			System.out.println(sourceFilePath);
//			System.out.println(task);
			
			processBuild = new ProcessBuilder(program, sourceFilePath, task, end);
			// time the process
			long startTime = System.currentTimeMillis();
			process = processBuild.start();
			System.out.println("Extracting mp3 from " + file.getName());
			System.out.println((sourceFiles.length - (i + 1)) + " file(s) remaining.");
			System.out.println(getEstimatedTime(i));
			// don't start next loop until the current process finishes
			process.waitFor();
			long endTime = System.currentTimeMillis();
			long timeTakenForProcess = endTime - startTime;
			timeTaken += timeTakenForProcess;
			
			
		}
		
		
		
	}
	
	
	public void convertFile(File aviFile) throws IOException, InterruptedException{
		System.out.println("Starting conversion..");
		String program = "vlc.exe";
		String sourceFilePath;
		String taskBeginning = "--sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100}:std{access=file,mux=raw,dst='";
		String outputFilePath;
		String taskClosing = "'}";
		String end = "vlc://quit";
		ProcessBuilder processBuild;
		Process process;
		
		sourceFilePath = aviFile.getPath();
		outputFilePath = getOutputFilePath(aviFile.getName());
		// insert the destination path into the task
		String task = taskBeginning + outputFilePath + taskClosing;

		//			System.out.println(sourceFilePath);
		//			System.out.println(task);

		processBuild = new ProcessBuilder(program, sourceFilePath, task, end);
		// time the process
		long startTime = System.currentTimeMillis();
		process = processBuild.start();
		System.out.println("Extracting mp3 from " + aviFile.getName());

		// don't start next loop until the current process finishes
		process.waitFor();
		long endTime = System.currentTimeMillis();
		long timeTakenForProcess = endTime - startTime;
		timeTaken += timeTakenForProcess;
		
		
	}
	
	
	private String getOutputFilePath(String sourceFilename){
		int filextensionStartsAt = sourceFilename.toLowerCase().indexOf(".avi");
		String fileWithoutExtension = sourceFilename.substring(0, filextensionStartsAt);
		
		// sidestep problems with "'"
		boolean containsApostrophe = fileWithoutExtension.contains("'");
		if(containsApostrophe){
			System.out.println("The current file name contains apostrophies that will be removed in the output file.");
		}
		fileWithoutExtension = fileWithoutExtension.replaceAll("'", "");
		
		return outputFolder.getPath() + "\\" + fileWithoutExtension + ".mp3";
	}

	
	
	private FilenameFilter createFilter(String fileExtension){
		
		FilenameFilter filter;
		
		filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toString().toLowerCase().endsWith(fileExtension);
			}
		};
		
		return filter;			
	}
	
	
	
	private String getEstimatedTime(int currentFileIndex){
		System.out.println("Estimating time..");
		String minutesFormat = "Estimated time left is %.1f minutes.";
		String secondsFormat = "Estimated time left is %d seconds.";

		int filesLeft = sourceFiles.length - (currentFileIndex);
		
		double minutesPerFile = getAvarageMinutesTakenPerFile(currentFileIndex);
		double minutesLeft;

		
		String estimatedTimeString;
		
		// time calculations.   if more than zero files left, then remaining time =  average time  * files left,  else just average time (* 1)
		
		if(filesLeft != 0){
			minutesLeft = filesLeft * getAvarageMinutesTakenPerFile(currentFileIndex);
			// show in minutes or seconds?
			if(minutesLeft < 1){
				// show in seconds
				int secondsLeft = (int )((minutesLeft * 60) + 0.5);
				estimatedTimeString = String.format(secondsFormat, secondsLeft);
			} else {
				// show in minutes
				estimatedTimeString = String.format(minutesFormat, minutesLeft);
			}
		} else {
			
			minutesLeft = getAvarageMinutesTakenPerFile(currentFileIndex);
			// show in min or sec
			if(minutesLeft < 1){
				// show in seconds
				int secondsLeft = (int )((minutesLeft * 60) + 0.5);
				estimatedTimeString = String.format(secondsFormat, secondsLeft);
			} else {
				// show in minutes
				estimatedTimeString = String.format(minutesFormat, minutesLeft);
			}
		}
		
		
		return estimatedTimeString;
	}
	
	private double getAvarageMinutesTakenPerFile(int currentFileIndex){
		
		// time taken is in ms
		int totalSeconds = (int) timeTaken / 1000;
		double secondsPerFile = totalSeconds / (currentFileIndex + 1);
		
		return secondsPerFile / 60;
	}

	
	
	
}

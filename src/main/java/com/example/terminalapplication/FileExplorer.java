package com.example.terminalapplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FileExplorer extends Application {

    private TextArea outputArea;
    private TextField inputField;
    private String currentDirectory = System.getProperty("user.dir");

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 600, 600);

        scene.getStylesheets().add((this.getClass().getResource("stylesheet.css")).toExternalForm());
        Label label = new Label("Enter command:");
        root.setTop(label);

        inputField = new TextField();
        inputField.setStyle("-fx-background-color:#443333;" +
                "-fx-text-fill:#ffffff;" +
                "-fx-font-size:20;" +
                "-fx-padding-bottom:10px;");
        inputField.setPromptText("write your command");
        inputField.setOnAction(e -> processInput());
        root.setTop(inputField);

        outputArea = new TextArea();
        outputArea.setStyle("-fx-text-fill:#ff7777;" +
                "-fx-font-size:20;" +
                "-fx-padding-bottom:10px;");
        outputArea.setCursor(Cursor.DISAPPEAR);
        root.setCenter(outputArea);


        primaryStage.setScene(scene);
        primaryStage.setTitle("Command Line Application");
        primaryStage.show();
    }

    private void processInput() {
        String command = inputField.getText().trim();
        inputField.clear();

        if (command.startsWith("dir")) {
            if(command.contains("dir -filer \"*.")){
                String endString = (((command.replace("dir -filer \"*" , ""))
                        .replace("\"" , ""))
                        .replace("*" , ""))
                        .trim();

                String[] files = (new File(new File(currentDirectory).toURI())).list(new FilenameFilter() {
                    public boolean accept(File dir, String file) {
                        return file.endsWith(endString);
                    }
                }); // an anonymous inner class as FilenameFilter
                outputArea.appendText("Current directory : " + currentDirectory + "\n");
                assert files != null;
                for (String file : files) {
                    outputArea.appendText(file + "\n");
                }

            }
//            "dir -filer \"ja*\""

            //starts with
            else if(command.contains("dir -filer \"")){
                String beginString = ((command.replace("dir -filer \"" , ""))
                        .replace("\"" , ""))
                        .replace("*" , "").trim();

                String[] files = (new File(currentDirectory)).list(new FilenameFilter() {
                    public boolean accept(File dir, String file) {
                        return file.startsWith(beginString);
                    }
                }); // an anonymous inner class as FilenameFilter
                outputArea.appendText("Current directory : " + currentDirectory + "\n");
                assert files != null;
                for (String file : files) {
                    outputArea.appendText(file + "\n");
                }

            }
            else if( command.compareTo("dir") == 0){
                File[] dirFiles = new File(currentDirectory).listFiles();
                outputArea.appendText("Current directory : " + currentDirectory + "\n");
                assert dirFiles != null;
                for (File file : dirFiles) {
                    outputArea.appendText(file.getName() + "\n");
                }
            }

        } else if (command.startsWith("cat")) {

              if(command.contains("cat") && command.contains(">")){
                StringTokenizer fileNames = new StringTokenizer(command.replace("cat " , "")
                        , ">");

                String firstFileName = (fileNames.nextElement().toString()).trim();
                String secondFileName = fileNames.nextElement().toString().trim();
                  System.out.println(fileNames +" :" +secondFileName);

                File sourceFile = new File(currentDirectory+firstFileName);
                File destFile = new File(currentDirectory+secondFileName);
                try{
                    FileReader fileReader = new FileReader(sourceFile);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    FileWriter fileWriter = new FileWriter(destFile , true);
                    String line = bufferedReader.readLine();
                    while (line != null){
                        fileWriter.write("\n"+line);
                        line = bufferedReader.readLine();
                    }
                    outputArea.appendText("successfully written "+firstFileName+" content to "+ secondFileName);
                    fileWriter.close();
                    bufferedReader.close();
                }catch (Exception e){
                    outputArea.appendText("Error: " + e.getMessage() + "\n");
                }
            }
//            =========================================================
            else if(command.contains("cat") && !command.contains(">")){
//                1. removing the cat string from the input command
                String fileName = (command.replace("cat" , "")
                        .replace(" " , "")).trim();
                try {
                        String content = new String(Files.readAllBytes(Paths.get(fileName)));
                        outputArea.appendText(content + "\n");
                    } catch (IOException e) {
                        outputArea.appendText("Error: " + e.getMessage() + "\n");
                    }
            }
        } else if (command.startsWith("rm")) {

            if(command.contains("rm ") && command.contains("*") && !command.contains("rm *")){
                String startString = ((command.replace("rm " , ""))
                        .replace("*" , ""));

                File[] files = new File(currentDirectory).listFiles((dir, name) -> name.startsWith(startString));

                if (files != null) {
                    for (File file : files) {
                        if(file.delete()) {
                            outputArea.appendText(file.getName()+" is deleted successfully");
                        }
                    }
                } else {
                    outputArea.appendText("No files found.\n");
                }
            }
            else if(command.contains("rm *") && !command.contains("rm -r")){
                String endString = command.replace("rm *" , "");
                File[] files = new File(currentDirectory).listFiles((dir, name) -> name.endsWith(endString));
                if (files != null) {
                    for (File file : files) {
                       file.delete();
                    }
                    outputArea.appendText("Deleted " + files.length + " file(s).\n");
                } else {
                    outputArea.appendText("No files found.\n");
                }
            }
            else if(command.contains("rm -r") && !command.contains("rm *")){
                String dirName = currentDirectory+"\\"+(command.replace("rm -r" , "")).trim();
                File  dir = new File(dirName);
                if (dir.exists()) {
                    deleteFolder(dir);//calling delete folder function
                    outputArea.appendText("Deleted folder " + dirName + " and its contents.\n");
                } else {
                    outputArea.appendText("Folder not found.\n");
                }
            }
        } else if (command.startsWith("cp")) {
            String[] parts = command.split(" ");
            if (parts.length == 3) {
                String fileName = parts[1];
                String folderName = parts[2];
                File file = new File(fileName);
                File folder = new File(folderName);
                if (file.exists() && folder.exists() && folder.isDirectory()) {
                    try {
                        Files.copy(file.toPath(), Paths.get(folder.toPath().toString(), file.getName()));
                        outputArea.appendText("Copied " + fileName + " to " + folderName + ".\n");
                    } catch (IOException e) {
                        outputArea.appendText("Error: " + e.getMessage() + "\n");
                    }
                } else {
                    outputArea.appendText("File or directory not found.\n");
                }
            } else {
                outputArea.appendText("Invalid command.\n");
            }
        } else if (command.startsWith("cd")) {

            String folderName = command.substring(command.indexOf("cd") + 2).trim();
            if (folderName.equals("..")) {
                String parentDirectory = new File(currentDirectory).getParent();
                if (parentDirectory != null) {
                    currentDirectory = parentDirectory;
                    outputArea.appendText("Changed to parent directory: " + currentDirectory + "\n");
                } else {
                    outputArea.appendText("Already at root directory.\n");
                }
            } else {
                File folder = new File(folderName);
                if (folder.exists() && folder.isDirectory()) {
                    currentDirectory = folder.getAbsolutePath();
                    outputArea.appendText("Changed to directory: " + currentDirectory + "\n");
                } else {
                    outputArea.appendText("Directory not found.\\n");
                }
            }
        }
        else if(command.equals("help")){
            outputArea.appendText("""
                     dir - list current directory files (including directory)
                     dir -filer "*.java" = list only files ending with .java
                     dir -filer "ja*" = list only files starting with ja
                     cat filename.txt = display content of given file
                     cat filename1.txt > filename2.txt = append content of the first file to the second file
                     rm filename* = delete all files start with specified string
                     rm *.txt = delete all files ending with specified string
                     rm -r folderName = delete specified folder with all its contents
                     cp filename.txt folderName = copy specified file to the specified folder
                    """);
        }
        else {
            outputArea.appendText("Invalid command.\n");
        }
        outputArea.appendText("----------------------------------------------\n");
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }
    public static void help(){
        System.out.println("\n dir - list current directory files (including directory)" +
                "\n dir -filer *.java” = list only files ending with .java" +
                "\n dir -filer ”ja*” = list only files starting with ja" +
                "\n cat filename.txt = display content of given file" +
                "\n cat filename1.txt > filename2.txt = append content of the first file to the second file" +
                "\n rm filename* = delete all files start with specified string" +
                "\n rm *.txt = delete all files ending with specified string" +
                "\n rm -r foldername = delete specified folder with all its contents" +
                "\n cp filename.txt foldername = copy specified file to the specified folder\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package jackielisummer17;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Java 8 code
public class gcodePlotter extends Application {
    private Desktop desktop = Desktop.getDesktop();
    private FileProcesser gcodeFile;
    private List<ParsedCommand> commandList = new ArrayList<>();
    private Stage thisStage;
    private Axes axes = new Axes(
            600, 600,
            -15, 15, 5,
            -10, 10, 5
    );
    private final Button viewPatButton = new Button("View a .pat file ...");
    private final Button gcodeToPatButton = new Button("Convert a gcode file to .pat ...");
    private final Button gcodeToLinearPatButton = new Button("Convert and linearize a gcode file to .pat ...");


    public static void main(String[] args) {
        launch(args);
    }

    private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                    gcodePlotter.class.getName()).log(
                    Level.SEVERE, null, ex
            );
        }
    }

    @Override
    public void start(final Stage stage) {
        final FileChooser fileChooser = new FileChooser();


        /** Initialize axes*/
        axes = new Axes(
                600, 600,
                -25, 25, 5,
                -25, 25, 5
        );

        /** Initialize the open file button*/
        viewPatButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Viewing a .pat ....");
                        gcodeFile = new FileProcesser(file.getPath(), file.getName());
                        try {
                            /** Process the gcode file */
                            commandList = gcodeFile.processLineByLine(false, true);
                            System.out.println("absV is rescaled to:" + gcodeFile.getAbsV());
                            axes = new Axes(
                                    600, 600,
                                    -1 * gcodeFile.getAbsV(), gcodeFile.getAbsV(), 5,
                                    -1 * gcodeFile.getAbsV(), gcodeFile.getAbsV(), 5
                            );
                            plotCommands();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        gcodeToPatButton.setOnAction(
                e -> {
                    System.out.println("Converting to .pat....");
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        gcodeFile = new FileProcesser(file.getPath(), file.getName());
                        try {
                            /** Process the gcode file */
                            commandList = gcodeFile.processLineByLine(false, false);
                            axes = new Axes(
                                    600, 600,
                                    -1 * gcodeFile.getAbsV(), gcodeFile.getAbsV(), 5,
                                    -1 * gcodeFile.getAbsV(), gcodeFile.getAbsV(), 5
                            );
                            plotCommands();
                            gcodeFile.outputCommands();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        gcodeToLinearPatButton.setOnAction(
                e -> {
                    System.out.println("Converting to a linearized .pat....");
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        gcodeFile = new FileProcesser(file.getPath(), file.getName());
                        try {
                            /** Process the gcode file */
                            commandList = gcodeFile.processLineByLine(true, false);
                            axes = new Axes(
                                    600, 600,
                                    -1 * gcodeFile.getAbsV(), gcodeFile.getAbsV(), 5,
                                    -1 * gcodeFile.getAbsV(), gcodeFile.getAbsV(), 5
                            );
                            plotCommands();
                            gcodeFile.outputCommands();

                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        /** Initialize A Default Function to Plot*/
        Plot plot = new Plot(
                x -> x * x,
                -8, 8, 0.1,
                axes
        );

        /** Initialize the stage*/
        stage.setTitle("y = \u00BC(x+4)(x+1)(x-2)");
        thisStage = stage;
        stage.setScene(new Scene(setLayoutWithGraph(plot), Color.rgb(35, 39, 50)));
        stage.show();
    }

    public BorderPane setLayoutWithGraph(Node plot) {
        BorderPane layout = new BorderPane();
        HBox buttons = new HBox(3);
        buttons.getChildren().addAll(viewPatButton, gcodeToPatButton, gcodeToLinearPatButton);
        layout.setBottom(buttons);
        layout.setCenter(plot);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        return layout;
    }

    public void plotCommands() {
        int count = 0;
        Pane graphs = new StackPane();

        for (int i = 1; i < commandList.size(); i++) {
            ParsedCommand currCommand = commandList.get(i);
            ParsedCommand prevCommand = commandList.get(i - 1);
            double x = currCommand.getX();
            double y = currCommand.getY();
            double prevX = prevCommand.getX();
            double prevY = prevCommand.getY();
            final double minX = (x < prevX) ? x : prevX;
            final double otherX = x + prevX - minX;

            if (gcodeFile.isLinearizedMode() || currCommand.getCommand() == 0 || currCommand.getCommand() == 1) {
                count++;
                double k = (y - prevY) / (x - prevX);
                Plot plot = new Plot(
                        xk -> k * (xk - prevCommand.getX()) + prevCommand.getY(),
                        minX, otherX, 0.01, axes);
                graphs.getChildren().addAll(plot);
            } else if (currCommand.isArc()) {
                System.out.println("Drawing arcs");
                double centerX = currCommand.getI();
                double centerY = currCommand.getJ();
                double squareX = Math.pow(centerX - prevX, 2);
                double squareY = Math.pow(centerY - prevY, 2);
                double R = Math.sqrt(squareX + squareY);

                if (currCommand.getCommand() == 3) {

                } else {

                }
                Plot plot = new Plot(
                        xk -> Math.sqrt(R * R - Math.pow(xk - centerX, 2)) + centerY,
                        minX, otherX, 0.01, axes);

                Plot plot2 = new Plot(
                        xk -> -Math.sqrt(R * R - Math.pow(xk - centerX, 2)) + centerY,
                        minX, otherX, 0.05, axes);

                graphs.getChildren().addAll(plot, plot2);
            }
        }
        System.out.println("total draw: " + count);
        thisStage.setTitle("plotted");
        thisStage.setScene(new Scene(setLayoutWithGraph(graphs), Color.rgb(35, 39, 50)));
    }
}


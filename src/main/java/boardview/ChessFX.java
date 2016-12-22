package boardview;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import gamecontrol.AIChessController;
import gamecontrol.ChessController;
import gamecontrol.GameController;
import gamecontrol.NetworkedChessController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Main class for the chess application
 * Sets up the top level of the GUI
 * @author Yufeng Wang
 * @version 1.10
 */
public class ChessFX extends Application {

    private GameController controller;
    private BoardView board;
    private Text state;
    private Text sideStatus;
    private VBox root;
    private Button humanVsHuman;
    private Button humanVsAi;
    private Button holdNet;
    private Button join;
    private Text ipAddress;
    private TextField inputline;
    private HBox linebox1;
    private HBox linebox2;

    @Override
    public void start(Stage primaryStage) {
        inputline = new TextField("Enter the IP address");
        humanVsHuman = new Button("Human VS Human");
        humanVsAi = new Button("Human VS AI");
        holdNet = new Button("host Net work");
        join = new Button("Join the Host");

        humanVsHuman.setOnMouseClicked(e -> board.reset(new ChessController()));
        humanVsAi.setOnMouseClicked(e -> board.reset(new AIChessController()));
        holdNet.setOnMouseClicked(makeHostListener());
        join.setOnMouseClicked(makeJoinListener(inputline));
        join.disableProperty().bind(Bindings.isEmpty(inputline.textProperty()));


        primaryStage.setTitle("Chess game");
        try {
            ipAddress = new Text(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            ipAddress.setText("Unfortunately you dont have IP address.");
        }
        controller = new ChessController();
        state = new Text("ongoing");
        sideStatus = new Text("white");
        linebox1 = new HBox(10);
        linebox2 = new HBox(10);
        linebox1.getChildren().addAll(humanVsHuman, humanVsAi,
            holdNet, ipAddress);
        linebox2.getChildren().addAll(state, sideStatus);
        board = new BoardView(controller, state, sideStatus);
        root = new VBox(10);
        root.getChildren().addAll(board.getView(), linebox2,
            linebox1, join, inputline);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private EventHandler<? super MouseEvent> makeHostListener() {
        return event -> {
            board.reset(new NetworkedChessController());
        };
    }

    private EventHandler<? super MouseEvent> makeJoinListener(TextField input) {
        return event -> {
            try {
                InetAddress addr = InetAddress.getByName(input.getText());
                GameController newController
                    = new NetworkedChessController(addr);
                board.reset(newController);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package boardview;
import javafx.scene.control.ButtonBar.ButtonData;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import javafx.scene.paint.Color;
import gamecontrol.AIChessController;
import gamecontrol.ChessController;
import gamecontrol.GameController;
import gamecontrol.GameState;
import gamecontrol.NetworkedChessController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.text.Text;
import model.Move;
import model.Piece;
import model.PieceType;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import model.chess.CastlingMove;
import model.chess.PawnCaptureMove;
import model.chess.PromotionMove;
import model.IllegalMoveException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Position;
import model.Side;
/**
 * A class for a view for a chess board. This class must have a reference
 * to a GameController for chess playing chess
 */
public class BoardView {

    protected GameController controller;
    private GridPane gridPane;
    private Tile[][] tiles;
    private Text sideStatus;
    private Text state;
    private boolean isRotated;
    private Tile clickedtile;
    private Set<Move> possibleMoveSet;
    private List<Position> possibleMoveSetPosition;
    private int clicktime;
    /**
     * Construct a BoardView with an instance of a GameController
     * and a couple of Text object for displaying info to the user
     * @param controller The controller for the chess game
     * @param state A Text object used to display state to the user
     * @param sideStatus A Text object used to display whose turn it is
     */
    public BoardView(GameController controller, Text state, Text sideStatus) {
        this.controller = controller;
        this.state = state;
        this.sideStatus = sideStatus;
        tiles = new Tile[8][8];
        gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color : goldenrod;");
        reset(controller);
        clicktime = 0;
    }

    /**
     * Listener for clicks on a tile
     *
     * @param tile The tile attached to this listener
     * @return The event handler for all tiles.
     */
    private EventHandler<? super MouseEvent> tileListener(Tile tile) {
        return event -> {
            if (controller instanceof NetworkedChessController
                    && controller.getCurrentSide()
                    != ((NetworkedChessController) controller).getLocalSide()) {
                //not your turn!
                return;
            }

            // Don't change the code above this :)
            if (clicktime == 0) {
                firstClick(tile);
            } else if (clicktime == 1) {
                secondClick(tile);
            }
        };
    }

    /**
     * Perform the first click functions, like displaying
     * which are the valid moves for the piece you clicked.
     * @param tile The TileView that was clicked
     */
    private void firstClick(Tile tile) {
        clickedtile = tile;
        Position p = tile.getPosition();
        possibleMoveSet = controller.getMovesForPieceAt(p);
        System.out.println(possibleMoveSet.size());
        if (!possibleMoveSet.isEmpty()) {
            this.getTileAt(p).highlight(Color.GOLD);
            for (Move move: possibleMoveSet) {
                Position newp = move.getDestination();
                System.out.println(newp);
                if (move instanceof PromotionMove) {
                    this.getTileAt(newp).highlight(Color.GOLD);
                } else if (controller.moveResultsInCapture(move)) {
                    if (move instanceof PawnCaptureMove
                        && ((PawnCaptureMove) move).isEnPassant()) {
                        this.getTileAt(newp).highlight(Color.PINK);
                        System.out.println("printle3");
                    } else {
                        this.getTileAt(newp).highlight(Color.RED);
                    }
                } else if (move instanceof CastlingMove) {
                    this.getTileAt(newp).highlight(Color.CYAN);
                    System.out.println("printle2");
                } else {
                    this.getTileAt(newp).highlight(Color.GRAY);
                    System.out.println("printle");
                }
            }
            clicktime = 1;

        } else {
            clicktime = 0;
        }
    }

    /**
     * Perform the second click functions, like
     * sending moves to the controller but also
     * checking that the user clicked on a valid position.
     * If they click on the same piece they clicked on for the first click
     * then you should reset to click state back to the first click and clear
     * the highlighting effected on the board.
     *
     * @param tile the TileView at which the second click occurred
     */
    private void secondClick(Tile tile) {
        Position startPosition = clickedtile.getPosition();
        Position destPosition = tile.getPosition();
        List<Position> newposition = new ArrayList<Position>();
        Set<Tile> newtiles = new HashSet<Tile>();
        Position oldP = clickedtile.getPosition();
        Set<Move> possibleMoves = controller.getMovesForPieceAt(oldP);
        for (Move move : possibleMoves) { //determine if clicked the same tile
            Position target = move.getDestination();
            newposition.add(target);
            newtiles.add(getTileAt(target));
        }

        if (tile == clickedtile || newtiles.contains(tile)) {
            clickedtile.clear();
            for (Tile oneTile : newtiles) {
                oneTile.clear();
            }
            clicktime = 0;
        }

        Move moving = new Move(startPosition, destPosition);
            //catch if IllegalMove
        try {
            controller.makeMove(moving);
            controller.endTurn();
            controller.beginTurn();
            reset(controller);
        } catch (IllegalMoveException ex) {
            clicktime = 0;
        }
    }
    /**
     * This method should be called any time a move is made on the back end.
     * It should update the tiles' highlighting and symbols to reflect the
     * change in the board state.
     *
     * @param moveMade the move to show on the view
     * @param capturedPositions a list of positions where pieces were captured
     *
     */
    public void updateView(Move moveMade, List<Position> capturedPositions) {
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                this.getTileAt(row, col).clear();
            }
        } // refresh the view

        if (moveMade.mustCapture()) {
            for (Position p : capturedPositions) {
                this.getTileAt(p).highlight(Color.GREEN);
            }
        } // hightlight the captured tile

        if (moveMade instanceof PawnCaptureMove
            && ((PawnCaptureMove) moveMade).isEnPassant()) {
            this.getTileAt(capturedPositions.get(1)).setSymbol("");
        }

        Tile moveTile = getTileAt(moveMade.getStart());
        Tile destTile = getTileAt(moveMade.getDestination());
        moveTile.highlight(Color.GOLD);
        destTile.highlight(Color.GOLD);
        String moveSymbol = controller.getSymbolForPieceAt(
            moveMade.getDestination());
        destTile.setSymbol(moveSymbol);
        moveTile.setSymbol("");

    }

    /**
     * Asks the user which PieceType they want to promote to
     * (suggest using Alert). Then it returns the Piecetype user selected.
     *
     * @return  the PieceType that the user wants to promote their piece to
     */
    private PieceType handlePromotion() {
        List<PieceType> pieceTypeList = controller.getPromotionTypes();
        ChoiceDialog<PieceType> dialog
            = new ChoiceDialog<>(pieceTypeList.get(0), pieceTypeList);
        dialog.setTitle("Choice Dialog");
        dialog.setHeaderText("Promotion time Baby");
        dialog.setContentText("Choose the piece you want to promote");

        Optional<PieceType> result = dialog.showAndWait();
        return result.get();
    }


    /**
     * Handles a change in the GameState (ie someone in check or stalemate).
     * If the game is over, it should open an Alert and ask to keep
     * playing or exit.
     *
     * @param s The new Game State
     */
    public void handleGameStateChange(GameState s) {
        state.setText(s.toString());
        if (controller.getCurrentState().isGameOver()) {
            controller.endTurn();
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Game is over");
            alert.setHeaderText("Do you want to restart the game?");
            alert.setContentText("Choose your option.");

            ButtonType buttonTypeOne = new ButtonType("VS Ai");
            ButtonType buttonTypeTwo = new ButtonType("VS Human");
            ButtonType buttonTypeThree = new ButtonType("Internet Game");
            ButtonType buttonTypeCancel = new ButtonType(
                "Cancel", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo,
                buttonTypeThree, buttonTypeCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeOne) {
                reset(new AIChessController());
            } else if (result.get() == buttonTypeTwo) {
                reset(new ChessController());
            } else if (result.get() == buttonTypeThree) {
                reset(new NetworkedChessController());
            } else {
                System.exit(0);
            }
        }
    }
    /**
     * Updates UI that depends upon which Side's turn it is
     * @param s The new Side whose turn it currently is
     */
    public void handleSideChange(Side s) {
        sideStatus.setText(controller.getCurrentSide() + "'s Turn");
    }

    /**
     * Resets this BoardView with a new controller.
     * This moves the chess pieces back to their original configuration
     * and calls startGame() at the end of the method
     * @param newController The new controller for this BoardView
     */
    public void reset(GameController newController) {
        if (controller instanceof NetworkedChessController) {
            ((NetworkedChessController) controller).close();
        }
        controller = newController;
        isRotated = false;
        if (controller instanceof NetworkedChessController) {
            Side mySide
                = ((NetworkedChessController) controller).getLocalSide();
            if (mySide == Side.BLACK) {
                isRotated = true;
            }
        }
        sideStatus.setText(controller.getCurrentSide() + "'s Turn");

        // controller event handlers
        // We must force all of these to run on the UI thread
        controller.addMoveListener(
                (Move move, List<Position> capturePositions) ->
                Platform.runLater(
                    () -> updateView(move, capturePositions)));

        controller.addCurrentSideListener(
                (Side side) -> Platform.runLater(
                    () -> handleSideChange(side)));

        controller.addGameStateChangeListener(
                (GameState state) -> Platform.runLater(
                    () -> handleGameStateChange(state)));

        controller.setPromotionListener(() -> handlePromotion());


        addPieces();
        controller.startGame();
        if (isRotated) {
            setBoardRotation(180);
        } else {
            setBoardRotation(0);
        }
    }

    /**
     * Initializes the gridPane object with the pieces from the GameController.
     * This method should only be called once before starting the game.
     */
    private void addPieces() {
        gridPane.getChildren().clear();
        Map<Piece, Position> pieces = controller.getAllActivePiecesPositions();
        /* Add the tiles */
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Tile tile = new TileView(new Position(row, col));
                gridPane.add(tile.getRootNode(),
                        1 + tile.getPosition().getCol(),
                        1 + tile.getPosition().getRow());
                GridPane.setHgrow(tile.getRootNode(), Priority.ALWAYS);
                GridPane.setVgrow(tile.getRootNode(), Priority.ALWAYS);
                getTiles()[row][col] = tile;
                tile.getRootNode().setOnMouseClicked(
                        tileListener(tile));
                tile.clear();
                tile.setSymbol("");
            }
        }
        /* Add the pieces */
        for (Piece p : pieces.keySet()) {
            Position placeAt = pieces.get(p);
            getTileAt(placeAt).setSymbol(p.getType().getSymbol(p.getSide()));
        }
        /* Add the coordinates around the perimeter */
        for (int i = 1; i <= 8; i++) {
            Text coord1 = new Text((char) (64 + i) + "");
            GridPane.setHalignment(coord1, HPos.CENTER);
            gridPane.add(coord1, i, 0);

            Text coord2 = new Text((char) (64 + i) + "");
            GridPane.setHalignment(coord2, HPos.CENTER);
            gridPane.add(coord2, i, 9);

            Text coord3 = new Text(9 - i + "");
            GridPane.setHalignment(coord3, HPos.CENTER);
            gridPane.add(coord3, 0, i);

            Text coord4 = new Text(9 - i + "");
            GridPane.setHalignment(coord4, HPos.CENTER);
            gridPane.add(coord4, 9, i);
        }
    }

    private void setBoardRotation(int degrees) {
        gridPane.setRotate(degrees);
        for (Node n : gridPane.getChildren()) {
            n.setRotate(degrees);
        }
    }

    /**
     * Gets the view to add to the scene graph
     * @return A pane that is the node for the chess board
     */
    public Pane getView() {
        return gridPane;
    }

    /**
     * Gets the tiles that belong to this board view
     * @return A 2d array of TileView objects
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    private Tile getTileAt(int row, int col) {
        return getTiles()[row][col];
    }

    private Tile getTileAt(Position p) {
        return getTileAt(p.getRow(), p.getCol());
    }

}

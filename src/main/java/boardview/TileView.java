package boardview;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.Position;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
/**
 * View class for a tile on a chess board
 * A tile should be able to display a chess piece
 * as well as highlight itself during the game.
 *
 * @author Yufeng Wang
 */
public class TileView implements Tile {
    private Position position;
    private Rectangle rectangle; //the hightlight rectangle
    private StackPane rootnode = new StackPane();
    private Color backgroundcolor;
    private Label symbolold;
    private int col;
    private int row;
    private int judgetheposition;
    /**
     * Creates a TileView with a specified position
     * @param p
     */
    public TileView(Position p) {
        this.position = p;
        this.col = p.getCol();
        this.row = p.getRow();
        symbolold = new Label("");
        judgetheposition = (col + row) % 2;
        this.rectangle = new Rectangle(70, 70, Color.rgb(255, 215, 0, 0.0));
        if (judgetheposition == 0) {
            backgroundcolor = Color.LIGHTGRAY;
        } else {
            backgroundcolor = Color.ORANGE;
        }
        Background back = new Background(new BackgroundFill(backgroundcolor,
            CornerRadii.EMPTY, Insets.EMPTY));
        rootnode.setBackground(back);
    }

    @Override
    public Position getPosition() {
        return position;
    }


    @Override
    public Node getRootNode() {
        rootnode.getChildren().clear();
        rootnode.getChildren().addAll(rectangle, symbolold);
        return rootnode;
    }

    @Override
    public void setSymbol(String symbol) {
        symbolold = new Label(symbol);
        symbolold.setFont(Font.font(30));
        rootnode.getChildren().add(symbolold);
    }


    @Override
    public String getSymbol() {
        return symbolold.getText();
    }

    @Override
    public void highlight(Color color) {
        if (judgetheposition == 0) {
            rectangle.setFill(color);
        } else {
            rectangle.setFill(color.brighter());
        }
    }

    @Override
    public void clear() {
        rectangle.setFill(Color.TRANSPARENT);
    }
}

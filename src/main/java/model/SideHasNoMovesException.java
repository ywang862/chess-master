package model;

/**
 * Gets thrown if the side cannot make any legal moves, which leads to game over
 *
 */
public class SideHasNoMovesException extends Exception {

    public SideHasNoMovesException(String msg) {
        super(msg);
    }
}

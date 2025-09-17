package ai;

import controller.Game;
import model.board.Move;

public abstract class IAAbstract implements IA {
    protected int depth;

    public IAAbstract(int depth) {
        this.depth = depth;
    }

    public abstract Move makeMove(Game game);
}
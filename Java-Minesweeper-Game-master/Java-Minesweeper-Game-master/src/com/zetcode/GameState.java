package com.zetcode;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int[] field;
    public int minesLeft;
    public int coinCount;
    public int N_ROWS, N_COLS, N_MINES;
    public boolean inGame;
}

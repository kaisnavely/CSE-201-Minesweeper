package com.zetcode;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Board extends JPanel {

    private static final int NUM_IMAGES = 13;
    private static final int CELL_SIZE = 15;

    private static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;

    private static final int N_MINES = 40;
    private static final int N_ROWS = 16;
    private static final int N_COLS = 16;

    private static final int BOARD_WIDTH = N_COLS * CELL_SIZE + 1;
    private static final int BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private Image[] img;

    private int allCells;
    private final JLabel statusbar;

    public Board(JLabel statusbar) {
        this.statusbar = statusbar;
        initBoard();
    }

    private void initBoard() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));

        img = new Image[NUM_IMAGES];
        for (int i = 0; i < NUM_IMAGES; i++) {
            img[i] = new ImageIcon("src/resources/" + i + ".png").getImage();
        }

        addMouseListener(new MinesAdapter());
        newGame();
    }

    private void newGame() {
        Random random = new Random();
        inGame = true;
        minesLeft = N_MINES;
        allCells = N_ROWS * N_COLS;
        field = new int[allCells];

        for (int i = 0; i < allCells; i++) {
            field[i] = COVER_FOR_CELL;
        }

        statusbar.setText(Integer.toString(minesLeft));

        int i = 0;
        while (i < N_MINES) {
            int position = random.nextInt(allCells);
            if (field[position] != COVERED_MINE_CELL) {
                field[position] = COVERED_MINE_CELL;
                updateNeighbors(position);
                i++;
            }
        }
    }

    private void updateNeighbors(int pos) {
        int col = pos % N_COLS;
        int[] offsets = {-N_COLS, +N_COLS, -1, +1, -N_COLS - 1, -N_COLS + 1, +N_COLS - 1, +N_COLS + 1};
        for (int offset : offsets) {
            int neighbor = pos + offset;
            int neighborCol = neighbor % N_COLS;
            if (neighbor >= 0 && neighbor < allCells && Math.abs(neighborCol - col) <= 1 && field[neighbor] != COVERED_MINE_CELL) {
                field[neighbor]++;
            }
        }
    }

    private void findEmptyCells(int j) {
        int col = j % N_COLS;
        int[] offsets = {-N_COLS, +N_COLS, -1, +1, -N_COLS - 1, -N_COLS + 1, +N_COLS - 1, +N_COLS + 1};
        for (int offset : offsets) {
            int neighbor = j + offset;
            int neighborCol = neighbor % N_COLS;
            if (neighbor >= 0 && neighbor < allCells && Math.abs(neighborCol - col) <= 1) {
                revealCell(neighbor);
            }
        }
    }

    private void revealCell(int pos) {
        if (field[pos] > MINE_CELL) {
            field[pos] -= COVER_FOR_CELL;
            if (field[pos] == EMPTY_CELL) {
                findEmptyCells(pos);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        int uncover = 0;
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                int index = i * N_COLS + j;
                int cell = field[index];

                if (inGame && cell == MINE_CELL) {
                    inGame = false;
                }

                if (!inGame) {
                    cell = switch (cell) {
                        case COVERED_MINE_CELL -> DRAW_MINE;
                        case MARKED_MINE_CELL -> DRAW_MARK;
                        default -> (cell > COVERED_MINE_CELL) ? DRAW_WRONG_MARK : (cell > MINE_CELL ? DRAW_COVER : cell);
                    };
                } else {
                    if (cell > COVERED_MINE_CELL) {
                        cell = DRAW_MARK;
                    } else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                        uncover++;
                    }
                }

                g.drawImage(img[cell], j * CELL_SIZE, i * CELL_SIZE, this);
            }
        }

        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }

    private class MinesAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;
            boolean doRepaint = false;

            if (!inGame) {
                newGame();
                repaint();
                return;
            }

            if (x >= N_COLS * CELL_SIZE || y >= N_ROWS * CELL_SIZE) return;

            int index = cRow * N_COLS + cCol;
            int cell = field[index];

            if (e.getButton() == MouseEvent.BUTTON3) { // Right click
                if (cell > MINE_CELL) {
                    doRepaint = true;
                    if (cell <= COVERED_MINE_CELL) {
                        if (minesLeft > 0) {
                            field[index] += MARK_FOR_CELL;
                            minesLeft--;
                        } else {
                            statusbar.setText("No marks left");
                        }
                    } else {
                        field[index] -= MARK_FOR_CELL;
                        minesLeft++;
                    }
                    statusbar.setText(Integer.toString(minesLeft));
                }
            } else { // Left click
                if (cell > COVERED_MINE_CELL) return;

                if (cell > MINE_CELL && cell < MARKED_MINE_CELL) {
                    field[index] -= COVER_FOR_CELL;
                    doRepaint = true;

                    if (field[index] == MINE_CELL) {
                        inGame = false;
                    } else if (field[index] == EMPTY_CELL) {
                        findEmptyCells(index);
                    }
                }
            }

            if (doRepaint) {
                repaint();
            }
        }
    }
}

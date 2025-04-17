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

    private static final int NUM_IMAGES = 14;
    private static final int CELL_SIZE = 15;

    private static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int TREASURE_CELL = 8;
    private static final int MINE_CELL = 9;
    private static final int COVERED_TREASURE_CELL = TREASURE_CELL + COVER_FOR_CELL;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;
    private static final int DRAW_TREASURE = 13;

    private static final int N_MINES = 40;
    private static final int INITIAL_CHESTS = 10;
    private static final int N_ROWS = 16;
    private static final int N_COLS = 16;

    private static final int BOARD_WIDTH = N_COLS * CELL_SIZE + 1;
    private static final int BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private Image[] img;
    private int allCells;
    private int coinCount = 0;
    private int continueCost = 1;

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
        coinCount = 0;
        continueCost = 1;

        for (int i = 0; i < allCells; i++) {
            field[i] = COVER_FOR_CELL;
        }

        statusbar.setText("Mines: " + minesLeft + " | Coins: " + coinCount);

        int i = 0;
        while (i < N_MINES) {
            int position = random.nextInt(allCells);
            if (field[position] == COVER_FOR_CELL) {
                field[position] = COVERED_MINE_CELL;
                updateNeighbors(position);
                i++;
            }
        }

        int chestsPlaced = 0;
        while (chestsPlaced < INITIAL_CHESTS) {
            int position = random.nextInt(allCells);
            if (field[position] == COVER_FOR_CELL) {
                field[position] = COVERED_TREASURE_CELL;
                chestsPlaced++;
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
      // Only reveal cells that are covered and not marked
      if (field[pos] > MINE_CELL && field[pos] < MARKED_MINE_CELL) {
          int originalValue = field[pos]; // Store the covered value

          // Reveal the cell by subtracting the cover value
          field[pos] -= COVER_FOR_CELL;

          // --- ADD COIN CHECK HERE ---
          if (field[pos] == TREASURE_CELL) {
              System.out.println("Revealed treasure via revealCell at: " + pos + " (was " + originalValue + ")"); // Debug
              coinCount++;
              updateStatusbar(); // Update status bar when coin is found
              // Treasure cell revealed, no further action needed for this specific cell
              // (it won't trigger findEmptyCells)
          }
          // --- END COIN CHECK ---

          // If the revealed cell is now empty, trigger chain reaction
          else if (field[pos] == EMPTY_CELL) {
               System.out.println("Found empty cell via revealCell, calling findEmptyCells for: " + pos); // Debug
               findEmptyCells(pos); // Start revealing neighbors
          }
          // If it's a numbered cell (1-8) or a mine (9), revealing stops here.
           else if (field[pos] == MINE_CELL) {
               // Mine hit during reveal process (likely via findEmptyCells)
               // The game over logic is handled elsewhere (paintComponent / mousePressed)
               System.out.println("Mine revealed via revealCell at: " + pos); // Debug
           }
      }
  }
    
 // Helper method to update status bar text
    private void updateStatusbar() {
         if (inGame) {
             statusbar.setText("Mines: " + minesLeft + " | Coins: " + coinCount);
         } else {
             // Optional: Differentiate between win/loss messages if needed after game ends
             // This will be overwritten by paintComponent's win/loss check anyway
              statusbar.setText("Game Over. Mines: " + minesLeft + " | Coins: " + coinCount);
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
                        case TREASURE_CELL -> DRAW_TREASURE;
                        default -> (cell > COVERED_MINE_CELL) ? DRAW_WRONG_MARK : (cell > MINE_CELL ? DRAW_COVER : cell);
                    };
                } else {
                    if (cell == TREASURE_CELL) {
                        cell = DRAW_TREASURE;
                    } else if (cell > COVERED_MINE_CELL) {
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
            statusbar.setText("Game won! Coins: " + coinCount);
        } else if (!inGame) {
            statusbar.setText("Game lost. Coins: " + coinCount);
        }
    }

    private class MinesAdapter extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
          int x = e.getX();
          int y = e.getY();
          int cCol = x / CELL_SIZE;
          int cRow = y / CELL_SIZE;
          boolean doRepaint = false; // Flag to repaint at the end

          if (!inGame) {
              newGame();
              repaint();
              return;
          }

          // Bounds check
          if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) return;

          int index = cRow * N_COLS + cCol;
          // Ensure index is valid (though bounds check should prevent this)
           if (index < 0 || index >= allCells) return;

          int cell = field[index];

          // --- Right Click (Marking) ---
          if (e.getButton() == MouseEvent.BUTTON3) {
              // Can only mark/unmark covered cells
              if (cell > MINE_CELL && cell < MARKED_MINE_CELL + MARK_FOR_CELL) { // Includes covered mines, numbers, treasures
                  doRepaint = true;
                  if (cell <= COVERED_MINE_CELL) { // If it's not already marked
                      if (minesLeft > 0) {
                           // Check if it's a covered treasure - don't mark treasures? Or allow?
                           // Let's allow marking anything covered for simplicity now.
                          if (cell == COVERED_TREASURE_CELL) {
                              field[index] = COVERED_TREASURE_CELL + MARK_FOR_CELL; // Mark the treasure spot
                          } else {
                              field[index] += MARK_FOR_CELL; // Mark mine or number
                          }
                          minesLeft--;
                      } else {
                          statusbar.setText("No marks left | Coins: " + coinCount); // Update status bar
                          doRepaint = false; // No change occurred
                      }
                  } else { // It is already marked, unmark it
                      minesLeft++;
                       // Check if it was a marked treasure
                       if (cell == COVERED_TREASURE_CELL + MARK_FOR_CELL) {
                           field[index] = COVERED_TREASURE_CELL; // Restore covered treasure
                       } else {
                          field[index] -= MARK_FOR_CELL; // Unmark back to covered mine/number
                       }
                  }
                  updateStatusbar(); // Update status bar text
              }
          }
          // --- Left Click (Revealing) ---
          else if (e.getButton() == MouseEvent.BUTTON1) {
              // Ignore clicks on already revealed cells or marked cells
              if (cell <= MINE_CELL || cell >= MARKED_MINE_CELL) {
                  System.out.println("Click ignored on revealed/marked cell: " + index + " value: " + cell); // Debug
                  return;
              }

              // --- This is the main reveal logic trigger ---
              // Handles covered mines, numbers, and treasures
              // The old direct treasure check is removed.
               System.out.println("Left click on covered cell: " + index + " value: " + cell); // Debug

              // Store the value *before* revealing, in case it's a mine
              int valueBeforeReveal = cell;

              // Call revealCell - this handles uncovering, coin counting, and chain reactions
              revealCell(index);

              // Now check the result *after* revealCell potentially modified field[index]
              int valueAfterReveal = field[index];

               // --- Game Over Check ---
               // Check if the specific cell clicked *was* a mine
               // Note: revealCell changes COVERED_MINE_CELL (19) to MINE_CELL (9)
              if (valueBeforeReveal == COVERED_MINE_CELL && valueAfterReveal == MINE_CELL) {
                  System.out.println("Direct mine hit at: " + index); // Debug
                   // Implement continue logic if desired
                   /*
                   if (coinCount >= continueCost) {
                       coinCount -= continueCost;
                       continueCost++; // Increase cost for next time
                       field[index] = COVERED_MINE_CELL; // Optionally re-cover the mine? Or mark it?
                       // field[index] = MARKED_MINE_CELL; // Auto-mark it
                       // minesLeft--;
                       updateStatusbar();
                       // Keep inGame = true
                       System.out.println("Used coins to survive mine!");
                   } else {
                       inGame = false; // Not enough coins, game over
                       System.out.println("Not enough coins to survive mine!");
                   }
                   */
                   // For now, just end the game on mine click
                   inGame = false;
              }

              doRepaint = true; // A reveal action always requires repaint
          }

          if (doRepaint) {
              repaint();
          }
      }
  }
}
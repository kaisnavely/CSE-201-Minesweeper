package com.zetcode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
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
    
    private String finalMessage = ""; // Stores the main "GAME OVER" or "YOU WON" message
    private String finalCoinMessage = ""; // Stores the "Final Coins: X" message
    private boolean showGameOverOverlay = false; // Flag to indicate if the overlay should be drawn

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
          }

          // If the revealed cell is now empty, trigger chain reaction
          else if (field[pos] == EMPTY_CELL) {
               System.out.println("Found empty cell via revealCell, calling findEmptyCells for: " + pos); // Debug
               findEmptyCells(pos); // Start revealing neighbors
          }
          // If it's a numbered cell (1-8) or a mine (9), revealing stops here.
           else if (field[pos] == MINE_CELL) {
               System.out.println("Mine revealed via revealCell at: " + pos); // Debug
           }
      }
  }
    
 // Helper method to update status bar text
    private void updateStatusbar() {
         if (inGame) {
             statusbar.setText("Mines: " + minesLeft + " | Coins: " + coinCount);
         } else {
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
            inGame = false; // Game won
            showGameOverOverlay = true; // We want to show the overlay
            finalMessage = "YOU WON!";
            finalCoinMessage = "Final Coins: " + coinCount;
            statusbar.setText("Game won! Coins: " + coinCount);
            System.out.println("GAME OVER: UI Handled - Win");
        } else if (!inGame) {
            showGameOverOverlay = true;
            statusbar.setText("GAME OVER! You have " + coinCount + " coins");
            System.out.println("GAME OVER: UI Handled - Loss");
        }

        // --- DRAW GAME OVER OVERLAY ONLY IF showGameOverOverlay IS TRUE ---
        if (showGameOverOverlay) {
            g.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black background
            g.fillRect((getWidth() - 150) / 2, (getHeight() - 150) / 2, 150, 150);

            String gameOverText = "Game Over";
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics(new Font("Arial", Font.BOLD, 24));
            g.drawString(gameOverText,
                    (getWidth() - 150) / 2 + (150 - fm.stringWidth(gameOverText)) / 2,
                    (getHeight() - 250) / 2 + (150 - fm.getAscent()) / 2 + fm.getAscent());

            String coinAmountText = "You Have " + coinCount + " Coins";
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fmCA = g.getFontMetrics(new Font("Arial", Font.PLAIN, 12));
            g.drawString(coinAmountText,
                    (getWidth() - 150) / 2 + (150 - fmCA.stringWidth(coinAmountText)) / 2,
                    (getHeight() - 200) / 2 + (150 - fmCA.getAscent()) / 2 + fmCA.getAscent());

            if (coinCount > 0) {
                String coinQuestionText = "Use " + continueCost + " Coin to Continue?";
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                FontMetrics fmCQ = g.getFontMetrics(new Font("Arial", Font.PLAIN, 12));
                g.drawString(coinQuestionText,
                        (getWidth() - 150) / 2 + (150 - fmCQ.stringWidth(coinQuestionText)) / 2,
                        (getHeight() - 165) / 2 + (150 - fmCQ.getAscent()) / 2 + fmCQ.getAscent());
                
                // Continue Button
                g.setColor(Color.gray);
                g.fillRect((getWidth() - 100) / 2, (getHeight() + 25) / 2, 100, 25);
                String continueButtonText = "Continue";
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                FontMetrics fmCQContinue = g.getFontMetrics(new Font("Arial", Font.PLAIN, 12));
                g.drawString(continueButtonText,
                        (getWidth() - 100) / 2 + (100 - fmCQContinue.stringWidth(continueButtonText)) / 2,
                        (getHeight() - 125) / 2 + (175 - fmCQContinue.getAscent()) / 2 + fmCQContinue.getAscent());
            }

           

            // Restart Button
            g.setColor(Color.gray);
            g.fillRect((getWidth() - 100) / 2, (getHeight() + 80) / 2, 100, 25);
            String restartButtonText = "Restart";
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fmRestart = g.getFontMetrics(new Font("Arial", Font.PLAIN, 12));
            g.drawString(restartButtonText,
                    (getWidth() - 100) / 2 + (100 - fmRestart.stringWidth(restartButtonText)) / 2,
                    (getHeight() - 125) / 2 + (230 - fmRestart.getAscent()) / 2 + fmRestart.getAscent());
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
          int index = cRow * N_COLS + cCol; // Define index here
          int cell = -1; // Initialize cell

          if (index >= 0 && index < allCells) {
              cell = field[index]; // Only access field if index is valid
          }

          if (e.getButton() == MouseEvent.BUTTON3) {
              // Can only mark/unmark covered cells
              if (cell > MINE_CELL && cell < MARKED_MINE_CELL + MARK_FOR_CELL && index >= 0 && index < allCells) { // Added index check
                  doRepaint = true;
                  if (cell <= COVERED_MINE_CELL) { // If it's not already marked
                      if (minesLeft > 0) {
                          if (cell == COVERED_TREASURE_CELL) {
                              field[index] = COVERED_TREASURE_CELL + MARK_FOR_CELL; // Mark the treasure spot
                          } else {
                              field[index] += MARK_FOR_CELL;
                          }
                          minesLeft--;
                      } else {
                          statusbar.setText("No marks left | Coins: " + coinCount);
                          doRepaint = false;
                      }
                  } else {
                      minesLeft++;
                      if (cell == COVERED_TREASURE_CELL + MARK_FOR_CELL) {
                          field[index] = COVERED_TREASURE_CELL; // Restore covered treasure
                      } else {
                          field[index] -= MARK_FOR_CELL; // Unmark back to covered mine/number
                      }
                  }
                  updateStatusbar(); // Update status bar text
              }
          }
          // --- Left Click ---
          else if (e.getButton() == MouseEvent.BUTTON1) {
              if (!inGame && showGameOverOverlay) {
                  // --- Game Over Overlay Click Handling ---
                  int continueButtonX = (getWidth() - 100) / 2;
                  int continueButtonY = (getHeight() + 25) / 2;
                  int buttonWidth = 100;
                  int buttonHeight = 25;

                  if (x >= continueButtonX && x <= continueButtonX + buttonWidth &&
                      y >= continueButtonY && y <= continueButtonY + buttonHeight && coinCount > 0) {
                      System.out.println("Continue button clicked!");
                      inGame = true;
                      coinCount--;
                      showGameOverOverlay = false;
                      // Reset revealed mines to covered state upon continuing
                      for (int i = 0; i < field.length; i++) {
                          if (field[i] == MINE_CELL) {
                              field[i] = COVERED_MINE_CELL;
                          }
                      }
                      repaint();
                      updateStatusbar();
                      return;
                  }

                  int restartButtonX = (getWidth() - 100) / 2;
                  int restartButtonY = (getHeight() + 80) / 2;

                  if (x >= restartButtonX && x <= restartButtonX + buttonWidth &&
                      y >= restartButtonY && y <= restartButtonY + buttonHeight) {
                      System.out.println("Restart button clicked!");
                      newGame();
                      showGameOverOverlay = false;
                      repaint();
                      return;
                  }
              } else if (inGame) {
                  // --- In-game left-click logic (revealing cells) ---
                  if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) return;
                  // index is already defined
                  if (index < 0 || index >= allCells) return;
                  // cell is already defined

                  if (cell <= MINE_CELL || cell >= MARKED_MINE_CELL) return;

                  int valueBeforeReveal = cell;
                  revealCell(index);
                  int valueAfterReveal = field[index];

                  if (valueBeforeReveal == COVERED_MINE_CELL && valueAfterReveal == MINE_CELL) {
                      System.out.println("GAME OVER, HIT MINE");
                      statusbar.setText("GAME OVER! You Have " + coinCount + " Coins");
                      inGame = false;
                  }
                  doRepaint = true;
              }
          }

          if (doRepaint) {
              repaint();
          }
      }
  }
}
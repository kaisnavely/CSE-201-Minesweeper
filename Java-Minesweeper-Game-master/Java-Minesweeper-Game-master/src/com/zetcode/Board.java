package com.zetcode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
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

    private static int N_MINES = 40;
    private static int INITIAL_CHESTS = 10;
    private static int N_ROWS = 16;
    private static int N_COLS = 16;

    private static int BOARD_WIDTH = N_COLS * CELL_SIZE + 1;
    private static int BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1;
    
    private boolean showGameOverOverlay = false; // Flag to indicate if the overlay should be drawn
    
    private boolean isInMenu = true;
    private boolean isInMainmenu = true;
    private boolean isInGUISize = false;
    private boolean isInTextSize = false;
    private boolean isInTextView = false;
    private boolean isInTestBoard = false;
    
    //test condition variables
    private boolean firstEightInOneColumn = false;
    private boolean noFirstEightAdjacent = false;
    private boolean ninthMineAdjacent = false;
    private boolean tenthMineIsolated = false;
    private boolean noMoreThanNineTreasures = false;

    private int[] field;
    private boolean inGame = false;
    private int minesLeft;
    private Image[] img;
    private int allCells;
    private int coinCount = 0;
    private int continueCost = 1;

    private final JLabel statusbar;
    private JTextField commandInput;

    public Board(JLabel statusbar) {
        this.statusbar = statusbar;
        initMenu();
    }

    private void initBoard() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + 150));

        img = new Image[NUM_IMAGES];
        for (int i = 0; i < NUM_IMAGES; i++) {
            img[i] = new ImageIcon("Java-Minesweeper-Game-master/src/resources/" + i + ".png").getImage();
        }
        newGame();
    }
    private void initLoadBoard() {
      setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + 40));

      img = new Image[NUM_IMAGES];
      for (int i = 0; i < NUM_IMAGES; i++) {
          img[i] = new ImageIcon("Java-Minesweeper-Game-master/src/resources/" + i + ".png").getImage();
      }
      //newGame();
  }
    
    private void initMenu() {
      setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
      addMouseListener(new MinesAdapter());
    }
    
    private void initTextSizeMenu() {
      removeAll(); // Clear any existing components
      setLayout(new BorderLayout());

      JLabel sizeLabel = new JLabel("Enter desired board size");
      JLabel sizeLabel2 = new JLabel("Small, Medium, or Large");
      commandInput = new JTextField(15); // Create the input field
      
      isInMainmenu = false;
      isInTextSize = true;

      commandInput.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              if (isInTextSize) {
                  String inputText = commandInput.getText().trim().toLowerCase();
                  switch (inputText){
                    case "small":
                      setValues(8, 8, 10, 4);
                      removeAll();
                      initTextMode();
                      commandInput.setText("");
                      break;
                    case "medium":
                      setValues(16, 16, 40, 10);
                      removeAll();
                      initTextMode();
                      commandInput.setText("");
                      break;
                    case "large":
                      setValues(16, 30, 99, 25);
                      removeAll();
                      initTextMode();
                      commandInput.setText("");
                      break;
                    default:
                      commandInput.setText("Either Small, Medium, or Large");
                  }
              }
          }
      });

      add(sizeLabel, BorderLayout.NORTH);
      add(sizeLabel2, BorderLayout.CENTER);
      add(commandInput, BorderLayout.SOUTH);
      

      revalidate();
      repaint();
  }
    
    private void initTextMode() {
      setPreferredSize(new Dimension(BOARD_WIDTH + 150, BOARD_HEIGHT + 150));
      Container parent = getParent();
      while (parent != null && !(parent instanceof JFrame)) {
          parent = parent.getParent();
      }
      if (parent instanceof JFrame) {
          JFrame frame = (JFrame) parent;
          frame.pack();
      }
      setLayout(new BorderLayout());

      JLabel textModeLabel = new JLabel("Enter command (e.g., reveal 0 0, mark 1 2):");
      commandInput = new JTextField();
      commandInput.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
           // if(isInTextView) {
              String command = commandInput.getText();
              updateStatusbar();
              processCommand(command);
              commandInput.setText("");
            //}
          }
      });

      removeMouseListener(getMouseListeners()[0]);
      add(textModeLabel, BorderLayout.NORTH);
      add(commandInput, BorderLayout.SOUTH);

      
      newGame();
      inGame = true;
      isInTextView = true;
      isInMenu = false;
      repaint();
      
  }
    
    private void processCommand(String command) {
      String[] parts = command.trim().split("\\s+");
      if(parts.length == 1) {
        String action = parts[0].toLowerCase();
        switch(action) {
          case "restart":
           newGame();
           inGame = true;
           repaint();
           break;
          case "continue":
            System.out.println("Continue Called");
            if(coinCount >= continueCost)
            {
             inGame = true;
             coinCount--;   
             updateStatusbar();
             repaint();
             }else {
               statusbar.setText("You don't have enough coins. Please type 'restart' to Start Over");
               System.out.println("cannot continue");
             }
            break;
          case "save":
            saveGame("data.csv");
            break;
          case "load":
            loadGame("data.csv");
            }
       
      }else if (parts.length >= 3) {
        String action = parts[0];
        int row = 0, col = 0;
        try {
            row = Integer.parseInt(parts[1]);
            col = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates!");
            //continue;
        }
       
        int index = row * N_COLS + col;
        if (index < 0 || index >= field.length) {
            System.out.println("Coordinates out of bounds!");
            //continue;
        }

        if (action.equalsIgnoreCase("reveal")) {
            System.out.println(row + " " + col);
            revealCell(index);
            repaint();
        }
        else if (action.equalsIgnoreCase("mark")) {
          System.out.println("M: " + row + " " + col);
          
          if (index > MINE_CELL && index < MARKED_MINE_CELL + MARK_FOR_CELL) { // Added index check
            if (index <= COVERED_MINE_CELL) { // If it's not already marked
                if (minesLeft > 0) {
                    if (index == COVERED_TREASURE_CELL) {
                        field[index] = COVERED_TREASURE_CELL + MARK_FOR_CELL;
                        repaint();
                    } else {
                        field[index] += MARK_FOR_CELL;
                    }
                    minesLeft--;
                } else {
                    statusbar.setText("No marks left | Coins: " + coinCount);
                    
                }
            } else {
                minesLeft++;
                if (index == COVERED_TREASURE_CELL + MARK_FOR_CELL) {
                    field[index] = COVERED_TREASURE_CELL; // Restore covered treasure
                } else {
                    field[index] -= MARK_FOR_CELL; // Unmark back to covered mine/number
                }
            }
            
            
            }
            repaint();
        }
      } else if(parts.length > 0){
          statusbar.setText("Please enter command in the format: action row col");
      }
  }
    
    private void loadTestBoard(String filename) {
      try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filename))) {
          List<Integer> tempField = new ArrayList<>();
          String line;
          List<String[]> rows = new ArrayList<>();

          while ((line = br.readLine()) != null) {
              String[] tokens = line.trim().split(",");
              rows.add(tokens);
          }

          HashSet<Integer> usedMineColumns = new HashSet<>();
          HashSet<Integer> usedMineRows = new HashSet<>();
          
          int numOfTreasures = 0;
          int numOfMines = 0;
          N_ROWS = rows.size();
          N_COLS = rows.get(0).length;
          allCells = N_ROWS * N_COLS;
          BOARD_WIDTH = N_COLS * CELL_SIZE + 1;
          BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1;
          field = new int[allCells];
          
          List <Integer> positionsOfMines = new ArrayList<>();
          for (int i = 0; i < N_ROWS; i++) {
              for (int j = 0; j < N_COLS; j++) {
                  int index = i * N_COLS + j;
                  int val = Integer.parseInt(rows.get(i)[j]);
                  switch (val) {
                      case 0: 
                        field[index] = COVER_FOR_CELL;
                        break;
                      case 1:  
                        field[index] = COVERED_MINE_CELL;
                        positionsOfMines.add(index);
                        
                        if(!usedMineColumns.contains(j)) {
                          usedMineColumns.add(j);
                        }
                        
                        if(!usedMineRows.contains(i)) {
                          usedMineRows.add(i);
                        }
                        
                        
                        
                        numOfMines++;
                        break;
                      case 2: 
                        field[index] = COVERED_TREASURE_CELL;
                        numOfTreasures++;
                        break;
                      default: 
                        field[index] = COVER_FOR_CELL;
                        break;
                  }
              }
          }
          // Update neighbor cells for mines
          for (int i = 0; i < allCells; i++) {
              if (field[i] == COVERED_MINE_CELL) {
                  updateNeighbors(i);
              }
          }

         minesLeft = (int) java.util.Arrays.stream(field)
              .filter(f -> f == COVERED_MINE_CELL)
              .count();
          coinCount = 0;
          continueCost = 1;
          inGame = true;
          isInMenu = false;
          showGameOverOverlay = false;
          
         if( numOfMines >= 8) {
        	 int firstColumn = positionsOfMines.get(0) % N_COLS;
        	 boolean allInSameColumn = true;
        	 for(int i=0; i<Math.min(8, positionsOfMines.size());i++) {
        		 if(positionsOfMines.get(i)% N_COLS != firstColumn) {
        			 allInSameColumn = false;
        		 }
        		 
        	 }
        	 
        	 if (allInSameColumn) {
        		 firstEightInOneColumn  = true;
        		 //redistributes mines 
        		redistributeMines(positionsOfMines,firstColumn);
        		 
        		 
        	 }
        	 
         }
          
          
          
          
          
          
          
          if(numOfTreasures <= 9) {
            noMoreThanNineTreasures = true;
          }
          
          for(int i = 0; i < N_COLS; i++) {
            if(usedMineColumns.contains(i) && usedMineRows.contains(i) && numOfMines > 8) {
              noFirstEightAdjacent = true;
            }
          }
          
          updateStatusbar();

          Container parent = getParent();
          while (parent != null && !(parent instanceof JFrame)) {
              parent = parent.getParent();
          }
          if (parent instanceof JFrame) {
              ((JFrame) parent).pack();
          }

          repaint();
          System.out.println("Test board loaded.");
          isInTestBoard = true;
      } catch (Exception e) {
          System.out.println("Failed to load test board: " + e.getMessage());
          statusbar.setText("Failed to load test board.");
      }
  }

    
    
    
    
    private void redistributeMines(List<Integer> positionsOfMines, int mineLoadedColumn) {
    	
	Random random  = new Random();
	
	int minesToMove = Math.min(4, positionsOfMines.size()/2);
	
	HashSet<Integer> usedPositions = new HashSet<>(positionsOfMines);
	
	
	
		
	}

	private void newGame() {
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

        // Efficient Mine Placement
        List<Integer> possibleMinePositions = new ArrayList<>();
        for (int i = 0; i < allCells; i++) {
            possibleMinePositions.add(i);
        }
        Collections.shuffle(possibleMinePositions);

        for (int i = 0; i < N_MINES; i++) {
            int minePosition = possibleMinePositions.get(i);
            field[minePosition] = COVERED_MINE_CELL;
            updateNeighbors(minePosition);
        }
       

        // Efficient Chest Placement (after mines)
        List<Integer> possibleChestPositions = new ArrayList<>();
        for (int i = 0; i < allCells; i++) {
            if (field[i] == COVER_FOR_CELL) { // Only consider remaining covered cells
                possibleChestPositions.add(i);
            }
        }
        Collections.shuffle(possibleChestPositions);

        int chestsPlaced = 0;
        for (int i = 0; i < Math.min(INITIAL_CHESTS, possibleChestPositions.size()); i++) {
            int chestPosition = possibleChestPositions.get(i);
            field[chestPosition] = COVERED_TREASURE_CELL;
            chestsPlaced++;
            System.out.println("Chests Placed: " + chestsPlaced);
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
              inGame = false; // Set inGame to false when a mine is revealed
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
      GraphicsHandler graphicHandler = new GraphicsHandler(g);
      if(isInTextView) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int y = 25;
        
        int rowNumberWidth = String.valueOf(N_ROWS - 1).length() + 1; // +1 for a trailing space
        String rowNumberFormat = "%-" + rowNumberWidth + "d";
        String initialSpacing = " ".repeat(rowNumberWidth);
        
        StringBuilder colNumbers = new StringBuilder(initialSpacing); // Initial spacing for row numbers
        for (int j = 0; j < N_COLS; j++) {
          if(j < 10) {
            colNumbers.append(String.format("%-2d", j));
          }else {
            colNumbers.append(String.format("%-3d", j));
          }
        }
        g.drawString(colNumbers.toString(), 17, 30);
        y += 20; // Move down after column numbers
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        for (int i = 0; i < N_ROWS; i++) {
          StringBuilder rowStr = new StringBuilder(String.format(rowNumberFormat, i));
         
            for (int j = 0; j < N_COLS; j++) {
              int index = i * N_COLS + j;
              int cellValue = field[index];

              if (cellValue >= MARKED_MINE_CELL) {
                  rowStr.append("M "); // Marked Mine
              } else if (cellValue == COVERED_MINE_CELL) {
                  rowStr.append(". "); // Covered Mine (Unmarked)
              } else if (cellValue >= COVERED_TREASURE_CELL + MARK_FOR_CELL) {
                  rowStr.append("M "); // Marked Treasure
              } else if (cellValue == COVERED_TREASURE_CELL) {
                  rowStr.append(". "); // Covered Treasure (Unmarked)
              } else if (cellValue >= COVER_FOR_CELL && cellValue < COVERED_TREASURE_CELL) {
                  rowStr.append(". "); // Other Covered (Empty or Numbered, Unmarked)
              } else if (cellValue == MINE_CELL) {
                  rowStr.append("* "); // Revealed Mine
              } else if (cellValue == TREASURE_CELL) {
                  rowStr.append("$ "); // Revealed Treasure
              } else if (cellValue == EMPTY_CELL) {
                  rowStr.append("  "); // Revealed Empty
              } else if (cellValue > EMPTY_CELL && cellValue < MINE_CELL) {
                  rowStr.append(cellValue).append(" "); // Revealed Number
              }
              
          }
            g.drawString(rowStr.toString(), 20, y);
            y += 20;
        }
      }else if(!isInMenu) {
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
        //Save Button
        graphicHandler.createButton(getWidth(), getHeight(), 45, 20, 45, 20, - getWidth() + 100, getHeight() - 12, getWidth() - 10, getHeight() - 45, "Save", Color.gray);
        //Load Button
        graphicHandler.createButton(getWidth(), getHeight(), 45, 20, 45, 20, getWidth() - 20, getHeight() - 12,  -getWidth() + 110, getHeight() - 45, "Load", Color.gray);

        if (uncover == 0 && inGame) {
            inGame = false; // Game won
            showGameOverOverlay = true; 
            statusbar.setText("Game won! Coins: " + coinCount);
            System.out.println("GAME OVER: UI Handled - Win");
        } else if (!inGame) {
            showGameOverOverlay = true;
            statusbar.setText("GAME OVER! You have " + coinCount + " coins");
            System.out.println("GAME OVER: UI Handled - Loss");
        }

        
        if (showGameOverOverlay) {
            graphicHandler.setUpGameOverScreen(getWidth(), getHeight());

            graphicHandler.createText(getWidth(), getHeight(), 150, 250, 20, 1, 150, 150, "Game Over", Color.red);
            
            graphicHandler.createText(getWidth(), getHeight(), 150, 200, 12, 0, 150, 150, "You Have " + coinCount + " Coins", Color.white);

            if (coinCount > 0) {
                graphicHandler.createText(getWidth(), getHeight(), 150, 165, 10, 0, 150, 150, "Use " + continueCost + " Coin to Continue?", Color.white);
    
                //continue button
                graphicHandler.createButton(getWidth(), getHeight(), 100, 25, 100, 125, 100, 175, 100, 25, "Continue", Color.gray);
            }

            //restartButton
            graphicHandler.createButton(getWidth(), getHeight(), 100, 25, 100, 125, 100, 230, 100, 80, "Restart", Color.gray);
        }
      }else {
        if(isInMainmenu) {
          
          graphicHandler.createText(getWidth(), getHeight(), 150, 250, 24, 1, 150, 150, "MINESWEEPER+" , Color.red);
          
          //GUI Button
          graphicHandler.createButton(getWidth(), getHeight(), 150, 25, 100, 125, 100, 230, 150, 80, "Graphic Mode", Color.gray);
          
          //Text Mode Button
          graphicHandler.createButton(getWidth(), getHeight(), 150, 25, 100, 180, 100, 230, 150, 25, "Text Mode", Color.gray);
          
          //Load Button
          graphicHandler.createButton(getWidth(), getHeight(), 72, 25, 175, 70, 100, 230, 150, 135, "Load", Color.gray);
          
          //Test Mode Button
          graphicHandler.createButton(getWidth(), getHeight(), 72, 25, 23, 70, 100, 230, -6, 135, "Test Mode", Color.gray);
        }else if(isInGUISize) {
          super.paintComponent(g); //clears
          
          //small field button
          graphicHandler.createButton(getWidth(), getHeight(), 150, 25, 100, 285, 100, 180, 150, -125, "Small", Color.gray);
          
          //medium field button
          graphicHandler.createButton(getWidth(), getHeight(), 150, 25, 100, 230, 100, 230, 150, -25, "Medium", Color.gray);
          
          //large field button
          graphicHandler.createButton(getWidth(), getHeight(), 150, 25, 100, 175, 100, 230 + (110 - 30), 150, 110, "Large", Color.gray);
        }
      }
      
      if(isInTestBoard && inGame) {
        if(firstEightInOneColumn) {
          graphicHandler.createText(getWidth(), getHeight(), -110, 150, 10, 0, 0, 0, "One Mine in each row" , Color.GREEN);
          graphicHandler.createText(getWidth(), getHeight(), -120, 130, 9, 0, 0, 0, "and column and Adjacent" , Color.GREEN);
        }else {
          graphicHandler.createText(getWidth(), getHeight(), -110, 150, 10, 0, 0, 0, "One Mine in each row" , Color.red);
          graphicHandler.createText(getWidth(), getHeight(), -120, 130, 9, 0, 0, 0, "and column and Adjacent" , Color.red);
        }
        
        if(noFirstEightAdjacent) {
          graphicHandler.createText(getWidth(), getHeight(), -110, 70, 10, 0, 0, 0, "One Mine with row " , Color.green);
          graphicHandler.createText(getWidth(), getHeight(), -120, 50, 9, 0, 0, 0, "and column the same" , Color.green);
        }else {
          graphicHandler.createText(getWidth(), getHeight(), -110, 70, 10, 0, 0, 0, "One Mine with row " , Color.red);
          graphicHandler.createText(getWidth(), getHeight(), -120, 50, 9, 0, 0, 0, "and column the same" , Color.red);
        }
       
        if(ninthMineAdjacent) {
          graphicHandler.createText(getWidth(), getHeight(), -110, -10, 10, 0, 0, 0, "Ninth mine adjacent" , Color.green);
          graphicHandler.createText(getWidth(), getHeight(), -120, -30, 9, 0, 0, 0, "to the 8th" , Color.green);
        }else {
          graphicHandler.createText(getWidth(), getHeight(), -110, -10, 10, 0, 0, 0, "Ninth mine adjacent" , Color.red);
          graphicHandler.createText(getWidth(), getHeight(), -120, -30, 9, 0, 0, 0, "to the 8th" , Color.red);
        }
        
        if(tenthMineIsolated) {
          graphicHandler.createText(getWidth(), getHeight(), -110, -80, 10, 0, 0, 0, "Tenth mine isolated from" , Color.green);
          graphicHandler.createText(getWidth(), getHeight(), -120, -100, 9, 0, 0, 0, "ninth and tenth mines" , Color.green);
        }else {
          graphicHandler.createText(getWidth(), getHeight(), -110, -80, 10, 0, 0, 0, "Tenth mine isolated from" , Color.red);
          graphicHandler.createText(getWidth(), getHeight(), -120, -100, 9, 0, 0, 0, "ninth and tenth mines" , Color.red);
        }
        
        if(!noMoreThanNineTreasures) {
          graphicHandler.createText(getWidth(), getHeight(), 110, -80, 10, 0, 0, 0, "No More than nine" , Color.red);
          graphicHandler.createText(getWidth(), getHeight(), 120, -100, 9, 0, 0, 0, "treasures on the board" , Color.red);
        }else {
          graphicHandler.createText(getWidth(), getHeight(), 110, -80, 10, 0, 0, 0, "No More than nine" , Color.GREEN);
          graphicHandler.createText(getWidth(), getHeight(), 120, -100, 9, 0, 0, 0, "treasures on the board" , Color.green);
        }
       
      }
    }
    
    private void saveGame(String filename) {
      try (ObjectOutputStream out = new ObjectOutputStream(new java.io.FileOutputStream(filename))) {
          GameState state = new GameState();
          state.field = this.field;
          state.minesLeft = this.minesLeft;
          state.coinCount = this.coinCount;
          state.N_ROWS = N_ROWS;
          state.N_COLS = N_COLS;
          state.N_MINES = N_MINES;
          state.inGame = this.inGame;
          out.writeObject(state);
          System.out.println("Game saved.");
      } catch (Exception e) {
          System.out.println("Error saving game: " + e.getMessage());
          statusbar.setText("Error Saving Game, please try again");
      }
    }
    
    private void loadGame(String filename) {
      try (ObjectInputStream in = new ObjectInputStream(new java.io.FileInputStream(filename))) {
          GameState state = (GameState) in.readObject();
          this.field = state.field;
          this.minesLeft = state.minesLeft;
          this.coinCount = state.coinCount;
          N_ROWS = state.N_ROWS;
          N_COLS = state.N_COLS;
          N_MINES = state.N_MINES;
          this.inGame = state.inGame;
          allCells = N_ROWS * N_COLS;
          BOARD_WIDTH = N_COLS * CELL_SIZE + 1;
          BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1;
          if(isInTextView) {
            setPreferredSize(new Dimension(BOARD_WIDTH + 150, BOARD_HEIGHT + 150));
          }else {
            setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + 40));
          }
          
          Container parent = getParent();
          while (parent != null && !(parent instanceof JFrame)) {
              parent = parent.getParent();
          }
          if (parent instanceof JFrame) {
              JFrame frame = (JFrame) parent;
              frame.pack();
          }
          showGameOverOverlay = !inGame; 
          statusbar.setText("Mines: " + minesLeft + " | Coins: " + coinCount);
          System.out.println("Game loaded.");
          repaint();
      } catch (Exception e) {
          System.out.println("Error loading game: " + e.getMessage());
      }
  }
    
    private void setValues(int row, int col, int mines, int chests) {
      N_ROWS = row;
      N_COLS = col;
      N_MINES = mines;
      INITIAL_CHESTS = chests;
      BOARD_WIDTH = N_COLS * CELL_SIZE + 1;
      BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1;
      if(!isInTextView) {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + 40));
      }else {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
      }
      
      Container parent = getParent();
      while (parent != null && !(parent instanceof JFrame)) {
          parent = parent.getParent();
      }
      if (parent instanceof JFrame) {
          JFrame frame = (JFrame) parent;
          frame.pack();
      }
    }
    
    private void handleClick(int buttonWidth, int buttonHeight, int posX, int posY, int x, int y, boolean condition, Runnable func) {

   //Calculate the button's top-left corner using the same logic as createButton
      int buttonX = (getWidth() - posX) / 2;
      int buttonY = (getHeight() + posY) / 2;

      // Calculate the button's bottom-right corner
      int buttonBottomX = buttonX + buttonWidth;
      int buttonBottomY = buttonY + buttonHeight;

      // Print the calculated rectangle
      System.out.println(getHeight());
      System.out.println("Rect: (" + buttonX + ", " + buttonY + ") - (" + buttonBottomX + ", " + buttonBottomY + ")");
      System.out.println("Pos: " + x + ", " + y); 

      // Your original click detection logic
      if (x >= buttonX && x <= buttonBottomX &&
          y >= buttonY && y <= buttonBottomY && condition) {
          func.run();
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
                  handleClick(100, 25, 100, 25, x, y, coinCount > 0, () -> {
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
                  });

                  handleClick(100, 25, 100, 80, x, y, true, () -> {
                    System.out.println("Restart button clicked!");
                    newGame();
                    showGameOverOverlay = false;
                    repaint();
                  });
              }else if(!inGame && isInMainmenu) {
              //Graphic mode
                handleClick(150, 25, 150, 80, x, y, true, () -> {
                  isInMainmenu = false;
                  isInGUISize = true;
                  repaint();
                });
                //TextMode
                handleClick(150, 25, 150, 25, x, y, true, () -> {
                  initTextSizeMenu();
                });
                //Load Game
                handleClick(72, 25, 150, 135, x, y, true, () -> {
                  System.out.println("Load Game button clicked!");
                  
                  inGame = true;
                  isInMainmenu = false;
                  isInMenu = false;
                  loadGame("data.csv");
                  initLoadBoard();
                  repaint();
                });
                //TestMode
                handleClick(72, 25, 6, 135, x, y, true, () -> {
                  System.out.println("Test Mode button clicked!");
                  loadTestBoard("test_board_2.csv");
                  initLoadBoard();
                });
              }else if(!inGame && isInGUISize) {
                AtomicBoolean clickHandled = new AtomicBoolean(false);
                
                if(!clickHandled.get()) {
                  handleClick(150, 25, 150, -125, x, y, true, () -> {
                    System.out.println("small");
                    setValues(8, 8, 10, 4);
                    isInMenu = false;
                    isInGUISize = false;
                    initBoard();
                    repaint();
                    clickHandled.set(true);
                    return;
                  });
                }
                
                
                
                //medium mode
                if(!clickHandled.get()) {
                  handleClick(150, 25, 150, -25, x, y, true, () -> {
                    System.out.println("medium");
                    setValues(16, 16, 40, 10);
                    isInMenu = false;
                    isInGUISize = false;
                    initBoard();
                    repaint();
                  });
                }
                
                
                //large mode
                handleClick(150, 25, 150, 110, x, y, true, () -> {
                  System.out.println("Large Button Clicked");
                  setValues(16, 30, 99, 25);
                  isInMenu = false;
                  isInGUISize = false;
                  initBoard();
                  repaint();
                });
              }
              else if (inGame) {
                handleClick(45, 20, getWidth() - 10, getHeight() - 45, x, y, true, () -> {
                  System.out.println("Save Button Clicked");
                  saveGame("data.csv"); // Call your save game function here
              });
                
             // Load Button Click Handling
                handleClick(45, 20, -getWidth() + 110, getHeight() - 45, x, y, true, () -> {
                    System.out.println("Load Button Clicked");
                    inGame = true;
                    isInMainmenu = false;
                    isInMenu = false;
                    loadGame("data.csv");
                    initLoadBoard();
                    repaint();
                });
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
package com.zetcode;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class GraphicsHandler {

   private Graphics g;
   private Board board;
  
   public GraphicsHandler(Graphics passedG) {
     this.g = passedG;
   }
   
   public void setUpGameOverScreen(int width, int height) {
     g.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black background
     g.fillRect((width - 150) / 2, (height - 150) / 2, 150, 150);
   }
   
   public void createText(int width, int height, int width1, int height1, int fontSize, int fontStyle, int posx, int posy, String passedText, Color color) {
     String text = passedText;
     g.setColor(color);
     Font font = new Font("Arial", fontStyle, fontSize);
     g.setFont(font);
     FontMetrics fm = g.getFontMetrics(font);
     g.drawString(text,
             (width - width1) / 2 + (posx - fm.stringWidth(text)) / 2,
             (height - height1) / 2 + (posy - fm.getAscent()) / 2 + fm.getAscent());
   }
   
   public void createButton(int screenWidth, int screenHeight, int rectWidth, int rectHeight, int textWidth, int textHeight, int textPosX, int textPosY, int rectPosX, int rectPosY, String title, Color color) {
     g.setColor(color);
     g.fillRect((screenWidth - rectPosX) / 2, (screenHeight + rectPosY) / 2, rectWidth, rectHeight);
     createText(screenWidth, screenHeight, textWidth, textHeight, 12, 0, textPosX, textPosY, title, Color.white);
 
   }
   
   
  
}

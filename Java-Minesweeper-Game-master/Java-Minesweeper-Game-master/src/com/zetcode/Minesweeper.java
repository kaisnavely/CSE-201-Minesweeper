package com.zetcode;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Minesweeper extends JFrame {

    private final JLabel statusbar;

    public Minesweeper() {
        statusbar = new JLabel(""); //creates a new JLabel and assigns it to status bar
        initUI();
    }

    private void initUI() {
        add(statusbar, BorderLayout.SOUTH);
        add(new Board(statusbar));

        setTitle("Minesweeper+");
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Minesweeper ex = new Minesweeper();
            ex.setVisible(true);
        });
    }
}

package com.demsasha.classes_non_core;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
 * ImprovedKeyAdapter sets input data restrictions.
 * */

public class ImprovedKeyAdapter extends KeyAdapter {
    private int maxValue;
    private int minValue;

    public ImprovedKeyAdapter(int maxValue, int minValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    /*
     * allows input only numbers,"BACK_SPACE","DELETE"
     * */
    @Override
    public void keyTyped(KeyEvent e) {
        if ((e.getKeyChar() > 47 && e.getKeyChar() < 58) ||
                e.getKeyChar() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_DELETE) {
            JTextField textField = (JTextField) e.getSource();
            if (textField.getText().equals("0")) {
                textField.setText("");
            }
            super.keyTyped(e);
        } else {
            e.consume();
        }
    }

    /*
     * forbids input numbers greater then maxValue and less minValue and  prevents empty field
     * */
    @Override
    public void keyReleased(KeyEvent e) {
        JTextField textField = (JTextField) e.getSource();
        if (textField.getText().equals("")) {
            textField.setText(""+minValue);
        } else {
            if (Integer.parseInt(textField.getText()) > maxValue) {
                textField.setText(""+maxValue);
            } else {
                if (Integer.parseInt(textField.getText()) < minValue) {
                    textField.setText(""+minValue);
                }
            }
        }
    }
}

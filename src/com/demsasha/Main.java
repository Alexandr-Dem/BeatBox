package com.demsasha;

import com.demsasha.network.client.ClientPanel;

import javax.swing.*;
/*
 *The starting point for launching the BeatBox program. Creates three main objects of the program:
 *
 *      animationPanel - Animation drawing;
 *      workZone - user interaction;
 *      clientPanel - chat client for messaging and collaborative track development
 *
 * Also creates and initializes the main window (JFrame).
 * @author Alexander Dementev
 * */

public class Main extends JFrame {
    private Animation animationPanel = new Animation(this); //Animation drawing
    private WorkZone workZone = new WorkZone(animationPanel); //User interaction
    private ClientPanel clientPanel = new ClientPanel(workZone);//chat client for messaging and collaborative track development

    public static void main(String[] args) {
        new Main();
    }

    private Main() {
        setIconImage(new ImageIcon(Main.class.getResource("resources/images/logo.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("BeatBox");
        setResizable(false);
        setLocation(100, 100);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        add(clientPanel);
        add(workZone);
        animationPanel.setVisible(false);
        add(animationPanel);
        pack();
        setVisible(true);
        clientPanel.setFocus();
    }

    /*
     * Called from the class and designed to start displaying the animation.
     */
    void beginningAnimation() {
        animationPanel.setPreferredSize(getPreferredSize());
        workZone.setVisible(false);
        clientPanel.setVisible(false);
        animationPanel.setVisible(true);
    }

    /**
     * Called from the class and designed to stop displaying the animation.
     */
    void stopAnimation() {
        animationPanel.setVisible(false);
        workZone.setVisible(true);
        clientPanel.setVisible(true);
        workZone.stopPlayingSong();
    }

}


package com.demsasha;


import javax.imageio.ImageIO;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.ShortMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/*
 * A class instance is attached by the listener to the player, which dispatches events when each note is played.
 * When starting the animation, the program window is blocked by a panel, which has a color - a blue gradient
 * When an event occurs, a circle is displayed on a blue background. The position, size and color of the circle are randomly selected.
 * When happens on Circle it will disappear.
 * */
public class Animation extends JPanel implements ControllerEventListener {
    private Main main;
    private Image closeImg;
    private ArrayList<Circle> circleList = new ArrayList<Circle>();

    Animation(Main main) {
        this.main = main;
        setBackground(Color.BLUE);
        ImageIcon closeImgIcon = new ImageIcon(Main.class.getResource("resources/images/close.png"));
        closeImg = closeImgIcon.getImage();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Circle deleteCircle = null;
                for (int i = circleList.size()-1; i >= 0; i--) {
                    int x1 = circleList.get(i).location[0];
                    if (x1 < e.getX()) {
                        int size = circleList.get(i).size;
                        int x2 = x1 + size;
                        if (x2 > e.getX()) {
                            int y1 = circleList.get(i).location[1];
                            int y2 = y1 + size;
                            if ((y1 < e.getY() && y2 > e.getY())) {
                                deleteCircle = circleList.get(i);
                                break;
                            }
                        }
                    }
                }
                if (deleteCircle != null) {
                    circleList.remove(deleteCircle);
                    repaint();
                }
            }


            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getX() < getWidth() && e.getX() > getWidth() - 20) {
                    if (e.getY() < 20) {
                        System.out.println("Close");
                        Animation.this.stop();
                    }
                }

            }
        });
    }

    public void start() {
        main.beginningAnimation();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    Point e = MouseInfo.getPointerInfo().getLocation();
//                    System.out.println(e);
//                    Circle deleteCircle = null;
//                    for (int i = circleList.size() - 1; i >= 0; i--) {
//                        int x1 = circleList.get(i).location[0];
//                        if (x1 < e.getX()) {
//                            int size = circleList.get(i).size;
//                            int x2 = x1 + size;
//                            if (x2 > e.getX()) {
//                                int y1 = circleList.get(i).location[1];
//                                int y2 = y1 + size;
//                                if ((y1 < e.getY() && y2 > e.getY())) {
//                                    deleteCircle = circleList.get(i);
//                                    System.out.println("Gjgjgd");
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    if (deleteCircle != null) {
//                        circleList.remove(deleteCircle);
//                        repaint();
//                    }
//                }
//            }
//        }).start();
        System.out.println("Animation START!");
    }

    private void stop() {
        main.stopAnimation();
        circleList.clear();
    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gradient;

        gradient = new GradientPaint(0, 0, new Color(0, 0, 150), 700, 700, new Color(0, 0, 40));
        g2D.setPaint(gradient);
        g2D.fillRect(0, 0, getWidth(), getHeight());


        circleList.add(new Circle());
        for (Circle circle : circleList) {
            if (circle.setTransparency()) {
                int x = circle.location[0];
                int y = circle.location[1];
                int size = circle.size;
                int grSize = size / 8;
                Color firstColor = circle.firstColor;
                Color secondColor = circle.secondColor;
                gradient = new GradientPaint(x + grSize, y + grSize, firstColor, x + size - grSize,
                        y + size - grSize, secondColor);
                g2D.setPaint(gradient);
                g2D.fillOval(x, y, size, size);
            }
        }
        while (circleList.size() > 172) {
            circleList.remove(0);
        }
        g.drawImage(closeImg, getWidth() - 20, 0, this);
    }

    @Override
    public void controlChange(ShortMessage event) {
        repaint();
    }

    /*
     * A class that represents an object of a circle. Instances of this class are created and saved in the Animation class.
     * Each instance has:
     * your color is a gradient consisting of two random numbers.
     * size - a random integer in the range from 5 to 125
     * location - an array of two integers (x and y)
     * life - an integer equal to 255, which decreases each time when drawing a circle, if
     * if the value is less than 125, then the circle will gradually become transparent until it completely disappears.
     * And an integer variable is responsible for transparency - transparency
     * */
    private class Circle {
        int size;
        int[] location;
        int transparency = 255;
        Color firstColor, secondColor;
        int life = 255;

        Circle() {
            firstColor = createColor();
            secondColor = createColor();
            size = createSize();
            location = createLocation();
        }

        boolean setTransparency() {
            if (--life > 125) {
                return true;
            }
            transparency = transparency - 6;
            if (transparency > 0) {
                firstColor = new Color(firstColor.getRed(), firstColor.getGreen(), firstColor.getBlue(), transparency);
                secondColor = new Color(secondColor.getRed(), secondColor.getGreen(), secondColor.getBlue(), transparency);
                return true;
            } else {
                return false;
            }
        }

        private Color createColor() {
            return new Color(new Random().nextInt(256), new Random().nextInt(256),
                    new Random().nextInt(256));
        }

        private int createSize() {
            return new Random().nextInt(120) + 5;
        }

        private int[] createLocation() {
            int x = new Random().nextInt(Animation.this.getWidth() - size);
            int y = new Random().nextInt(Animation.this.getHeight() - size);
            return new int[]{x, y};
        }
    }
}


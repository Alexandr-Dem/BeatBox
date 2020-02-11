package com.demsasha.song;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
/*
* The panel is designed so that the user can choose at what tact the sound will start and at what end
 * The user can select either one tact or several linked tacts at once.
 * If one tact is selected, the sound will end playback on the next measure
 * If the several linked tacts is selected, the sound will start on the first selected tact and end playback
 *  on the last selected tact
* */
class TicksPanel extends JPanel {
    ArrayList<OneTick> panels = new ArrayList<OneTick>();
    private int countTicks;
    private GridBagConstraints c;

    TicksPanel(int count, LineElements lineElements) {
        countTicks = count;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.BLACK));
        setPreferredSize(new Dimension(countTicks*20,27));
        setBackground(Color.WHITE);
        c = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1,0,1,0), 0, 0);
        for (int i = 0; i < countTicks; i++) {
            OneTick p = new OneTick();
            panels.add(p);
            add(p,c);
        }
    }

    void setCountTicks(int countTicks) {
        if (this.countTicks > countTicks) {
            this.countTicks = countTicks;
            setPreferredSize(new Dimension(20*countTicks,27));
            change(panels.get(panels.size() - 1));
            this.remove(panels.get(panels.size() - 1));
            panels.remove(panels.size() - 1);
            return;
        }
        if (this.countTicks < countTicks) {
            this.countTicks = countTicks;
            setPreferredSize(new Dimension(20 * countTicks, 27));
            OneTick p = new OneTick();
            panels.add(p);
            add(p, c);
        }
    }



    private void dragged(OneTick panel, int x) {
        if (panels.indexOf(panel) + 1 >= panels.size()) {
            return;
        } else {
            panel.setValue(OneTick.START_VALUE);
        }
        for (int i = panels.indexOf(panel); i < panels.size(); i++) {
            OneTick p = panels.get(i);
            if ((p.getX() < x) && (p.getX() + p.getWidth() > x)) {
                panel.too = p == panel;
                OneTick nextPanel;
                if (p.getValue() == OneTick.EMPTY_VALUE || p.getValue() == OneTick.FULL_VALUE) {
                    p.setValue(OneTick.END_VALUE);
                    if (panels.get(i - 1).getValue() == OneTick.END_VALUE) {
                        panels.get(i - 1).setValue(OneTick.CONTINUE_VALUE);
                    }
                    return;
                }
                if (p.getValue() == OneTick.CONTINUE_VALUE) {
                    p.setValue(OneTick.END_VALUE);
                    nextPanel = panels.get(panels.indexOf(p) + 1);
                    nextPanel.setValue(OneTick.EMPTY_VALUE);
                    return;
                }
                if (p.getValue() == OneTick.START_VALUE) {
                    nextPanel = panels.get(panels.indexOf(p) + 1);
                    if (nextPanel.getValue() == OneTick.END_VALUE) {
                        p.setValue(OneTick.EMPTY_VALUE);
                        nextPanel.setValue(OneTick.EMPTY_VALUE);
                        return;
                    }
                    if (nextPanel.getValue() == OneTick.CONTINUE_VALUE){
                        change(p);
                    }
                }
            }
        }
    }

    private void change(OneTick panel) {
        OneTick panelNext;
        OneTick panelPrevious;
        switch (panel.getValue()) {
            case OneTick.START_VALUE:
                panel.setValue(OneTick.EMPTY_VALUE);
                panelNext = panels.get(panels.indexOf(panel)+1);
                if (panelNext.getValue() == OneTick.END_VALUE) {
                    panelNext.setValue(OneTick.FULL_VALUE);
                } else {
                    panelNext.setValue(OneTick.START_VALUE);
                }
                break;
            case OneTick.CONTINUE_VALUE:
                panel.setValue(OneTick.EMPTY_VALUE);
                panelNext = panels.get(panels.indexOf(panel)+1);
                panelPrevious = panels.get(panels.indexOf(panel)-1);
                if (panelNext.getValue() == OneTick.END_VALUE) {
                    panelNext.setValue(OneTick.FULL_VALUE);
                } else {
                    panelNext.setValue(OneTick.START_VALUE);
                }
                if (panelPrevious.getValue() == OneTick.START_VALUE) {
                    panelPrevious.setValue(OneTick.FULL_VALUE);
                } else {
                    panelPrevious.setValue(OneTick.END_VALUE);
                }
                break;
            case OneTick.END_VALUE:
                panel.setValue(OneTick.EMPTY_VALUE);
                panelPrevious = panels.get(panels.indexOf(panel)-1);
                if (panelPrevious.getValue() == OneTick.START_VALUE) {
                    panelPrevious.setValue(OneTick.FULL_VALUE);
                } else {
                    if (panelPrevious.getValue() == OneTick.CONTINUE_VALUE){
                        panelPrevious.setValue(OneTick.END_VALUE);
                    }
                }

                break;
            case OneTick.EMPTY_VALUE:
                panel.setValue(OneTick.FULL_VALUE);
                break;
            case OneTick.FULL_VALUE:
                panel.setValue(OneTick.EMPTY_VALUE);
                break;
        }
        if (panel.getValue() == OneTick.START_VALUE) {
            panel.setValue(OneTick.EMPTY_VALUE);
            panels.get(panels.indexOf(panel)+1).setValue(OneTick.START_VALUE);
        }
    }

    public class OneTick extends JPanel {
        private int value = EMPTY_VALUE;
        static final int START_VALUE = 1;
        static final int CONTINUE_VALUE = 2;
        static final int END_VALUE = 3;
        static final int EMPTY_VALUE = 4;
        static final int FULL_VALUE = 5;
        boolean too = false;

        OneTick() {
            setPreferredSize(new Dimension(20,23));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    change(OneTick.this);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (too) {
                        setValue(EMPTY_VALUE);
                        too = false;
                    }
                }

            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (value == START_VALUE || value == EMPTY_VALUE || value == FULL_VALUE) {
                        int x = e.getX() + getX();
                        if (x > getX()) {
                            dragged(OneTick.this,x);
                        }
                    } else {
                        change(OneTick.this);
                    }
                }
            });
        }

        int getValue() {
            return value;
        }

        void setValue(int value) {
            this.value = value;
            repaint();
            updateUI();
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2D.setColor(Color.WHITE);
            g2D.fillRect(0, 0, getWidth(), getHeight());
            if (value == EMPTY_VALUE) {
                g2D.setColor(Color.LIGHT_GRAY);
                g2D.fillOval(2, 0, getWidth()-4, getWidth()-4);
                g2D.setColor(Color.BLUE);
                g2D.drawOval(2, 0, getWidth()-4, getWidth()-4);
            } else {
                g2D.setColor(Color.BLUE);
                g2D.fillOval(2, 0, getWidth()-4, getWidth()-4);
                g2D.setColor(Color.DARK_GRAY);
                g2D.drawOval(2, 0, getWidth()-4, getWidth()-4);
            }
            if (value == CONTINUE_VALUE) {
                g2D.setColor(Color.BLUE);
                g2D.drawLine(0,getHeight()-2, getWidth(),getHeight()-2);
                g2D.drawLine(getWidth()/2,getHeight()/2,getWidth()/2,getHeight()-2);

                return;
            }
            if (value == START_VALUE) {
                g2D.setColor(Color.BLUE);
                g2D.drawLine(getWidth()/2,getHeight()-2,getWidth(),getHeight()-2);
                g2D.drawLine(getWidth()/2,getHeight()/2,getWidth()/2,getHeight()-2);
                return;
            }
            if (value == END_VALUE) {
                g2D.setColor(Color.BLUE);
                g2D.drawLine(0,getHeight()-2,getWidth()/2,getHeight()-2);
                g2D.drawLine(getWidth()/2,getHeight()/2,getWidth()/2,getHeight()-2);
            }

        }
    }
}

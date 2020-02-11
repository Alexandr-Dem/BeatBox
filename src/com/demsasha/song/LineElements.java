package com.demsasha.song;

import com.demsasha.WorkZone;
import com.demsasha.classes_non_core.ImprovedKeyAdapter;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Instances of this class are a track of graphic elements for interaction with the user.
 * Thanks to the components included in an instance of this class, the user can select the desired instrument, the desired note,
 * volume of a note, pre-listen to the sound of a note, enable or disable a track,
 * specify in which measures the note will be played.
 * Each instance is assigned its own channel. In total, 15 such tracks can be created, that is, simultaneously in the program
 * You can play 15 different parallel sounds.
 * */
public class LineElements {
    private WorkZone workZone;
    private ArrayList<String> instruments = new ArrayList<String>();
    private HashMap<String, Integer> allInstruments = new HashMap<String, Integer>();
    private String[][] groups;
    private JPanel instrumentsPanel;
    private JComboBox instrumentsComboBox;
    private JComboBox<String> groupInstrumentsComboBox;
    private JTextField noteTextField = new JTextField(3);
    private JTextField volumeTextField = new JTextField(3);
    private JLabel deleteButton = new JLabel();
    private ArrayList<OneSound> soundsList = new ArrayList<OneSound>();
    private TicksPanel ticksPanel;
    private int countTicks;
    private int chanel;
    private boolean isPlayTrack = true;
    private ImageIcon noteImageIcon;
    private ImageIcon note1ImageIcon;
    private ImageIcon volumeOnImageIcon;
    private ImageIcon volumeOn1ImageIcon;
    private ImageIcon volumeOffImageIcon;
    private ImageIcon volumeOff1ImageIcon;

    /*
     * Constructor accept
     * workZone - an instance of the WorkZone class, so that the object can interact with the main program
     * countTicks - the number of measures that the user can change in the range from 16 to 100
     * chanel - channel of this track
     * */
    public LineElements(WorkZone workZone, int countTicks, int chanel) {
        this.workZone = workZone;
        this.countTicks = countTicks;
        this.chanel = chanel;
        //Panel of selection instruments
        instrumentsPanel = new JPanel();
        instrumentsPanel.setPreferredSize(new Dimension(250, 30));
        instrumentsPanel.setBackground(Color.WHITE);
        //set HashMap allInstruments
        {
            allInstruments.put("Пианино", 0);
            allInstruments.put("Электро пианино", 4);

            allInstruments.put("Гитара", 24);
            allInstruments.put("Электро гитара", 27);
            allInstruments.put("Бас гитара", 33);
            allInstruments.put("Джаз гитара", 36);

            allInstruments.put("Акустический бас", 32);
            allInstruments.put("Щипковый бас", 34);
            allInstruments.put("Синтечисечкий бас", 38);
            allInstruments.put("Слэп-бас 1", 36);
            allInstruments.put("Слэп-бас 2", 37);

            allInstruments.put("Скрипка", 40);
            allInstruments.put("Виолончель", 42);
            allInstruments.put("Контрабас", 43);

            allInstruments.put("Труба", 56);
            allInstruments.put("Тромбон", 57);
            allInstruments.put("Кларнет", 71);
            allInstruments.put("Флейта", 74);

            allInstruments.put("Колокольчик", 9);
            allInstruments.put("Голос", 53);
            allInstruments.put("Фантазия", 88);
            allInstruments.put("Космос", 91);
            allInstruments.put("Ледяной дождь", 96);
            allInstruments.put("Кристал", 98);
            allInstruments.put("Гоблин", 101);
            allInstruments.put("Выстрел", 127);
            allInstruments.put("Лады гитары", 120);

            groups = new String[][]{{"Пианино", "Фортепиано"},
                    {"Электро пианино", "Фортепиано"},
                    {"Гитара", "Гитара"},
                    {"Электро гитара", "Гитара"},
                    {"Бас гитара", "Гитара"},
                    {"Джаз гитара", "Гитара"},
                    {"Акустический бас", "Бас"},
                    {"Щипковый бас", "Бас"},
                    {"Синтечисечкий бас", "Бас"},
                    {"Слэп-бас 1", "Бас"},
                    {"Слэп-бас 2", "Бас"},
                    {"Скрипка", "Струнные"},
                    {"Виолончель", "Струнные"},
                    {"Контрабас", "Струнные"},
                    {"Труба", "Духовые"},
                    {"Тромбон", "Духовые"},
                    {"Кларнет", "Духовые"},
                    {"Флейта", "Духовые"},
                    {"Колокольчик", "Эффекты"},
                    {"Голос", "Эффекты"},
                    {"Фантазия", "Эффекты"},
                    {"Космос", "Эффекты"},
                    {"Ледяной дождь", "Эффекты"},
                    {"Кристал", "Эффекты"},
                    {"Гоблин", "Эффекты"},
                    {"Выстрел", "Эффекты"},
                    {"Лады гитары", "Эффекты"},
            };
        }
        String[] groupInstruments = {"Фортепиано",
                "Гитара",
                "Бас",
                "Струнные",
                "Духовые",
                "Эффекты"};
        groupInstrumentsComboBox = new JComboBox<String>(groupInstruments);
        groupInstrumentsComboBox.setPreferredSize(new Dimension(100, 25));
        groupInstrumentsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                instrumentsPanel.remove(instrumentsComboBox);
                instruments = createInstruments((String) groupInstrumentsComboBox.getSelectedItem());
                instrumentsComboBox = new JComboBox<>(instruments.toArray(new String[instruments.size()]));
                instrumentsComboBox.setPreferredSize(new Dimension(140, 25));
                instrumentsPanel.add(instrumentsComboBox);
                workZone.drawPanel();

            }
        });
        instruments = createInstruments("Фортепиано");
        instrumentsComboBox = new JComboBox<>(instruments.toArray(new String[instruments.size()]));
        instrumentsComboBox.setPreferredSize(new Dimension(140, 25));
        groupInstrumentsComboBox.setSelectedItem("Фортепиано");
        instrumentsComboBox.setSelectedItem("Пианино");
        instrumentsPanel.add(groupInstrumentsComboBox);
        instrumentsPanel.add(instrumentsComboBox);

        noteTextField.setText("50");
        noteTextField.addKeyListener(new ImprovedKeyAdapter(127, 0));

        volumeTextField.setText("80");
        volumeTextField.addKeyListener(new ImprovedKeyAdapter(127, 0));

        ticksPanel = new TicksPanel(countTicks, this);

        ImageIcon delIcon = new ImageIcon(WorkZone.class.getResource("resources/images/delete.png"));
        ImageIcon delIcon1 = new ImageIcon(WorkZone.class.getResource("resources/images/delete1.png"));
        deleteButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        deleteButton.setIcon(delIcon);
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setIcon(delIcon1);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setIcon(delIcon);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (workZone.deletingLine(LineElements.this)) {
                    workZone.drawPanel();
                }
            }
        });
        loadImages();
    }

    private void loadImages() {
        noteImageIcon = new ImageIcon(WorkZone.class.getResource("resources/images/note.png"));
        note1ImageIcon = new ImageIcon(WorkZone.class.getResource("resources/images/note1.png"));
        volumeOnImageIcon = new ImageIcon(WorkZone.class.getResource("resources/images/volumeOn.png"));
        volumeOn1ImageIcon = new ImageIcon(WorkZone.class.getResource("resources/images/volumeOn1.png"));
        volumeOffImageIcon = new ImageIcon(WorkZone.class.getResource("resources/images/volumeOff.png"));
        volumeOff1ImageIcon = new ImageIcon(WorkZone.class.getResource("resources/images/volumeOff1.png"));
    }

    /*
     * Initializes the lineElements and adds its components to the WorkPanel
     * */
    public void drawLineElements(int y, int countTicks) {
        JLabel exampleLabel = new JLabel(noteImageIcon);
        exampleLabel.setBackground(Color.WHITE);
        exampleLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        exampleLabel.setPreferredSize(new Dimension(18, 18));
        exampleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                playExample();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                exampleLabel.setIcon(note1ImageIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exampleLabel.setIcon(noteImageIcon);
            }
        });

        JLabel isPlayTrackLabel = new JLabel();
        if (isPlayTrack) {
            isPlayTrackLabel.setIcon(volumeOnImageIcon);
        } else {
            isPlayTrackLabel.setIcon(volumeOffImageIcon);
        }
        isPlayTrackLabel.setBackground(Color.WHITE);
        isPlayTrackLabel.setPreferredSize(new Dimension(18, 18));
        isPlayTrackLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isPlayTrack = !isPlayTrack;
                if (isPlayTrack) {
                    isPlayTrackLabel.setIcon(volumeOn1ImageIcon);
                } else {
                    isPlayTrackLabel.setIcon(volumeOff1ImageIcon);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (isPlayTrack) {
                    isPlayTrackLabel.setIcon(volumeOn1ImageIcon);
                } else {
                    isPlayTrackLabel.setIcon(volumeOff1ImageIcon);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isPlayTrack) {
                    isPlayTrackLabel.setIcon(volumeOnImageIcon);
                } else {
                    isPlayTrackLabel.setIcon(volumeOffImageIcon);
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, y, 1, 1,
                0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(3, 2, 2, 2), 0, 0);
        workZone.workPanel.add(instrumentsPanel, c);
        workZone.workPanel.add(noteTextField, c);
        workZone.workPanel.add(exampleLabel, c);
        workZone.workPanel.add(volumeTextField, c);
        workZone.workPanel.add(isPlayTrackLabel, c);
        c.insets.left = 1;
        c.insets.right = 1;
        c.gridwidth = countTicks;
        ticksPanel.setCountTicks(countTicks);
        workZone.workPanel.add(ticksPanel, c);
        c.gridwidth = 1;
        c.insets.left = 2;
        c.insets.right = 2;
        workZone.workPanel.add(deleteButton, c);
        instrumentsComboBox.requestFocus(true);
    }

    /*
     * Returns a list of sounds (OneSound). Each sound contains information about the LineElements settings and the tact,
     * in which he needs to play
     * */
    public ArrayList<OneSound> getSounds() {
        soundsList.clear();
        if (isPlayTrack) {
            int intInstrument = allInstruments.get(instrumentsComboBox.getSelectedItem());//get the selected tool at a construction site
            int intNote = Integer.parseInt(noteTextField.getText());//get the selected note
            int intVolume = Integer.parseInt(volumeTextField.getText());//get the selected volume
            for (int i = 0; i <= countTicks - 1; i++) {
                if (ticksPanel.panels.get(i).getValue() != TicksPanel.OneTick.EMPTY_VALUE) {
                    if (ticksPanel.panels.get(i).getValue() == TicksPanel.OneTick.FULL_VALUE) {
                        soundsList.add(new OneSound(intInstrument, intNote, intVolume, i, 1, chanel));
                    } else {
                        if (ticksPanel.panels.get(i).getValue() == TicksPanel.OneTick.START_VALUE) {
                            for (int j = i + 1; j < countTicks; j++) {
                                if (ticksPanel.panels.get(j).getValue() == TicksPanel.OneTick.END_VALUE) {
                                    soundsList.add(new OneSound(intInstrument, intNote, intVolume, i, j - i, chanel));
                                    i = j;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return soundsList;
    }

    /*
     * Creates a separate Thread that plays only the selected note
     * */
    private void playExample() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Sequencer player = MidiSystem.getSequencer();
                    player.open();
                    Sequence seq = new Sequence(Sequence.PPQ, 4);
                    Track track = seq.createTrack();
                    track.add(new MidiEvent(new ShortMessage(192, 1,
                            allInstruments.get(instrumentsComboBox.getSelectedItem()), 1), 0));
                    track.add(new MidiEvent(new ShortMessage(144, 1,
                            Integer.parseInt(noteTextField.getText()), Integer.parseInt(volumeTextField.getText())), 0));
                    track.add(new MidiEvent(new ShortMessage(128, 1,
                            Integer.parseInt(noteTextField.getText()), Integer.parseInt(volumeTextField.getText())), 2));
                    player.setSequence(seq);
                    player.start();
                    Thread.sleep(1000);
                    player.stop();
                    player.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }).start();
    }

    /*
     * Returns a list of tools that match the selected group
     * */
    private ArrayList<String> createInstruments(String group) {
        instruments.clear();
        for (int i = 0; i < groups.length; i++) {
            if (groups[i][1].equals(group)) {
                instruments.add(groups[i][0]);
            }
        }
        return instruments;
    }

    /*
     * Sets the line to the default settings
     * */
    public void clearLine() {
        soundsList.clear();
        for (TicksPanel.OneTick panel : ticksPanel.panels) {
            panel.setValue(TicksPanel.OneTick.EMPTY_VALUE);
        }
        noteTextField.setText("50");
        volumeTextField.setText("80");
    }

    /*
     * Saves the state of LineElements in array of objects
     * */
    public Object[] saveLine() {
        Object[] array = new Object[5];
        array[0] = instrumentsComboBox.getSelectedItem();
        array[1] = groupInstrumentsComboBox.getSelectedItem();
        array[2] = noteTextField.getText();
        array[3] = volumeTextField.getText();
        int[] valueTicks = new int[countTicks];
        for (int i = 0; i < countTicks; i++) {
            valueTicks[i] = ticksPanel.panels.get(i).getValue();
        }
        array[4] = valueTicks;
        return array;
    }

    /*
     * Based on the input array of objects, sets LineElements settings
     * */
    public void loadLine(Object[] array) {
        String ins = (String) array[0];
        String gr = (String) array[1];

        groupInstrumentsComboBox.setSelectedItem(gr);

        instrumentsPanel.remove(instrumentsComboBox);
        instruments = createInstruments(gr);
        instrumentsComboBox = new JComboBox<>(instruments.toArray(new String[instruments.size()]));
        instrumentsComboBox.setPreferredSize(new Dimension(140, 25));
        instrumentsPanel.add(instrumentsComboBox);
        instrumentsComboBox.setSelectedItem(ins);
        workZone.drawPanel();
        noteTextField.setText((String) array[2]);
        volumeTextField.setText((String) array[3]);
        int[] newValues = (int[]) array[4];
        for (int i = 0; i < countTicks; i++) {
            ticksPanel.panels.get(i).setValue(newValues[i]);
        }
    }

    public void setCountTics(int countTicks) {
        this.countTicks = countTicks;
    }

    public int getCountTicks() {
        return countTicks;
    }

    public int getChanel() {
        return chanel;
    }
}

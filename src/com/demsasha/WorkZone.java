package com.demsasha;

import com.demsasha.classes_non_core.ButtonUI;
import com.demsasha.classes_non_core.ImprovedKeyAdapter;
import com.demsasha.song.BeatBoxSong;
import com.demsasha.song.LineElements;
import com.demsasha.song.OneSound;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

/*
 *The main user interaction panel. Initializes a graphical interface consisting of two sub-panels:
 * workPanel - panel, is a convenient platform for creating a midi track;
 * buttonsPanel - a panel for entering information about the track (note duration, tempo, etc.)
 * and for interaction with the workPanel.
 * Allows you to start a track (with or without animation) to clear the working area, save, load a track, export to a midi file.
 * */
public class WorkZone extends JPanel {
    public JPanel workPanel = new JPanel(); //panel, is a convenient platform for creating a midi track
    private JScrollPane workPanelScroll = new JScrollPane(workPanel);
    private JPanel buttonsPanel = new JPanel(); //a panel for entering information about the track (note duration, tempo, etc.)
    //and for interaction with the workPanel.
    /*
     *The workPanel mainly consists of class LineElements objects.
     * Each object plays the role of a track in a track;
     * You can set the sound, volume and playback time.
     * The resulting track is collected from all created objects of the class LineElements
     * */
    private ArrayList<LineElements> lineElementsList = new ArrayList<LineElements>();
    private BeatBoxSong beatBoxSong; // beatBoxSong - the object that can create musics based on user input

    private JButton startStopButton; //The button starts and stops the sound
    private JTextField firstTickTextField;//input field of first tick which the track begins
    private JTextField tempTextField; //tempo setting field
    private JComboBox<Integer> lengthNoteComboBox; //note length selection
    private JFileChooser windowSaveLoad;
    private int countTicks = 16; //16 ticks are created at startup
    private boolean[] channels = new boolean[16]; //shows some occupied music channels
    private JDialog exportDialog;

    private ImageIcon plusImageIcon;
    private ImageIcon plus1ImageIcon;
    private ImageIcon minusImageIcon;
    private ImageIcon minus1ImageIcon;
    private ImageIcon notesImageIcon;

    WorkZone(Animation animation) {
        setPreferredSize(new Dimension(855, 570));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        beatBoxSong = new BeatBoxSong(animation);

        workPanel.setLayout(new GridBagLayout());
        workPanel.setBackground(Color.WHITE);
        workPanelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        workPanelScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        workPanelScroll.getHorizontalScrollBar().setUnitIncrement(
                workPanelScroll.getHorizontalScrollBar().getUnitIncrement() * 15);
        workPanelScroll.setBorder(BorderFactory.createEmptyBorder());

        windowSaveLoad = new JFileChooser();
        windowSaveLoad.setFileFilter(new FileNameExtensionFilter("file BeatBox", "bbx"));

        channels[9] = true;//Channel 9 in MIDI reserved only for percussion instruments

        //create default three LineElements
        createLines();
        createLines();
        createLines();

        initButtonsPanel();
        initImages();
        drawPanel();
    }

    private void initImages() {
        plusImageIcon = new ImageIcon(WorkZone.class.getResource("/images/plus.png"));
        plus1ImageIcon = new ImageIcon(WorkZone.class.getResource("/images/plus1.png"));
        minusImageIcon = new ImageIcon(WorkZone.class.getResource("/images/minus.png"));
        minus1ImageIcon = new ImageIcon(WorkZone.class.getResource("/images/minus1.png"));
        notesImageIcon = new ImageIcon(WorkZone.class.getResource("/images/notes.png"));
    }

    /*
     * Initializes the buttonsPanel and its components
     * */
    private void initButtonsPanel() {
        buttonsPanel.setLayout(new GridBagLayout());
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        //CheckBox indicates whether to start animation when playing a track
        JCheckBox animationCheckBox = new JCheckBox("Анимация");
        animationCheckBox.setBackground(Color.WHITE);

        //Panel contains input field of first tick which the track begins
        JPanel firstTickPanel = new JPanel();
        firstTickPanel.setBackground(Color.WHITE);
        firstTickPanel.setLayout(new BoxLayout(firstTickPanel, BoxLayout.X_AXIS));
        firstTickPanel.add(new JLabel("Начать с "));
        firstTickTextField = new JTextField(2);
        firstTickTextField.setText("1");
        firstTickTextField.addKeyListener(new ImprovedKeyAdapter(countTicks, 0));
        firstTickPanel.add(firstTickTextField);
        firstTickPanel.add(new JLabel(" такта    "));

        //The button starts and stops the sound
        startStopButton = new JButton("Старт");
        ButtonUI.setupButtonUI(startStopButton, -1);
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startOrStop(startStopButton.getText().equals("Старт"), animationCheckBox.isSelected());
            }
        });

        //Setting note duration
        Integer[] lengths = {4, 8, 16, 32};
        lengthNoteComboBox = new JComboBox<Integer>(lengths);

        //The field changes tempo of the sound
        tempTextField = new JTextField("120", 5);
        tempTextField.addKeyListener(new ImprovedKeyAdapter(500, 0));

        //The button increases tempo sound
        JButton tempUpButton = new JButton("+");
        ButtonUI.setupButtonUI(tempUpButton, -1);
        tempUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int temp = getTemp();
                if (temp < 591) {
                    temp += 10;
                    tempTextField.setText("" + temp);
                }
            }
        });

        //The button decreases tempo sound
        JButton tempDownButton = new JButton("-");
        ButtonUI.setupButtonUI(tempDownButton, -1);
        tempDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int temp = getTemp();
                if (temp > 11) {
                    temp -= 10;
                    tempTextField.setText("" + temp);
                }
            }
        });

        //Loads a example sound
        String[] tracks = new String[]{"Пианино", "Гитара"};
        JComboBox<String> exampleComboBox = new JComboBox<String>(tracks);
        exampleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadExampleSound((String) exampleComboBox.getSelectedItem());
            }
        });

        //The button clears lineElements of entered date
        JButton clearButton = new JButton("Очистить");
        ButtonUI.setupButtonUI(clearButton, -1);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });

        Font font = new Font("TimesRoman", Font.BOLD, 11);

        //The button saves sound like file.bbx
        JButton saveButton = new JButton("Сохранить");
        saveButton.setFont(font);
        ButtonUI.setupButtonUI(saveButton, -1);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSong();
            }
        });

        //The button load sound from file.bbx
        JButton loadButton = new JButton("Загрузить");
        loadButton.setFont(font);
        ButtonUI.setupButtonUI(loadButton, -1);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLoadDialog();
            }
        });

        //The button opens exportDialog
        JButton exportButton = new JButton("Экспорт");
        ButtonUI.setupButtonUI(exportButton, -1);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openExportDialog();
            }
        });

        //The button opens window contains images (notes.png)
        JButton noteNumberButton = new JButton("Номера нот");
        ButtonUI.setupButtonUI(noteNumberButton, -1);
        noteNumberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWindowNotes();
            }
        });

        //placement of elements in buttonsPanel
        GridBagConstraints c = new GridBagConstraints(0, 0, 4, 1, 0.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0);
        buttonsPanel.add(startStopButton, c);
        c.gridy++;
        buttonsPanel.add(firstTickPanel, c);
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 3;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 4;
        c.gridy++;
        c.gridx = 0;
        buttonsPanel.add(animationCheckBox, c);
        c.gridy++;
        c.insets.top = 20;
        c.insets.bottom = 0;
        buttonsPanel.add(new JLabel("Длительность"), c);
        c.gridy++;
        c.insets.top = 0;
        c.gridwidth = 2;
        buttonsPanel.add(new JLabel("ноты: "), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        buttonsPanel.add(lengthNoteComboBox, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.insets.top = 20;
        c.insets.bottom = 2;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        buttonsPanel.add(new JLabel("Темп (bpm)"), c);
        c.gridy++;
        c.insets.top = 2;
        buttonsPanel.add(tempTextField, c);
        c.gridy++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        buttonsPanel.add(tempUpButton, c);
        c.gridx = 2;
        c.anchor = GridBagConstraints.WEST;
        buttonsPanel.add(tempDownButton, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.CENTER;
        c.insets.top = 20;
        c.gridy++;
        buttonsPanel.add(new JLabel("Образец"), c);
        c.gridy++;
        c.insets.top = 2;
        buttonsPanel.add(exampleComboBox, c);
        c.insets.top = 20;
        c.gridy++;
        buttonsPanel.add(clearButton, c);
        c.gridy++;
        c.gridwidth = 2;
        buttonsPanel.add(saveButton, c);
        c.gridx = 2;
        buttonsPanel.add(loadButton, c);
        c.gridx = 0;
        c.gridwidth = 4;
        c.gridy++;
        buttonsPanel.add(exportButton, c);
        c.gridy++;
        c.insets.bottom = 100;
        buttonsPanel.add(noteNumberButton, c);
    }

    /*
     * Сreates a list of musical sounds based on the entered data, uploads it to the beatBoxSong and starts the song.
     * Then allows you to stop the song
     * */
    private void startOrStop(boolean start, boolean withAnimation) {
        if (start) {
            if (beatBoxSong.isReady(generateSoundsList(), 0,
                    1, Integer.parseInt(firstTickTextField.getText()) - 1)) {
                beatBoxSong.setTemp(getFinallyTemp());
                beatBoxSong.start(withAnimation);
                startStopButton.setText("Стоп");
            }
        } else {
            stopPlayingSong();
        }
    }

    /*
     * Opens window contains images (notes.png)
     * */
    private void openWindowNotes() {
        JFrame main = (JFrame) WorkZone.this.getParent().getParent().getParent().getParent();
        JDialog noteNumberDialog = new JDialog(main, "Номера нот", false);
        noteNumberDialog.setLocationRelativeTo(main);
        noteNumberDialog.add(new JLabel(notesImageIcon));
        noteNumberDialog.pack();
        noteNumberDialog.setVisible(true);
        noteNumberDialog.setResizable(false);
    }

    /*
     * Opens modal window contains ExportPanel which exports sound in file.midi
     * */
    private void openExportDialog() {
        stopPlayingSong();
        JFrame main = (JFrame) WorkZone.this.getParent().getParent().getParent().getParent();
        exportDialog = new JDialog(main, "Экспорт в midi", true);
        exportDialog.setLocationRelativeTo(main);
        exportDialog.add(new ExportPanel());
        exportDialog.pack();
        exportDialog.setVisible(true);
        exportDialog.setResizable(false);
    }

    /*
     * Cleans the WorkZone and places components on it
     * */
    public void drawPanel() {
        removeAll();
        updateWorkPanel();
        add(workPanelScroll);
        add(buttonsPanel);
        updateUI();
    }

    /*
     * Clears the workPanel and adds previously modified components to it
     * */
    private void updateWorkPanel() {
        workPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1,
                0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(3, 20, 2, 2), 0, 0);

        //Adding top elements
        workPanel.add(new JLabel("Инструмент:"), c);
        c.insets.left = 2;
        c.gridwidth = 2;
        workPanel.add(new JLabel("Нота:"), c);
        workPanel.add(new JLabel("Громкость:"), c);
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 1;
        c.insets.left = 0;
        c.insets.right = 0;
        for (int i = 0; i < countTicks; i++) {
            JPanel numberPanel = new JPanel(new GridBagLayout());
            numberPanel.setBackground(Color.WHITE);
            numberPanel.setPreferredSize(new Dimension(20, 16));
            JLabel label = new JLabel("" + (1 + i));
            numberPanel.add(label);
            workPanel.add(numberPanel, c);
        }
        //add the ability to add tick (if countTicks<100)
        if (countTicks < 100) {
            JLabel addTickLabel = new JLabel();
            addTickLabel.setIcon(plusImageIcon);
            addTickLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    addTickLabel.setIcon(plus1ImageIcon);

                }

                @Override
                public void mouseExited(MouseEvent e) {
                    addTickLabel.setIcon(plusImageIcon);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    addTick();
                }
            });
            workPanel.add(addTickLabel, c);
        }
        //add the ability to remove tick (if countTicks>16)
        if (countTicks > 16) {
            JLabel deleteTickLabel = new JLabel();
            deleteTickLabel.setIcon(minusImageIcon);
            deleteTickLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    deleteTickLabel.setIcon(minus1ImageIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    deleteTickLabel.setIcon(minusImageIcon);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    deleteTick();
                }
            });
            workPanel.add(deleteTickLabel, c);
        }
        c.insets.left = 2;
        c.insets.right = 2;
        c.gridy = 1;

        //Adding LineElements
        for (LineElements lineElements : lineElementsList) {
            lineElements.drawLineElements(c.gridy++, countTicks);
        }

        //adding the ability add new LineElements (if them < 15)
        if (lineElementsList.size() < 15) {
            JLabel addLineLabel = new JLabel();
            addLineLabel.setIcon(plusImageIcon);
            addLineLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    createLines();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    addLineLabel.setIcon(plus1ImageIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    addLineLabel.setIcon(plusImageIcon);
                }
            });
            c.gridy++;
            c.anchor = GridBagConstraints.WEST;
            c.insets.left = 30;
            workPanel.add(addLineLabel, c);

            //add a empty panel designed for alignment in GridBagLayout
            JPanel emptyPanel = new JPanel();
            emptyPanel.setPreferredSize(new Dimension(2, 0));
            c.weighty = 0.9;
            c.gridy = 18;
            workPanel.add(emptyPanel, c);
        }
    }

    private void deleteTick() {
        setCountTicks(countTicks - 1);
        //checking that the firstTickTextField does not have a value greater than countTicks
        if (Integer.parseInt(firstTickTextField.getText()) > countTicks) {
            firstTickTextField.setText("" + countTicks);
        }
    }

    private void addTick() {
        setCountTicks(countTicks + 1);
        //scroll to the right on maximum value
        workPanelScroll.getHorizontalScrollBar().setValue(
                workPanelScroll.getHorizontalScrollBar().getMaximum());
    }

    /*
     * Sets new countTicks and update countTicks in other objects
     * */
    private void setCountTicks(int newCountTicks) {
        countTicks = newCountTicks;
        beatBoxSong.setCountTics(countTicks);
        for (LineElements lineElements : lineElementsList) {
            lineElements.setCountTics(countTicks);
        }
        drawPanel();
    }


    public boolean deletingLine(LineElements d) {
        if (lineElementsList.size() == 1) {
            return false;
        }
        lineElementsList.remove(d);
        channels[d.getChanel()] = false;
        drawPanel();
        return true;
    }

    /*
     * Receives sounds (OneSound) from all lineElementsList and combines them into one list of sounds (soundsList)
     * */
    private ArrayList<OneSound> generateSoundsList() {
        ArrayList<OneSound> soundsList = new ArrayList<OneSound>();
        for (LineElements lineElements : lineElementsList) {
            soundsList.addAll(lineElements.getSounds());
        }
        return soundsList;
    }

    /*
     * Show save-dialog.
     * Providing the opportunity for the user to select the save directory and write the name of the future file.
     * */
    private void saveSong() {
        windowSaveLoad.setDialogTitle("Сохранение трека");
        windowSaveLoad.setSelectedFile(new File("*.bbx"));
        windowSaveLoad.showSaveDialog(this);
        if (windowSaveLoad.getSelectedFile() != null) {
            try {
                File file = null;
                String fileName = windowSaveLoad.getSelectedFile().getName();
                if (fileName.substring(fileName.length() - 4).equalsIgnoreCase(".bbx")) {
                    file = windowSaveLoad.getSelectedFile();
                } else {
                    file = new File(windowSaveLoad.getSelectedFile().getParent(), fileName + ".bbx");
                }
                save(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /*
     * Gets a file to save and serializes song information into it
     * Sequentially recorded: the countticks, the duration of the note, tempo, and the state of the lineElementses
     * */
    private void save(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(countTicks);
        os.writeObject(lengthNoteComboBox.getSelectedIndex());
        os.writeObject(getTemp());
        ArrayList<Object[]> lines = new ArrayList<Object[]>();
        for (LineElements lineElements : lineElementsList) {
            lines.add(lineElements.saveLine());
        }
        os.writeObject(lines);
        os.close();
    }

    /*
     * Opens load-dialog  and transmits selected file
     * */
    private void openLoadDialog() {
        windowSaveLoad.setSelectedFile(null);
        windowSaveLoad.setDialogTitle("Загрузка трека");
        windowSaveLoad.showDialog(WorkZone.this, "Загрузить");
        if (windowSaveLoad.getSelectedFile() != null) {
            loadFromFile(windowSaveLoad.getSelectedFile());
        }
    }

    /*
     * Loads the selected example sound
     * */
    private void loadExampleSound(String selectedItem) {
        InputStream is;
        if (selectedItem.equals("Пианино")) {
            is = getClass().getResourceAsStream("/examples/example1.bbx");
        } else {
            is = getClass().getResourceAsStream("/examples/example2.bbx");
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            loadFromStream(ois);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Gets a file and creates ObjectInputStream
     * */
    private void loadFromFile(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(is);
            loadFromStream(ois);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Gets a ObjectInputStream and reads from: it newCountTicks,newLengthNote,newTemp and newLineElementList.
     * Passes the received data to the method loadFromArray.
     * */
    private void loadFromStream(ObjectInputStream ois) {
        try {
            int newCountTicks = (int) ois.readObject();
            int newLengthNote = (int) ois.readObject();
            int newTemp = (int) ois.readObject();
            ArrayList<Object[]> newLineElementList = (ArrayList<Object[]>) ois.readObject();
            loadFromArray(newCountTicks, newLengthNote, newTemp, newLineElementList);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
     * Offers to keep the original track before downloading. To do this, a dialog is launched.
     * If the user chooses to save, then the method saveSong() starts.
     * On the basis of the data getting, filling of the work area is completed and new LineElements with
     * the specified characteristics are created.
     * */
    public void loadFromArray(int newCountTicks, int newLengthNote, int newTemp, ArrayList<Object[]> arrayList) {
        int result = JOptionPane.showConfirmDialog(this,
                "Сохранить ваш трек перед загрузкой?", "Окно подтверждения", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            saveSong();
        } else {
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        lineElementsList.clear();
        for (int i = 0; i < 16; i++) channels[i] = false;
        channels[9] = true;
        countTicks = newCountTicks;
        beatBoxSong.setCountTics(newCountTicks);
        int i = 0;
        for (Object[] lines : arrayList) {
            LineElements l = createLines();
            l.loadLine(lines);
        }
        lengthNoteComboBox.setSelectedIndex(newLengthNote);
        tempTextField.setText("" + newTemp);
        drawPanel();
    }

    /*
     * Searches for a free channel. Creates an instance of class LineElements. Adds it to lineElementsList.
     * Аnd returns this instance
     * */
    private LineElements createLines() {
        int channel = 0;
        for (int i = 0; i < 16; i++) {
            if (!channels[i]) {
                channel = i;
                break;
            }
        }
        LineElements l = new LineElements(WorkZone.this, countTicks, channel);
        lineElementsList.add(l);
        channels[channel] = true;
        drawPanel();
        return l;
    }

    /*
     * Stops playing a song
     * */
    void stopPlayingSong() {
        beatBoxSong.stop();
        startStopButton.setText("Старт");
    }

    /*
     * clears user input
     * */
    private void clear() {
        for (LineElements line : lineElementsList) {
            line.clearLine();
        }
        beatBoxSong.setTemp(120);
        tempTextField.setText("120");
        stopPlayingSong();
    }

    public int getTemp() {
        return Integer.parseInt(tempTextField.getText());
    }

    private int getFinallyTemp() {
        return getTemp() * (lengthNoteComboBox.getSelectedIndex() + 1);
    }

    public int getLengthNote() {
        return lengthNoteComboBox.getSelectedIndex();
    }

    public ArrayList<LineElements> getLineElementsList() {
        return lineElementsList;
    }


    /*
     * This class is designed to display the export window. Retrieve custom export data.
     * And export to a midi file according to the specified characteristics.
     * The user is given the opportunity to choose the duration of the song
     * and the ability to loop the song the specified number of times.
     * */
    private class ExportPanel extends JPanel {
        private JLabel durationLabel = new JLabel("Продолжительность :");
        private JCheckBox toSoundCheckBox = new JCheckBox("До последнего звука");
        private JCheckBox toTickCheckBox = new JCheckBox("До определенного такта");
        private JTextField tickTextField = new JTextField();
        private JLabel circleLabel = new JLabel("Цикличность :");
        private JCheckBox circleCheckBox = new JCheckBox("Зациклить?");
        private JLabel countCircleLabel = new JLabel("Количество повторений :");
        private JTextField countCircleTextField = new JTextField();
        private JButton exportButton = new JButton("Экспорт в midi");
        private JFileChooser fileExport = new JFileChooser();

        private ExportPanel() {
            setBorder(BorderFactory.createTitledBorder("Настройки"));
            setSize(500, 550);
            toSoundCheckBox.setSelected(true);
            tickTextField.setEnabled(false);
            countCircleTextField.setEnabled(false);
            countCircleTextField.setText("2");
            tickTextField.setText("16");
            fileExport.setDialogTitle("Сохранение Midi файла");
            fileExport.setFileFilter(new FileNameExtensionFilter("midi files", "mid"));
            fileExport.setSelectedFile(new File("*.mid"));

            loadListener();
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.insets = new Insets(15, 10, 1, 10);
            c.fill = GridBagConstraints.HORIZONTAL;
            add(durationLabel, c);
            c.insets.top = 1;
            add(toSoundCheckBox, c);
            add(toTickCheckBox, c);
            add(tickTextField, c);
            c.insets.top = 15;
            add(circleLabel, c);
            c.insets.top = 1;
            add(circleCheckBox, c);
            add(countCircleLabel, c);
            add(countCircleTextField, c);
            c.insets.top = 15;
            c.insets.bottom = 10;
            add(exportButton, c);
        }

        private void loadListener() {
            toSoundCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (toSoundCheckBox.isSelected()) {
                        toTickCheckBox.setSelected(false);
                        tickTextField.setEnabled(false);
                    }
                }
            });
            toTickCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (toTickCheckBox.isSelected()) {
                        toSoundCheckBox.setSelected(false);
                        tickTextField.setEnabled(true);
                    } else {
                        tickTextField.setEnabled(false);
                    }
                }
            });
            circleCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    countCircleTextField.setEnabled(!countCircleTextField.isEnabled());
                }
            });
            tickTextField.addKeyListener(new ImprovedKeyAdapter(100, 0));
            countCircleTextField.addKeyListener(new ImprovedKeyAdapter(10, 2));
            exportButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exportInMidi();
                }
            });
        }

        private void exportInMidi() {
            if (!toSoundCheckBox.isSelected() && !toTickCheckBox.isSelected()) {
                toSoundCheckBox.setSelected(true);
            }
            fileExport.showSaveDialog(ExportPanel.this);
            if (fileExport.getSelectedFile() != null) {
                File file = null;
                String fileName = fileExport.getSelectedFile().getName();
                if (fileName.substring(fileName.length() - 4).equalsIgnoreCase(".mid")) {
                    file = fileExport.getSelectedFile();
                } else {
                    file = new File(fileExport.getSelectedFile().getParent(), fileName + ".mid");
                }

                int lastTick;
                if (toSoundCheckBox.isSelected()) {
                    lastTick = 0;
                } else {
                    lastTick = Integer.parseInt(tickTextField.getText());
                }
                int coff = 1;
                if (circleCheckBox.isSelected()) {
                    coff = Integer.parseInt(countCircleTextField.getText());
                }

                if (beatBoxSong.isReady(generateSoundsList(), lastTick, coff, 0)) {
                    beatBoxSong.export(file, getFinallyTemp());
                }
                exportDialog.setVisible(false);
            }
        }
    }
}

package com.demsasha.network.client;

import com.demsasha.WorkZone;
import com.demsasha.classes_non_core.ButtonUI;
import com.demsasha.classes_non_core.ImprovedKeyAdapter;
import com.demsasha.network.NetMessage;
import com.demsasha.network.TcpConnection;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/*
* An instance of this class is responsible for displaying a panel that is designed to connect to the server and communicate
* between different users of the program. Each user has the ability: to receive messages from other users, send messages,
* connect and disconnect from the server.
* To collaborate on a track, each user can modify the track that he developed to the message.
* All other users can upload this track to WorkZone and make their changes.
* */
public class ClientPanel extends JPanel {
    private WorkZone workZone;
    private JPanel ipPanel; //the panel for input ip-address
    private JTextField[] ip = new JTextField[4];
    private JTextField nickNameTextField;//the TextFiled for input NickName

    private JPanel messagePanel;//the panel for output messages
    private GridBagConstraints cForMP;
    private JPanel emptyPanel;//the panel designed for alignment in GridBagLayout
    private JScrollPane scrollMessagePanel;
    private boolean scrollDown = false;

    private JTextArea textForSend;//the panel for input of text for send on server
    private JScrollPane scrollPaneTFS;
    private JButton sendButton; //the button for send of message
    private JCheckBox addTrackCheckBox;//if true, then when sending, a track will be attached to the message
    private JButton connectButton;//the button to connect to the server
    private TcpConnection connection; //implements work with the network
    private Thread readThread; //the thread reads the input stream
    private boolean isConnection = false;


    public ClientPanel(WorkZone workZone) {
        this.workZone = workZone;
        setPreferredSize(new Dimension(220, 540));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        initComponentsClientPanel();
        drawClientPanel();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (scrollDown) {
                        for (int i = 0; i < 10; i++) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            scrollMessagePanel.getVerticalScrollBar().setValue
                                    (scrollMessagePanel.getVerticalScrollBar().getMaximum());
                            scrollDown = false;
                            updateUI();
                        }
                    }
                }
            }
        }).start(); //A thread that is designed to automatically roll scrollMessagePanel down

    }

    /*
    * initializes components to ClientPanel
    * */
    private void initComponentsClientPanel() {
        //creating panel for input ip-address
        ipPanel = new JPanel();
        ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.X_AXIS));
        ipPanel.setBackground(Color.WHITE);
        ipPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        for (int i = 0; i < 4; i++) {
            JTextField t = new JTextField(4);
            t.addKeyListener(new ImprovedKeyAdapter(255, 0));
            t.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 1));
            ip[i] = t;
            ipPanel.add(t);
            if (i < 3) {
                ipPanel.add(new JLabel("."));
            }
        }
        ip[0].setText("127");
        ip[1].setText("0");
        ip[2].setText("0");
        ip[3].setText("1");

        //creating TextFiled for input NickName
        nickNameTextField = new JTextField(10);
        nickNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 58 || e.getKeyChar() == 32) {
                    e.consume();
                    return;
                }
                if (nickNameTextField.getText().length() > 13) {
                    if (e.getKeyChar() != KeyEvent.VK_BACK_SPACE || e.getKeyChar() != KeyEvent.VK_DELETE ||
                            e.getKeyChar() != KeyEvent.VK_TAB) {
                        e.consume();
                    }
                }
            }
        });
        nickNameTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        //creating panel for output messages
        messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(Color.GRAY);
        emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(188, 0));
        cForMP = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1,
                1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0);
        messagePanel.add(emptyPanel, cForMP);

        scrollMessagePanel = new JScrollPane(messagePanel);
        scrollMessagePanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollMessagePanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollMessagePanel.getVerticalScrollBar().setUnitIncrement(scrollMessagePanel.getVerticalScrollBar().getUnitIncrement() * 13);

        textForSend = new JTextArea(2, 10);
        textForSend.setLineWrap(true);
        textForSend.setWrapStyleWord(true);
        textForSend.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (sendButton.isEnabled()) {
                        sendMessage();
                    }
                    e.consume();
                }
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    addTrackCheckBox.requestFocus();
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                if (textForSend.getText().equals("") && !addTrackCheckBox.isSelected()) {
                    sendButton.setEnabled(false);
                } else {
                    if (isConnection) {
                        sendButton.setEnabled(true);
                    }
                }
            }
        });

        scrollPaneTFS = new JScrollPane(textForSend);
        scrollPaneTFS.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneTFS.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        sendButton = new JButton("Отправить");
        sendButton.setEnabled(false);
        ButtonUI.setupButtonUI(sendButton, -1);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        addTrackCheckBox = new JCheckBox("Прикрепить трек");
        addTrackCheckBox.setBackground(Color.WHITE);
        addTrackCheckBox.setFont(new Font("TimesRoman", Font.PLAIN, 11));
        addTrackCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (addTrackCheckBox.isSelected() && isConnection) {
                    sendButton.setEnabled(true);
                } else {
                    if (textForSend.getText().equals("")) {
                        sendButton.setEnabled(false);
                    }
                }
            }
        });

        connectButton = new JButton("Подключиться");
        connectButton.setMinimumSize(new Dimension(99, 26));
        ButtonUI.setupButtonUI(connectButton, -1);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectButton.getText().equals("Подключиться")) {
                    connect();
                } else {
                    connection.killTcpConnection();
                }
            }
        });
    }

    private void drawClientPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(5, 2, 2, 2), 0, 0);

        add(new JLabel("Server's ip"), c);
        c.gridwidth = 2;
        add(ipPanel, c);
        c.insets.top = 2;
        c.gridwidth = 1;
        c.gridy = 1;
        add(new JLabel("Nickname"), c);
        c.gridwidth = 2;
        add(nickNameTextField, c);
        c.gridy = 2;
        c.gridwidth = 3;
        c.weighty = 0.9f;
        add(scrollMessagePanel, c);
        c.gridy = 3;
        c.weighty = 0.1f;
        add(scrollPaneTFS, c);
        c.gridwidth = 2;
        c.weighty = 0.0f;
        c.gridy = 4;
        add(sendButton, c);
        c.gridwidth = 1;
        add(addTrackCheckBox, c);
        c.gridy = 5;
        c.gridwidth = 2;
        c.insets.top = 10;
        c.insets.bottom = 5;
        add(connectButton, c);
    }

    /*
     * Checks if the correct ip-address and nickname are entered and
     * if the checks are passed then initializes the connection to the server
     * */
    private void connect() {
        if (connectButton.getText().equals("Подключиться")) {

            //check if the correct ip-address is entered
            for (JTextField textField : ip) {
                if (textField.getText().equals("")) {
                    textField.requestFocus();
                    outputMessage(new NetMessage("Введите корректный ip-адрес", true, false));
                    return;
                }
            }

            //check if the correct Nickname is entered
            if (nickNameTextField.getText().equals("")) {
                nickNameTextField.requestFocus();
                outputMessage(new NetMessage("Введите Nickname", true, false));
                return;
            }

            try {
                initConnection(generationIp());
                connectButton.setText("Отключиться");
                textForSend.requestFocus();
            } catch (IOException ex) {
                outputMessage(new NetMessage("Ошибка при подключении", true, false));
                scrollMessagePanel.getVerticalScrollBar().setValue
                        (scrollMessagePanel.getVerticalScrollBar().getMaximum());
            }
        } else {
            connection.killTcpConnection();
        }
    }

    /*
    * initializes the connection to the server and starts message reading thread
    * */
    private void initConnection(String ipAddress) throws IOException {
        connection = new TcpConnection(ipAddress, 5556);
        isConnection = true;
        if (!textForSend.getText().equals("") || addTrackCheckBox.isSelected()) {
            sendButton.setEnabled(true);
        }
        readThread = new Thread(() -> {
            System.out.println("Начинаем чтение");
            while (!Thread.interrupted()) {
                try {
                    NetMessage netM = (NetMessage) connection.reading();
                    if ((netM.getText()).equals("Отключиться")) throw new IOException();
                    outputMessage(netM);
                } catch (IOException ex) {
                    System.out.println("Error read");
                    disconnect();
                    break;
                }
            }
        });
        readThread.start();
        connection.sendMessage(new NetMessage(nickNameTextField.getText(),false,false));
        nickNameTextField.setEnabled(false);
        nickNameTextField.setBackground(null);
        for (Component ip : ipPanel.getComponents()) {
            ip.setEnabled(false);
            ip.setBackground(null);
        }
        ipPanel.setBackground(null);
        ipPanel.setBorder(BorderFactory.createLineBorder(new Color(0xD1E0FF)));
    }

    /*
    * Composes and returns ip-address
    * */
    private String generationIp() {
        return (ip[0].getText() + "." + ip[1].getText() + "." + ip[2].getText() + "." + ip[3].getText());
    }

    /*
    * Checks if the text is entered, addTrackCheckBox is pressed, if the verification is completed,
    * it composes a message and sends it to the server
    * */
    private void sendMessage() {
        if (addTrackCheckBox.isSelected()) {
            if (textForSend.getText().equals("")) {
                //only treck
                connection.sendMessage(new NetMessage(nickNameTextField.getText() + "\n " ,workZone.getLengthNote(),
                        workZone.getTemp(), workZone.getLineElementsList()));
            }
            else {
                //text with track
                connection.sendMessage(new NetMessage(nickNameTextField.getText() + "\n" + textForSend.getText(),
                        workZone.getLengthNote(), workZone.getTemp(), workZone.getLineElementsList()));
            }
        } else {
            //only text
            connection.sendMessage(new NetMessage(nickNameTextField.getText()+"\n"+textForSend.getText(),false,false));
        }
        textForSend.setText("");
        textForSend.requestFocus();
        sendButton.setEnabled(false);
        addTrackCheckBox.setSelected(false);
    }

    /*
     * receives NetMessage as input, reads information from it and displays it in messagePanel
     *  */
    private synchronized void outputMessage(NetMessage nM) {
        String title;
        String text;
        if (nM.isConnect() || nM.isDisconnect()) {
            title = "BeatBox";
            text = nM.getText();
        } else {
            String[] s = nM.getText().split("\n");
            text = s[1];
            title = s[0];
        }
        if (nM.isDisconnect()) {
            isConnection = false;
        }
        BgMessagePanel p1 = new BgMessagePanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        p1.setBorder(BorderFactory.createTitledBorder(title));
        p1.setBackground(Color.WHITE);
        JTextArea textTextArea = new JTextArea(text);

        if (!text.equals(" ")) {
            textTextArea.setAlignmentX(CENTER_ALIGNMENT);
            textTextArea.setText(text);
            textTextArea.setEnabled(false);
            textTextArea.setDisabledTextColor(Color.BLACK);
            textTextArea.setLineWrap(true);
            textTextArea.setWrapStyleWord(true);
            p1.add(textTextArea);
        }

        JLabel trackLabel = new JLabel("+Трек");
        if (nM.withTrack()) {
            trackLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            trackLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    trackLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    workZone.loadFromArray(nM.getCountTicks(), nM.getLengthNote(), nM.getTemp(), nM.getLines());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    trackLabel.setBorder(BorderFactory.createRaisedBevelBorder());
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    trackLabel.setBorder(BorderFactory.createRaisedBevelBorder());
                    trackLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    trackLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                }
            });
            p1.add(trackLabel);
        }

        if (title.equals("BeatBox")) {
            cForMP.insets = new Insets(2, 0, 2, 15);
        } else {
            if (title.equals(nickNameTextField.getText())) {
                p1.setBackground(new Color(185, 255, 253));
                p1.left = false;
                textTextArea.setBackground(new Color(185, 255, 253, 145));
                trackLabel.setBackground(new Color(185, 255, 253));
                cForMP.insets = new Insets(2, 15, 2, 0);
            } else {
                p1.setBackground(new Color(160, 255, 168));
                textTextArea.setBackground(new Color(160, 255, 168, 145));
                trackLabel.setBackground(new Color(160, 255, 168));
                cForMP.insets = new Insets(2, 0, 2, 15);
            }
        }
        messagePanel.remove(emptyPanel);
        messagePanel.add(p1, cForMP);
        messagePanel.revalidate();
        cForMP.weighty = 0.9;
        cForMP.insets = new Insets(0, 0, 0, 0);
        messagePanel.add(emptyPanel, cForMP);
        messagePanel.revalidate();
        cForMP.weighty = 0;
        scrollDown = true;
        scrollMessagePanel.getVerticalScrollBar().setValue
                (scrollMessagePanel.getVerticalScrollBar().getMaximum());
    }

    /*
    *It disconnects from the server and displays a message about it in the messagePanel
    * */
    private void disconnect() {
        readThread.interrupt();
        connection = null;
        if (isConnection) {
            outputMessage(new NetMessage("Вы отключились от сервера:(", false, true));
            isConnection = false;
        }
        connectButton.setText("Подключиться");
        nickNameTextField.setEnabled(true);
        nickNameTextField.setBackground(Color.WHITE);
        for (Component ip : ipPanel.getComponents()) {
            ip.setEnabled(true);
            ip.setBackground(Color.WHITE);
        }
        ipPanel.setBackground(Color.WHITE);
        ipPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        nickNameTextField.requestFocus();
        sendButton.setEnabled(false);
    }

    public void setFocus() {
        nickNameTextField.requestFocus();
    }

    /*
    * The panel that is the background for messages
    * */
    private class BgMessagePanel extends JPanel {
        boolean left = true;


        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setColor(getBackground());
            if (left) {
                g2D.fillRoundRect(-10, 0, getWidth() + 10, getHeight(), 12, 12);
            } else {
                g2D.fillRoundRect(0, 0, getWidth() + 10, getHeight(), 12, 12);

            }

        }
    }
}

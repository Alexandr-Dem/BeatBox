package com.demsasha.network.server;

/*
* During startup, the server creates a socket with port number 5556
* The endless cycle begins, in which it waits for new users to connect
* When a new user connects, their own socket is created and
* it is assigned an InputStream and an OutputStream
* Using OutputStream, a PrintWriter is created for the user and placed in an ArrayList
* BufferReader is created using an InputStream and it is passed to a new read stream

* ReadMessage read stream expends class Thread and overrides its run() method
* ReadingMessage constructor accepts BufferReader thanks to him, he reads messages from the stream
* as soon as a message arrives, it passes it to the sendMessage () method

* When connecting a new user, the server saves its nickname and notifies all previously connected users
* that a new user has been connected.
* And the server sends message to  new user sends, which containing a list of nicknames of connected users.
* If the nickname of the new user is already being used by another user, then the connection will not occur,
* and a message will be sent to the new user stating that such a name is already taken

* When a user is disconnected, the server notifies all other users that the user has been disconnected
*/

import com.demsasha.network.NetMessage;
import com.demsasha.network.TcpConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server {
    /*
    * For each new user, a TcpConnection is created, which contains the username and can read
     * and send messages. These TcpConnection are stored in the TcpConnectionList
    * */
    private ArrayList<TcpConnection> TcpConnectionList = new ArrayList<TcpConnection>();

    public static void main(String[] args) {
        new Server().go();
    }

    private void go() {
        TcpConnection connection = null;
        ReadingMessageThread readingThread = null;
        try (ServerSocket server = new ServerSocket(5556)) {
            System.out.println("Сервер поднят");
            while (true) {
                System.out.println("Сервер ждет нового пользователя");
                connection = new TcpConnection(server.accept());
                System.out.println("Создал пользовательский сокет");
                TcpConnectionList.add(connection);
                readingThread = new ReadingMessageThread(connection);
                readingThread.start();
            }
        } catch (IOException ex) {
            System.out.println("Сервер не поднят");
        }
    }

    //create class, which extends Thread
    private class ReadingMessageThread extends Thread {

        TcpConnection connection;

        ReadingMessageThread(TcpConnection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            NetMessage nM = null;
            System.out.println("Начинаем чтение");
            while (!interrupted()) {
                try {
                    nM = (NetMessage) connection.reading(); //reading
                    //Checking for a new user
                    if (connection.getName() == null) { //If name = null, then this is a new user and you need to initialize it
                        String newUsersName = nM.getText();
                        for (TcpConnection c : TcpConnectionList) { //Name duplication check
                            String name = c.getName();
                            if (name != null && name.equalsIgnoreCase(newUsersName)) {
                                connection.sendMessage(new NetMessage("Пользователь с таким именем уже в сети!",
                                        false, true));
                                throw new IOException();
                            }
                        }
                        connection.setName(newUsersName);
                        sendToEveryone(new NetMessage("Пользователь '" + connection.getName() + "' подключен",
                                true, false));
                        if (TcpConnectionList.size() != 1) { // If in addition to the new user on the network
                            // there are still users, then a list of their names will be transferred to the new user
                            String alsoOnline = "В сети так же:";
                            for (int i = TcpConnectionList.size() - 2; i >= 0; i--) {
                                alsoOnline = alsoOnline.concat(" '" + TcpConnectionList.get(i).getName() + "';");
                            }
                            alsoOnline = alsoOnline.substring(0, alsoOnline.length() - 1) + ".";
                            connection.sendMessage(new NetMessage(alsoOnline, true, false));
                        }
                    } else {
                        sendToEveryone(nM);
                    }
                } catch (IOException ex) {
                    disconnect();
                }
            }
            System.out.println("Поток завершен");
        }

        private void disconnect() {
            connection.killTcpConnection();
            TcpConnectionList.remove(connection);
            if (connection.getName() != null) {
                sendToEveryone(new NetMessage("Пользователь '" + connection.getName() + "' был отключен ", true, false));
            }
            this.interrupt();
        }
    }

    /*
    * The sendToEveryone method accepts an Object message and passes it to everyone on the list
    * */
    private synchronized void sendToEveryone(Object message) {
        for (TcpConnection connection : TcpConnectionList) {
            connection.sendMessage(message);
            System.out.println("Успешно отправленно ");
        }
    }

}

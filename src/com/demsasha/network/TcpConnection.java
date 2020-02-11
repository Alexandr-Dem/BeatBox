package com.demsasha.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * The class implements work with the network. The data class is used by both the server and users.
 * It implements creating a socket, sending messages, reading messages, removing a connection.
* */
public class TcpConnection {
    private Socket socket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private String name;

    //TcpConnection is created by server
    public TcpConnection (Socket socket) throws IOException {
        this.socket = socket;
        System.out.println("Инициализировали сокет");
        writer = new ObjectOutputStream(socket.getOutputStream());
        InputStream is = socket.getInputStream();
        System.out.println("Создали InputStream");
        reader = new ObjectInputStream(is);
        System.out.println("Создали reader");
        System.out.println("Инициализированно подключение к серверу");
    }

    //TcpConnection is created by users
    public TcpConnection (String ip, int host) throws IOException {
        this(new Socket(ip,host));
    }

    /*
    * returns an object read from the input stream
    * */
    public Object reading() throws IOException  {
        Object message = null;
        try {
            if ((message = reader.readObject()) != null) { //freezes waiting for a message
                return message;
            }
        } catch (ClassNotFoundException ex) {
            throw new IOException();
        }
        return null;
    }

    /*
    * Accepts an object that sends to the output stream.
    * */
    public void sendMessage(Object message){
        try {
            writer.writeObject(message);
            writer.flush();
        } catch (IOException ex) {
            System.out.println("Ошибка при отправке");
        }
    }

    /*
    * Clears memory and closes Socket, InputStream and OutputStream
    * */
    public void killTcpConnection() {
        try {
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException ex) {
            System.out.println("Ошибка при закрытии");
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

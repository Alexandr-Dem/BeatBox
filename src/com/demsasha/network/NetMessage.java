package com.demsasha.network;

import com.demsasha.song.LineElements;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * An instance of this class is designed to transmit information over a network.
 * NetMessage necessarily consists of text ("Text") and two Boolean variables that indicate the purpose of the track:
 * "connect" - message intended to initialize the connection
 * "disconnect" - message intended to disconnect the user from the server
 * Also a track can be transferred to NetMessage. If NetMessage contains a track, then the boolean withTrack = true and
 * NetMessage includes track info:
 * int tempo - tempo of the song
 * int countTicks - the number of measures in the song
 * int lengthNote - duration of the note in the song
 * And an list consisting of an array of objects "lines", which carries information about LineElements of which the track consists
* */
public class NetMessage implements Serializable {
    private String text;
    private int tempo;
    private ArrayList<Object[]> lines;
    private boolean withTrack = false;
    private boolean connect = false;
    private boolean disconnect = false;
    private int countTicks;
    private int lengthNote;

    /*
    * NetMessage constructor, which contains the text and information about the track
    * */
    public NetMessage(String text,int lengthNote, int temp, ArrayList<LineElements> lineElementsList) {
        this.text = text;
        this.lengthNote = lengthNote;
        this.tempo = temp;
        withTrack = true;
        countTicks = lineElementsList.get(0).getCountTicks();
        lines = new ArrayList<Object[]>();
        createLines(lineElementsList);
    }

    /*
    * NetMessage constructor, which contains only the text and information about the purpose of the message (connect, disconnect)
    * */
    public NetMessage(String text, boolean connect, boolean disconnect) {
        this.text = text;
        this.connect = connect;
        this.disconnect = disconnect;
    }

    /*
    * It goes through all transmitted LineElements and fills the "lines" list with arrays of objects
    * containing information about these LineElements
    * */
    private void createLines(ArrayList<LineElements> lineElementsList) {
        for (LineElements lineElements : lineElementsList) {
            lines.add(lineElements.saveLine());
        }
    }
    public boolean isConnect() {
        return connect;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public String getText() {
        return text;
    }

    public int getTemp() {
        return tempo;
    }

    public ArrayList<Object[]> getLines() {
        return lines;
    }

    public boolean withTrack() {
        return withTrack;
    }

    public int getCountTicks() {
        return countTicks;
    }

    public int getLengthNote() {
        return lengthNote;
    }
}

package com.demsasha.song;

/*
 * Each instance of the class stores data about one sound.
 * This data is initialized when created:
 * instrument - an integer variable indicating which instrument will play the sound
 * note - an integer variable indicating which note will be played
 * volume - an integer variable indicating what volume the sound will have
 * tact - an integer variable indicating at what tact it is necessary to start playing sound
 * length - an integer variable indicating how long the sound will play
 * channel - an integer variable indicating in which music channel sound will be played
 * */
public class OneSound {
    private int instrument;
    private int note;
    private int volume;
    private int tact;
    private int length;
    private int channel;

    OneSound(int instrument, int note, int volume, int tact, int length, int channel) {
        this.instrument = instrument;
        this.note = note;
        this.volume = volume;
        this.tact = tact;
        this.length = length;
        this.channel = channel;
    }

    int getInstrument() {
        return instrument;
    }

    int getChannel() {
        return channel;
    }

    int getNote() {
        return note;
    }

    int getVolume() {
        return volume;
    }

    int getTact() {
        return tact;
    }

    int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "OneSound{" +
                "instrument=" + instrument +
                ", note=" + note +
                ", volume=" + volume +
                ", tact=" + tact +
                ", length= " + length +
                ", channel= " + channel +
                '}';
    }
}

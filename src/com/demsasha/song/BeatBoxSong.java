package com.demsasha.song;

import com.demsasha.Animation;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/*
 * A class instance is responsible for playing music. This class can create midi-events based on input data.
 * And then record these events in Track, place the track in Sequence and play it using the Player -  object of the Sequencer class.
 * Besides to creating, starting and stopping a song, this class can export the created song to file.mid
 * */
public class BeatBoxSong {
    private ArrayList<OneSound> sounds; //спсок звуков из которых будут генерироваться события
    private Sequencer player;
    private Track track;
    private Sequence seq;
    private Animation animation;
    private int countTics = 16;

    /*
     * The constructor initializes the variables: animation and player
     * */
    public BeatBoxSong(Animation animation) {
        this.animation = animation;
        try {
            player = MidiSystem.getSequencer();
            player.open();
            player.setLoopCount(player.LOOP_CONTINUOUSLY);
        } catch (MidiUnavailableException e) {
            System.out.println("Error Create BeatBoxSong.player");
            e.printStackTrace();
        }
    }

    /*
     * Checks if the music is ready to play, returns false if the list of sounds is empty.
     * Returns if the Sequence creation and initialization was successful.
     * The method accepts:
     * soundsList - a list of objects of the OneSound class, on the basis of which the song will be created
     * lastTick - an integer variable indicating to which measure a melody will be created
     * countCircle - integer variable indicating the number of repetitions of the melody
     * firstTick - an integer variable indicating the measure number from which you want to record sounds in Sequence
     * */
    public boolean isReady(ArrayList<OneSound> soundsList, int lastTick, int countCircle, int firstTick) {
        sounds = soundsList;
        if (sounds.size() > 0) {
            try {
                seq = new Sequence(Sequence.PPQ, 4);
                track = seq.createTrack();
                int lengthTrack = 0;
                for (int i = 1; i <= countCircle; i++) {
                    lengthTrack = buildSong(lastTick, lengthTrack, firstTick);
                }
                player.setSequence(seq);
                player.setMicrosecondPosition(0);
                return true;
            } catch (InvalidMidiDataException e) {
                System.out.println("Error setSequence");
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /*
     * Initializes the Track. Adds objects of the MidiEvent class to an object of the Track class.
     * The method takes each object from the list of sounds and if the tick of the sound is greater than or equal to
     * the firstTick, converts it into three MidiEvent objects:
     * 1. Event of the instrument selection
     * 2. Event of the beginning of playing a note
     * 3. Event of completion of playing a note
     * Also, if an event for the listener was not previously created at the tick of the beginning of playing the note,
     * then it is created
     * */
    private int buildSong(int lastTick, int lengthTrack, int firstTick) {
        int newLengthTreck = lastTick;
        boolean[] isEvent = new boolean[countTics];
        for (int i = 0; i < countTics; i++) {
            isEvent[i] = true;
        }
        for (OneSound sound : sounds) {
            if (sound.getTact() >= firstTick) {
                System.out.println(sound);
                int tact = sound.getTact() + lengthTrack - firstTick;
                track.add(getMidiEvent(192, sound.getChannel(), sound.getInstrument(), sound.getTact(), tact));//Event of the instrument selection
                track.add(getMidiEvent(144, sound.getChannel(), sound.getNote(), sound.getVolume(), tact));//Event of the beginning of playing a note
                tact += sound.getLength();
                track.add(getMidiEvent(128, sound.getChannel(), sound.getNote(), sound.getVolume(), tact));//Event of completion of playing a note

                if (newLengthTreck < tact) newLengthTreck = tact;
                if (isEvent[sound.getTact()]) {
                    track.add(getMidiEvent(176, 9, 127, 0, sound.getTact()));//event for drawing graphics
                    isEvent[sound.getTact()] = false;
                }
            }
        }
        track.add(getMidiEvent(176, 9, 1, 0, lastTick + lengthTrack)); //an empty event to always reach the last tick
        if (newLengthTreck < lastTick + lengthTrack) newLengthTreck = lastTick + lengthTrack;
        return newLengthTreck;
    }

    /*
     * Returns MidiEvent based on input.
     * comd - command (192 - Select a tool, 144 - start playing a note,
     * 128 - end of note playback, 176 - event for the listener)
     * channel - playback channel (from 0 to 15 inclusive)
     * one, two - command settings (note selection, volume selection, musical instrument selection)
     * tact - tact
     * */
    private MidiEvent getMidiEvent(int comd, int channel, int one, int two, int tact) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, channel, one, two);
            event = new MidiEvent(a, tact * 4);
        } catch (Exception ex) {
            System.out.println("Error");
        }
        return event;
    }

    /*
     * Launches the music. Accepts a boolean variable that indicates the presence of animation.
     * */
    public void start(boolean withAnimation) {
        if (withAnimation) {
            player.addControllerEventListener(animation, new int[]{127});
            animation.start();
        }
        player.start();
    }

    /*
     * Stops playing music
     * */
    public void stop() {
        player.stop();
        player.removeControllerEventListener(animation, new int[]{127});
    }

    /*
     * Exports the created song to file.mid. Accepts the file to which the song will be recorded,
     * and the tempo that must be set
     * */
    public void export(File file, int temp) {
        MetaMessage mt = new MetaMessage();
        //Add metaMessage to change the tempo
        byte[] bt = toByteArray(60000000 / temp);
        try {
            mt.setMessage(0x51, bt, 3);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        MidiEvent me = new MidiEvent(mt, 0);
        track.add(me);
        int[] fileTypes = MidiSystem.getMidiFileTypes(seq);
        try {
            MidiSystem.write(seq, fileTypes[0], file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Converts an int variable to Array of byte
     * */
    private static byte[] toByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public void setTemp(int temp) {
        player.setTempoInBPM(temp);
    }

    public void setCountTics(int countTicks) {
        this.countTics = countTicks;
    }
}

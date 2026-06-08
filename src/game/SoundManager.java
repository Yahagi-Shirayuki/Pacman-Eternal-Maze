package game;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundManager {

    private static final String SFX_DIR = "res/sfx/";
    private static final String MUSIC_PATH = "res/music/Pac Terror.wav";

    private final Map<String, SoundData> sounds = new HashMap<>();
    private final Map<String, Clip> exclusiveClips = new HashMap<>();
    private Clip laserLoopClip;
    private Clip musicClip;
    private boolean laserLooping = false;
    private boolean musicPlaying = false;
    private boolean musicUnavailable = false;
    private boolean stopMusicAfterFade = false;
    private double musicFade = 0.0;
    private double targetMusicFade = 0.0;
    private int masterVolume = 100;
    private int sfxVolume = 100;
    private int musicVolume = 100;
    private static final double MUSIC_FADE_STEP = 1.0 / 240.0; // Lower denominator = faster; higher denominator = smoother/slower music fade.

    public SoundManager() {
        load("bombaim", "bombaim.wav");
        load("boardhalf", "boardhalf.wav");
        load("boardclear", "boardclear.wav");
        load("menumove", "menumove.wav");
        load("menuconfirm", "menuconfirm.wav");
        load("lazestart", "lazestart.wav");
        load("lazeloop", "lazeloop.wav");
        load("lazeend", "lazeend.wav");
        load("fire", "fire.wav");
        load("exploded", "exploded.wav");
        load("eatpower", "eatpower.wav");
        load("eatghost", "eatghost.wav");
        load("eatfruit", "eatfruit.wav");
        load("eatdot", "eatdot.wav");
        load("die", "die.wav");
        load("detection", "detection.wav");
    }

    public void playEatDot() {
        playExclusive("eatdot");
    }

    public void playEatGhost() {
        playExclusive("eatghost");
    }

    public void playEatPower() {
        playExclusive("eatpower");
    }

    public void playEatFruit() {
        playExclusive("eatfruit");
    }

    public void playDeath() {
        stopLaser();
        playExclusive("die");
    }

    public void playBombAim() {
        play("bombaim");
    }

    public void playExplosion() {
        playExclusive("exploded");
    }

    public void playFire() {
        playExclusive("fire");
    }

    public void playDetection() {
        playExclusive("detection");
    }

    public void playBoardHalf() {
        playExclusive("boardhalf");
    }

    public void playBoardClear() {
        playExclusive("boardclear");
    }

    public void playMenuMove() {
        playExclusive("menumove");
    }

    public void playMenuConfirm() {
        playExclusive("menuconfirm");
    }

    public void startMusic() {
        if (musicPlaying) {
            targetMusicFade = 1.0;
            stopMusicAfterFade = false;
            return;
        }

        musicPlaying = true;
        stopMusicAfterFade = false;
        targetMusicFade = 1.0;

        if (musicUnavailable) {
            return;
        }

        try {
            ensureMusicPlayer();

            if (musicClip == null) {
                return;
            }

            applyMusicVolume();
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | RuntimeException e) {
            musicUnavailable = true;
            System.err.println("Could not start music " + MUSIC_PATH + ": " + e.getMessage());
        }
    }

    public void stopMusic() {
        musicPlaying = false;
        stopMusicAfterFade = false;
        targetMusicFade = 0.0;
        musicFade = 0.0;

        if (musicClip == null) {
            return;
        }

        musicClip.stop();
        musicClip.close();
        musicClip = null;
    }

    public void fadeOutMusicAndStop() {
        if (!musicPlaying) {
            stopMusic();
            return;
        }

        stopMusicAfterFade = true;
        targetMusicFade = 0.0;
    }

    public void setMusicPaused(boolean paused) {
        if (!musicPlaying || stopMusicAfterFade) {
            return;
        }

        targetMusicFade = paused ? 0.0 : 1.0;
    }

    public void update() {
        updateMusicFade();
    }

    public void setMasterVolume(int volume) {
        masterVolume = clampVolume(volume);
        applyMusicVolume();
        updateLaserLoopVolume();
    }

    public void setSfxVolume(int volume) {
        sfxVolume = clampVolume(volume);
        updateLaserLoopVolume();
    }

    public void setMusicVolume(int volume) {
        musicVolume = clampVolume(volume);
        applyMusicVolume();
    }

    public void setLaserActive(boolean active) {
        if (active == laserLooping) {
            return;
        }

        laserLooping = active;

        if (active) {
            playExclusive("lazestart");
            startLaserLoop();
        } else {
            stopLaserLoop();
            playExclusive("lazeend");
        }
    }

    public void stopLaser() {
        if (!laserLooping) {
            stopLaserLoop();
            return;
        }

        laserLooping = false;
        stopLaserLoop();
    }

    private void play(String name) {
        SoundData sound = sounds.get(name);

        if (sound == null) {
            return;
        }

        try {
            Clip clip = createClip(sound);
            applySfxVolume(clip);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            System.err.println("Could not play sound " + name + ": " + e.getMessage());
        }
    }

    private void playExclusive(String name) {
        SoundData sound = sounds.get(name);

        if (sound == null) {
            return;
        }

        try {
            Clip clip = exclusiveClips.get(name);

            if (clip == null || !clip.isOpen()) {
                clip = createClip(sound);
                exclusiveClips.put(name, clip);
            }

            clip.stop();
            clip.setFramePosition(0);
            applySfxVolume(clip);
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            System.err.println("Could not play sound " + name + ": " + e.getMessage());
        }
    }

    private void startLaserLoop() {
        SoundData sound = sounds.get("lazeloop");

        if (sound == null) {
            return;
        }

        try {
            stopLaserLoop();
            laserLoopClip = createClip(sound);
            applySfxVolume(laserLoopClip);
            laserLoopClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | IllegalArgumentException e) {
            System.err.println("Could not start laser loop: " + e.getMessage());
        }
    }

    private void stopLaserLoop() {
        if (laserLoopClip == null) {
            return;
        }

        laserLoopClip.stop();
        laserLoopClip.close();
        laserLoopClip = null;
    }

    private Clip createClip(SoundData sound) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(Clip.class, sound.format);
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(sound.format, sound.bytes, 0, sound.bytes.length);
        return clip;
    }

    private void applySfxVolume(Clip clip) {
        applyClipVolume(clip, getCombinedVolume(sfxVolume));
    }

    private void applyClipVolume(Clip clip, double volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float gain;

        if (volume <= 0) {
            gain = gainControl.getMinimum();
        } else {
            gain = (float) (20.0 * Math.log10(volume));
            gain = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gain));
        }

        gainControl.setValue(gain);
    }

    private void updateLaserLoopVolume() {
        if (laserLoopClip != null) {
            applySfxVolume(laserLoopClip);
        }

        for (Clip clip : exclusiveClips.values()) {
            if (clip != null && clip.isOpen()) {
                applySfxVolume(clip);
            }
        }
    }

    private void ensureMusicPlayer() throws LineUnavailableException {
        if (musicClip != null || musicUnavailable) {
            return;
        }

        File musicFile = new File(MUSIC_PATH);

        if (!musicFile.exists()) {
            musicUnavailable = true;
            System.err.println("Missing music file: " + musicFile.getPath());
            return;
        }

        SoundData music = loadSoundData(musicFile);

        if (music == null) {
            musicUnavailable = true;
            return;
        }

        musicClip = createClip(music);
        musicClip.setFramePosition(0);
        applyMusicVolume();
    }

    private void applyMusicVolume() {
        if (musicClip == null) {
            return;
        }

        applyClipVolume(musicClip, getCombinedVolume(musicVolume) * musicFade);
    }

    private void updateMusicFade() {
        if (musicFade < targetMusicFade) {
            musicFade = Math.min(targetMusicFade, musicFade + MUSIC_FADE_STEP);
            applyMusicVolume();
        } else if (musicFade > targetMusicFade) {
            musicFade = Math.max(targetMusicFade, musicFade - MUSIC_FADE_STEP);
            applyMusicVolume();
        }

        if (stopMusicAfterFade && musicFade <= 0.0) {
            stopMusic();
        }
    }

    private double getCombinedVolume(int channelVolume) {
        return (masterVolume / 100.0) * (channelVolume / 100.0);
    }

    private int clampVolume(int volume) {
        return Math.max(0, Math.min(100, volume));
    }

    private void load(String name, String fileName) {
        File file = new File(SFX_DIR + fileName);
        SoundData sound = loadSoundData(file);

        if (sound == null) {
            return;
        }

        sounds.put(name, sound);
    }

    private SoundData loadSoundData(File file) {
        if (!file.exists()) {
            System.err.println("Missing sound file: " + file.getPath());
            return null;
        }

        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file);
                AudioInputStream decodedStream = getDecodedStream(sourceStream)) {
            return new SoundData(decodedStream.getFormat(), readAllBytes(decodedStream));
        } catch (IOException | UnsupportedAudioFileException e) {
            System.err.println("Could not load sound " + file.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    private AudioInputStream getDecodedStream(AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();

        if (sourceFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
                && sourceFormat.getSampleSizeInBits() == 16
                && !sourceFormat.isBigEndian()) {
            return sourceStream;
        }

        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);

        return AudioSystem.getAudioInputStream(decodedFormat, sourceStream);
    }

    private byte[] readAllBytes(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = stream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        return output.toByteArray();
    }

    private static class SoundData {
        final AudioFormat format;
        final byte[] bytes;

        SoundData(AudioFormat format, byte[] bytes) {
            this.format = format;
            this.bytes = bytes;
        }
    }
}


package main;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {

	Clip clip;
	URL soundURL[] = new URL[30];

	public Sound() {
		soundURL[0] = getClass().getResource("/sound/select.wav");
		soundURL[1] = getClass().getResource("/sound/machinegun.wav");
		soundURL[2] = getClass().getResource("/sound/rifle.wav");
		soundURL[3] = getClass().getResource("/sound/shotgun.wav");
		soundURL[4] = getClass().getResource("/sound/sniper.wav");
		soundURL[5] = getClass().getResource("/sound/map.wav");
		soundURL[6] = getClass().getResource("/sound/countdown.wav");
		soundURL[7] = getClass().getResource("/sound/crateopen.wav");
		soundURL[8] = getClass().getResource("/sound/pickup.wav");
		soundURL[9] = getClass().getResource("/sound/drop.wav");
		soundURL[10] = getClass().getResource("/sound/win.wav");
		soundURL[11] = getClass().getResource("/sound/lose.wav");
	}

	public void setFile(int i) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
			clip = AudioSystem.getClip();
			clip.open(ais);
		} catch (Exception e) {
		}
	}

	public void play() {
		clip.start();
	}

	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stop() {
		clip.stop();
	}

}
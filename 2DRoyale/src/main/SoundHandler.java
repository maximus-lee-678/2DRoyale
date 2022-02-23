
package main;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundHandler {

	private Clip clip;
	private URL soundURL[] = new URL[12];

	public SoundHandler() {
		//input your sounds here!
		this.soundURL[0] = getClass().getResource("/sound/select.wav");
		this.soundURL[1] = getClass().getResource("/sound/machinegun.wav");
		this.soundURL[2] = getClass().getResource("/sound/rifle.wav");
		this.soundURL[3] = getClass().getResource("/sound/shotgun.wav");
		this.soundURL[4] = getClass().getResource("/sound/sniper.wav");
		this.soundURL[5] = getClass().getResource("/sound/map.wav");
		this.soundURL[6] = getClass().getResource("/sound/countdown.wav");
		this.soundURL[7] = getClass().getResource("/sound/crateopen.wav");
		this.soundURL[8] = getClass().getResource("/sound/pickup.wav");
		this.soundURL[9] = getClass().getResource("/sound/drop.wav");
		this.soundURL[10] = getClass().getResource("/sound/win.wav");
		this.soundURL[11] = getClass().getResource("/sound/lose.wav");
	}

	public void playSound(int i) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
			clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
		} catch (Exception e) {
		}
	}

}
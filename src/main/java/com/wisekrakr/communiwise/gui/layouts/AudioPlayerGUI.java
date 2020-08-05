package com.wisekrakr.communiwise.gui.layouts;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.sound.sampled.*;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileFilter;


public class AudioPlayerGUI extends JFrame implements ActionListener {
	private Thread playbackThread;
	private AudioPlayTimer timer;

	private boolean isPlaying = false;
	private boolean isPause = false;
	
	private String audioFilePath;
	private String lastOpenPath;
	
	private JLabel labelFileName = new JLabel("Playing File:");
	private JLabel labelTimeCounter = new JLabel("00:00:00");
	private JLabel labelDuration = new JLabel("00:00:00");
	
	private JButton buttonOpen = new JButton("Open");
	private JButton buttonPlay = new JButton("Play");
	private JButton buttonPause = new JButton("Pause");
	
	private JSlider sliderTime = new JSlider();

	private Thread player;
	private Clip audioClip;

	public AudioPlayerGUI() {
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.WEST;

		labelTimeCounter.setFont(new Font("Sans", Font.BOLD, 12));
		labelDuration.setFont(new Font("Sans", Font.BOLD, 12));
		
		sliderTime.setPreferredSize(new Dimension(400, 20));
		sliderTime.setEnabled(false);
		sliderTime.setValue(0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		add(labelFileName, constraints);
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		add(labelTimeCounter, constraints);
		
		constraints.gridx = 1;
		add(sliderTime, constraints);
		
		constraints.gridx = 2;
		add(labelDuration, constraints);
		
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
		panelButtons.add(buttonOpen);
		panelButtons.add(buttonPlay);
		panelButtons.add(buttonPause);
		
		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(panelButtons, constraints);
		
		buttonOpen.addActionListener(this);
		buttonPlay.addActionListener(this);
		buttonPause.addActionListener(this);
		
		pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	/**
	 * Handle click events on the buttons.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JButton) {
			JButton button = (JButton) source;
			if (button == buttonOpen) {
				openFile();
			} else if (button == buttonPlay) {
				if (!isPlaying) {
					playBack();
				} else {
					stopPlaying();
				}
			} else if (button == buttonPause) {
				if (!isPause) {
					pausePlaying();
				} else {
					resumePlaying();
				}
			}
		}
	}

	private void openFile() {
		JFileChooser fileChooser = null;
		
		if (lastOpenPath != null && !lastOpenPath.equals("")) {
			fileChooser = new JFileChooser(lastOpenPath);
		} else {
			fileChooser = new JFileChooser();
		}
		
		FileFilter wavFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Sound file (*.WAV)";
			}

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				} else {
					return file.getName().toLowerCase().endsWith(".wav");
				}
			}
		};

		
		fileChooser.setFileFilter(wavFilter);
		fileChooser.setDialogTitle("Open Audio File");
		fileChooser.setAcceptAllFileFilterUsed(false);

		int userChoice = fileChooser.showOpenDialog(this);
		if (userChoice == JFileChooser.APPROVE_OPTION) {
			audioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			lastOpenPath = fileChooser.getSelectedFile().getParent();

			playBack();
		}
	}

	private void playBack() {

		timer = new AudioPlayTimer(labelTimeCounter, sliderTime);
		timer.start();

		player = new Thread(new AudioPlayer(audioFilePath));
		player.setDaemon(true);
		player.start();

		isPlaying = false;

		playbackThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					buttonPlay.setText("Stop");
					buttonPlay.setEnabled(true);
					
					buttonPause.setText("Pause");
					buttonPause.setEnabled(true);

					labelFileName.setText("Playing File: " + audioFilePath);

					resetControls();

				} catch (Exception ex) {
					System.out.println(" unable to play back");
				}

			}
		});

		playbackThread.start();
	}

	private void stopPlaying() {
		isPlaying = false;

		buttonPause.setText("Pause");
		buttonPause.setEnabled(false);
		timer.reset();
		timer.interrupt();

		player.interrupt();

		playbackThread.interrupt();
	}
	
	private void pausePlaying() {
		buttonPause.setText("Resume");

		isPause = true;

		if(!player.isInterrupted()){
			timer.pauseTimer();
			playbackThread.interrupt();

			player.interrupt();
		}


	}
	
	private void resumePlaying() {
		buttonPause.setText("Pause");
		isPause = false;

		if(player.isInterrupted()){
			buttonPause.setText("Pause");
			timer.resumeTimer();
			playbackThread.interrupt();

			player.start();
		}

	}
	
	private void resetControls() {
		timer.reset();
		timer.interrupt();

		buttonPlay.setText("Play");

		buttonPause.setEnabled(false);
		
		isPlaying = false;		
	}

	class AudioPlayer implements Runnable {
		private static final int SECONDS_IN_HOUR = 60 * 60;
		private static final int SECONDS_IN_MINUTE = 60;
		private final File audioFile;


		public AudioPlayer(String filePath) {
			audioFile = new File(filePath);
		}

		public long getClipSecondLength() {
			return audioClip.getMicrosecondLength() / 1_000_000;
		}

		public String getClipLengthString() {
			String length = "";
			long hour = 0;
			long minute = 0;
			long seconds = audioClip.getMicrosecondLength() / 1_000_000;

			System.out.println(seconds);

			if (seconds >= SECONDS_IN_HOUR) {
				hour = seconds / SECONDS_IN_HOUR;
				length = String.format("%02d:", hour);
			} else {
				length += "00:";
			}

			minute = seconds - hour * SECONDS_IN_HOUR;
			if (minute >= SECONDS_IN_MINUTE) {
				minute = minute / SECONDS_IN_MINUTE;
				length += String.format("%02d:", minute);

			} else {
				minute = 0;
				length += "00:";
			}

			long second = seconds - hour * SECONDS_IN_HOUR - minute * SECONDS_IN_MINUTE;

			length += String.format("%02d", second);

			return length;
		}

		@Override
		public void run() {

			try {
				while (!Thread.currentThread().isInterrupted()) {

					AudioInputStream audioStream = AudioSystem
							.getAudioInputStream(audioFile);

					AudioFormat format = audioStream.getFormat();

					DataLine.Info info = new DataLine.Info(Clip.class, format);

					audioClip = (Clip) AudioSystem.getLine(info);

					audioClip.open(audioStream);
				}
				audioClip.start();

			}catch (Exception e){
				System.out.println(" Unable to load file into audio player " + e);
			}

			audioClip.stop();
			audioClip.close();
		}
	}

	class AudioPlayTimer extends Thread {
		private DateFormat dateFormater = new SimpleDateFormat("HH:mm:ss");
		private boolean isRunning = false;
		private boolean isPause = false;
		private boolean isReset = false;
		private long startTime;
		private long pauseTime;

		private JLabel labelRecordTime;
		private JSlider slider;

		public AudioPlayTimer(JLabel labelRecordTime, JSlider slider) {
			this.labelRecordTime = labelRecordTime;
			this.slider = slider;
		}

		public void run() {
			isRunning = true;

			startTime = System.currentTimeMillis();

			while (isRunning) {
				try {
					if (!isPause) {
						if (audioClip != null && audioClip.isRunning()) {
							labelRecordTime.setText(toTimeString());
							int currentSecond = (int) audioClip.getMicrosecondPosition() / 1_000_000;
							slider.setValue(currentSecond);
						}
					} else {
						pauseTime += 100;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if (isReset) {
						slider.setValue(0);
						labelRecordTime.setText("00:00:00");
						isRunning = false;
						break;
					}
				}
			}
		}


		/**
		 * Reset counting to "00:00:00"
		 */
		public void reset() {
			isReset = true;
			isRunning = false;
		}

		public void pauseTimer() {
			isPause = true;
		}

		public void resumeTimer() {
			isPause = false;
		}

		/**
		 * Generate a String for time counter in the format of "HH:mm:ss"
		 * @return the time counter
		 */
		private String toTimeString() {
			long now = System.currentTimeMillis();
			Date current = new Date(now - startTime - pauseTime);
			dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
			String timeCounter = dateFormater.format(current);
			return timeCounter;
		}
	}

}
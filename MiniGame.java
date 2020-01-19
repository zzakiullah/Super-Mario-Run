/*
 * MiniGame.java
 * Zulaikha Zakiullah
 * This is an oversimplified version of New Super Mario Bros Wii. It's kind of like Super Mario Run in the sense that it's an endless run until you die, but
 * the enemies, pipes, and coins are randomly generated. Some of the pipes do lead underground or into the sky, and at the same time some pipes do contain
 * piranha plants in them.
 *
 * Note: When jumping on top of pipes, you may fall through them at times.
 */

import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class MiniGame extends JFrame implements ActionListener, KeyListener {
	public static final int BELOW_GROUND = 0, ABOVE_GROUND = 1, IN_SKY = 2;
		
	// all panels
	JLayeredPane introPane;
	JLayeredPane gamePane;
	JLayeredPane howToPlayPane;
	JLayeredPane highScorePane;
	JLayeredPane creditsPane;
	
	GamePanel game; // where game itself is contained
	
	JLabel introBack1, introBack2, fadeBack;  // introBacks - background for intro scene; fadeBack - used for fadeout when transitioning to game
	Icon [] fadePics; 				// used for fadeout sequence when starting game
	
	Music introClip;
	
	javax.swing.Timer myTimer, sceneTimer;  // myTimer - general timer; sceneTimer - used specifically for fading scene when pressing "start" to play
	
	JPanel cards, scorePanel;  // scorePanel - contains text of top 10 high scores
	CardLayout cLayout = new CardLayout();
	String cardShown = "intro", prevCardShown = "";  // cardShown - current card showing; prevCardShown - previous card shown

	ArrayList<String []> scoreStats = new ArrayList<String []>(); // contains names of players and their high scores
	
	int frameX, frameY;  // size of frame
	
	boolean paused = false, fadingIn = false, fadingOut = false, soundOn = true, musicOn = true;
	// paused - flag for whether game is paused or not
	// fadingIn - flag for whether screen is fading in to new JLayeredPane; fadingOut - flag for whether screen is fading out to new JLayeredPane 
	// soundOn - whether volume for sound clips is on or not; musicOn - whether volume for music clips is on or not
	
	// all buttons
	JButton startBtn = new JButton(new ImageIcon("Pictures/other/start.png"));
	JButton howToPlayBtn = new JButton(new ImageIcon("Pictures/other/how-to-play.png"));
	JButton highScoreBtn = new JButton(new ImageIcon("Pictures/other/high-scores.png"));
	JButton creditsBtn = new JButton(new ImageIcon("Pictures/other/credits.png"));
	JButton settingsBtn = new JButton(new ImageIcon("Pictures/other/settings.png"));
	JButton backBtn = new JButton(new ImageIcon("Pictures/other/back.png"));
	JButton pauseBtn = new JButton(new ImageIcon("Pictures/other/pause.png"));
	JButton soundBtn = new JButton(new ImageIcon("Pictures/other/sound.png")); 
	JButton musicBtn = new JButton(new ImageIcon("Pictures/other/music.png")); 
	JLabel cancelSound = new JLabel(new ImageIcon("Pictures/other/cancel.png"));
	JLabel cancelMusic = new JLabel(new ImageIcon("Pictures/other/cancel.png"));
		
	public static void main(String [] args) {
		new MiniGame(1200, 700);
	}
	
    public MiniGame(int frameWidth, int frameHeight) {
    	super("Super Mario Mini Game");
    	setIconImage(new ImageIcon("Pictures/other/Mario-logo.png").getImage());  // setting JFrame icon
    	setSize(frameWidth, frameHeight);
    	
    	frameX = frameWidth;
    	frameY = frameHeight;
    	
    	// Timers
    	myTimer = new javax.swing.Timer(10, this);
    	myTimer.start();
    	sceneTimer = new javax.swing.Timer(50, this);
    	
    	// retrieving high scores from file
    	readHighScores("high-scores.txt");
    	
    	// loading images in fadeout sequence
    	String filePath = "Pictures/transitions/fade-out/";
    	try {
			File dir = new File(filePath);
			fadePics = new Icon[dir.list().length];
			for (int i=0; i<dir.list().length; i++) {
				fadePics[i] = new ImageIcon(filePath+dir.list()[i]);
			}
		}
		catch(Exception ex) {
			System.err.println(ex+": "+filePath);
		}
		fadeBack = new JLabel();
		fadeBack.setSize(frameX, frameY);
    	fadeBack.setLocation(0, 0);
    	
    	// -------------------- Buttons ------------------- //
    	
    	startBtn.addActionListener(this);
    	startBtn.setSize(206, 70);
    	startBtn.setLocation(frameWidth/2-startBtn.getWidth()/2, 295);
    	startBtn.setContentAreaFilled(false);
    	startBtn.setFocusPainted(false);
        startBtn.setBorderPainted(false);
    	
    	howToPlayBtn.addActionListener(this);
    	howToPlayBtn.setSize(288, 50);
    	howToPlayBtn.setLocation(frameWidth/2-howToPlayBtn.getWidth()/2, 510);
    	howToPlayBtn.setContentAreaFilled(false);
    	howToPlayBtn.setFocusPainted(false);
        howToPlayBtn.setBorderPainted(false);
    	
    	highScoreBtn.addActionListener(this);
    	highScoreBtn.setSize(264, 50);
    	highScoreBtn.setLocation(frameWidth/2-highScoreBtn.getWidth()/2, 560);
    	highScoreBtn.setContentAreaFilled(false);
    	highScoreBtn.setFocusPainted(false);
        highScoreBtn.setBorderPainted(false);
    	
    	creditsBtn.addActionListener(this);
    	creditsBtn.setSize(171, 50);
    	creditsBtn.setLocation(frameWidth/2-creditsBtn.getWidth()/2, 610);
    	creditsBtn.setContentAreaFilled(false);
    	creditsBtn.setFocusPainted(false);
        creditsBtn.setBorderPainted(false);
    	
		backBtn.addActionListener(this);
		backBtn.setSize(105, 50);
		backBtn.setLocation(8, 610);
		backBtn.setContentAreaFilled(false);
    	backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
		
		pauseBtn.addActionListener(this);
		pauseBtn.setSize(54, 54);
		pauseBtn.setLocation(1130, 610);
		pauseBtn.setContentAreaFilled(false);
    	pauseBtn.setFocusPainted(false);
        pauseBtn.setBorderPainted(false);
		
		soundBtn.addActionListener(this);
		soundBtn.setSize(54, 54);
		soundBtn.setLocation(1065, 610);
		soundBtn.setContentAreaFilled(false);
    	soundBtn.setFocusPainted(false);
        soundBtn.setBorderPainted(false);
        
        musicBtn.addActionListener(this);
		musicBtn.setSize(54, 54);
		musicBtn.setLocation(1000, 610);
		musicBtn.setContentAreaFilled(false);
    	musicBtn.setFocusPainted(false);
        musicBtn.setBorderPainted(false);

        settingsBtn.addActionListener(this);
		settingsBtn.setSize(54, 54);
		settingsBtn.setLocation(1130, 610);
		settingsBtn.setContentAreaFilled(false);
    	settingsBtn.setFocusPainted(false);
        settingsBtn.setBorderPainted(false);
		
		// cancel labels
		cancelSound.setSize(44, 44);
		cancelSound.setLocation(soundBtn.getX()+soundBtn.getWidth()/2-cancelSound.getWidth()/2, soundBtn.getY()+soundBtn.getHeight()/2-cancelSound.getHeight()/2);
		cancelSound.setVisible(false);
		cancelMusic.setSize(44, 44);
		cancelMusic.setLocation(musicBtn.getX()+musicBtn.getWidth()/2-cancelMusic.getWidth()/2, musicBtn.getY()+musicBtn.getHeight()/2-cancelMusic.getHeight()/2);
		cancelMusic.setVisible(false);
		
    	// --------------------- Panels --------------------- //
    	
    	// intro panel
    	introPane = new JLayeredPane();
    	introPane.setLayout(null);
    	introBack1 = new JLabel(new ImageIcon("Pictures/other/backdrop.png"));
    	introBack2 = new JLabel(new ImageIcon("Pictures/other/backdrop.png"));
    	JLabel player = new JLabel(new ImageIcon("Pictures/other/mario-run.gif"));
    	JLabel title = new JLabel(new ImageIcon("Pictures/other/title.png"));
    	introBack1.setSize(frameX, frameY);
    	introBack2.setSize(frameX, frameY);
    	player.setSize(108, 106);
    	title.setSize(643, 276);
    	introBack1.setLocation(0, 0);
    	introBack2.setLocation(1200, 0);
    	player.setLocation(frameX/2-player.getWidth()/2, frameY-200-player.getHeight());
    	title.setLocation(frameX/2-title.getWidth()/2, 20);
    	introPane.add(introBack1, JLayeredPane.DEFAULT_LAYER);
    	introPane.add(introBack2, JLayeredPane.DEFAULT_LAYER);
    	introPane.add(player, JLayeredPane.PALETTE_LAYER);
    	introPane.add(title, JLayeredPane.PALETTE_LAYER);
    	introPane.add(startBtn, JLayeredPane.MODAL_LAYER);
    	introPane.add(howToPlayBtn, JLayeredPane.MODAL_LAYER);
    	introPane.add(highScoreBtn, JLayeredPane.MODAL_LAYER);
    	introPane.add(creditsBtn, JLayeredPane.MODAL_LAYER);
    	introPane.add(settingsBtn, JLayeredPane.MODAL_LAYER);
    	
    	// game panel
    	gamePane = new JLayeredPane();
    	gamePane.setLayout(null);
    	game = new GamePanel(frameWidth, frameHeight, myTimer.getDelay());
    	game.setSize(frameX, frameY);
    	game.setLocation(0, 0);
    	gamePane.add(game, JLayeredPane.DEFAULT_LAYER);
    	gamePane.add(pauseBtn, JLayeredPane.MODAL_LAYER);
    	gamePane.add(soundBtn, JLayeredPane.MODAL_LAYER);
    	gamePane.add(musicBtn, JLayeredPane.MODAL_LAYER);
    	gamePane.add(cancelSound, JLayeredPane.POPUP_LAYER);
    	gamePane.add(cancelMusic, JLayeredPane.POPUP_LAYER);
    	
    	// how-to-play panel
    	howToPlayPane = new JLayeredPane();
    	howToPlayPane.setLayout(null);
    	JLabel howToPlayBack = new JLabel(new ImageIcon("Pictures/other/how-to-play-back.png"));
    	howToPlayBack.setSize(frameX, frameY);
    	howToPlayBack.setLocation(0, 0);
    	howToPlayPane.add(howToPlayBack, JLayeredPane.DEFAULT_LAYER);
    	
    	// high score panel
    	highScorePane = new JLayeredPane();
    	highScorePane.setLayout(null);
    	JLabel highScoreBack = new JLabel(new ImageIcon("Pictures/other/high-scores-back.png"));
    	highScoreBack.setSize(frameX, frameY);
    	highScoreBack.setLocation(0, 0);
    	highScorePane.add(highScoreBack, JLayeredPane.DEFAULT_LAYER);
    	scorePanel = getHighScorePanel();
    	highScorePane.add(scorePanel, JLayeredPane.PALETTE_LAYER);
    	
    	// credits panel
    	creditsPane = new JLayeredPane();
    	creditsPane.setLayout(null);
    	JLabel creditsBack = new JLabel(new ImageIcon("Pictures/other/credits-back.png"));
    	creditsBack.setSize(frameX, frameY);
    	creditsBack.setLocation(0, 0);
    	creditsPane.add(creditsBack, JLayeredPane.DEFAULT_LAYER);
    	
    	// -------------------------------------------------- //
    	
    	// adding panels to card layout
    	cards = new JPanel(cLayout);
    	cards.add(introPane, "intro");
    	cards.add(gamePane, "game");
    	cards.add(howToPlayPane, "how to play");
    	cards.add(highScorePane, "high scores");
    	cards.add(creditsPane, "credits");
    	add(cards);
    	
    	// plays intro music
    	introClip = new Music("Sounds/title-theme.wav", musicOn);
    	introClip.play(Clip.LOOP_CONTINUOUSLY);
    	
    	addKeyListener(this);
    	requestFocus();
    	setLocationRelativeTo(null);  // centre the window
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setResizable(false);
    	setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
    	Object source = e.getSource();
    	Component frameParent = getFrames()[0];  // frame that is dialog boxes' parent
    	
    	if (source == startBtn) {
    		new Music("Sounds/start.wav", soundOn).play(0);
    		introClip.getClip().stop();
    		
    		// add fading background to introPane
    		introPane.add(fadeBack, JLayeredPane.DRAG_LAYER);
    		sceneTimer.start();
    		fadingOut = true;
    	}
    	else if (source == howToPlayBtn) {
    		if (!(prevCardShown.equals("how to play"))) {
    			howToPlayPane.add(backBtn, JLayeredPane.MODAL_LAYER);
    		}
    		new Music("Sounds/choose.wav", soundOn).play(0);
    		introClip.getClip().stop();
    		introClip = new Music("Sounds/main-menu.wav", musicOn); 
    		introClip.play(Clip.LOOP_CONTINUOUSLY);
    		
    		prevCardShown = cardShown;
    		cardShown = "how to play";
    		cLayout.show(cards, "how to play");
    	}
    	else if (source == highScoreBtn) {
    		if (!(prevCardShown.equals("high scores"))) {
    			highScorePane.add(backBtn, JLayeredPane.MODAL_LAYER);
    		}
    		
    		new Music("Sounds/choose.wav", soundOn).play(0);
    		introClip.getClip().stop();
    		introClip = new Music("Sounds/main-menu.wav", musicOn);
    		introClip.play(Clip.LOOP_CONTINUOUSLY);
    		
    		highScorePane.remove(scorePanel);
    		highScorePane.revalidate();
    		scorePanel = getHighScorePanel();
    		highScorePane.add(scorePanel, JLayeredPane.PALETTE_LAYER);
    		
    		prevCardShown = cardShown;
    		cardShown = "high scores";
    		cLayout.show(cards, "high scores");
    	}
    	else if (source == creditsBtn) {
    		if (!(prevCardShown.equals("credits"))) {
    			creditsPane.add(backBtn, JLayeredPane.MODAL_LAYER);
    		}
    		
    		new Music("Sounds/choose.wav", soundOn).play(0);
    		introClip.getClip().stop();
    		introClip = new Music("Sounds/credits-theme.wav", musicOn);
    		introClip.play(Clip.LOOP_CONTINUOUSLY);
    		
    		prevCardShown = cardShown;
    		cardShown = "credits";
    		cLayout.show(cards, "credits");
    	}
    	else if (source == settingsBtn) {
    		new Music("Sounds/choose.wav", soundOn).play(0);
    		Object settingsMsg = "Sound/Music Control";
    		Object [] opts = {"Sound "+(soundOn ? "Off" : "On"), "Music "+(musicOn ? "Off" : "On"), "Cancel"};
    		int settingsAns = new JOptionPane().showOptionDialog(frameParent, settingsMsg, "Settings", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opts, opts[2]);
    		if (settingsAns == 0) {  		// if user chooses to turn sound on/off
    			soundOn = soundOn ? false : true;
    			new Music("Sounds/choose.wav", soundOn).play(0);
    			cancelSound.setVisible(soundOn ? false : true);
    		}
    		else if (settingsAns == 1) {	// if user chooses to turn music on/off
    			musicOn = musicOn ? false : true;
    			new Music("Sounds/choose.wav", soundOn).play(0);
    			cancelMusic.setVisible(musicOn ? false : true);
    			introClip.getClip().stop();
    			introClip.setVolumeOn(musicOn);
    			introClip.play(Clip.LOOP_CONTINUOUSLY);
    		}
    		else {							// if user clicks "Cancel"
    			new Music("Sounds/cancel.wav", soundOn).play(0);
    		}
    		game.setSoundOn(soundOn);
    		game.setMusicOn(musicOn);
    	}
    	else if (source == backBtn) {
    		prevCardShown = cardShown;
    		
    		new Music("Sounds/cancel.wav", soundOn).play(0);
    		introClip.getClip().stop();
    		introClip = new Music("Sounds/title-theme.wav", musicOn);
    		introClip.play(Clip.LOOP_CONTINUOUSLY);
    		
    		cardShown = "intro";
    		cLayout.show(cards, "intro");
    	}
    	else if (source == pauseBtn) {
    		new Music("Sounds/pause.wav", soundOn).play(0);
    		game.stopBackMusic();
    		paused = true;
	    	while (true) {
	    		// display dialog box to ask what user would like to do (continue or exit course)
	    		Object pauseMsg = "Current Score: "+Long.toString(game.getScore())+"\nWhat would you like to do?";
	    		Object [] opts = {"Continue", "Exit Course"};  // options user can choose from dialog box
	    		int pauseAns = new JOptionPane().showOptionDialog(frameParent, pauseMsg, "Game Paused", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
    			if (pauseAns == 0) {  // if user chooses to continue game
	    			new Music("Sounds/cancel.wav", soundOn).play(0);
	    			game.playBackMusic();
	    			gamePane.requestFocus();
	    			break;
	    		}
	    		else {  // if user chooses to exit course
	    			new Music("Sounds/choose.wav", soundOn).play(0);
	    			// displaying dialog box to ask user if they really want to exit course
		    		Object exitMsg = "Are you sure you want to exit this course?";  // message shown in dialog box
		    		int exitAns = new JOptionPane().showConfirmDialog(frameParent, exitMsg, "Exiting Course", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
		    		if (exitAns == 0) {  // if user chose "yes" to exiting course
		    			new Music("Sounds/choose.wav", soundOn).play(0);
		    			game.closeBackMusic();
		    			
		    			prevCardShown = cardShown;
		    			
    					soundOn = game.getSoundOn();
    					musicOn = game.getMusicOn();
    					introClip.getClip().stop();
    					introClip = new Music("Sounds/title-theme.wav", musicOn);
    					introClip.play(Clip.LOOP_CONTINUOUSLY);
		    			
		    			cardShown = "intro";
		    			cLayout.show(cards, "intro");
		    			introPane.requestFocus();
		    			break;
		    		}
		    		else {  // if user chose "no" to exiting course
		    			new Music("Sounds/cancel.wav", soundOn).play(0);
		    		}
	    		}
    		}
    		requestFocus();
    		paused = false;
	    }
	    else if (source == soundBtn && soundOn) {
	    	soundOn = false;
	    	game.setSoundOn(false);
	    	cancelSound.setVisible(true);
	    	gamePane.requestFocus();
	    	requestFocus();
	    }
	    else if (source == soundBtn && soundOn == false) {
	    	soundOn = true;
	    	game.setSoundOn(true);
	    	cancelSound.setVisible(false);
	    	gamePane.requestFocus();
	    	requestFocus();
	    }
	     else if (source == musicBtn && musicOn) {
	    	musicOn = false;
	    	game.setMusicOn(false);
	    	introClip.setVolumeOn(false);
	    	cancelMusic.setVisible(true);
	    	gamePane.requestFocus();
	    	requestFocus();
	    }
	    else if (source == musicBtn && musicOn == false) {
	    	musicOn = true;
	    	game.setMusicOn(true);
	    	introClip.setVolumeOn(true);
	    	cancelMusic.setVisible(false);
	    	gamePane.requestFocus();
	    	requestFocus();
	    }
    	if (source == myTimer && cardShown.equals("game") && gamePane != null && game != null && paused == false) {
    		game.refresh();
    		game.repaint();
    		if (game.isLost()) {
    			musicBtn.setVisible(false);
    			soundBtn.setVisible(false);
    			pauseBtn.setVisible(false);
    			cancelMusic.setVisible(false);
    			cancelSound.setVisible(false);
    		}
    		else if (game.isDone()) {
    			// check if player's score can be put in high scores
    			if (getHighScorePos(game.getScore()) != -1) {
    				Object highScoreMsg = "Enter your name:";
	    			String name = new JOptionPane().showInputDialog(frameParent, highScoreMsg, "You Got a High Score!", JOptionPane.INFORMATION_MESSAGE);
    				name = name == null ? "Unknown" : name;
    				updateHighScores(getHighScorePos(game.getScore()), name, game.getScore());
    				writeHighScores("high-scores.txt");
    			}
    			// ask player whether to restart or exit
    			Object finishMsg = "What would you like to do?";
    			Object [] opts = {"Restart", "Exit"};
    			int finishAns = new JOptionPane().showOptionDialog(frameParent, finishMsg, "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
    			if (finishAns == 0) {  // if player chooses to restart game
    				game.reset();
    				game.playBackMusic();
    			}
    			else {				   // if player chooses to exit (go back to main menu)
    				game.reset();
    				prevCardShown = cardShown;
    				cardShown = "intro";
    				cLayout.show(cards, "intro");
    			}
    			musicBtn.setVisible(true);
    			soundBtn.setVisible(true);
    			pauseBtn.setVisible(true);
    			cancelMusic.setVisible(musicOn ? false : true);
    			cancelSound.setVisible(soundOn ? false : true);
    		}
    	}
    	else if (source == myTimer && cardShown.equals("intro") && introBack1 != null && introBack2 != null) {
    		int xPos1 = introBack1.getX()-5 <= -1*frameX ? frameX : introBack1.getX()-5;
    		int xPos2 = introBack2.getX()-5 <= -1*frameX ? frameX : introBack2.getX()-5;
    		introBack1.setLocation(xPos1, 0);
    		introBack2.setLocation(xPos2, 0);
    	}
    	
    	if (source == sceneTimer && fadingOut) {		// if sceneTimer is running and scene is fading out of intro screen
    		if (fadeBack.getIcon() == null) {
    			fadeBack.setIcon(fadePics[fadePics.length-1]);
    		}
    		else if (Arrays.asList(fadePics).indexOf(fadeBack.getIcon()) > 0) {
    			fadeBack.setIcon(fadePics[Arrays.asList(fadePics).indexOf(fadeBack.getIcon())-1]);
    		}
    		else {
    			fadingOut = false;
    			fadingIn = true;
    			fadeBack.setIcon(fadePics[0]);
    			gamePane.add(fadeBack, JLayeredPane.DRAG_LAYER);
    			prevCardShown = cardShown;
	    		cardShown = "game";
	    		cLayout.show(cards, "game");
	    		introPane.remove(fadeBack);
	    		introPane.revalidate();
	    		introPane.repaint();
	    		
	    		gamePane.requestFocus();
	    		requestFocus();
    		}
    	}
    	else if (source == sceneTimer && fadingIn) {	// if sceneTimer is running and screen is fading into game screen
    		if (fadeBack.getIcon().equals(fadePics[0])) {
    			fadeBack.setIcon(fadePics[Arrays.asList(fadePics).indexOf(fadeBack.getIcon())+1]);
    			game.reset();
    			game.changeBackMusic();
    			game.playBackMusic();
    		}
    		else if (Arrays.asList(fadePics).indexOf(fadeBack.getIcon()) < fadePics.length-1) {
    			fadeBack.setIcon(fadePics[Arrays.asList(fadePics).indexOf(fadeBack.getIcon())+1]);
    		}
    		else {
    			gamePane.remove(fadeBack);
    			gamePane.revalidate();
    			fadingIn = false;
    			sceneTimer.stop();
    		}
    	}
    }
    
    // --------------- KeyListener methods -------------- //
   	public void keyTyped(KeyEvent e) {}
   	public void keyPressed(KeyEvent e) {
   		game.setKey(e.getKeyCode(), true);
   	}
   	public void keyReleased(KeyEvent e) {
		game.setKey(e.getKeyCode(), false);
   	}
   	// -------------------------------------------------- //
   	
   	public void readHighScores(String filePath) {   // reads high-scores txt file to get all current high scores
   		try {
   			Scanner inFile = new Scanner(new File(filePath));
   			int n = Integer.parseInt(inFile.nextLine());
   			for (int i=0; i<n; i++) {
   				String [] scoreStat = inFile.nextLine().split(",");
   				scoreStats.add(scoreStat);
   			}
   		}
   		catch(Exception ex) {
   			System.err.println(ex+": "+filePath);
   		}
   	}
   	public int getHighScorePos(long playerScore) {   // checks if player has gotten into the high score position and returns what position
   		for (int i=0; i<scoreStats.size(); i++) {
   			long score = Long.MAX_VALUE;
   			for (int j=0; j<scoreStats.get(i)[1].length(); j++) {
   				if (j == scoreStats.get(i)[1].length()-1 || !(scoreStats.get(i)[1].substring(j, j+1).equals("0"))) {
   					score = Long.parseLong(scoreStats.get(i)[1].substring(j));
   					break;
   				}
   			}
   			if (playerScore >= score) {
   				return i;
   			}
   		}
   		return -1;
   	}
   	public void updateHighScores(int pos, String name, long score) {  // updates high score ArrayList
   		String strScore = "";
   		for (int i=0; i<15-Long.toString(score).length(); i++) {
   			strScore += "0";
   		}
   		strScore += Long.toString(score);
   		scoreStats.add(pos, new String[] {name, strScore});
   		scoreStats.remove(scoreStats.size()-1);
   	}
   	
   	public JPanel getHighScorePanel() {  // displays high scores on a JPanel
   		JPanel scorePanel = new JPanel();
   		scorePanel.setLayout(null);
   		scorePanel.setOpaque(false);
   		scorePanel.setSize(frameX, frameY);
   		scorePanel.setLocation(0, 0);

   		Font agencyFB = new Font("Agency FB", Font.PLAIN, 40);
   		FontMetrics metrics = scorePanel.getFontMetrics(agencyFB);
   		for (int i=0; i<scoreStats.size(); i++) {
   			JLabel numTxt = new JLabel(Integer.toString(i+1)+")");
   			JLabel nameTxt = new JLabel(scoreStats.get(i)[0]);
   			JLabel scoreTxt = new JLabel(scoreStats.get(i)[1]);
   			numTxt.setForeground(Color.WHITE);
   			nameTxt.setForeground(Color.WHITE);
   			scoreTxt.setForeground(Color.WHITE);
   			numTxt.setFont(agencyFB);
   			nameTxt.setFont(agencyFB);
   			scoreTxt.setFont(agencyFB);
   			try {
   				numTxt.setSize(metrics.stringWidth(numTxt.getText()), metrics.getHeight());
   				nameTxt.setSize(metrics.stringWidth(nameTxt.getText()), metrics.getHeight());
   				scoreTxt.setSize(metrics.stringWidth(scoreTxt.getText()), metrics.getHeight());
   			}
   			catch(Exception ex) {
   				System.err.println(ex);
   			}
   			numTxt.setLocation(200, 80+metrics.getHeight()+i*numTxt.getHeight());
   			nameTxt.setLocation(240, 80+metrics.getHeight()+i*nameTxt.getHeight());
   			scoreTxt.setLocation(frameX-200-scoreTxt.getWidth(), 80+metrics.getHeight()+i*scoreTxt.getHeight());
   			scorePanel.add(numTxt);
   			scorePanel.add(nameTxt);
   			scorePanel.add(scoreTxt);
   		}
   		return scorePanel;
   	}
   	
   	public void writeHighScores(String filename) {  // overwrites high scores to txt file
   		try {
   			PrintWriter writer = new PrintWriter(filename); 
   			writer.println(scoreStats.size());
   			for (String [] scoreStat : scoreStats) {
   				writer.println(scoreStat[0]+","+scoreStat[1]);
   			}
   			writer.close();
   		}
   		catch(Exception ex) {
			System.err.println(ex+": "+filename);
		}
   	}
}


class Music {  // class that deals with the music/sounds, so multiple try/catch statements don't have to be written
	private Clip clip;
	private BooleanControl volumeControl;  	 // controls volume of clip
	private boolean volumeOn;

	public Music(String filePath, boolean volumeOn) {
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new File(filePath)));
			volumeControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
			this.volumeOn = volumeOn;
		}
		catch(Exception ex) {
			System.err.println(ex+": "+filePath);
		}
	}
	
	public void play(int loopNum) {  // plays song
	// filePath - directory of music/sound file; loopNum - number of times music/sound loops
		try {
			volumeControl.setValue(volumeOn ? false : true);
			clip.start();
			clip.loop(loopNum);
		}
		catch(Exception ex) {
			System.err.println(ex);
		}
	}
	
	public void setVolumeOn(boolean newVolumeOn) {
		volumeOn = newVolumeOn;
	}
	
	public Clip getClip() {
		return clip;
	}
}


class GamePanel extends JPanel {
	private final int GRAV = 1, BELOW_GROUND = 0, ABOVE_GROUND = 1, IN_SKY = 2, RECOVERY_TIME = 2000;
	// GRAV - gravity; BELOW_GROUND, ABOVE_GROUND, IN_SKY - represent 3 scenes in game, RECOVERY_TIME - max time player can spend recovering
	
	private Random rand = new Random();
	
	private Music musicClip;  
	// musicClip - used for playing background music
	private String [] musicFileNames;  // file names for background music
	
	private int numLives, numCoins, timeDelay, groundHeight, standHeight, frameX, frameY, sceneSpeed, scene, oldScene, timeElapsed, timeSwitching, timeTransitioning, centreX, centreY, hitTime;
	// snumLives - number of lives player has left; numCoins - number of coins player has collected
	// timeDelay - amount of time Timer triggers itself again; groundHeight - height of the ground in the scene; standHeight - height at which characters stand
	// frameX - width of frame; frameY - height of frame; sceneSpeed - amount the background shifts by every time the game refreshes
	// scene - int representing which scene is showing (ABOVE_GROUND, BELOW_GROUND, or IN_SKY)
	// oldScene - previous scene that was shown; newScene - scene that will be shown
	// timeElapsed - amount of time (in milliseconds) that game has been running
	// centreX, centreY - centre of circle drawn when changing scenes
	// hitTime - amount of time player has hit something
	
	private long score;
	
	private boolean switchingScene, goingInPipe, goingOutPipe, soundOn, musicOn, gameLost, gameDone, hitOnce;
	// switchingScene - flag for if player is travelling through a pipe
	// goingInPipe - if player is going in pipe; goingOutPipe - if player is going out pipe
	// soundOn - indicates whether volume for sound clips is on or not
	// musicOn - indicates whether volume for music clips is on or not
	// gameLost - whether player has lost game (has no lives left)
	// gameDone - if player has lost and game over sequence is finished
	// hitOnce - if player has hit goomba already; prevents game from constantly calling a game over from player colliding with same object after some milliseconds
	
	private boolean [] keys;			   // array of booleans indicating which keys are being pressed  
	private Image [][] scenePics;		   // sky and ground images for different scenes
	
	private Image [] coinPics, pipePics, whiteNumbers, yellowNumbers, sceneChangePics, bowserFacePics, gameOverPics;
	private Image [][] plantPics, goombaPics;
	private Image sceneChangePic, bowserFacePic, gameOverPic;
	// coin pics - contains pictures in coin animation sequence; pipePics - all variations of pipes (colours and orientation)
	// whiteNumbers - numbers used when displaying score and number of lives; yellowNumbers - numbers used when displaying number of coins
	// plantPics - pictures in piranha plant animation sequence (up and down orientation)
	// goombaPics - pictures for goomba's move sequence and squash sequence (when it is being squashed by player)
	// whiteNumbers - numbers in Super Mario 256 font used for dislaying score and number of lives
	// yellowNumbers - numbers in Super Mario 256 font used for dislaying number of coins
	// sceneChangePics - pics used when scene is changing; sceneChangePic - current transition pic shown
	// bowserFacePics - pics used when player loses
	
	private Rectangle [][] plantHitboxes;  // rectangle objects that hold the boundaries which the piranha plant takes up
	
	private int [] backXPos;  // contains x position of ground and sky
	
	// ArrayLists of objects on screen
	private ArrayList<Coin> coins;
	private ArrayList<Pipe> pipes;
	private ArrayList<PiranhaPlant> plants;
	private ArrayList<Goomba> goombas;
	
	private Player hero;  // user/player
	
	private Pipe pipeHeroIsAt;  // pipe that player is currently standing on or is directly under; can be null
	
	public GamePanel(int frameWidth, int frameHeight, int delay) {  
	// delay - amount of time Timer triggers itself again
		soundOn = musicOn = true;
		musicFileNames = new String[] {"Sounds/underground-theme.wav", "Sounds/ground-theme.wav", "Sounds/sky-theme.wav"};
		scene = oldScene = ABOVE_GROUND;
		musicClip = new Music(musicFileNames[scene], musicOn);
		
		score = 0;
		numCoins = timeElapsed = timeSwitching = timeTransitioning = centreX = centreY = hitTime = 0;
		numLives = 4;
		sceneSpeed = 5;
		timeDelay = delay;
		keys = new boolean[KeyEvent.KEY_LAST+1];
		frameX = frameWidth;
		frameY = frameHeight;
		backXPos = new int [] {0, 1200};
		switchingScene = goingInPipe = goingOutPipe = gameLost = gameDone = hitOnce = false;
		
		coins = new ArrayList<Coin>();
		plants = new ArrayList<PiranhaPlant>();
		pipes = new ArrayList<Pipe>();
		goombas = new ArrayList<Goomba>();
		
		scenePics = new Image [][] {loadPics("Pictures/background/below_ground/"), loadPics("Pictures/background/above_ground/"), loadPics("Pictures/background/in_sky/")};
		
		coinPics = loadPics("Pictures/coin/");
		pipePics = loadPics("Pictures/pipes/");
		goombaPics = new Image [][] {loadPics("Pictures/enemies/goomba/move/"), loadPics("Pictures/enemies/goomba/squash/")};
		plantPics = new Image [][] {loadPics("Pictures/enemies/piranha-plant/up/"), loadPics("Pictures/enemies/piranha-plant/down/")};
		plantHitboxes = new Rectangle [][] {makeHitboxes("Pictures/enemies/piranha-plant/up/"), makeHitboxes("Pictures/enemies/piranha-plant/down/")};
		groundHeight = standHeight = scenePics[scene][0].getHeight(this);
		
		whiteNumbers = loadPics("Pictures/numbers/white/");
		yellowNumbers = loadPics("Pictures/numbers/yellow/");
		
		sceneChangePics = loadPics("Pictures/transitions/scene-change/");
		bowserFacePics = loadPics("Pictures/transitions/bowser-faces/");
		gameOverPics = loadPics("Pictures/transitions/game-over/");
		
		// loading player sprite images
		Image [] imgs = loadPics("Pictures/player-move/");
		// making player
		hero = new Player(imgs, frameX/2-imgs[0].getWidth(this)/2, frameY-groundHeight-imgs[0].getHeight(this), imgs[0].getWidth(this), imgs[0].getHeight(this), 0, 5, 0);
	}
	
	// resets all stats at beginning of game
	public void reset() {
		scene = oldScene = ABOVE_GROUND;
		musicClip = new Music(musicFileNames[scene], musicOn);
		
		score = 0;
		numCoins = timeElapsed = timeSwitching = timeTransitioning = centreX = centreY = hitTime = 0;
		numLives = 4;
		sceneSpeed = 5;
		keys = new boolean[KeyEvent.KEY_LAST+1];
		backXPos = new int [] {0, 1200};
		switchingScene = goingInPipe = goingOutPipe = gameLost = gameDone = hitOnce = false;
		
		coins = new ArrayList<Coin>();
		plants = new ArrayList<PiranhaPlant>();
		pipes = new ArrayList<Pipe>();
		goombas = new ArrayList<Goomba>();
		
		groundHeight = standHeight = scenePics[scene][0].getHeight(this);
		
		pipeHeroIsAt = null;
		sceneChangePic = bowserFacePic = gameOverPic = null;
		
		Image [] imgs = loadPics("Pictures/player-move/");
		hero = new Player(imgs, frameX/2-imgs[0].getWidth(this)/2, frameY-groundHeight-imgs[0].getHeight(this), imgs[0].getWidth(this), imgs[0].getHeight(this), 0, 5, 0);
	}
	
	// used for handling game music in public class MiniGame
	public void changeBackMusic() {
		musicClip = new Music(musicFileNames[scene], musicOn);
	}
	public void playBackMusic() {
		musicClip.play(Clip.LOOP_CONTINUOUSLY);
	}
	public void stopBackMusic() {
		musicClip.getClip().stop();
	}
	public void closeBackMusic() {
		musicClip.getClip().close();
	}
	public void setSoundOn(boolean newSoundOn) {
		soundOn = newSoundOn;
	}
	public void setMusicOn(boolean newMusicOn) {
		musicOn = newMusicOn;
		if (musicClip.getClip().isRunning()) {
			musicClip.getClip().stop();
			musicClip.setVolumeOn(musicOn);
			musicClip.play(Clip.LOOP_CONTINUOUSLY);
		}
		else {
			musicClip.setVolumeOn(musicOn);
		}
	}
	public boolean getSoundOn() {
		return soundOn;
	}
	public boolean getMusicOn() {
		return musicOn;
	}

	public void setKey(int pos, boolean state) {  // set which keys are being pressed
		keys[pos] = state;
	} 

	public long getScore() {   // returns player's score (to be displayed in pause dialog box)
		return score;
	}
	public boolean isLost() {  // checks if player has lost game
		return gameLost;
	}
	public boolean isDone() {  // checks if game is done (player has lost and game over sequence is finished)
		return gameDone;
	}
	
	public Image [] loadPics(String filePath) {  // loading images for sprites and putting into an array
		try {
			File dir = new File(filePath);
			Image [] pics = new Image[dir.list().length];
			for (int i=0; i<dir.list().length; i++) {
				pics[i] = new ImageIcon(filePath+dir.list()[i]).getImage();
			}
			return pics;
		}
		catch(Exception ex) {
			System.err.println(ex+": "+filePath);
		}
		return new Image[] {};
	}
	
	public Rectangle [] makeHitboxes(String filePath) {  // makes array of hitboxes for characters on screen
		try {
			File dir = new File(filePath);
			// minimum and maximum x and y pos in which a coloured/shaded pixel is found
			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
			Rectangle [] hitboxes = new Rectangle [dir.list().length];
			for (int i=0; i<dir.list().length; i++) {
				BufferedImage img = ImageIO.read(new File(filePath+dir.list()[i]));
				for (int x=0; x<img.getWidth(); x++) {
					for (int y=0; y<img.getHeight(); y++) {
						if (img.getRGB(x, y) != 0 && x < minX) {
							minX = x;
						}
						if (img.getRGB(x, y) != 0 && y < minY) {
							minY = y;
						}
						if (img.getRGB(x, y) != 0 && x > maxX) {
							maxX = x;
						}
						if (img.getRGB(x, y) != 0 && y > maxY) {
							maxY = y;
						}
					}
				}
				if (minX == Integer.MAX_VALUE && minY == Integer.MAX_VALUE && maxX == Integer.MIN_VALUE && maxY == Integer.MIN_VALUE) {
					hitboxes[i] = null;
				}
				else {
					hitboxes[i] = new Rectangle(minX, minY, maxX-minX, maxY-minY);
				}
			}
			return hitboxes;
		}
		catch(Exception ex) {
			System.err.println(ex+": "+filePath);
		}
		return new Rectangle [] {};
	}
		
	public int changeFrame(int frameNum, int seqSize, int maxTime) {  // change picture in an object's move sequence
		if (timeElapsed % maxTime == 0) {
			if (frameNum+1 == seqSize) {
				return 0;
			}
			else {
				return frameNum+1;
			}
		}
		return frameNum;
	}
	
	public void makePipes() {   // making pipes to put on screen
		int yPos, type;
		// first 4 pipe images in pipePics are pipes leading down, last 4 are pipes leading up; pipe has 50% chance of pointing up and 50% chance pointing down
		int pipePicPos = rand.nextInt(pipePics.length);
		Image pipePic = pipePics[pipePicPos];  // randomly chooses a pipe image
		if (pipePicPos < 4) {   // first 4 pipe images are pipes that lead down
			yPos = frameY-groundHeight-60-rand.nextInt(frameY-(groundHeight+210));  // mouth of the pipe may start 150 pixels from top of screen to 60 pixels above the ground
			type = Pipe.UP;
		}
		else {  	// last 4 pipe images are pipes that lead up
			yPos = 60+rand.nextInt(frameY-(groundHeight+210))-pipePic.getHeight(this); // mouth of the pipe may start 60 pixels from top of screen to 150 pixels above the ground
			type = Pipe.DOWN;
		}
		boolean containsPlant = (rand.nextFloat() >= 0.5);
		if (containsPlant && type == Pipe.UP) {
			plants.add(new PiranhaPlant(plantPics[type-1], frameX+(pipePic.getWidth(this)/2)-(plantPics[type-1][0].getWidth(this)/2), yPos-plantPics[type-1][0].getHeight(this), plantPics[type-1][0].getWidth(this), plantPics[type-1][0].getHeight(this), 0, type, plantHitboxes[type-1]));
		}
		else if (containsPlant && type == Pipe.DOWN) {
			plants.add(new PiranhaPlant(plantPics[type-1], frameX+(pipePic.getWidth(this)/2)-(plantPics[type-1][0].getWidth(this)/2), yPos+pipePic.getHeight(this), plantPics[type-1][0].getWidth(this), plantPics[type-1][0].getHeight(this), 0, type, plantHitboxes[type-1]));
		}
		pipes.add(new Pipe(new Image [] {pipePic}, frameX, yPos, pipePic.getWidth(this), pipePic.getHeight(this), 0, containsPlant, type, (rand.nextFloat() >= 0.5)));
	}
	
	public void makeCoins(int frameNum) {
		coins.add(new Coin(coinPics, frameX, frameY-groundHeight-coinPics[0].getHeight(this), coinPics[0].getWidth(this), coinPics[0].getHeight(this), frameNum));
	}
	
	public void makeGoombas() {     // making goombas to put on screen
		//(Image [] moveSeq, int x, int y, int width, int height, int frameNum, Image [] squashSeq, int sqFrameNum) {
		goombas.add(new Goomba(goombaPics[0], frameX, frameY-groundHeight-goombaPics[0][0].getHeight(this), goombaPics[0][0].getWidth(this), goombaPics[0][0].getHeight(this), 0, goombaPics[1], 0));
	}
	
	public void refresh() {
		if (gameLost) {
			int timePerFrame = 60;
			if (timeTransitioning == 0 && bowserFacePic == null && gameOverPic == null) {
				new Music("Sounds/death-music.wav", soundOn).play(0);
				bowserFacePic = bowserFacePics[0];
				timeTransitioning += timeDelay;
			}
			else if (timeTransitioning/timePerFrame < bowserFacePics.length && gameOverPic == null) {
				// display Bowser sign
				bowserFacePic = bowserFacePics[timeTransitioning/timePerFrame];
				timeTransitioning += timeDelay;
			}
			else if (timeTransitioning/timePerFrame >= bowserFacePics.length && gameOverPic == null) {
				timeTransitioning = 0;
				gameOverPic = gameOverPics[0];
			}
			else if (timeTransitioning == 0 && bowserFacePic != null) {
				new Music("Sounds/game-over.wav", soundOn).play(0);
				timeTransitioning += timeDelay;
			}
			else if (timeTransitioning/timePerFrame < gameOverPics.length) {
				// display game over sign
				gameOverPic = gameOverPics[timeTransitioning/timePerFrame];
				timeTransitioning += timeDelay;
			}
			else if (timeTransitioning/timePerFrame < gameOverPics.length+25) {
				timeTransitioning += timeDelay;
			}
			else {
				timeTransitioning = 0;
				gameLost = false;
				gameDone = true;
			}
		}
		else if (gameDone == false) {
			if (switchingScene) {
				if (pipeHeroIsAt.getType() == Pipe.UP && timeSwitching == 0 && goingInPipe) {
					hero.setY(hero.getY()-20);
				}
				else if (pipeHeroIsAt.getType() == Pipe.DOWN && timeSwitching == 0 && goingInPipe) {
					hero.setY(hero.getY()+20);
				}
				// going into pipe
				if (goingInPipe) {
					if (timeSwitching == 0) {
						new Music("Sounds/warp-pipe.wav", soundOn).play(0);
					}
					timeSwitching = goInPipe(timeSwitching);
				}
				if (pipeHeroIsAt.getType() == Pipe.UP && hero.getY() >= frameY-groundHeight || pipeHeroIsAt.getType() == Pipe.DOWN && hero.getY() <= 100-hero.getY()) {
					hero.setY(-1*hero.getHeight()-50);
					goingInPipe = false;
				}
				
				// transition phase, where black screen gradually appears
				if (goingInPipe == false && goingOutPipe == false) {  
					if (timeTransitioning < 40*sceneChangePics.length) {
						centreX = pipeHeroIsAt.getX()+pipeHeroIsAt.getWidth()/2;
						if (pipeHeroIsAt.getType() == Pipe.UP) {
							centreY = pipeHeroIsAt.getY();
						}
						else {
							centreY = pipeHeroIsAt.getY()+pipeHeroIsAt.getHeight();
						}
						sceneChangePic = sceneChangePics[(sceneChangePics.length-1)-(timeTransitioning/40)];
						timeTransitioning += timeDelay;
					}
					else if (timeTransitioning == 40*sceneChangePics.length && scene == oldScene) {
						if (pipeHeroIsAt.getType() == Pipe.UP) {
							scene--;	
						}
						else {
							scene++;
						}
						stopBackMusic();
						closeBackMusic();
						changeBackMusic();
						playBackMusic();
						timeTransitioning += timeDelay;
						pipeHeroIsAt = new Pipe(pipeHeroIsAt.getMoveSeq(), pipeHeroIsAt.getX(), pipeHeroIsAt.getY(), pipeHeroIsAt.getWidth(), pipeHeroIsAt.getHeight(), 0, false, pipeHeroIsAt.getType(), true);
						pipes.clear();
						plants.clear();
						switchPipe();
						if (pipes.get(0).getType() == Pipe.UP) {
							hero.setY(pipes.get(0).getY()+40);
							centreY = pipes.get(0).getY();
						}
						else {
							hero.setY(pipes.get(0).getY()+pipes.get(0).getHeight()-hero.getHeight()-40);
							centreY = pipes.get(0).getY()+pipes.get(0).getHeight();
						}
						timeTransitioning += timeDelay;
					}
					else if (timeTransitioning > 40*sceneChangePics.length && timeTransitioning < 80*sceneChangePics.length) {
						sceneChangePic = sceneChangePics[(timeTransitioning-40*sceneChangePics.length)/40];
						timeTransitioning += timeDelay;
					}
					else {
						timeSwitching = 0;
						goingOutPipe = true;
					}	
				}
				// going out pipe
				if (goingOutPipe) {
					if (timeSwitching == 0) {
						new Music("Sounds/warp-pipe.wav", soundOn).play(0);
					}
					timeSwitching = goOutPipe(timeSwitching, pipes.get(0), 60);
				}
				if (goingOutPipe && pipes.isEmpty() == false && pipeHeroIsAt.getType() == Pipe.UP && hero.getY() >= pipes.get(0).getY()+pipes.get(0).getHeight()) {
					hero.setY(pipes.get(0).getY()+pipes.get(0).getHeight());
					goingOutPipe = switchingScene = false;
					timeSwitching = timeTransitioning = 0;
					pipeHeroIsAt = null;
				}
				else if (goingOutPipe && pipes.isEmpty() == false && pipeHeroIsAt.getType() == Pipe.DOWN && hero.getY() <= pipes.get(0).getY()-hero.getHeight()) {
					hero.setY(pipes.get(0).getY()-hero.getHeight());
					goingOutPipe = switchingScene = false;
					timeSwitching = timeTransitioning = 0;
					pipeHeroIsAt = null;
				}
			}
			else {
				// set player's initial positions
				hero.setInitX(hero.getX());
				hero.setInitY(hero.getY());
				
				// adjust scene position
				if (backXPos[0]-sceneSpeed == -1*frameX) {
					backXPos[0] = frameX;
					backXPos[1] -= sceneSpeed;
				}
				else if (backXPos[1]-sceneSpeed == -1*frameX) {
					backXPos[0] -= sceneSpeed;
					backXPos[1] = frameX;
				}
				else {
					backXPos[0] -= sceneSpeed;
					backXPos[1] -= sceneSpeed;
				}
				
				// adjust pipe position
				for (int i=pipes.size()-1; i>=0; i--) {
					pipes.get(i).setX(pipes.get(i).getX()-sceneSpeed);
					pipes.get(i).setRect();
					if (pipes.get(i).getX()+pipes.get(i).getWidth() < 0) {
						pipes.remove(pipes.get(i));
					}
				}
				
				// adjust piranha plant position
				for (int i=plants.size()-1; i>=0; i--) {
					plants.get(i).setX(plants.get(i).getX()-sceneSpeed);
					plants.get(i).setFrameNum(changeFrame(plants.get(i).getFrameNum(), plants.get(i).getMoveSeq().length, 60));
					plants.get(i).setRect();
					if (plants.get(i).getX()+plants.get(i).getWidth() < 0) {
						plants.remove(plants.get(i));
					}
				}
				
				// adjust player position
				hero.setFrameNum(changeFrame(hero.getFrameNum(), hero.getMoveSeq().length, 70));
				hero.setRect();
				
				if (timeElapsed%100 == 0 && rand.nextFloat() >= 0.5 && (pipes.isEmpty() || frameX-(pipes.get(pipes.size()-1).getX()+pipes.get(pipes.size()-1).getWidth()) >= 200)) {
					makePipes();
				}
				if (timeElapsed%100 == 0 && rand.nextFloat() >= 0.5 && (pipes.isEmpty() || pipes.get(pipes.size()-1).getX()+pipes.get(pipes.size()-1).getWidth() < frameX)) {
					if (coins.isEmpty()) {
						makeCoins(0);
					}
					else {
						makeCoins(coins.get(0).getFrameNum());
					}
				}
				if (timeElapsed%100 == 0 && rand.nextFloat() >= 0.7 && (pipes.isEmpty() || pipes.get(pipes.size()-1).getX()+pipes.get(pipes.size()-1).getWidth() < frameX)) {
					makeGoombas();
				}
			
				if (keys[KeyEvent.VK_RIGHT]) {
					if (hero.getX()+hero.getVelX()+hero.getWidth() <= frameX) {
						hero.setX(hero.getX()+hero.getVelX());
					}
					else {
						hero.setX(frameX-hero.getWidth());
					}
				}
				if (keys[KeyEvent.VK_LEFT]) {
					if (hero.getX()-hero.getVelX() >= 0) {
						hero.setX(hero.getX()-hero.getVelX());
					}
					else {
						hero.setX(0);
					}
				}
				
				if (pipeHeroIsAt == null && keys[KeyEvent.VK_UP] && (hero.isJumping() == false || hero.isJumping() && hero.getJumpTime() < Player.MAX_JUMP_TIME) && (hero.hasJumpedOnce() || hero.hasJumpedOnce() == false && hero.getY()+hero.getHeight() == frameY-standHeight)) {
					hero.setJumping(true);
					hero.setVelY(-18);
					if (hero.hasJumpedOnce() == false) {
						hero.setJumpedOnce(true);
						new Music("Sounds/jump.wav", soundOn).play(0);
					}
				}
				else if (pipeHeroIsAt != null && pipeHeroIsAt.getType() == Pipe.UP && keys[KeyEvent.VK_UP] && (hero.isJumping() == false || hero.isJumping() && hero.getJumpTime() < Player.MAX_JUMP_TIME) && (hero.hasJumpedOnce() || hero.hasJumpedOnce() == false)) {
					hero.setJumping(true);
					hero.setVelY(-18);
					if (hero.hasJumpedOnce() == false) {
						hero.setJumpedOnce(true);
						new Music("Sounds/jump.wav", soundOn).play(0);
					}
					hero.setJumpingFromPipe(true);
				}
				else if (pipeHeroIsAt != null && pipeHeroIsAt.getType() == Pipe.DOWN && pipeHeroIsAt.hasPlayerOnBottom() && pipeHeroIsAt.doesLead() && keys[KeyEvent.VK_UP] && scene != IN_SKY) {
					oldScene = scene;	
					switchingScene = true;
					goingInPipe = true;
					hero.setInitX(hero.getX());
					hero.setInitY(hero.getY());
				}
				else if (keys[KeyEvent.VK_UP] == false && hero.getY()+hero.getHeight() < frameY-standHeight) {
					hero.setJumpedOnce(false);
				}
				if (switchingScene == false) {
					if (keys[KeyEvent.VK_DOWN] && hero.getY()+hero.getHeight()+hero.getVelY()+5 <= frameY-standHeight) {
						hero.setVelY(hero.getVelY()+5);
					}
					else if (pipeHeroIsAt != null && pipeHeroIsAt.getType() == Pipe.UP && pipeHeroIsAt.hasPlayerOnTop() && pipeHeroIsAt.doesLead() && keys[KeyEvent.VK_DOWN] && hero.getY()+hero.getHeight()+hero.getVelY()+5 > frameY-standHeight && scene != BELOW_GROUND) {
						oldScene = scene;
						switchingScene = true;
						goingInPipe = true;
						hero.setInitX(hero.getX());
						hero.setInitY(hero.getY());
					}
					else if (keys[KeyEvent.VK_DOWN] && hero.getY()+hero.getHeight()+hero.getVelY()+5 > frameY-standHeight) {
						hero.setY(frameY-standHeight-hero.getHeight());
						hero.setJumping(false);
						hero.setJumpTime(0);
						hero.setVelY(0);
					}	
				}
			}
			if (switchingScene == false) {
				if (hero.isJumping()) {
					if (hero.getJumpTime() < Player.MAX_JUMP_TIME) {
						hero.setY(hero.getY()+hero.getVelY());
						hero.setJumpTime(hero.getJumpTime()+timeDelay);
						hero.setVelY(hero.getVelY()+GRAV);
					}
					else if (hero.getY()+hero.getHeight()+hero.getVelY() < frameY-standHeight) {
						hero.setY(hero.getY()+hero.getVelY());
						hero.setJumpTime(hero.getJumpTime()+timeDelay);
						hero.setVelY(hero.getVelY()+GRAV);
					}
					else if (hero.getY()+hero.getHeight()+hero.getVelY() >= frameY-standHeight) {
						hero.setY(frameY-standHeight-hero.getHeight());
						hero.setJumpTime(0);
						hero.setVelY(0);
						hero.setJumping(false);
					}
				}
				
				// adjust coin position
				for (int i=coins.size()-1; i>=0; i--) {
					coins.get(i).setX(coins.get(i).getX()-sceneSpeed);
					coins.get(i).setFrameNum(changeFrame(coins.get(i).getFrameNum(), coins.get(i).getMoveSeq().length, 50));
					coins.get(i).setRect();
					if (coins.get(i).getX()+coins.get(i).getWidth() < 0) {
						coins.remove(coins.get(i));
					}
				}
				
				// adjust goomba position
				for (int i=goombas.size()-1; i>=0; i--) {
					int initSqFrNum = 0;
					goombas.get(i).setX(goombas.get(i).getX()-sceneSpeed);
					if (goombas.get(i).isSquashed()) {
						initSqFrNum = goombas.get(i).getSqFrameNum();
						goombas.get(i).setSqFrameNum(changeFrame(goombas.get(i).getSqFrameNum(), goombas.get(i).getSquashSeq().length, 20));
					}
					else {
						goombas.get(i).setFrameNum(changeFrame(goombas.get(i).getFrameNum(), goombas.get(i).getMoveSeq().length, 50));
					}
					goombas.get(i).setRect();
					if (goombas.get(i).getX()+goombas.get(i).getWidth() < 0 || goombas.get(i).getSqFrameNum() == 0 && initSqFrNum == goombas.get(i).getSquashSeq().length-1) {
						goombas.remove(goombas.get(i));
					}
				}
				
				// if player is standing on ground then player is not jumping from pipe
				// this is put to prevent a bug when a player jumps from one pipe to another and afterwards is not able to fall toward the ground
				if (hero.getY()+hero.getHeight() == frameY-standHeight) {
					hero.setJumpingFromPipe(false);
				}
				
				checkCollisions();
				
				timeElapsed += timeDelay;
				hitTime += hitOnce ? timeDelay : 0;
				if (hitTime > RECOVERY_TIME) {
					hitTime = 0;
					hitOnce = false;
				}
			}
		}
	}
	
	public void switchPipe() {
		if (pipeHeroIsAt.getType() == Pipe.UP) {
			hero.setJumping(true);  // allows player to fall from pipe hanging from above
			hero.setJumpTime(Player.MAX_JUMP_TIME);
			hero.setVelY(0);
			int index = Arrays.asList(pipePics).indexOf(pipeHeroIsAt.getFramePic())+4;
			Image pipePic = pipePics[index];
			int yPos = 60+rand.nextInt(frameY-(groundHeight+210))-pipePic.getHeight(this);
			pipes.add(new Pipe(new Image [] {pipePic}, pipeHeroIsAt.getX(), yPos, pipePic.getWidth(this), pipePic.getHeight(this), 0, false, Pipe.DOWN, false));
		}
		else {
			int index = Arrays.asList(pipePics).indexOf(pipeHeroIsAt.getFramePic())-4;
			Image pipePic = pipePics[index];
			int yPos = frameY-groundHeight-60-rand.nextInt(frameY-(groundHeight+210));
			pipes.add(new Pipe(new Image [] {pipePic}, pipeHeroIsAt.getX(), yPos, pipePic.getWidth(this), pipePic.getHeight(this), 0, false, Pipe.UP, false));
		}
		standHeight = groundHeight;
	}
	
	// used when player goes into a pipe
	public int goInPipe(int timePassed) {
		hero.setFrameNum(0);
		hero.setX(pipeHeroIsAt.getX()+pipeHeroIsAt.getWidth()/2-hero.getWidth()/2);
		if (pipeHeroIsAt.getType() == Pipe.UP && (timePassed+timeDelay)%60 == 0) {
			hero.setY(hero.getY()+20);
		}
		else if (pipeHeroIsAt.getType() == Pipe.DOWN && (timePassed+timeDelay)%60 == 0) {
			hero.setY(hero.getY()-20);
		}
		return timePassed+timeDelay;
	}
	
	// used when player comes out of a pipe
	public int goOutPipe(int timePassed, Pipe p, int frameRate) {
		if (p.getType() == Pipe.UP && (timePassed+timeDelay)%frameRate == 0) {
			hero.setY(hero.getY()-20);
		}
		else if (p.getType() == Pipe.DOWN && (timePassed+timeDelay)%frameRate == 0) {
			hero.setY(hero.getY()+20);
		}
		return timePassed+timeDelay;
	}
	
	public void checkCollisions() {   // checks collision between player and coin, player and enemy, player and block, and player and pipe
		int deltaX = hero.getX()-hero.getInitX(); // change in player's x pos
		int deltaY = hero.getY()-hero.getInitY(); // change in player's y pos
		
		// player and coin(s)
		for (int i=coins.size()-1; i>=0; i--) {
			if (hero.getRect().intersects(coins.get(i).getRect())) {
				new Music("Sounds/coin.wav", soundOn).play(0);
				score += 25;
				numCoins++;
				if (numCoins == 100) {  							// when player has collected 100 coins, the number of coins is set to zero again and 
					numCoins = 0;									// the player gains a life to a maximum of 99 lives
					new Music("Sounds/1-up.wav", soundOn).play(0);
					numLives = numLives < 99 ? numLives+1 : 99;
				}
				coins.remove(coins.get(i));
			}
		}
		
		// player and pipe(s)
		for (Pipe p : pipes) {
			if (hero.getRect().intersects(p.getRect())) {
				int maxDist = 24;  // max number of pixels player's y pos can be below the mouth of the pipe to reposition directly below it; if player's y pos is beyond this maxDist, player is positioned to the side of the pipe
				if (p.getType() == Pipe.UP) {  // if pipe sticks up from ground
					// if player hits top of pipe
					if (p.hasPlayerAtLeft() == false && p.hasPlayerAtRight() == false && deltaY > 0 && (hero.getX()+hero.getWidth() > p.getX() && hero.getX()+hero.getWidth() < p.getX()+p.getWidth() || hero.getX()+hero.getWidth()/2 > p.getX() && hero.getX()+hero.getWidth()/2 < p.getX()+p.getWidth() || hero.getX() > p.getX() && hero.getX() < p.getX()+p.getWidth()) && (hero.getY()+hero.getHeight()-p.getY() <= maxDist || hero.getVelY() > maxDist || keys[KeyEvent.VK_DOWN])) {
						standHeight = frameY-p.getY();
						hero.setJumping(false);
						hero.setJumpTime(0);
						hero.setVelY(0);
						hero.setY(p.getY()-hero.getHeight());
						p.setPlayerOnTop(true);
						pipeHeroIsAt = p;
					}
					// if player hits side of pipe from left
					else if (hero.getX()+hero.getWidth() <= p.getX()+p.getWidth()/2 && p.hasPlayerOnTop() == false && p.hasPlayerAtRight() == false) {
						hero.setX(p.getX()-hero.getWidth());
						p.setPlayerAtLeft(true);
					}
					// if player hits side of pipe from right
					else if (hero.getX() >= p.getX()+p.getWidth()/2 && p.hasPlayerOnTop() == false && p.hasPlayerAtLeft() == false) {
						hero.setX(p.getX()+p.getWidth());
						p.setPlayerAtLeft(true);
					}
				}
				
				else if (p.getType() == Pipe.DOWN) {  // if pipe sticks down from above					
					// if player hits bottom of pipe
					if (p.hasPlayerAtLeft() == false && p.hasPlayerAtRight() == false && ((hero.getX()+hero.getWidth()/2 >= p.getX() && hero.getX()+hero.getWidth()/2 <= p.getX()+p.getWidth() && hero.getY() <= p.getY()+p.getHeight()) || (deltaX >= 0 && deltaY <= 0 && hero.getX()+hero.getWidth() >= p.getX() && hero.getX()+hero.getWidth() < p.getX()+p.getWidth() && hero.getY() >= p.getY()+p.getHeight()-maxDist) || (deltaX <= 0 && deltaY <= 0 && hero.getX() <= p.getX()+p.getWidth() && hero.getX() > p.getX() && hero.getY() >= p.getY()+p.getHeight()-maxDist))) {
						p.setPlayerOnBottom(true);
						pipeHeroIsAt = p;
						hero.setY(p.getY()+p.getHeight());
						hero.setVelY(0);
						hero.setJumpTime(Player.MAX_JUMP_TIME); // prevents player from being able to continue going up after hitting a pipe above
					}
					// if player hits side of pipe from left
					else if (hero.getX()+hero.getWidth() <= p.getX()+p.getWidth()/2 && p.hasPlayerOnBottom() == false && p.hasPlayerAtRight() == false) {
						hero.setX(p.getX()-hero.getWidth());
						p.setPlayerAtLeft(true);
					}
					// if player hits side of pipe from right
					else if (hero.getX() >= p.getX()+p.getWidth()/2 && p.hasPlayerAtLeft() == false) {
						hero.setX(p.getX()+p.getWidth());
						p.setPlayerAtRight(true);
					}
				}
			}
			else {  // if pipe has no longer collided with player
				// if pipe's field "playerOnTop" is true and player's x position is such that it cannot be standing on the pipe
				if (p.hasPlayerOnTop() && !(hero.getX()+hero.getWidth() > p.getX() && hero.getX()+hero.getWidth() < p.getX()+p.getWidth() || hero.getX()+hero.getWidth()/2 > p.getX() && hero.getX()+hero.getWidth()/2 < p.getX()+p.getWidth() || hero.getX() > p.getX() && hero.getX() < p.getX()+p.getWidth())) {
					p.setPlayerOnTop(false);
					standHeight = groundHeight;
					pipeHeroIsAt = null;
					if (hero.isJumpingFromPipe() == false) {
						hero.setJumping(true);
						hero.setJumpTime(Player.MAX_JUMP_TIME);
						hero.setVelY(GRAV);
					}
				}
				// if pipe's field "playerOnBottom" is true it must be set to false as the player and pipe have no longer collided
				else if (p.hasPlayerOnBottom()) {
					p.setPlayerOnBottom(false);
					pipeHeroIsAt = null;
				}
				p.setPlayerAtLeft(false);
				p.setPlayerAtRight(false);
			}
		}
		
		// player and piranha plant(s)
		for (PiranhaPlant pp : plants) {
			if (pp.getRect() != null && hero.getRect().intersects(pp.getRect()) && hitOnce == false) {
				hitOnce = true;
				hitTime = 0;
				new Music("Sounds/death.wav", soundOn).play(0);
				stopBackMusic();
				if (numLives-1 == -1) {
					closeBackMusic();
					gameLost = true;
					timeTransitioning = 0;
				}
				else {
					playBackMusic();
					numLives--;
				}
			}
		}
		
		for (Goomba g : goombas) {
			if (hero.getRect().intersects(g.getRect())) {
				if (g.isSquashed() == false && deltaY > 0 && (hero.getX()+hero.getWidth() > g.getX() && hero.getX()+hero.getWidth() < g.getX()+g.getWidth() || hero.getX()+hero.getWidth()/2 > g.getX() && hero.getX()+hero.getWidth()/2 < g.getX()+g.getWidth() || hero.getX() > g.getX() && hero.getX() < g.getX()+g.getWidth())) {
					score += 200;
					new Music("Sounds/goomba-squash.wav", soundOn).play(0);
					g.setSquashed(true);
					g.setSqFrameNum(0);
					hero.setVelY(-5);  // lets player hop in air for a bit after squashing a goomba
					hero.setJumping(true);
					hero.setJumpTime(Player.MAX_JUMP_TIME);
				}
				else if (g.isSquashed() == false && hitOnce == false) {
					hitOnce = true;
					hitTime = 0;
					new Music("Sounds/death.wav", soundOn).play(0);
					stopBackMusic();
					if (numLives-1 == -1) {
						closeBackMusic();
						gameLost = true;
						timeTransitioning = 0;
					}
					else {
						playBackMusic();
						numLives--;
					}
				}
			}
		}
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		// display background
		g.drawImage(scenePics[scene][1], backXPos[0], 0, this);
		g.drawImage(scenePics[scene][1], backXPos[1], 0, this);
		
		// display coins
		for (Coin c : coins) {
			g.drawImage(c.getFramePic(), c.getX(), c.getY(), this);
		}
		// display goombas
		for (Goomba goom : goombas) {
			if (goom.isSquashed()) {
				g.drawImage(goom.getSquashPic(), goom.getX(), goom.getY(), this);
			}
			else {
				g.drawImage(goom.getFramePic(), goom.getX(), goom.getY(), this);
			}
		}
		
		// display player
		g.drawImage(hero.getFramePic(), hero.getX(), hero.getY(), this);
		
		// display pipes
		for (Pipe p : pipes) {
			g.drawImage(p.getFramePic(), p.getX(), p.getY(), this);
		}
		// display piranha plants
		for (PiranhaPlant pp : plants) {
			g.drawImage(pp.getFramePic(), pp.getX(), pp.getY(), this);
		}
		
		// display ground
		g.drawImage(scenePics[scene][0], backXPos[0], frameY-groundHeight, this);
		g.drawImage(scenePics[scene][0], backXPos[1], frameY-groundHeight, this);
		
		// ------------------------- display stats ------------------------- //
		//display score
		String strScore = Long.toString(score);
		for (int i=0; i<15-Long.toString(score).length(); i++) {
			strScore = "0"+strScore;
		}
		for (int j=0; j<strScore.toCharArray().length; j++) {
			g.drawImage(whiteNumbers[Integer.parseInt(strScore.substring(j, j+1))], 800+24*j, 20, this);
		}
		//display number of lives
		Image marioFace = new ImageIcon("Pictures/other/mario-face.png").getImage();
		String strNumLives = numLives < 10 ? "0"+Integer.toString(numLives) : Integer.toString(numLives);
		g.drawImage(marioFace, 20, 20, this);
		g.drawImage(new ImageIcon("Pictures/numbers/white/x.png").getImage(), 62, 27, this);
		g.drawImage(whiteNumbers[Integer.parseInt(strNumLives.substring(0, 1))], 84, 20, this);
		g.drawImage(whiteNumbers[Integer.parseInt(strNumLives.substring(1))], 104, 20, this);
		//display number of coins
		Image coinPic = new ImageIcon("Pictures/other/coin.png").getImage();
		String strNumCoins = numCoins < 10 ? "0"+Integer.toString(numCoins) : Integer.toString(numCoins);
		g.drawImage(coinPic, 25, 80, this);
		g.drawImage(yellowNumbers[Integer.parseInt(strNumCoins.substring(0, 1))], 62, 74, this);
		g.drawImage(yellowNumbers[Integer.parseInt(strNumCoins.substring(1))], 84, 74, this);
		// ----------------------------------------------------------------- //
		
		// display black screen if applicable
		if (gameLost == false && sceneChangePic != null && switchingScene && goingInPipe == false && goingOutPipe == false && pipeHeroIsAt != null && pipeHeroIsAt.getType() == Pipe.UP) {
			g.drawImage(sceneChangePic, centreX-sceneChangePic.getWidth(this)/2, centreY-sceneChangePic.getHeight(this)/2, this);
		}
		else if (gameLost == false && sceneChangePic != null && switchingScene && goingInPipe == false && goingOutPipe == false && pipeHeroIsAt != null && pipeHeroIsAt.getType() == Pipe.DOWN) {
			g.drawImage(sceneChangePic, centreX-sceneChangePic.getWidth(this)/2, centreY-sceneChangePic.getHeight(this)/2, this);
		}
		else if ((gameLost || gameDone) && bowserFacePic != null) {
			g.drawImage(bowserFacePic, frameX/2-bowserFacePic.getWidth(this)/2, frameY/2-bowserFacePic.getHeight(this)/2, this);
			if (gameOverPic != null) {
				g.drawImage(gameOverPic, frameX/2-gameOverPic.getWidth(this)/2, frameY/2-gameOverPic.getHeight(this)/2-40, this);
			}
		}
	}
}

class ObjectOnScreen {
	private int x, y, width, height, frameNum;
	private Rectangle rect;
	private Image [] moveSeq;
	
	public ObjectOnScreen(Image [] moveSeq, int x, int y, int width, int height, int frameNum) {
		this.moveSeq = new Image [moveSeq.length];
		for (int i=0; i<moveSeq.length; i++) {
			this.moveSeq[i] = moveSeq[i];
		}
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.rect = new Rectangle(x, y, width, height);
		this.frameNum = frameNum;
	}
	
	public void setX(int newX) {
		x = newX;
	}
	public void setY(int newY) {
		y = newY;
	}
	public void setRect() {
		rect.setBounds(x, y, width, height);
	}
	public void setFrameNum(int newFrameNum) {
		frameNum = newFrameNum;
	}
	public Image [] getMoveSeq() {
		return moveSeq;
	}
	public Image getFramePic() {
		return moveSeq[frameNum];
	}
	public int getFrameNum() {
		return frameNum;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public Rectangle getRect() {
		return rect;
	}
}


class Player extends ObjectOnScreen {
	public static final int MAX_JUMP_TIME = 200;
	private int initX, initY, velX, velY, jumpTime, lives;
	private boolean jumping, jumpedOnce, jumpingFromPipe;
	
	public Player(Image [] moveSeq, int x, int y, int width, int height, int frameNum, int velX, int velY) {
		super(moveSeq, x, y, width, height, frameNum);
		initX = getX();
		initY = getY();
		this.velX = velX;
		this.velY = velY;
		jumping = jumpedOnce = jumpingFromPipe = false;
	}
	
	public void setInitX(int newInitX) {
		initX = newInitX;
	}
	public void setInitY(int newInitY) {
		initY = newInitY;
	}
	public void setVelY(int newVelY) {
		velY = newVelY;
	}
	public void setJumping(boolean state) {
		jumping = state;
	}
	public void setJumpTime(int newJumpTime) {
		jumpTime = newJumpTime;
	}
	public void setJumpedOnce(boolean state) {
		jumpedOnce = state;
	}
	public void setJumpingFromPipe(boolean state) {
		jumpingFromPipe = state;
	}
	
	public int getInitX() {
		return initX;
	}
	public int getInitY() {
		return initY;
	}
	public int getVelX() {
		return velX;
	}
	public int getVelY() {
		return velY;
	}
	public boolean isJumping() {
		return jumping;
	}
	public int getJumpTime() {
		return jumpTime;
	}
	public boolean hasJumpedOnce() {
		return jumpedOnce;
	}
	public boolean isJumpingFromPipe() {
		return jumpingFromPipe;
	}
}


class Coin extends ObjectOnScreen {
	public Coin(Image [] moveSeq, int x, int y, int width, int height, int frameNum) {
		super(moveSeq, x, y, width, height, frameNum);
	}
}

class Goomba extends ObjectOnScreen {
	private Image [] squashSeq;
	private int sqFrameNum;
	private boolean squashed;
	
	public Goomba(Image [] moveSeq, int x, int y, int width, int height, int frameNum, Image [] squashSeq, int sqFrameNum) {
		super(moveSeq, x, y, width, height, frameNum);
		this.squashSeq = new Image[squashSeq.length];
		for (int i=0; i<squashSeq.length; i++) {
			this.squashSeq[i] = squashSeq[i];
		}
		this.sqFrameNum = sqFrameNum;
		squashed = false;
	}
	
	public void setSquashed(boolean squashed) {
		this.squashed = squashed;
	}
	public void setSqFrameNum(int newSqFrameNum) {
		sqFrameNum = newSqFrameNum;
	}
	public int getSqFrameNum() {
		return sqFrameNum;
	}
	public Image [] getSquashSeq() {
		return squashSeq;
	}
	public Image getSquashPic() {
		return squashSeq[sqFrameNum];
	}
	public boolean isSquashed() {
		return squashed;
	}
}

class PiranhaPlant extends ObjectOnScreen {
	private static final int UP = 1, DOWN = 2;
	private int type;
	private Rectangle [] hitboxes;
	
	public PiranhaPlant(Image [] moveSeq, int x, int y, int width, int height, int frameNum, int type, Rectangle [] hitboxes) {
		super(moveSeq, x, y, width, height, frameNum);
		this.type = type;
		this.hitboxes = new Rectangle[hitboxes.length];
		for (int i=0; i<hitboxes.length; i++) {
			this.hitboxes[i] = new Rectangle((int)(hitboxes[i].getX()), (int)(hitboxes[i].getY()), (int)(hitboxes[i].getWidth()), (int)(hitboxes[i].getHeight()));
		}
	}
	
	@Override
	public void setRect() {
		Rectangle hb = hitboxes[getFrameNum()];
		getRect().setBounds((int)(hb.getX()+getX()), (int)(hb.getY()+getY()), (int)(hb.getWidth()), (int)(hb.getHeight()));
	}
	public int getType() {
		return type;
	}
	public Rectangle [] getHitboxes() {
		return hitboxes;
	}
}

class Pipe extends ObjectOnScreen {
	public static final int UP = 1, DOWN = 2;  // two types of pipes - one sticking up and the other sticking down
	private boolean containsPlant, leads, playerOnTop, playerOnBottom, playerAtLeft, playerAtRight; 
	// containsPlant - has a piranha plant inside it; leads - if pipe leads elsewhere
	// playerOnTop - if player is standing on pipe (only applies to pipes that are of "up" type)
	// playerOnBottom - if player is hitting on the bottom of the pipe (only applied to pipes of the "down" type)
	// playerOnLeft - if player is touching the left side of a pipe
	// playerOnRight - if player is touching the right side of a pipe
	private int type;
	
	public Pipe(Image [] moveSeq, int x, int y, int width, int height, int frameNum, boolean containsPlant, int type, boolean leads) {
		super(moveSeq, x, y, width, height, frameNum);
		this.containsPlant = containsPlant;
		// if direction given is valid (nowhere, up, or down) then boolean leads is given that direction, otherwise the direction is set to nowhere
		this.type = type;
		this.leads = leads;
		playerOnTop = playerOnBottom = playerAtLeft = playerAtRight = false;
	}
	
	public void setPlayerOnTop(boolean state) {
		// if the type of pipe is "up", meaning it sticks up from the ground, then playerOnTop changes to the given boolean
		// otherwise playerOnTop cannot change as a pipe sticking down cannot have someone standing on top of it
		playerOnTop = type == UP ? state : playerOnTop;
	}
	public void setPlayerOnBottom(boolean state) {
		// if the type of pipe is "down", meaning it sticks up from the ground, then playerOnBottom changes to the given boolean
		// otherwise playerOnBottom cannot change as a pipe sticking up cannot have someone standing on the bottom of it
		playerOnBottom = type == DOWN ? state : playerOnBottom;
	}
	public void setPlayerAtLeft(boolean state) {
		playerAtLeft = state;
	}
	public void setPlayerAtRight(boolean state) {
		playerAtRight = state;
	}

	public boolean doesContainPlant() {
		return containsPlant;
	}
	public boolean doesLead() {
		return leads;
	}
	public int getType() {
		return type;
	}
	public boolean hasPlayerOnTop() {
		return playerOnTop;
	}
	public boolean hasPlayerOnBottom() {
		return playerOnBottom;
	}
	public boolean hasPlayerAtLeft() {
		return playerAtLeft;
	}
	public boolean hasPlayerAtRight() {
		return playerAtRight;
	}
}
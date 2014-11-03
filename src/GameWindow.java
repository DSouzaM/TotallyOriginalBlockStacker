import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;


public class GameWindow {
	static GameWindow gameWindow;
	static int score;
	static int lines;

	JButton startButton;
	Image titleScreen;
	JPanel startPanel;
	JLabel titleLabel;

	JFrame gameFrame;
	JPanel gPanel;
	JPanel sidePanel;

	JButton newShape;
	static Tetromino t;
	static Tetromino next;
	public final Color[] colors = { null, new Color(0, 255, 255),
			new Color(255, 255, 0), Color.green, Color.red,
			new Color(255, 128, 0), Color.blue, new Color(127, 0, 255),
			Color.black };
	static int[][] cells;
	static Timer time;
	TimerListener timerListener;
	static boolean playing;
	int avoidTriple;
	int delayTime;

	JLabel scoreLabel;
	JLabel linesLabel;
	ShapePanel nextShapePanel;
	JLabel controls;
	JButton endGame;

	JTextField nameField;
	JButton submit;
	JTextArea scoresList;
	HighScoreParser hsParser;
	boolean submitted;
	JButton restart;

	public static void main(String[] args) {
		gameWindow = new GameWindow();
		gameWindow.menu();
	}

	public void menu() {
		gameFrame = new JFrame("Totally Original Block Stacker - Matt D'Souza");

		try {
		titleScreen = ImageIO.read(new File("TOBS.png"));
		 titleLabel = new JLabel(new ImageIcon(titleScreen));
		} catch (Exception ex) { ex.printStackTrace(); }
		
		startButton = new JButton("Click here to start.");
		startButton.addActionListener(new StartListener());
		
		gameFrame.add(BorderLayout.CENTER,titleLabel);
		gameFrame.add(BorderLayout.SOUTH, startButton);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setSize(800, 600);
		gameFrame.setVisible(true);
	}

	public void start() {
		cells = new int[22][12];
		score = 0;
		lines = 0;
		t = new Tetromino();
		next = new Tetromino();
		avoidTriple = 0;

		gameFrame.dispose();
		gameFrame = new JFrame("Totally Original Block Stacker - Matt D'Souza");
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setSize(600, 600);
		gameFrame.setVisible(true);

		gPanel = new GamePanel();
		gPanel.addKeyListener(new MyKeyListener());
		gPanel.requestFocusInWindow();
		for (int row = 0; row < 22; row++) {
			cells[row][0] = 8;
			cells[row][11] = 8;
		}
		for (int col = 1; col < 11; col++) {
			cells[21][col] = 8;
		}

		scoreLabel = new JLabel("Score: " + score);
		linesLabel = new JLabel("Lines: " + lines);
		JLabel nextShapeLabel = new JLabel("Next Shape:");
		nextShapeLabel.setSize(150, 80);
		nextShapePanel = new ShapePanel(next);
		controls = new JLabel("<html>Controls:<br>a/d - move left/right<br>s - move down<br>w - bring to bottom<br>q/e - rotate</html>");
		restart = new JButton("Restart");
		restart.addActionListener(new RestartListener());
		endGame = new JButton("End game");
		endGame.addActionListener(new endGameListener());
		

		sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.add(scoreLabel);
		sidePanel.add(linesLabel);
		sidePanel.add(nextShapeLabel);
		sidePanel.add(nextShapePanel);
		sidePanel.add(controls);
		sidePanel.add(endGame);
		sidePanel.add(restart);
		sidePanel.setPreferredSize(new Dimension(150, 500));

		gameFrame.add(BorderLayout.CENTER, gPanel);
		gameFrame.add(BorderLayout.EAST, sidePanel);

		delayTime = 1000;
	
		timerListener = new TimerListener();
		time = new Timer(delayTime, timerListener);
		time.start();

		gPanel.requestFocusInWindow();
	}

	public void postGameMenu() {
		hsParser = new HighScoreParser();
		submitted = false;

		gameFrame.dispose();
		gameFrame = new JFrame("Totally Original Block Stacker - Matt D'Souza");
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setSize(800, 600);
		gameFrame.setVisible(true);

		sidePanel = new JPanel();

		JLabel userScore = new JLabel("Game over! Your score was " + score
				+ ".");

		nameField = new JTextField(20);
		JLabel nameLabel = new JLabel("Enter your name:");
		submit = new JButton("Submit High Score");
		submit.addActionListener(new SubmitListener());
		JPanel namePanel = new JPanel();
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		namePanel.add(submit);

		scoresList = new JTextArea(6, 20);
		scoresList.append("\tHigh Scores\n");
		scoresList.append(hsParser.scoresToString());

		restart = new JButton("Click to restart!");
		restart.addActionListener(new RestartListener());

		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		sidePanel.add(userScore);
		sidePanel.add(namePanel);
		sidePanel.add(scoresList);
		sidePanel.add(restart);

		gameFrame.add(BorderLayout.CENTER, gPanel);
		gameFrame.add(BorderLayout.EAST, sidePanel);

	}

	static boolean checkRoom(int x, int y, int rot) { // attempted x, y, and
														// rotation of shape
		boolean isThereRoom = true;
		int value = 0;
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				if (rot == -1) {
					rot = 0;
				}
				value = t.rotations[rot][row][col];
				if (value != 0
						&& ((x + col < 0) || (x + col > 11) || cells[y + row][x
								+ col] != 0)) {
					isThereRoom = false;
				}
			}
		}
		return isThereRoom;
	}

	public void mergeMino() {
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				int value = t.rotations[t.currentShape][row][col];
				if (value != 0) {
					cells[row + t.y][col + t.x] = t.rotations[t.currentShape][row][col];
				}
			}
		}
		gPanel.repaint();
		clearFullLines();
	}

	public void clearFullLines() {
		int fullLines = 0;
		for (int row = 0; row < cells.length - 1; row++) {
			boolean noEmptySpace = true;
			for (int col = 1; col < cells[row].length - 1; col++) {
				if (cells[row][col] == 0) {
					noEmptySpace = false;
				}
			}

			if (noEmptySpace) {
				fullLines++;
				for (int col = 1; col < cells[row].length - 1; col++) {
					cells[row][col] = 0;
				}
				gameFrame.repaint();
				for (int dRow = row; dRow > 0; dRow--) {
					for (int dCol = 1; dCol < cells[dRow].length - 1; dCol++) {
						cells[dRow][dCol] = cells[dRow - 1][dCol];
					}
				}
				try {
					Thread.sleep(35);
				} catch (Exception e) {
				}

			}
		}
		int multiplier;
		switch (fullLines) {
		case 1:
			multiplier = 40;
			break;
		case 2:
			multiplier = 100;
			break;
		case 3:
			multiplier = 300;
			break;
		case 4:
			multiplier = 1200;
			break;
		default:
			multiplier = 0;
			break;
		}
		score += multiplier * ((lines / 10) + 1);
		lines += fullLines;
		scoreLabel.setText("Score: " + score);
		linesLabel.setText("Lines: " + lines);
		
		time.setDelay((int)(1000*Math.pow(0.93,(lines/10))));
		time.start();
	}

	public void genNewShape() {
		mergeMino();
		if (t.shape == next.shape) {
			avoidTriple = t.shape;
		}
		t = next;
		do{
		next = new Tetromino();
		} while (next.shape == avoidTriple);
		nextShapePanel.changeShape(next);
		nextShapePanel.repaint();
		if (!checkRoom(t.x, t.y, t.currentShape)) {
			time.stop();
			playing = false;
			gameWindow.postGameMenu();
		} else {

			gameFrame.repaint();

		}
	}


	class HighScoreParser {
		File scoreList = new File("highscores.txt");
		Scanner scoreScanner;
		int[] scores;
		String[] names;

		public HighScoreParser() {
			scoreScanner = null;
			try {
				scoreScanner = new Scanner(/*scoreList*/this.getClass().getResourceAsStream("highscores.txt"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			scores = new int[5];
			names = new String[5];

			for (int i = 0; i < 5; i++) {
				if (scoreScanner.hasNextInt()) {
					scores[i] = scoreScanner.nextInt();
				} else {
					scores[i] = 0;
				}
				if (scoreScanner.hasNext()) {
					names[i] = scoreScanner.next();
				} else {
					names[i] = "";
				}
			}
		}

		public void updateHighScore(int newScore, String newName) {
			for (int i = 0; i < 5; i++) {
				if (newScore > scores[i]) {
					for (int j = 4; j > i; j--) {
						scores[j] = scores[j - 1];
						names[j] = names[j - 1];
					}
					scores[i] = newScore;
					names[i] = newName;
					rewriteScores();
					break;
				}
			}
		}

		public void rewriteScores() {
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(scoreList);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			writer.flush();
			String newList = "";
			for (int i = 0; i < 5; i++) {
				newList += scores[i] + " " + names[i] + " ";
			}
			writer.write(newList);
			writer.close();
		}

		public String scoresToString() {
			String s = "";
			for (int i = 0; i < 5; i++) {
				s += scores[i] + " - " + names[i] + "\n";
			}
			return s;
		}
	}

	
	class GamePanel extends JPanel {

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, this.WIDTH, this.HEIGHT);
			for (int row = 0; row < cells.length; row++) {
				for (int col = 0; col < cells[row].length; col++) {
					int value = cells[row][col];
					if (value != 0) {
						g2d.setColor(colors[value]);
						g2d.fill3DRect(col * 20, row * 20, 18, 18, false);
					}
				}
			}

			for (int row = 0; row < 4; row++) {
				for (int col = 0; col < 4; col++) {
					int value = t.rotations[t.currentShape][row][col];
					if (value != 0) {
						g2d.setColor(colors[value]);
						g2d.fill3DRect(t.x * 20 + col * 20,
								t.y * 20 + row * 20, 18, 18, false);
					}
				}
			}
		}
	}

	class ShapePanel extends JPanel {
		Tetromino shape;

		public ShapePanel(Tetromino shape1) {
			shape = shape1;
		}

		public void changeShape(Tetromino tet) {
			shape = tet;
		}

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			for (int row = 0; row < 4; row++) {
				for (int col = 0; col < 4; col++) {
					int value = shape.rotations[0][row][col];
					if (value != 0) {
						g2d.setColor(colors[value]);
						g2d.fill3DRect(col * 20, row * 20, 18, 18, false);
					}
				}
			}
		}
	}

	class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_S:
				t.moveDown();
				gameFrame.repaint();
				break;
			case KeyEvent.VK_A:
				t.moveLeft();
				gameFrame.repaint();
				break;
			case KeyEvent.VK_D:
				t.moveRight();
				gameFrame.repaint();
				break;
			case KeyEvent.VK_E:
				t.rotateCW();
				gameFrame.repaint();
				break;
			case KeyEvent.VK_Q:
				t.rotateCCW();
				gameFrame.repaint();
				break;
			case KeyEvent.VK_W:
				t.moveToBottom();
				genNewShape();
				break;
			default:
				break;
			}
		}

		public void keyReleased(KeyEvent e) {

		}

		public void keyTyped(KeyEvent e) {

		}

	}

	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			t.moveDown();
			if (t.downCount <= 0) {
				genNewShape();
			}
			if (playing) {
				gameFrame.repaint();
				time.start();
			}
		}
	}

	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			playing = true;
			gameWindow.start();
		}
	}

	class SubmitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String newName = nameField.getText().replaceAll("\\s", "");
			if (newName.equals("")) {
				newName = "noName";
			}
			if (!submitted) {
				hsParser.updateHighScore(score, newName);
				submitted = true;
			}
			scoresList.setText("");
			scoresList.append("\tHigh Scores\n");
			scoresList.append(hsParser.scoresToString());

		}
	}

	class RestartListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			time.removeActionListener(timerListener);
			playing = true;
			gameWindow.start();
		}
	}
	class endGameListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			time.removeActionListener(timerListener);
			playing = false;
			gameFrame.dispose();
			gameFrame = new JFrame("Tetris");
			menu();			
		}
	}
}

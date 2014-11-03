import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;



public class Tetromino {
	public int shape;
	public int currentShape;
	public int[][][] rotations;
	public int numOfRots;
	public int x, y;
	public int downCount;
	public Tetromino() {
		this(1 + (int) (Math.random() * 700) % 7);
	}

	public Tetromino(int num) {
		x = 3;
		y = 0;
		downCount = 1;
		shape = num;
		File minoTxt = null;
		Scanner minoList = null;
		try {
			minoTxt = new File("tetrominos.txt");
			minoList = new Scanner(minoTxt);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		minoList.findWithinHorizon(shape + ".", 0);
		numOfRots = minoList.nextInt();
		rotations = new int[numOfRots][4][4];
		for (int rot = 0; rot < rotations.length; rot++) {
			for (int row = 0; row <= 3; row++) {
				for (int col = 0; col <= 3; col++) {
					rotations[rot][row][col] = minoList.nextInt();
				}
			}
		}
		currentShape = 0;
	}

	public void moveLeft() {
		if (GameWindow.checkRoom(x - 1, y, currentShape)) {
			x--;
		}
	}

	public void moveRight() {
		if (GameWindow.checkRoom(x + 1, y, currentShape)) {
			x++;
		}
	}

	public void moveDown() {
		if (GameWindow.checkRoom(x, y + 1, currentShape)) {
			y++;
			downCount = 1;
		} else {
			downCount--;
		}
	}

	public void rotateCW() {
		if (currentShape == numOfRots - 1) {
			if (GameWindow.checkRoom(x, y, 0)) {
				currentShape = 0;
			}
		} else if (GameWindow.checkRoom(x, y, currentShape + 1)) {
			currentShape++;
		}
	}

	public void rotateCCW() {
		if (currentShape == 0) {
			if (GameWindow.checkRoom(x, y, numOfRots - 1)) {
				currentShape = numOfRots - 1;
			}
		} else if (GameWindow.checkRoom(x, y, currentShape - 1)) {
			currentShape--;
		}
	}

	public void moveToBottom() {
		while (GameWindow.checkRoom(this.x, this.y + 1, this.currentShape)) {
			moveDown();
		}
	}
}

import java.util.*;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GUI extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage theStage) {

		int SIZE = 10;
		int numOfMines = 8;
		Tile.numOfMines = numOfMines;
		Tile.numOfClearedTiles = 0;
		Tile.firstClick = true;
		GridPane grid = new GridPane();
		SmileyButton smiley = new SmileyButton();
		smiley.setOnAction(e -> start(theStage));

		InfoPane time = new InfoPane("Times", 35);
		InfoPane mines = new InfoPane(Integer.toString(numOfMines), 35);

		HBox header = new HBox(15);
		header.getChildren().addAll(mines, smiley, time);

		BorderPane main = new BorderPane();
		main.setTop(header);
		main.setCenter(grid);

		Tile buttons[][] = new Tile[SIZE][SIZE];
		for (int row = 0; row < buttons.length; row++) {
			for (int col = 0; col < buttons[row].length; col++) {
				buttons[row][col] = new Tile(row, col, smiley, SIZE, mines, time, buttons);
				Tile current = buttons[row][col];
				// buttons[row][col].setOnAction(buttons[row][col]);
				current.setOnMouseClicked(e -> {
					if (e.getButton() == MouseButton.PRIMARY) {
						current.reveal();
					}
					if (e.getButton() == MouseButton.SECONDARY) {
						current.flag();
					}
				});
				grid.add(buttons[row][col], row, col);
			}
		}

		theStage.setScene(new Scene(main));
		theStage.show();
	}

}

class Tile extends Button implements EventHandler<ActionEvent> {

	static int numOfMines;
	static int numOfClearedTiles;
	static int numOfTiles;
	static boolean firstClick = true;
	static long score;
	static long start;
	static long end;

	boolean locked = false;
	double imageSize = 30;
	boolean revealed;
	boolean isMine;
	boolean flagged;
	int state, row, col;
	ImageView imageCover, imageRevealed;
	SmileyButton smiley;
	InfoPane mines;
	InfoPane time;
	Tile[][] buttons;

	public Tile(int row, int col, SmileyButton smiley, int size, InfoPane mines, InfoPane time, Tile[][] buttons) {
		this.row = row;
		this.col = col;
		this.smiley = smiley;
		this.mines = mines;
		this.buttons = buttons;
		this.time = time;
		numOfTiles = (size * size) - numOfMines;
		revealed = false;
		isMine = false;
		state = 0; // -1 if it is a mine

		setMinWidth(imageSize);
		setMaxWidth(imageSize);
		setMinHeight(imageSize);
		setMaxHeight(imageSize);

		imageCover = new ImageView(new Image("file:res/cover.png"));
		setGraphic(imageCover);
		imageCover.setFitWidth(imageSize);
		imageCover.setFitHeight(imageSize);
	}

	public void handle(ActionEvent e) {
		this.reveal();
	}

	public void reveal() {

		if (firstClick) {
			firstClick = false;
			this.locked = true;
			lockSurroundingTiles();
			generateBoard(buttons);
		}
		if (revealed) {
			clearSurroundingTiles();
		}

		if (state >= 0 && !revealed && !flagged) {
			numOfClearedTiles++;
			if (numOfClearedTiles == numOfTiles) {
				smiley.winFace();
				numOfClearedTiles = 0;
				for (int row = 0; row < buttons.length; row++) {
					for (int col = 0; col < buttons[row].length; col++) {
						buttons[row][col].revealed = true;
					}
				}
				
			}
			revealed = true;
			imageRevealed = new ImageView(new Image("file:res/" + state + ".png"));
			setGraphic(imageRevealed);
			imageRevealed.setFitHeight(imageSize);
			imageRevealed.setFitWidth(imageSize);
			if (state == 0) {
				clearSurroundingTiles();
			}
		}
		if (state == -1 && !revealed && !flagged) {
			showAllMines(buttons);
			revealed = true;
			imageRevealed = new ImageView(new Image("file:res/mine-red.png"));
			setGraphic(imageRevealed);
			imageRevealed.setFitHeight(imageSize);
			imageRevealed.setFitWidth(imageSize);
			smiley.deadFace();
			numOfClearedTiles = 0;
		}
	}

	public static void generateBoard(Tile[][] buttons) {
		int i = 0;
		while (i < numOfMines) {
			int x = (int) (Math.random() * (buttons.length - 1));
			int y = (int) (Math.random() * (buttons[x].length - 1));
			if (buttons[x][y].state != -1 && !buttons[x][y].locked) {
				buttons[x][y].state = -1;
				i++;
			}
		}
		for (int row = 0; row < buttons.length; row++) {
			for (int col = 0; col < buttons[row].length; col++) {
				buttons[row][col].setState();
			}
		}
	}

	public void clearSurroundingTiles() {
		int numOfSurroundingFlags = getNumOfSurroundingFlags();

		if (state == numOfSurroundingFlags) {
			for (int i = -1; i <= 1; i++) {
				if (this.row + i < 0 || this.row + i >= buttons.length)
					continue;
				for (int j = -1; j <= 1; j++) {
					if (this.col + j < 0 || this.col + j >= buttons[this.row].length)
						continue;
					Tile temp = buttons[this.row + i][this.col + j];
					if (temp.revealed == false) {
						temp.reveal();
					}
				}
			}
		}
	}

	public int getNumOfSurroundingFlags() {
		int numOfSurroundingFlags = 0;
		for (int i = -1; i <= 1; i++) {
			if (this.row + i < 0 || this.row + i >= buttons.length)
				continue;
			for (int j = -1; j <= 1; j++) {
				if (this.col + j < 0 || this.col + j >= buttons[this.row].length)
					continue;
				Tile temp = buttons[this.row + i][this.col + j];
				if (temp.flagged == true) {
					numOfSurroundingFlags++;
				}
			}
		}
		return numOfSurroundingFlags;
	}

	public void lockSurroundingTiles() {
		for (int i = -1; i <= 1; i++) {
			if (this.row + i < 0 || this.row + i >= buttons.length)
				continue;
			for (int j = -1; j <= 1; j++) {
				if (this.col + j < 0 || this.col + j >= buttons[this.row].length)
					continue;
				buttons[this.row + i][this.col + j].locked = true;
			}
		}
	}

	public static void showAllMines(Tile[][] buttons) {
		for (int row = 0; row < buttons.length; row++) {
			for (int col = 0; col < buttons[row].length; col++) {
				Tile current = buttons[row][col];
				if (current.state == -1) {
					current.imageRevealed = new ImageView(new Image("file:res/mine-grey.png"));
					current.setGraphic(current.imageRevealed);
					current.imageRevealed.setFitHeight(current.imageSize);
					current.imageRevealed.setFitWidth(current.imageSize);
				}
				current.revealed = true;
			}
		}
	}

	public void flag() {
		if (flagged) {
			flagged = false;
			numOfMines++;
			mines.getChildren().clear();
			mines.getChildren().add(new Label(Integer.toString(numOfMines)));
			imageCover = new ImageView(new Image("file:res/cover.png"));
			setGraphic(imageCover);
			imageCover.setFitWidth(imageSize);
			imageCover.setFitHeight(imageSize);
		} else if (!flagged && !revealed) {
			flagged = true;
			numOfMines--;
			mines.getChildren().clear();
			mines.getChildren().add(new Label(Integer.toString(numOfMines)));
			imageCover = new ImageView(new Image("file:res/flag.png"));
			setGraphic(imageCover);
			imageCover.setFitWidth(imageSize);
			imageCover.setFitHeight(imageSize);
		}
	}

	public void setState() {
		if (state == -1) {
			isMine = true;
		} else {
			int total = 0;
			for (int i = -1; i <= 1; i++) {
				if (this.row + i < 0 || this.row + i >= buttons.length)
					continue;
				for (int j = -1; j <= 1; j++) {
					if (this.col + j < 0 || this.col + j >= buttons[this.row].length)
						continue;
					Tile temp = buttons[this.row + i][this.col + j];
					if (temp.state == -1) {
						total++;
					}
				}
			}
			this.state = total;
		}
	}

}

class SmileyButton extends Button {
	double size = 50;
	int state;
	ImageView imageCover;

	public SmileyButton() {
		state = 0;// 0 for smile, 1 for win, -1 for dead
		setMinWidth(size);
		setMaxWidth(size);
		setMinHeight(size);
		setMaxHeight(size);
		imageCover = new ImageView(new Image("file:res/face-smile.png"));
		setGraphic(imageCover);
		imageCover.setFitWidth(size);
		imageCover.setFitWidth(size);
	}

	public void deadFace() {
		imageCover = new ImageView(new Image("file:res/face-dead.png"));
		setGraphic(imageCover);
		imageCover.setFitWidth(size);
		imageCover.setFitWidth(size);
	}

	public void winFace() {
		imageCover = new ImageView(new Image("file:res/face-win.png"));
		setGraphic(imageCover);
		imageCover.setFitWidth(size);
		imageCover.setFitWidth(size);
	}
}

class InfoPane extends StackPane {
	double size;

	public InfoPane(String title, double size) {
		this.size = size;
		getChildren().add(new Label(title));
		// setStyle("-fx-border-color:red");
		setPadding(new Insets(1, 1, 1, 1));
		setMinWidth(size);
		setMaxWidth(size);
		setMinHeight(size);
		setMaxHeight(size);
	}
}

package stonesmoving.javafx.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import stonesmoving.results.GameResult;
import stonesmoving.results.GameResultDao;
import stonesmoving.state.StoneState;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class GameController {

    @Inject
    private FXMLLoader fxmlLoader;

    @Inject
    private GameResultDao gameResultDao;

    private String playerName;
    private StoneState gameState;
    private IntegerProperty steps = new SimpleIntegerProperty();
    private IntegerProperty scores = new SimpleIntegerProperty();
    private Instant startTime;
    private List<Image> stoneImages;

    @FXML
    private TextField display;

    @FXML
    private Label messageLabel;

    @FXML
    private GridPane gameGrid;

    @FXML
    private Label scoresLabel;

    @FXML
    private Label stepsLabel;

    @FXML
    private Label stopWatchLabel;

    private Timeline stopWatchTimeline;

    @FXML
    private Button resetButton;

    @FXML
    private Button giveUpButton;

    private BooleanProperty gameOver = new SimpleBooleanProperty();

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @FXML
    public void initialize() {
        stoneImages =List.of(
                new Image(getClass().getResource("/images/stone.png").toExternalForm())
        );
        scoresLabel.textProperty().bind(scores.asString());
        stepsLabel.textProperty().bind(steps.asString());
        gameOver.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                log.info("Game is over");
                log.debug("Saving result to database...");
                gameResultDao.persist(createGameResult());
                stopWatchTimeline.stop();
            }
        });
        resetGame();
    }


    private void resetGame() {
        gameState = new StoneState(StoneState.INITIAL);
        steps.set(0);
        scores.set(4);
        startTime = Instant.now();
        gameOver.setValue(false);
        createStopWatch();
        displayGameState(0,0);
        Platform.runLater(() -> messageLabel.setText("Good luck, " + playerName + "!"));
    }


    private void displayGameState(int row,int col) {
        ImageView view = (ImageView) gameGrid.getChildren().get(0);
        log.trace("Image({}, {}) = {}", row, col, view.getImage());
        view.setImage(stoneImages.get(gameState.getMatrix()[row][col].getValue()));
    }


    public void handleClickOnStone(MouseEvent mouseEvent) {
        int row = GridPane.getRowIndex((Button) mouseEvent.getSource());
        int col = GridPane.getColumnIndex((Button) mouseEvent.getSource());
        String mark = ((Button) mouseEvent.getSource()).getText();
        log.debug("Cube ({}, {}) is pressed", row, col);
        if (! gameState.isSolved() && gameState.canBeMoved(row, col)) {
            steps.set(steps.get() + 1);
            scores.set(scores.get() + Integer.parseInt(mark));
            gameState.moveToNext(row, col);
            if (gameState.isSolved()) {
                gameOver.setValue(true);
                log.info("Player {} has solved the game in {} steps with {} scores", playerName, steps.get(),scores.get());
                messageLabel.setText("Congratulations, " + playerName + "!");
                resetButton.setDisable(true);
                giveUpButton.setText("Finish");
            }
        }
        displayGameState(row,col);
    }

   /* public void handleClickOnStone(MouseEvent mouseEvent) {
        int row = GridPane.getRowIndex((Node) mouseEvent.getSource());
        int col = GridPane.getColumnIndex((Node) mouseEvent.getSource());
        String mark = ((Node) mouseEvent.getSource()).getAccessibleText();
        log.debug("Cube ({}, {}) is pressed", row, col);
        if (! gameState.isSolved() && gameState.canBeMoved(row, col)) {
            steps.set(steps.get() + 1);
            scores.set(scores.get() + Integer.parseInt(mark));
            gameState.moveToNext(row, col);
            if (gameState.isSolved()) {
                gameOver.setValue(true);
                log.info("Player {} has solved the game in {} steps with {} scores", playerName, steps.get(),scores.get());
                messageLabel.setText("Congratulations, " + playerName + "!");
                resetButton.setDisable(true);
                giveUpButton.setText("Finish");
            }
        }
        displayGameState();
    }*/

    public void handleResetButton(ActionEvent actionEvent)  {
        log.debug("{} is pressed", ((Button) actionEvent.getSource()).getText());
        log.info("Resetting game...");
        stopWatchTimeline.stop();
        resetGame();
    }

    public void handleGiveUpButton(ActionEvent actionEvent) throws IOException {
        String buttonText = ((Button) actionEvent.getSource()).getText();
        log.debug("{} is pressed", buttonText);
        if (buttonText.equals("Give Up")) {
            log.info("The game has been given up");
        }
        gameOver.setValue(true);
        log.info("Loading high scores scene...");
        fxmlLoader.setLocation(getClass().getResource("/fxml/highscores.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private GameResult createGameResult() {
        GameResult result = GameResult.builder()
                .player(playerName)
                .solved(gameState.isSolved())
                .duration(Duration.between(startTime, Instant.now()))
                .scores(scores.get())
                .steps(steps.get())
                .build();
        return result;
    }

    private void createStopWatch() {
        stopWatchTimeline = new Timeline(new KeyFrame(javafx.util.Duration.ZERO, e -> {
            long millisElapsed = startTime.until(Instant.now(), ChronoUnit.MILLIS);
            stopWatchLabel.setText(DurationFormatUtils.formatDuration(millisElapsed, "HH:mm:ss"));
        }), new KeyFrame(javafx.util.Duration.seconds(1)));
        stopWatchTimeline.setCycleCount(Animation.INDEFINITE);
        stopWatchTimeline.play();
    }

}

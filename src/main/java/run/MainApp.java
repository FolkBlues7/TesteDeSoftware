package run;

import controllers.GameController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Missão Moedas - Edição MVC");

		// No MVC, o Controller coordena a inicialização
		GameController gameController = new GameController(primaryStage);
		gameController.iniciarJogo();

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
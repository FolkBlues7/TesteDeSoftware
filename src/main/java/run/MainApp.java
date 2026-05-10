package run;

import controllers.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) {
		// Agora o MainApp apenas inicia o sistema de Login
		LoginController loginController = new LoginController(primaryStage);
		loginController.exibirLogin();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
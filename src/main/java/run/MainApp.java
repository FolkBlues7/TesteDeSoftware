package run;

import controllers.GameController;
import controllers.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Usuario;
import views.GameView;
import views.LoginView;

public class MainApp extends Application {

	private LoginController loginController;

	@Override
	public void start(Stage primaryStage) {
		loginController = new LoginController();

		// Tela de login inicial – ao logar, chama iniciarJogoImpl
		LoginView loginView = new LoginView(loginController, this::iniciarJogoImpl);
		Scene scene = new Scene(loginView, 600, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Método centralizado para iniciar o jogo (usado no primeiro acesso
	 * e após retornar do jogo ao menu).
	 */
	private void iniciarJogoImpl(Usuario usuarioLogado) {
		Stage stage = (Stage) Stage.getWindows().getFirst(); // obtém o stage principal

		GameController gameController = new GameController(usuarioLogado, () -> {
			// Ao sair do jogo, salva dados e volta ao menu
			loginController.salvarDadosNoArquivo();
			Scene loginScene = new Scene(
					new LoginView(loginController, this::iniciarJogoImpl),
					600, 600
			);
			stage.setScene(loginScene);
		});

		GameView gameView = new GameView(gameController, stage);
		gameController.carregarNivel();   // gera o mapa e notifica as views
		gameView.iniciar();             // monta e exibe a cena
	}

	public static void main(String[] args) {
		launch(args);
	}
}
package run;

import controllers.GameController;
import controllers.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Usuario;
import views.GameView;
import views.LoginView;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class MainApp extends Application {

	static Consumer<String[]> launchAction = Application::launch;

	private LoginController loginController;
	private final Supplier<LoginController> loginControllerFactory;
	private final IntFunction<models.Mapa> mapaFactory;

	public MainApp() {
		this(LoginController::new, null);
	}

	public MainApp(Supplier<LoginController> loginControllerFactory, IntFunction<models.Mapa> mapaFactory) {
		this.loginControllerFactory = loginControllerFactory;
		this.mapaFactory = mapaFactory;
	}

	@Override
	public void start(Stage primaryStage) {
		loginController = loginControllerFactory.get();

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

		Runnable voltarMenu = () -> {
			// Ao sair do jogo, salva dados e volta ao menu
			loginController.salvarDadosNoArquivo();
			Scene loginScene = new Scene(
					new LoginView(loginController, this::iniciarJogoImpl),
					600, 600
			);
			stage.setScene(loginScene);
		};

		GameController gameController = mapaFactory == null
				? new GameController(usuarioLogado, voltarMenu)
				: new GameController(
						usuarioLogado,
						new models.SessaoJogo(),
						voltarMenu,
						mapaFactory,
						loginController::salvarDadosNoArquivo
				);

		GameView gameView = new GameView(gameController, stage);
		gameController.carregarNivel();   // gera o mapa e notifica as views
		gameView.iniciar();             // monta e exibe a cena
	}

	public static void main(String[] args) {
		launchAction.accept(args);
	}
}

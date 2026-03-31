package run;

import entities.Mapa;
import graphics.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
	private int xAtual = 0;
	private int yAtual = 0;
	private Mapa mapa;
	private GameView gameView;
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		iniciarNovaFase();
		primaryStage.setTitle("Missão Moedas Aleatórias - Avaliação 1");
		primaryStage.show();
	}

	private void iniciarNovaFase() {
		// Reinicia posição
		xAtual = 0;
		yAtual = 0;

		// Cria novo mapa aleatório
		this.mapa = new Mapa(15, 15);
		this.mapa.gerarCenarioAleatorio(3);
		this.gameView = new GameView(mapa);

		// BorderPane ajuda a organizar se quisermos adicionar placar depois
		BorderPane root = new BorderPane();
		root.setCenter(gameView);

		Scene scene = new Scene(root);

		// Configura controles (agora vinculados à nova cena)
		configurarControles(scene);

		primaryStage.setScene(scene);
		gameView.render();
	}

	private void configurarControles(Scene scene) {
		scene.setOnKeyPressed(event -> {
			int novoX = xAtual;
			int novoY = yAtual;

			switch (event.getCode()) {
			case UP -> novoY--;
			case DOWN -> novoY++;
			case LEFT -> novoX--;
			case RIGHT -> novoX++;
			case R -> {
				iniciarNovaFase();
				return;
			} // Tecla para resetar se prender
			}

			// Lógica validada
			if (mapa.podeMover(novoX, novoY)) {
				xAtual = novoX;
				yAtual = novoY;
				mapa.adicionarMovimento(xAtual, yAtual);
				gameView.render();

				// Verifica condição de vitória
				if (mapa.faseConcluida()) {
					System.out.println("Fase Concluída! Gerando nova fase...");
					iniciarNovaFase();
				}
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}

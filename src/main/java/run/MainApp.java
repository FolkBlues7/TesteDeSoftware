package run;

import entities.Mapa;
import graphics.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
	private int xAtual = 0;
	private int yAtual = 0;

	@Override
	public void start(Stage primaryStage) {
		Mapa mapa = new Mapa(15, 15);
		GameView gameView = new GameView(mapa);

		Scene scene = new Scene(gameView);

		// CONTROLES PELO TECLADO
		scene.setOnKeyPressed(event -> {
			int novoX = xAtual;
			int novoY = yAtual;

			switch (event.getCode()) {
			case UP -> novoY--;
			case DOWN -> novoY++;
			case LEFT -> novoX--;
			case RIGHT -> novoX++;
			}

			// AQUI ESTÁ A RASTREABILIDADE: Chamamos a lógica validada por testes!
			// Para o teste, passamos false no 'temItem' por enquanto
			if (mapa.podeMover(novoX, novoY, false)) {
				xAtual = novoX;
				yAtual = novoY;
				mapa.adicionarMovimento(xAtual, yAtual);
				gameView.render(); // Atualiza a tela
			}
		});

		primaryStage.setTitle("Missão JavaFX - Avaliação 1");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

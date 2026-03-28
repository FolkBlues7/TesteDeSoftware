package graphics;

import entities.Mapa;
import entities.Ponto;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class GameView extends StackPane {
	private final Canvas canvas;
	private final Mapa mapa;
	private final int TAMANHO_CELULA = 40;

	public GameView(Mapa mapa) {
		this.mapa = mapa;
		this.canvas = new Canvas(mapa.getColunas() * TAMANHO_CELULA, mapa.getLinhas() * TAMANHO_CELULA);
		getChildren().add(canvas);
		render();
	}

	public void render() {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// Fundo
		gc.setFill(Color.WHITESMOKE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		// 1. Desenha Obstáculos e Grid
		for (int i = 0; i < mapa.getColunas(); i++) {
			for (int j = 0; j < mapa.getLinhas(); j++) {
				if (mapa.isObstaculo(i, j)) {
					gc.setFill(Color.web("#34495e")); // Cinza escuro
					gc.fillRect(i * TAMANHO_CELULA, j * TAMANHO_CELULA, TAMANHO_CELULA, TAMANHO_CELULA);
				}
				gc.setStroke(Color.LIGHTGRAY);
				gc.strokeRect(i * TAMANHO_CELULA, j * TAMANHO_CELULA, TAMANHO_CELULA, TAMANHO_CELULA);
			}
		}

		// 2. Desenha Moedas
		gc.setFill(Color.web("#f1c40f")); // Amarelo Ouro
		for (Ponto moeda : mapa.getMoedas()) {
			gc.fillOval(moeda.x() * TAMANHO_CELULA + 10, moeda.y() * TAMANHO_CELULA + 10, 20, 20);
		}

		// 3. Desenha o Trajeto
		gc.setStroke(Color.web("#3498db"));
		gc.setLineWidth(3);
		var pontos = mapa.getTrajeto();
		for (int i = 0; i < pontos.size() - 1; i++) {
			Ponto p1 = pontos.get(i);
			Ponto p2 = pontos.get(i + 1);
			gc.strokeLine(p1.x() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0),
					p1.y() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0), p2.x() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0),
					p2.y() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0));
		}

		// 4. Desenha o Jogador
		Ponto atual = pontos.get(pontos.size() - 1);
		gc.setFill(Color.web("#e74c3c"));
		gc.fillOval(atual.x() * TAMANHO_CELULA + 5, atual.y() * TAMANHO_CELULA + 5, 30, 30);
	}
}

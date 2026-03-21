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

		// 1. Limpa o fundo
		gc.setFill(Color.WHITESMOKE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		// 2. Desenha o Grid
		gc.setStroke(Color.LIGHTGRAY);
		gc.setLineWidth(1);
		for (int i = 0; i <= mapa.getColunas(); i++) {
			gc.strokeLine(i * TAMANHO_CELULA, 0, i * TAMANHO_CELULA, canvas.getHeight());
		}
		for (int j = 0; j <= mapa.getLinhas(); j++) {
			gc.strokeLine(0, j * TAMANHO_CELULA, canvas.getWidth(), j * TAMANHO_CELULA);
		}

		// 3. Desenha o Trajeto (Rastro)
		gc.setStroke(Color.web("#3498db")); // Azul moderno
		gc.setLineWidth(4);
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
		gc.setFill(Color.web("#e74c3c")); // Vermelho
		gc.fillOval(atual.x() * TAMANHO_CELULA + 5, atual.y() * TAMANHO_CELULA + 5, 30, 30);
	}
}

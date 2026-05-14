package views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import models.Mapa;
import models.Ponto;

public class CanvasView extends StackPane {
    private final Canvas canvas;
    private static final int TAMANHO_CELULA = 40;

    public CanvasView(Mapa mapa) {
        this.canvas = new Canvas(mapa.getColunas() * TAMANHO_CELULA, mapa.getLinhas() * TAMANHO_CELULA);
        getChildren().add(canvas);
    }

    public void render(Mapa mapa) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < mapa.getColunas(); i++) {
            for (int j = 0; j < mapa.getLinhas(); j++) {
                if (mapa.isObstaculo(i, j)) {
                    gc.setFill(Color.web("#34495e"));
                    gc.fillRect(i * TAMANHO_CELULA, j * TAMANHO_CELULA, TAMANHO_CELULA, TAMANHO_CELULA);
                }
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeRect(i * TAMANHO_CELULA, j * TAMANHO_CELULA, TAMANHO_CELULA, TAMANHO_CELULA);
            }
        }

        gc.setFill(Color.web("#f1c40f"));
        for (Ponto moeda : mapa.getMoedas()) {
            gc.fillOval(moeda.x() * TAMANHO_CELULA + 10, moeda.y() * TAMANHO_CELULA + 10, 20, 20);
        }

        Ponto item = mapa.getItemEspecial();
        if (item != null) {
            gc.setFill(Color.web("#9b59b6"));
            gc.fillRect(item.x() * TAMANHO_CELULA + 10, item.y() * TAMANHO_CELULA + 10, 20, 20);
        }

        Ponto alcapao = mapa.getAlcapao();
        if (alcapao != null) {
            gc.setFill(Color.web("#5c4033"));
            gc.fillRect(alcapao.x() * TAMANHO_CELULA + 5, alcapao.y() * TAMANHO_CELULA + 5, 30, 30);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(alcapao.x() * TAMANHO_CELULA + 5, alcapao.y() * TAMANHO_CELULA + 5, 30, 30);
        }

        gc.setStroke(Color.web("#3498db"));
        gc.setLineWidth(3);
        var pontos = mapa.getTrajeto();
        for (int i = 0; i < pontos.size() - 1; i++) {
            Ponto p1 = pontos.get(i);
            Ponto p2 = pontos.get(i + 1);
            gc.strokeLine(
                    p1.x() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0),
                    p1.y() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0),
                    p2.x() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0),
                    p2.y() * TAMANHO_CELULA + (TAMANHO_CELULA / 2.0));
        }

        if (!pontos.isEmpty()) {
            Ponto atual = pontos.get(pontos.size() - 1);
            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(atual.x() * TAMANHO_CELULA + 5, atual.y() * TAMANHO_CELULA + 5, 30, 30);
        }
    }
}
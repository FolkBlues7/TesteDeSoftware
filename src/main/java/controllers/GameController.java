package controllers;

import models.Mapa;
import views.GameView;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GameController {
    private Mapa mapa;
    private GameView gameView;
    private Stage stage;
    private int xAtual = 0;
    private int yAtual = 0;

    public GameController(Stage stage) {
        this.stage = stage;
    }

    public void iniciarJogo() {
        iniciarNovaFase();
    }

    private void iniciarNovaFase() {
        this.xAtual = 0;
        this.yAtual = 0;

        // Model
        this.mapa = new Mapa(15, 15);
        this.mapa.gerarCenarioAleatorio(3);

        // View
        this.gameView = new GameView(mapa);

        BorderPane root = new BorderPane();
        root.setCenter(gameView);
        Scene scene = new Scene(root);

        // Vincula os eventos de teclado
        scene.setOnKeyPressed(this::tratarTeclado);

        stage.setScene(scene);
        gameView.render();
    }

    private void tratarTeclado(KeyEvent event) {
        int novoX = xAtual;
        int novoY = yAtual;

        switch (event.getCode()) {
            case UP -> novoY--;
            case DOWN -> novoY++;
            case LEFT -> novoX--;
            case RIGHT -> novoX++;
            case R -> { iniciarNovaFase(); return; }
        }

        if (mapa.podeMover(novoX, novoY)) {
            xAtual = novoX;
            yAtual = novoY;
            mapa.adicionarMovimento(xAtual, yAtual);
            gameView.render();

            if (mapa.faseConcluida()) {
                System.out.println("Fase Concluída! Avançando...");
                iniciarNovaFase();
                // Futuramente, aqui chamaremos sessao.proximoNivel()
            }
        }
    }
}
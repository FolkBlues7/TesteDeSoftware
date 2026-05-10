package controllers;

import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import views.GameView;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GameController {
    private Usuario usuario;
    private SessaoJogo sessao;
    private Mapa mapa;
    private GameView gameView;
    private Stage stage;
    private int xAtual = 0;
    private int yAtual = 0;

    // ATUALIZADO: Agora o construtor recebe o usuário logado
    public GameController(Stage stage, Usuario usuario) {
        this.stage = stage;
        this.usuario = usuario;
        this.sessao = new SessaoJogo(); // Inicia a sessão no nível 1
    }

    public void iniciarJogo() {
        carregarNivel();
    }

    private void carregarNivel() {
        this.xAtual = 0;
        this.yAtual = 0;

        System.out.println("Jogador: " + usuario.getNome() + " | Nível: " + sessao.getNivelAtual());

        this.mapa = sessao.getMapaDoNivelAtual();

        if (this.mapa == null) {
            this.mapa = new Mapa(15, 15);
            this.mapa.gerarCenarioAleatorio(3 + sessao.getNivelAtual());
            sessao.salvarMapa(this.mapa);
        } else {
            this.mapa.getTrajeto().clear();
            this.mapa.adicionarMovimento(xAtual, yAtual);
            this.mapa.renascerItemEspecial();
        }

        this.gameView = new GameView(mapa);
        BorderPane root = new BorderPane();
        root.setCenter(gameView);

        Scene scene = new Scene(root);
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
            case R -> { carregarNivel(); return; }
        }

        if (mapa.podeMover(novoX, novoY)) {

            // 1. Regra do Alçapão
            if (mapa.isAlcapao(novoX, novoY)) {
                if (sessao.isTemItemEspecial()) {
                    System.out.println("Cristal utilizado!");
                    sessao.avancarNivel();
                } else {
                    System.out.println("Caiu no alçapão!");
                    sessao.voltarNivel();
                }
                carregarNivel();
                return;
            }

            // 2. Coleta de Moedas (Pontuação Local do Usuário)
            Ponto futuraPosicao = new Ponto(novoX, novoY);
            if (mapa.getMoedas().contains(futuraPosicao)) {
                // Remove a moeda do mapa e soma pontos ao usuário
                mapa.coletarMoeda(futuraPosicao);
                usuario.adicionarPontos(10);
                System.out.println("Moeda coletada! Total de " + usuario.getNome() + ": " + usuario.getPontuacaoTotal());
            }

            // 3. Atualiza posição e rastro
            xAtual = novoX;
            yAtual = novoY;
            mapa.adicionarMovimento(xAtual, yAtual);

            // 4. Regra do Item Especial
            if (mapa.isItemEspecial(xAtual, yAtual)) {
                System.out.println("Cristal coletado!");
                sessao.setTemItemEspecial(true);
                mapa.coletarItemEspecial();
            }

            gameView.render();
        }
    }
}
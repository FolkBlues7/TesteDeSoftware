package controllers;

import models.Mapa;
import models.SessaoJogo;
import views.GameView;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GameController {
    private SessaoJogo sessao;
    private Mapa mapa;
    private GameView gameView;
    private Stage stage;
    private int xAtual = 0;
    private int yAtual = 0;

    public GameController(Stage stage) {
        this.stage = stage;
        this.sessao = new SessaoJogo(); // Inicia a sessão no nível 1
    }

    public void iniciarJogo() {
        carregarNivel();
    }

    private void carregarNivel() {
        // Reinicia a posição do jogador para a origem em cada carregamento
        this.xAtual = 0;
        this.yAtual = 0;

        System.out.println("Iniciando Nível " + sessao.getNivelAtual());

        // 1. Tenta buscar o mapa na memória da sessão
        this.mapa = sessao.getMapaDoNivelAtual();

        // 2. Se o mapa não existe (é a primeira vez no nível), gera um novo e salva
        if (this.mapa == null) {
            System.out.println("Gerando novo cenário para o nível " + sessao.getNivelAtual());
            this.mapa = new Mapa(15, 15);
            this.mapa.gerarCenarioAleatorio(3 + sessao.getNivelAtual());
            sessao.salvarMapa(this.mapa);
        } else {
            // 3. Se o mapa já existia, restauramos o estado para uma nova tentativa
            System.out.println("Mapa restaurado da memória.");

            // Limpa o trajeto antigo para começar o rastro do zero
            this.mapa.getTrajeto().clear();
            this.mapa.adicionarMovimento(xAtual, yAtual);

            // Faz o cristal renascer na posição original (conforme combinado)
            this.mapa.renascerItemEspecial();
        }

        // Configuração da Interface
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

        // Validação de movimento
        if (mapa.podeMover(novoX, novoY)) {

            // Regra do Alçapão (Trapdoor)
            if (mapa.isAlcapao(novoX, novoY)) {
                if (sessao.isTemItemEspecial()) {
                    System.out.println("Cristal utilizado! Subindo de nível...");
                    sessao.avancarNivel();
                } else {
                    System.out.println("Sem cristal! Você caiu no alçapão e regrediu de nível.");
                    sessao.voltarNivel();
                }
                carregarNivel(); // Recarrega o mapa (novo ou da memória)
                return;
            }

            // Executa o movimento
            xAtual = novoX;
            yAtual = novoY;
            mapa.adicionarMovimento(xAtual, yAtual);

            // Coleta do Cristal (Item Especial)
            if (mapa.isItemEspecial(xAtual, yAtual)) {
                System.out.println("Cristal coletado! Agora você pode usar o alçapão.");
                sessao.setTemItemEspecial(true);
                mapa.coletarItemEspecial();
            }

            gameView.render();
        }
    }
}
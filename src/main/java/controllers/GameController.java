package controllers;

import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import views.GameView;
import views.HUDView;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GameController {
    private HUDView hud;
    private Usuario usuario;
    private SessaoJogo sessao;
    private Mapa mapa;
    private GameView gameView;
    private Stage stage;
    private Runnable onVoltarMenu;

    // Deixei public para podermos acessar nos testes
    public int xAtual = 0;
    public int yAtual = 0;
    public boolean modoTeste = false; // Flag para ignorar a UI nos testes

    public GameController(Stage stage, Usuario usuario, Runnable onVoltarMenu) {
        this.stage = stage;
        this.usuario = usuario;
        this.onVoltarMenu = onVoltarMenu;
        this.sessao = new SessaoJogo();
    }

    // Construtor exclusivo para os Testes (sem o Stage do JavaFX)
    public GameController(Usuario usuario, SessaoJogo sessao, Mapa mapa, Runnable onVoltarMenu) {
        this.usuario = usuario;
        this.sessao = sessao;
        this.mapa = mapa;
        this.onVoltarMenu = onVoltarMenu;
        this.modoTeste = true;
    }

    public void iniciarJogo() {
        carregarNivel();
    }

    public void carregarNivel() {
        this.xAtual = 0;
        this.yAtual = 0;

        if (!modoTeste) {
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
            atualizarInterfaceGrafica();
        }
    }

    private void atualizarInterfaceGrafica() {
        this.gameView = new GameView(mapa);
        this.hud = new HUDView();
        this.hud.atualizar(usuario, sessao);
        this.hud.getBtnSair().setOnAction(e -> onVoltarMenu.run());

        BorderPane root = new BorderPane();
        root.setCenter(gameView);
        root.setTop(hud);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::tratarTeclado);

        stage.setScene(scene);
        stage.sizeToScene();
        root.requestFocus();
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
            case ESCAPE -> { onVoltarMenu.run(); return; }
        }

        aplicarRegrasDeMovimento(novoX, novoY);
    }

    public void aplicarRegrasDeMovimento(int novoX, int novoY) {
        if (mapa.podeMover(novoX, novoY)) {

            if (mapa.isAlcapao(novoX, novoY)) {
                if (sessao.isTemItemEspecial()) {
                    sessao.avancarNivel();
                } else {
                    sessao.voltarNivel();
                }
                carregarNivel(); // Reinicia a posição
                return;
            }

            Ponto futuraPosicao = new Ponto(novoX, novoY);
            if (mapa.getMoedas().contains(futuraPosicao)) {
                mapa.coletarMoeda(futuraPosicao);
                usuario.adicionarPontos(10);
            }

            // Atualiza posição
            xAtual = novoX;
            yAtual = novoY;
            mapa.adicionarMovimento(xAtual, yAtual);

            if (mapa.isItemEspecial(xAtual, yAtual)) {
                sessao.setTemItemEspecial(true);
                mapa.coletarItemEspecial();
            }

            if (!modoTeste) {
                gameView.render();
                hud.atualizar(usuario, sessao);
            }
        }
    }
}
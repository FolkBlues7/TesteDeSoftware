package controllers;

import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;

public class GameController {
    public interface GameListener {
        void render();
        void atualizarHUD();
    }

    private final Usuario usuario;
    private SessaoJogo sessao;
    private Mapa mapa;
    private GameListener listener;
    private Runnable onVoltarMenu;

    public int xAtual = 0;
    public int yAtual = 0;
    public boolean modoTeste = false;

    // Construtor usado pela aplicação real (modo normal)
    public GameController(Usuario usuario, Runnable onVoltarMenu) {
        this.usuario = usuario;
        this.onVoltarMenu = onVoltarMenu;
        this.sessao = new SessaoJogo();
        this.modoTeste = false;
    }

    // Construtor exclusivo para os testes (injeta dependências mockadas)
    public GameController(Usuario usuario, SessaoJogo sessao, Mapa mapa, Runnable onVoltarMenu) {
        this.usuario = usuario;
        this.sessao = sessao;
        this.mapa = mapa;
        this.onVoltarMenu = onVoltarMenu;
        this.modoTeste = true;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
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
                getMapa().getTrajeto().clear();
                getMapa().adicionarMovimento(xAtual, yAtual);
                getMapa().renascerItemEspecial();
            }
            notificarViews();
        }
    }
    public void aplicarRegrasDeMovimento(int novoX, int novoY) {
        if (getMapa().podeMover(novoX, novoY)) {

            if (getMapa().isAlcapao(novoX, novoY)) {
                if (getSessao().isTemItemEspecial()) {
                    getSessao().avancarNivel();
                } else {
                    getSessao().voltarNivel();
                }
                carregarNivel();
                return;
            }

            Ponto futuraPosicao = new Ponto(novoX, novoY);
            if (getMapa().getMoedas().contains(futuraPosicao)) {
                getMapa().coletarMoeda(futuraPosicao);
                getUsuario().adicionarPontos(10);
            }

            xAtual = novoX;
            yAtual = novoY;
            getMapa().adicionarMovimento(xAtual, yAtual);

            if (getMapa().isItemEspecial(xAtual, yAtual)) {
                getSessao().setTemItemEspecial(true);
                getMapa().coletarItemEspecial();
            }

            if (!modoTeste) {
                notificarViews();
            }
        }
    }

    public Usuario getUsuario() { return usuario; }
    public SessaoJogo getSessao() { return sessao; }
    public Mapa getMapa() { return mapa; }
    public Runnable getOnVoltarMenu() { return onVoltarMenu; }

    private void notificarViews() {
        if (listener != null) {
            listener.render();
            listener.atualizarHUD();
        }
    }
}
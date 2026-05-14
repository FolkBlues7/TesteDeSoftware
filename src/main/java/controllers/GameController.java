package controllers;

import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;

/**
 * Controlador principal do jogo, responsável por coordenar as ações do jogador,
 * aplicar regras de movimento e notificar a interface gráfica.
 *
 * <p>Invariante: após {@link #carregarNivel()}, {@code xAtual} e {@code yAtual} são
 * sempre posições dentro dos limites do mapa carregado (se houver mapa). Durante o
 * modo normal, a cada alteração de estado as views são notificadas.
 */
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

    /**
     * Construtor usado pela aplicação real (modo normal).
     *
     * <pre>
     * Pré-condição: {@code usuario} e {@code onVoltarMenu} não nulos.
     * Pós-condição: modoTeste = false, sessão e mapa serão criados sob demanda.
     * </pre>
     */
    public GameController(Usuario usuario, Runnable onVoltarMenu) {
        assert usuario != null : "Usuario não pode ser nulo";
        assert onVoltarMenu != null : "Runnable de voltar ao menu não pode ser nulo";
        this.usuario = usuario;
        this.onVoltarMenu = onVoltarMenu;
        this.sessao = new SessaoJogo();
        this.modoTeste = false;
    }

    /**
     * Construtor exclusivo para testes, permitindo a injeção de dependências mockadas.
     *
     * <pre>
     * Pré-condição: todos os parâmetros são não nulos.
     * Pós-condição: modoTeste = true.
     * </pre>
     */
    public GameController(Usuario usuario, SessaoJogo sessao, Mapa mapa, Runnable onVoltarMenu) {
        assert usuario != null : "Usuario não pode ser nulo";
        assert sessao != null : "SessaoJogo não pode ser nula";
        assert mapa != null : "Mapa não pode ser nulo";
        assert onVoltarMenu != null : "Runnable de voltar ao menu não pode ser nulo";
        this.usuario = usuario;
        this.sessao = sessao;
        this.mapa = mapa;
        this.onVoltarMenu = onVoltarMenu;
        this.modoTeste = true;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    /**
     * Prepara ou restaura o nível atual do jogo.
     *
     * <pre>
     * Pós-condição:
     *   - xAtual = 0, yAtual = 0.
     *   - Se modoTeste == false e o mapa do nível atual não existia, um novo mapa é gerado
     *     e salvo na sessão; caso contrário, o mapa existente é reutilizado com trajeto limpo
     *     e item especial renascido.
     *   - Se houver um listener registrado e modoTeste == false, os métodos {@code render()}
     *     e {@code atualizarHUD()} são invocados.
     * </pre>
     */
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
        // Invariante simples: se há mapa, a posição inicial deve ser válida
        assert mapa == null || mapa.podeMover(xAtual, yAtual)
                : "Posição inicial (" + xAtual + "," + yAtual + ") inválida no mapa";
    }

    /**
     * Processa uma tentativa de movimento para a coordenada (novoX, novoY).
     * Aplica todas as regras do jogo: validação de obstáculo, coleta de moedas,
     * ativação de item especial e interação com alçapão.
     *
     * <pre>
     * Pré-condição: {@code mapa} não nulo (o nível deve ter sido carregado).
     * Pós-condição:
     *   - Se o movimento é válido e a célula não é alçapão:
     *        xAtual == novoX, yAtual == novoY, e o movimento é registrado no mapa.
     *        Caso a célula contenha moeda ou item especial, os efeitos correspondentes são aplicados.
     *   - Se o movimento é válido e a célula é alçapão:
     *        a sessão avança ou volta de nível (dependendo da posse do item especial) e
     *        o nível é recarregado, resetando as coordenadas.
     *   - Se o movimento é inválido, o estado não é alterado.
     *   - No modo normal (não teste), as views são notificadas após qualquer alteração.
     * </pre>
     */
    public void aplicarRegrasDeMovimento(int novoX, int novoY) {
        assert mapa != null : "Mapa não foi carregado";

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
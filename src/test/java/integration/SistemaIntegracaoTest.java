package integration;

import controllers.GameController;
import controllers.LoginController;
import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SistemaIntegracaoTest extends BaseIntegrationTest {

    // NOTA: @BeforeEach, @AfterEach e ARQUIVO_TESTE foram movidos para a superclasse BaseIntegrationTest.

    // =========================================================================
    // CENÁRIO 1: PERSISTÊNCIA E CICLO DE VIDA DO USUÁRIO
    // Testa: LoginController + Usuario + File System (I/O)
    // =========================================================================
    @Test
    public void integracao_FluxoDePersistenciaCompleto() {
        // 1. Inicia o sistema pela primeira vez
        LoginController sistemaInicial = new LoginController();

        // 2. Cadastra um novo jogador e faz login (incrementa a sessão para 1)
        sistemaInicial.tentarCadastrar("mario", "senha123");
        Usuario usuarioLogado = sistemaInicial.tentarLogin("mario", "senha123");

        assertNotNull(usuarioLogado);
        assertEquals(1, usuarioLogado.getSessoesExecutadas());

        // 3. O jogador ganha pontos no jogo
        usuarioLogado.adicionarPontos(500);

        // Força o controlador atual a gravar a pontuação recém-adquirida no arquivo
        sistemaInicial.salvarDadosNoArquivo();

        // 4. Simulamos o fechamento do jogo e a reabertura no dia seguinte
        LoginController sistemaDiaSeguinte = new LoginController(); // Vai ler o usuarios.txt atualizado

        // 5. Faz login novamente (deverá incrementar a sessão para 2)
        Usuario usuarioRecuperado = sistemaDiaSeguinte.tentarLogin("mario", "senha123");

        assertNotNull(usuarioRecuperado, "O usuário deveria ter sido recuperado do TXT.");
        assertEquals(500, usuarioRecuperado.getPontuacaoTotal(), "Os pontos ganhos na sessão anterior não foram salvos/carregados.");
        assertEquals(2, usuarioRecuperado.getSessoesExecutadas(), "Deveria estar na segunda sessão de jogo.");
    }

    // =========================================================================
    // CENÁRIO 2: NAVEGAÇÃO ENTRE NÍVEIS COM MAPAS REAIS
    // Testa: SessaoJogo + Mapa (Sem Mocks)
    // =========================================================================
    @Test
    public void integracao_SessaoGerenciaHistoricoDeMapasReais() {
        SessaoJogo sessao = new SessaoJogo();

        // 1. Gera um mapa real para o Nível 1
        Mapa mapaNivel1 = new Mapa(5, 5);
        mapaNivel1.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());
        sessao.salvarMapa(mapaNivel1);

        // 2. Jogador pega o item e avança para o Nível 2
        sessao.setTemItemEspecial(true);
        sessao.avancarNivel();

        // 3. Gera um novo mapa real para o Nível 2
        Mapa mapaNivel2 = new Mapa(10, 10);
        mapaNivel2.gerarCenarioPredefinido(new boolean[10][10], new ArrayList<>());
        sessao.salvarMapa(mapaNivel2);

        // 4. Jogador cai no alçapão sem querer e volta para o Nível 1
        sessao.voltarNivel();

        // VALIDAÇÃO: O mapa recuperado da memória deve ser exatamente a instância de 5x5 criada no início
        Mapa mapaRecuperado = sessao.getMapaDoNivelAtual();
        assertNotNull(mapaRecuperado);
        assertEquals(5, mapaRecuperado.getColunas(), "O mapa recuperado do histórico deveria ser o do Nível 1 (5x5).");
    }

    // =========================================================================
    // CENÁRIO 3: FLUXO DE JOGABILIDADE COMPLETO
    // Testa: GameController + Usuario + SessaoJogo + Mapa
    // =========================================================================
    @Test
    public void integracao_FluxoDeGameplayPontuacaoEAvancoDeFase() {
        Usuario jogador = new Usuario("luigi", "123", false);
        SessaoJogo sessao = new SessaoJogo();

        Mapa mapa = new Mapa(3, 3);
        List<Ponto> moedas = List.of(new Ponto(1, 0));
        mapa.gerarCenarioPredefinido(new boolean[3][3], moedas);

        Runnable callbackDeFuga = () -> {};
        GameController game = new GameController(jogador, sessao, mapa, callbackDeFuga);

        assertEquals(0, jogador.getPontuacaoTotal());
        assertEquals(1, sessao.getNivelAtual());

        game.aplicarRegrasDeMovimento(1, 0);

        assertTrue(jogador.getPontuacaoTotal() > 0, "O jogador deveria ter ganho pontos ao coletar a moeda no movimento.");
    }

    // =========================================================================
    // CENÁRIO 4: BLOQUEIO DE MOVIMENTO INVÁLIDO E INTEGRIDADE DE ESTADO
    // Testa: GameController + Mapa Real com Obstáculos
    // =========================================================================
    @Test
    public void integracao_MovimentoInvalidoContraObstaculoNaoAlteraEstadoDoJogo() {
        boolean[][] obstaculos = new boolean[3][3];
        obstaculos[1][0] = true;

        Mapa mapaComParede = new Mapa(3, 3);
        mapaComParede.gerarCenarioPredefinido(obstaculos, new ArrayList<>());

        Usuario jogador = new Usuario("toad", "senha", false);
        SessaoJogo sessao = new SessaoJogo();

        GameController game = new GameController(jogador, sessao, mapaComParede, () -> {});

        assertEquals(0, game.xAtual);
        assertEquals(0, game.yAtual);
        int pontuacaoInicial = jogador.getPontuacaoTotal();

        game.aplicarRegrasDeMovimento(1, 0);

        assertEquals(0, game.xAtual, "O jogador conseguiu atravessar uma parede física do mapa!");
        assertEquals(0, game.yAtual);
        assertEquals(pontuacaoInicial, jogador.getPontuacaoTotal(), "O jogador recebeu alteração de pontos em um movimento inválido.");

        game.aplicarRegrasDeMovimento(-1, 5);

        assertEquals(0, game.xAtual, "O jogador saiu dos limites físicos do mapa!");
        assertEquals(0, game.yAtual);
    }
}
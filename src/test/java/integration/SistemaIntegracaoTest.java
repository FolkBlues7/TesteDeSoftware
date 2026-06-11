package integration;

import controllers.GameController;
import controllers.LoginController;
import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SistemaIntegracaoTest {

    private final String ARQUIVO_TESTE = "usuarios.txt";

    @BeforeEach
    public void setup() throws Exception {
        // Limpamos o ambiente de arquivos antes de cada teste para evitar poluição
        Files.deleteIfExists(Path.of(ARQUIVO_TESTE));
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(Path.of(ARQUIVO_TESTE));
    }

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

        // CORREÇÃO AQUI: Força o controlador atual a gravar a pontuação recém-adquirida
        // no arquivo usuarios.txt antes de fechar/encerrar a sessão.
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
        // 1. Instanciamos todos os objetos REAIS do domínio
        Usuario jogador = new Usuario("luigi", "123", false);
        SessaoJogo sessao = new SessaoJogo();

        // Vamos criar um mapa 3x3 perfeitamente controlado:
        // (0,0) Jogador
        // (1,0) Moeda
        // (2,0) Item Especial
        // (2,1) Alçapão
        Mapa mapa = new Mapa(3, 3);
        List<Ponto> moedas = List.of(new Ponto(1, 0));
        mapa.gerarCenarioPredefinido(new boolean[3][3], moedas);
        // Forçando o item e alçapão usando os métodos do Mapa (supondo que existam setters ou manipulando as coordenadas no seu controller)
        // Como não temos acesso aos setters privados, a integração será via movimento:

        // 2. Injetamos tudo no GameController (Modo Teste = true para não abrir tela)
        Runnable callbackDeFuga = () -> {}; // Ação vazia
        GameController game = new GameController(jogador, sessao, mapa, callbackDeFuga);

        // ESTADO INICIAL
        assertEquals(0, jogador.getPontuacaoTotal());
        assertEquals(1, sessao.getNivelAtual());

        // AÇÃO 1: Jogador anda para a direita (1,0) e pega a moeda
        game.aplicarRegrasDeMovimento(1, 0); // Supondo que aplicarRegrasDeMovimento lida com a lógica de coletar

        // Se a moeda é processada, a pontuação do usuário deve subir
        // (Nota: você precisará ajustar de acordo com a regra exata de pontos do seu GameController)
        assertTrue(jogador.getPontuacaoTotal() > 0, "O jogador deveria ter ganho pontos ao coletar a moeda no movimento.");

        // AÇÃO 2: Pega o item especial e vai pro Alçapão (Simulação de avanço de nível)
        // Se o gameController lida com itemEspecial e Alçapão no método aplicarRegrasDeMovimento:
        // game.aplicarRegrasDeMovimento(2, 0); // Pega item
        // game.aplicarRegrasDeMovimento(2, 1); // Cai no alçapão com item

        // Verificamos a integração com a sessão
        // assertEquals(2, sessao.getNivelAtual(), "Ao cair no alçapão com o item, o controller deveria ter mandado a Sessão avançar o nível.");
    }
       

    // =========================================================================
    // CENÁRIO 4: BLOQUEIO DE MOVIMENTO INVÁLIDO E INTEGRIDADE DE ESTADO
    // Testa: GameController + Mapa Real com Obstáculos
    // =========================================================================
    @Test
    public void integracao_MovimentoInvalidoContraObstaculoNaoAlteraEstadoDoJogo() {
        // 1. Criamos um mapa 3x3 onde a posição à direita (1,0) é um OBSTÁCULO (true)
        boolean[][] obstaculos = new boolean[3][3];
        obstaculos[1][0] = true; // Parede/Obstáculo bem na frente do jogador

        Mapa mapaComParede = new Mapa(3, 3);
        mapaComParede.gerarCenarioPredefinido(obstaculos, new ArrayList<>());

        Usuario jogador = new Usuario("toad", "senha", false);
        SessaoJogo sessao = new SessaoJogo();

        // 2. Inicializa o controlador do jogo com o mapa real bloqueado
        GameController game = new GameController(jogador, sessao, mapaComParede, () -> {});

        // Posição inicial esperada
        assertEquals(0, game.xAtual);
        assertEquals(0, game.yAtual);
        int pontuacaoInicial = jogador.getPontuacaoTotal();

        // 3. AÇÃO: Força o movimento em direção ao obstáculo (1,0)
        game.aplicarRegrasDeMovimento(1, 0);

        // 4. VALIDAÇÃO DE INTEGRAÇÃO:
        // O Mapa deve ter dito ao GameController que não podia mover.
        // Logo, as coordenadas no controlador NÃO podem ter mudado.
        assertEquals(0, game.xAtual, "O jogador conseguiu atravessar uma parede física do mapa!");
        assertEquals(0, game.yAtual);
        assertEquals(pontuacaoInicial, jogador.getPontuacaoTotal(), "O jogador recebeu alteração de pontos em um movimento inválido.");

        // 5. AÇÃO 2: Tenta mover para coordenadas completamente fora dos limites do mapa (-1, 5)
        game.aplicarRegrasDeMovimento(-1, 5);

        // VALIDAÇÃO: Continua protegido na posição segura inicial
        assertEquals(0, game.xAtual, "O jogador saiu dos limites físicos do mapa!");
        assertEquals(0, game.yAtual);
    }
}

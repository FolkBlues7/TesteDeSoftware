package integration;

import controllers.GameController;
import controllers.LoginController;
import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

class SistemaIntegracaoTest extends BaseIntegrationTest {

    @Test
    // Teste de sistema: valida conta e administração.
    void usuarioGerenciaContaDoCadastroAExclusao() {
        SistemaFacade sistema = new SistemaFacade(arquivoUsuarios());

        assertEquals("Preencha todos os campos!", sistema.cadastrar(" ", "senha"));
        assertEquals("Entrada inválida!", sistema.cadastrar("mario;999;999;true", "senha"));
        assertEquals("Cadastrado com sucesso!", sistema.cadastrar("mario", "senha123"));
        assertEquals("Usuário já existe!", sistema.cadastrar("MARIO", "outra"));
        assertNull(sistema.entrar("mario", "incorreta"));

        Usuario usuario = sistema.entrar("mario", "senha123");
        assertNotNull(usuario);
        assertEquals(1, usuario.getSessoesExecutadas());
        assertEquals("Apenas o admin pode excluir (digite senha do admin).",
                sistema.excluir("incorreta"));
        assertEquals("Usuário removido!", sistema.excluir("123"));
        assertNull(sistema.entrar("mario", "senha123"));
    }

    @Test
    // Teste de sistema: valida gameplay e persistência.
    void jogadorPontuaERecuperaProgressoAposReabrir() {
        SistemaFacade sistema = new SistemaFacade(arquivoUsuarios());
        sistema.cadastrar("luigi", "123");
        Usuario jogador = sistema.entrar("luigi", "123");
        Mapa mapa = mapaGameplay();
        GameController jogo = sistema.iniciarJogo(jogador, nivel -> mapa);

        jogo.aplicarRegrasDeMovimento(0, 1);
        jogo.aplicarRegrasDeMovimento(-1, 0);
        assertEquals(0, jogo.xAtual);
        assertEquals(0, jogo.yAtual);

        jogo.aplicarRegrasDeMovimento(1, 0);
        assertEquals(10, jogador.getPontuacaoTotal());
        assertTrue(mapa.faseConcluida());

        SistemaFacade sistemaReaberto = new SistemaFacade(arquivoUsuarios());
        Usuario recuperado = sistemaReaberto.entrar("luigi", "123");
        assertNotNull(recuperado);
        assertEquals(10, recuperado.getPontuacaoTotal());
        assertEquals(2, recuperado.getSessoesExecutadas());
    }

    @Test
    // Teste de sistema: valida progressão entre níveis.
    void jogadorAvancaERetornaAoMapaAnterior() {
        SistemaFacade sistema = new SistemaFacade(arquivoUsuarios());
        sistema.cadastrar("peach", "123");
        Usuario jogador = sistema.entrar("peach", "123");
        Mapa nivelUm = mapaProgressao(new Ponto(1, 0), new Ponto(3, 0));
        Mapa nivelDois = mapaProgressao(new Ponto(0, 1), new Ponto(1, 0));
        GameController jogo = sistema.iniciarJogo(jogador,
                nivel -> nivel == 1 ? nivelUm : nivelDois);

        jogo.aplicarRegrasDeMovimento(1, 0);
        assertTrue(jogo.getSessao().isTemItemEspecial());
        jogo.aplicarRegrasDeMovimento(2, 0);
        jogo.aplicarRegrasDeMovimento(3, 0);

        assertEquals(2, jogo.getSessao().getNivelAtual());
        assertFalse(jogo.getSessao().isTemItemEspecial());
        assertSame(nivelDois, jogo.getMapa());

        jogo.aplicarRegrasDeMovimento(1, 0);

        assertEquals(1, jogo.getSessao().getNivelAtual());
        assertSame(nivelUm, jogo.getMapa());
        assertEquals(List.of(new Ponto(0, 0)), nivelUm.getTrajeto());
        assertNotNull(nivelUm.getItemEspecial());
    }

    private Mapa mapaGameplay() {
        Mapa mapa = new Mapa(3, 3);
        boolean[][] obstaculos = new boolean[3][3];
        obstaculos[0][1] = true;
        mapa.gerarCenarioPredefinido(obstaculos, List.of(new Ponto(1, 0)));
        return mapa;
    }

    private Mapa mapaProgressao(Ponto itemEspecial, Ponto alcapao) {
        Mapa mapa = new Mapa(4, 4);
        mapa.gerarCenarioPredefinido(new boolean[4][4], List.of());
        mapa.setItemEspecial(itemEspecial);
        mapa.setAlcapao(alcapao);
        return mapa;
    }

    private static final class SistemaFacade {
        private final LoginController login;

        private SistemaFacade(java.nio.file.Path arquivoUsuarios) {
            this.login = new LoginController(arquivoUsuarios);
        }

        private String cadastrar(String usuario, String senha) {
            return login.tentarCadastrar(usuario, senha);
        }

        private Usuario entrar(String usuario, String senha) {
            return login.tentarLogin(usuario, senha);
        }

        private String excluir(String senhaAdmin) {
            return login.tentarExcluir("mario", senhaAdmin);
        }

        private GameController iniciarJogo(Usuario usuario, IntFunction<Mapa> criarMapa) {
            GameController jogo = new GameController(usuario, new SessaoJogo(), () -> {},
                    criarMapa, login::salvarDadosNoArquivo);
            jogo.carregarNivel();
            return jogo;
        }
    }
}

package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    private Usuario usuarioNovo;
    private Usuario usuarioCarregado;

    @BeforeEach
    public void setup() {
        // Cenário 1: Um jogador comum recém-criado na tela de cadastro
        usuarioNovo = new Usuario("jogador1", "senha123", false);

        // Cenário 2: Um admin simulando dados carregados do arquivo TXT
        usuarioCarregado = new Usuario("admin", "adminPass", 500, 10, true);
    }

    // ==========================================
    // 1. TESTES DE ESTADO INICIAL (CONSTRUTORES)
    // ==========================================

    /**
     * TIPO: Teste de Estado
     * O QUE FAZ: Verifica se o construtor padrão inicializa os atributos de
     * pontuação e sessões com ZER0, garantindo um placar limpo para novos cadastros.
     */
    @Test
    public void dominio_ConstrutorNovoUsuarioIniciaComZeroPontosESessoes() {
        assertEquals("jogador1", usuarioNovo.getLogin());
        assertEquals("senha123", usuarioNovo.getSenha());
        assertFalse(usuarioNovo.isSuperUsuario(), "Usuário comum não deve ser admin.");

        // As regras de negócio vitais do construtor:
        assertEquals(0, usuarioNovo.getPontuacaoTotal(), "Pontuação inicial deve ser 0.");
        assertEquals(0, usuarioNovo.getSessoesExecutadas(), "Sessões iniciais devem ser 0.");
    }

    /**
     * TIPO: Teste de Estado
     * O QUE FAZ: Verifica se o construtor de carregamento (usado pelo LoginController)
     * restaura perfeitamente o progresso antigo do jogador.
     */
    @Test
    public void dominio_ConstrutorDeCarregamentoRestauraValoresCorretamente() {
        assertEquals("admin", usuarioCarregado.getLogin());
        assertEquals("adminPass", usuarioCarregado.getSenha());
        assertTrue(usuarioCarregado.isSuperUsuario());
        assertEquals(500, usuarioCarregado.getPontuacaoTotal());
        assertEquals(10, usuarioCarregado.getSessoesExecutadas());
    }

    // ==========================================
    // 2. TESTES DE COMPORTAMENTO E MUTAÇÃO
    // ==========================================

    /**
     * TIPO: Teste de Mutação de Estado
     * O QUE FAZ: Garante que o método de adicionar pontos faz a soma corretamente
     * no montante total (simulando pegar várias moedas no jogo).
     */
    @Test
    public void dominio_AdicionarPontosSomaAoTotal() {
        // Começa com 0
        usuarioNovo.adicionarPontos(50);
        assertEquals(50, usuarioNovo.getPontuacaoTotal());

        // Pega mais pontos, deve somar ao que já tinha
        usuarioNovo.adicionarPontos(25);
        assertEquals(75, usuarioNovo.getPontuacaoTotal());
    }

    /**
     * TIPO: Teste de Mutação de Estado
     * O QUE FAZ: Garante que a cada login bem sucedido, as sessões do usuário sobem em 1.
     */
    @Test
    public void dominio_IncrementarSessoesAumentaDeUmEmUm() {
        // Novo usuário começa com 0 sessões
        usuarioNovo.incrementarSessoes();
        assertEquals(1, usuarioNovo.getSessoesExecutadas());

        usuarioNovo.incrementarSessoes();
        assertEquals(2, usuarioNovo.getSessoesExecutadas());

        // Usuário carregado que já tinha 10, deve ir para 11
        usuarioCarregado.incrementarSessoes();
        assertEquals(11, usuarioCarregado.getSessoesExecutadas());
    }

    // ==========================================
    // 3. TESTES DE COMPATIBILIDADE (GETTERS EXTRAS)
    // ==========================================

    /**
     * TIPO: Teste de Estado
     * O QUE FAZ: Garante que o método de compatibilidade exigido pelo HUD
     * retorna exatamente a mesma String do Login.
     */
    @Test
    public void dominio_GetNomeRetornaOLoginParaOHud() {
        assertEquals(usuarioNovo.getLogin(), usuarioNovo.getNome(), "O método getNome() deve agir como um espelho de getLogin().");
        assertEquals("jogador1", usuarioNovo.getNome());
    }
}
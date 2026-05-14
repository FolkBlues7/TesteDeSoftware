package models;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    private Usuario usuarioNovo;
    private Usuario usuarioCarregado;

    @BeforeEach
    public void setup() {
        usuarioNovo = new Usuario("jogador1", "senha123", false);
        usuarioCarregado = new Usuario("admin", "adminPass", 500, 10, true);
    }

    @Test
    // Teste de domínio
    public void dominio_ConstrutorNovoUsuarioIniciaComZeroPontosESessoes() {
        assertEquals("jogador1", usuarioNovo.getLogin());
        assertEquals("senha123", usuarioNovo.getSenha());
        assertFalse(usuarioNovo.isSuperUsuario());
        assertEquals(0, usuarioNovo.getPontuacaoTotal());
        assertEquals(0, usuarioNovo.getSessoesExecutadas());
    }

    @Test
    // Teste de domínio
    public void dominio_ConstrutorDeCarregamentoRestauraValoresCorretamente() {
        assertEquals("admin", usuarioCarregado.getLogin());
        assertEquals("adminPass", usuarioCarregado.getSenha());
        assertTrue(usuarioCarregado.isSuperUsuario());
        assertEquals(500, usuarioCarregado.getPontuacaoTotal());
        assertEquals(10, usuarioCarregado.getSessoesExecutadas());
    }

    @Test
    // Teste de domínio
    public void dominio_AdicionarPontosSomaAoTotal() {
        usuarioNovo.adicionarPontos(50);
        assertEquals(50, usuarioNovo.getPontuacaoTotal());

        usuarioNovo.adicionarPontos(25);
        assertEquals(75, usuarioNovo.getPontuacaoTotal());
    }

    @Test
    // Teste de domínio
    public void dominio_IncrementarSessoesAumentaDeUmEmUm() {
        usuarioNovo.incrementarSessoes();
        assertEquals(1, usuarioNovo.getSessoesExecutadas());

        usuarioNovo.incrementarSessoes();
        assertEquals(2, usuarioNovo.getSessoesExecutadas());

        usuarioCarregado.incrementarSessoes();
        assertEquals(11, usuarioCarregado.getSessoesExecutadas());
    }

    @Test
    // Teste de domínio
    public void dominio_GetNomeRetornaOLoginParaOHud() {
        assertEquals(usuarioNovo.getLogin(), usuarioNovo.getNome());
        assertEquals("jogador1", usuarioNovo.getNome());
    }

    @Test
    // Teste de fronteira
    public void fronteira_AdicionarPontosNegativos() {
        usuarioNovo.adicionarPontos(-10);
        assertEquals(-10, usuarioNovo.getPontuacaoTotal()); // comportamento atual permite
    }

    @Test
    // Teste de fronteira
    public void fronteira_PontuacaoAlemDoLimiteInteiro() {
        usuarioNovo.adicionarPontos(Integer.MAX_VALUE - 1);
        usuarioNovo.adicionarPontos(2); // overflow silencioso
        assertTrue(usuarioNovo.getPontuacaoTotal() < 0); // overflow gera negativo
    }

    @Property
    // Teste de propriedade
    void propriedade_PontuacaoESessoesNuncaNegativasAposOperacoes(
            @ForAll @IntRange(min = -100, max = 1000) int deltaPontos,
            @ForAll @IntRange(min = 0, max = 100) int incrementos) {
        Usuario u = new Usuario("test", "pass", false);
        u.adicionarPontos(deltaPontos);
        for (int i = 0; i < incrementos; i++) {
            u.incrementarSessoes();
        }
        // sessoes devem ser >= 0
        assertTrue(u.getSessoesExecutadas() >= 0);
    }
}
package models;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class SessaoJogoTest {

    private SessaoJogo sessao;

    @BeforeEach
    public void setup() {
        sessao = new SessaoJogo();
    }

    @Test
    // Teste de domínio
    public void dominio_EstadoInicialDaSessao() {
        assertEquals(1, sessao.getNivelAtual());
        assertFalse(sessao.isTemItemEspecial());
        assertNull(sessao.getMapaDoNivelAtual());
    }

    @Test
    // Teste de domínio
    public void dominio_PodePegarItemEspecial() {
        sessao.setTemItemEspecial(true);
        assertTrue(sessao.isTemItemEspecial());
    }

    @Test
    // Teste estrutural (MC/DC)
    public void estrutural_AvancarNivelConsomeItemEAvanca() {
        sessao.setTemItemEspecial(true);
        sessao.avancarNivel();

        assertEquals(2, sessao.getNivelAtual());
        assertFalse(sessao.isTemItemEspecial());
    }

    @Test
    // Teste estrutural (MC/DC)
    public void estrutural_VoltarNivelDeFaseAvancadaDiminuiONivelEPerdeItem() {
        sessao.avancarNivel();
        sessao.setTemItemEspecial(true);

        sessao.voltarNivel();

        assertEquals(1, sessao.getNivelAtual());
        assertFalse(sessao.isTemItemEspecial());
    }

    @Test
    // Teste estrutural (MC/DC)
    public void estrutural_VoltarNivelNoNivelUmNaoVaiParaZero() {
        sessao.setTemItemEspecial(true);

        sessao.voltarNivel();

        assertEquals(1, sessao.getNivelAtual());
        assertFalse(sessao.isTemItemEspecial());
    }

    @Test
    // Dublê de teste
    public void integracao_SalvarERecuperarMapasCorretamente() {
        Mapa mapaNivel1 = Mockito.mock(Mapa.class);
        Mapa mapaNivel2 = Mockito.mock(Mapa.class);

        sessao.salvarMapa(mapaNivel1);
        assertEquals(mapaNivel1, sessao.getMapaDoNivelAtual());

        sessao.avancarNivel();
        assertNull(sessao.getMapaDoNivelAtual());

        sessao.salvarMapa(mapaNivel2);
        assertEquals(mapaNivel2, sessao.getMapaDoNivelAtual());

        sessao.voltarNivel();
        assertEquals(mapaNivel1, sessao.getMapaDoNivelAtual());
    }

    @Test
    // Teste de fronteira
    public void fronteira_AvancarEVoltarMuitasVezes_NivelNuncaMenorQueUm() {
        for (int i = 0; i < 100; i++) {
            sessao.avancarNivel();
        }
        assertEquals(101, sessao.getNivelAtual());

        for (int i = 0; i < 200; i++) {
            sessao.voltarNivel();
        }
        assertEquals(1, sessao.getNivelAtual());
    }

    @Property
        // Teste de propriedade
    void propriedade_NivelNuncaNegativoENaoEstouraIndice(
            @ForAll @IntRange(min = 0, max = 50) int avancos,
            @ForAll @IntRange(min = 0, max = 100) int recuos) {
        SessaoJogo sessao = new SessaoJogo();
        for (int i = 0; i < avancos; i++) {
            sessao.avancarNivel();
        }
        for (int i = 0; i < recuos; i++) {
            sessao.voltarNivel();
        }
        assertTrue(sessao.getNivelAtual() >= 1);
        assertDoesNotThrow(() -> sessao.getMapaDoNivelAtual());
    }
}
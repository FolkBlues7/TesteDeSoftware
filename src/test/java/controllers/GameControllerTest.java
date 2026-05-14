package controllers;

import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameControllerTest {

    private GameController controller;
    private Usuario usuarioMock;
    private SessaoJogo sessaoMock;
    private Mapa mapaMock;
    private Runnable voltarMock;

    @BeforeEach
    void setup() {
        usuarioMock = spy(new Usuario("jogador", "123", false));
        sessaoMock = mock(SessaoJogo.class);
        mapaMock = mock(Mapa.class);
        voltarMock = mock(Runnable.class);

        controller = new GameController(usuarioMock, sessaoMock, mapaMock, voltarMock);
    }

    @Test
    // Teste de domínio
    void construtorComUsuarioSessaoEMapaIniciaModoTeste() {
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
        assertTrue(controller.modoTeste);
    }

    @Test
    // Teste de domínio
    void construtorSemSessaoEMapaIniciaModoNormal() {
        GameController ctrl = new GameController(new Usuario("u", "p", false), () -> {});
        assertFalse(ctrl.modoTeste);
    }

    @Test
    // Teste de domínio
    void carregarNivelResetaCoordenadas() {
        controller.xAtual = 10;
        controller.yAtual = 14;
        controller.carregarNivel();
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Dublê de teste
    void carregarNivelEmModoNormalNotificaListener() {
        GameController ctrl = new GameController(usuarioMock, sessaoMock, mapaMock, voltarMock);
        ctrl.modoTeste = false;

        when(sessaoMock.getMapaDoNivelAtual()).thenReturn(mapaMock);
        when(mapaMock.getTrajeto()).thenReturn(new ArrayList<>(List.of(new Ponto(0, 0))));

        GameController.GameListener listener = mock(GameController.GameListener.class);
        ctrl.setListener(listener);

        ctrl.carregarNivel();

        verify(listener).render();
        verify(listener).atualizarHUD();
    }

    @Test
    // Dublê de teste + Teste estrutural (MC/DC)
    void carregarNivelComMapaNuloCriaNovoMapaENotifica() {
        GameController ctrl = new GameController(usuarioMock, sessaoMock, null, voltarMock);
        ctrl.modoTeste = false;

        when(sessaoMock.getMapaDoNivelAtual()).thenReturn(null); // mapa nulo
        when(sessaoMock.getNivelAtual()).thenReturn(1);

        GameController.GameListener listener = mock(GameController.GameListener.class);
        ctrl.setListener(listener);

        ctrl.carregarNivel();

        verify(sessaoMock).salvarMapa(any(Mapa.class)); // novo mapa salvo
        verify(listener).render();
        verify(listener).atualizarHUD();
    }

    @Test
    // Teste estrutural (MC/DC) – listener == null
    void carregarNivelSemListenerNaoLancaExcecao() {
        GameController ctrl = new GameController(usuarioMock, sessaoMock, mapaMock, voltarMock);
        ctrl.modoTeste = false;

        when(sessaoMock.getMapaDoNivelAtual()).thenReturn(mapaMock);
        when(mapaMock.getTrajeto()).thenReturn(new ArrayList<>(List.of(new Ponto(0, 0))));

        // não setamos listener – deve executar sem erro
        assertDoesNotThrow(() -> ctrl.carregarNivel());
    }

    @Test
    // Teste de domínio
    void getOnVoltarMenuRetornaRunnableInjetado() {
        assertSame(voltarMock, controller.getOnVoltarMenu());
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_MovimentoParaPosicaoBloqueadaNaoAlteraEstado() {
        when(mapaMock.podeMover(1, 0)).thenReturn(false);
        controller.aplicarRegrasDeMovimento(1, 0);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_AlcapaoSemItemVoltaNivel() {
        when(mapaMock.podeMover(2, 2)).thenReturn(true);
        when(mapaMock.isAlcapao(2, 2)).thenReturn(true);
        when(sessaoMock.isTemItemEspecial()).thenReturn(false);

        controller.aplicarRegrasDeMovimento(2, 2);

        verify(sessaoMock).voltarNivel();
        verify(sessaoMock, never()).avancarNivel();
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_AlcapaoComItemAvancaNivel() {
        when(mapaMock.podeMover(2, 2)).thenReturn(true);
        when(mapaMock.isAlcapao(2, 2)).thenReturn(true);
        when(sessaoMock.isTemItemEspecial()).thenReturn(true);

        controller.aplicarRegrasDeMovimento(2, 2);

        verify(sessaoMock).avancarNivel();
        verify(sessaoMock, never()).voltarNivel();
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_MovimentoComMoedaAdicionaPontos() {
        Ponto destino = new Ponto(1, 0);
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        when(mapaMock.getMoedas()).thenReturn(List.of(destino));
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);

        verify(mapaMock).coletarMoeda(destino);
        verify(usuarioMock).adicionarPontos(10);
        assertEquals(1, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_MovimentoComCristalAtivaItemEspecial() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        when(mapaMock.getMoedas()).thenReturn(new ArrayList<>());
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(true);

        controller.aplicarRegrasDeMovimento(1, 0);

        verify(sessaoMock).setTemItemEspecial(true);
        verify(mapaMock).coletarItemEspecial();
        assertEquals(1, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste de domínio
    void movimentoValidoAtualizaCoordenadasERegistraMovimento() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        when(mapaMock.getMoedas()).thenReturn(new ArrayList<>());
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);
        verify(mapaMock).adicionarMovimento(1, 0);
        assertEquals(1, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste de fronteira
    void movimentoForaDosLimitesNaoAlteraPosicao() {
        when(mapaMock.podeMover(-1, 0)).thenReturn(false);
        controller.aplicarRegrasDeMovimento(-1, 0);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Dublê de teste
    void movimentoValidoEmModoNormalNotificaListener() {
        GameController ctrl = new GameController(usuarioMock, sessaoMock, mapaMock, voltarMock);
        ctrl.modoTeste = false;

        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        when(mapaMock.getMoedas()).thenReturn(new ArrayList<>());
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(false);

        GameController.GameListener listener = mock(GameController.GameListener.class);
        ctrl.setListener(listener);

        ctrl.aplicarRegrasDeMovimento(1, 0);

        verify(listener).render();
        verify(listener).atualizarHUD();
    }

    @Property
    // Teste de propriedade
    void movimentoInvalidoNuncaAlteraPosicao(
            @ForAll @IntRange(min = -100, max = 100) int x,
            @ForAll @IntRange(min = -100, max = 100) int y) {
        Mapa mapa = mock(Mapa.class);
        when(mapa.podeMover(anyInt(), anyInt())).thenReturn(false);
        GameController ctrl = new GameController(
                spy(new Usuario("teste", "123", false)),
                mock(SessaoJogo.class),
                mapa,
                mock(Runnable.class)
        );

        int xAntes = ctrl.xAtual;
        int yAntes = ctrl.yAtual;
        ctrl.aplicarRegrasDeMovimento(x, y);

        assertEquals(xAntes, ctrl.xAtual);
        assertEquals(yAntes, ctrl.yAtual);
    }
}
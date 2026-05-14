package controllers;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import views.GameView;
import views.HUDView;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameControllerTest {

    private GameController controller;
    private Usuario usuarioMock;
    private SessaoJogo sessaoMock;
    private Mapa mapaMock;
    private Runnable voltarMenuMock;

    @BeforeAll
    public static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
        }
    }

    @BeforeEach
    public void setup() {
        usuarioMock = spy(new Usuario("teste", "123", false));
        sessaoMock = mock(SessaoJogo.class);
        mapaMock = mock(Mapa.class);
        voltarMenuMock = mock(Runnable.class);

        controller = new GameController(usuarioMock, sessaoMock, mapaMock, voltarMenuMock);
    }

    @Test
    // Teste de domínio
    public void construtorComStageEIniciarJogo() {
        Stage stageMock = mock(Stage.class);
        Usuario usuario = new Usuario("jogador", "123", false);
        Runnable voltarMock = mock(Runnable.class);

        GameController ctrl = new GameController(stageMock, usuario, voltarMock);
        ctrl.modoTeste = true;
        ctrl.iniciarJogo();

        assertEquals(0, ctrl.xAtual);
        assertEquals(0, ctrl.yAtual);
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_teclaUp_chamaRegrasComNovasCoordenadas() {
        GameController spyController = spy(controller);
        KeyEvent eventUp = mock(KeyEvent.class);
        when(eventUp.getCode()).thenReturn(KeyCode.UP);

        spyController.tratarTeclado(eventUp);
        verify(spyController).aplicarRegrasDeMovimento(0, -1);
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_teclaDown_chamaRegrasComNovasCoordenadas() {
        GameController spyController = spy(controller);
        KeyEvent eventDown = mock(KeyEvent.class);
        when(eventDown.getCode()).thenReturn(KeyCode.DOWN);

        spyController.tratarTeclado(eventDown);
        verify(spyController).aplicarRegrasDeMovimento(0, 1);
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_teclaLeft_chamaRegrasComNovasCoordenadas() {
        GameController spyController = spy(controller);
        KeyEvent eventLeft = mock(KeyEvent.class);
        when(eventLeft.getCode()).thenReturn(KeyCode.LEFT);

        spyController.tratarTeclado(eventLeft);
        verify(spyController).aplicarRegrasDeMovimento(-1, 0);
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_teclaRight_chamaRegrasComNovasCoordenadas() {
        GameController spyController = spy(controller);
        KeyEvent eventRight = mock(KeyEvent.class);
        when(eventRight.getCode()).thenReturn(KeyCode.RIGHT);

        spyController.tratarTeclado(eventRight);
        verify(spyController).aplicarRegrasDeMovimento(1, 0);
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_teclaR_chamaCarregarNivelENaoChamaMovimento() {
        GameController spyController = spy(controller);
        KeyEvent eventR = mock(KeyEvent.class);
        when(eventR.getCode()).thenReturn(KeyCode.R);

        spyController.tratarTeclado(eventR);
        verify(spyController).carregarNivel();
        verify(spyController, never()).aplicarRegrasDeMovimento(anyInt(), anyInt());
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_teclaEscape_chamaOnVoltarMenu() {
        KeyEvent eventEsc = mock(KeyEvent.class);
        when(eventEsc.getCode()).thenReturn(KeyCode.ESCAPE);

        controller.tratarTeclado(eventEsc);
        verify(voltarMenuMock).run();
    }

    @Test
    // Dublê de teste
    public void tratarTeclado_outraTecla_chamaRegrasComPosicaoAtual() {
        GameController spyController = spy(controller);
        KeyEvent eventF = mock(KeyEvent.class);
        when(eventF.getCode()).thenReturn(KeyCode.F);

        spyController.tratarTeclado(eventF);
        verify(spyController).aplicarRegrasDeMovimento(0, 0);
        verify(spyController, never()).carregarNivel();
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_PodeMoverFalso_NadaAcontece() {
        when(mapaMock.podeMover(1, 0)).thenReturn(false);
        controller.aplicarRegrasDeMovimento(1, 0);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_CaiuNoAlcapao_SemItem_VoltaNivel() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(true);
        when(sessaoMock.isTemItemEspecial()).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);
        verify(sessaoMock).voltarNivel();
        verify(sessaoMock, never()).avancarNivel();
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_CaiuNoAlcapao_ComItem_AvancaNivel() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(true);
        when(sessaoMock.isTemItemEspecial()).thenReturn(true);

        controller.aplicarRegrasDeMovimento(1, 0);
        verify(sessaoMock).avancarNivel();
        verify(sessaoMock, never()).voltarNivel();
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_PodeMover_ComMoeda_SemCristal() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        List<Ponto> moedas = List.of(new Ponto(1, 0));
        when(mapaMock.getMoedas()).thenReturn(moedas);
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);
        verify(mapaMock).coletarMoeda(any(Ponto.class));
        verify(usuarioMock).adicionarPontos(10);
        assertEquals(1, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_PodeMover_SemMoeda_ComCristal() {
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
    public void aplicarRegrasDeMovimento_movimentoSimplesValido_atualizaPosicao() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        when(mapaMock.getMoedas()).thenReturn(new ArrayList<>());
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);
        assertEquals(1, controller.xAtual);
        assertEquals(0, controller.yAtual);
        verify(mapaMock).adicionarMovimento(1, 0);
        verify(usuarioMock, never()).adicionarPontos(anyInt());
        verify(sessaoMock, never()).setTemItemEspecial(anyBoolean());
    }

    @Test
    // Teste de domínio
    public void aplicarRegrasDeMovimento_celulaComMoedaEItemEspecial_coletaAmbos() {
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);
        List<Ponto> moedas = List.of(new Ponto(1, 0));
        when(mapaMock.getMoedas()).thenReturn(moedas);
        when(mapaMock.isItemEspecial(1, 0)).thenReturn(true);

        controller.aplicarRegrasDeMovimento(1, 0);
        verify(mapaMock).coletarMoeda(any(Ponto.class));
        verify(usuarioMock).adicionarPontos(10);
        verify(sessaoMock).setTemItemEspecial(true);
        verify(mapaMock).coletarItemEspecial();
        assertEquals(1, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Dublê de teste
    public void aplicarRegrasDeMovimento_modoNaoTeste_atualizaUI() {
        Stage stageMock = mock(Stage.class);
        GameView gameViewMock = mock(GameView.class);
        HUDView hudViewMock = mock(HUDView.class);
        Usuario usuario = new Usuario("teste", "123", false);
        SessaoJogo sessao = mock(SessaoJogo.class);
        Mapa mapa = mock(Mapa.class);
        Runnable voltarMock = mock(Runnable.class);

        GameController ctrl = new GameController(stageMock, usuario, voltarMock);
        ctrl.sessao = sessao;
        ctrl.mapa = mapa;
        ctrl.setGameView(gameViewMock);
        ctrl.setHud(hudViewMock);
        ctrl.modoTeste = false;
        ctrl.xAtual = 0;
        ctrl.yAtual = 0;

        when(mapa.podeMover(1, 0)).thenReturn(true);
        when(mapa.isAlcapao(1, 0)).thenReturn(false);
        when(mapa.getMoedas()).thenReturn(new ArrayList<>());
        when(mapa.isItemEspecial(1, 0)).thenReturn(false);

        ctrl.aplicarRegrasDeMovimento(1, 0);
        verify(gameViewMock).render();
        verify(hudViewMock).atualizar(usuario, sessao);
    }

    @Test
    // Teste de domínio
    public void dominio_ResetDeCoordenadasAoCarregarNivel() {
        controller.xAtual = 10;
        controller.yAtual = 14;
        controller.carregarNivel();
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Test
    // Teste de fronteira
    public void fronteira_MovimentoParaForaDoMapa_NaoAlteraPosicao() {
        when(mapaMock.podeMover(-1, 0)).thenReturn(false);
        when(mapaMock.podeMover(0, -1)).thenReturn(false);
        when(mapaMock.podeMover(100, 0)).thenReturn(false);
        when(mapaMock.podeMover(0, 100)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(-1, 0);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);

        controller.aplicarRegrasDeMovimento(0, -1);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);

        controller.aplicarRegrasDeMovimento(100, 0);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);

        controller.aplicarRegrasDeMovimento(0, 100);
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    @Property
    // Teste de propriedade
    void propriedade_MovimentoInvalidoNuncaAlteraPosicao(
            @ForAll @IntRange(min = -100, max = 100) int x,
            @ForAll @IntRange(min = -100, max = 100) int y) {
        Usuario usuario = spy(new Usuario("teste", "123", false));
        SessaoJogo sessao = mock(SessaoJogo.class);
        Mapa mapa = mock(Mapa.class);
        when(mapa.podeMover(anyInt(), anyInt())).thenReturn(false);
        GameController controller = new GameController(usuario, sessao, mapa, mock(Runnable.class));

        int xAntes = controller.xAtual;
        int yAntes = controller.yAtual;
        controller.aplicarRegrasDeMovimento(x, y);

        assertEquals(xAntes, controller.xAtual);
        assertEquals(yAntes, controller.yAtual);
    }
}
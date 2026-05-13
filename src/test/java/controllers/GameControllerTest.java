package controllers;

import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

    @BeforeEach
    public void setup() {
        // Criando os Dublês de Teste (Mocks)
        usuarioMock = spy(new Usuario("teste", "123", false));
        sessaoMock = mock(SessaoJogo.class);
        mapaMock = mock(Mapa.class);
        voltarMenuMock = mock(Runnable.class);

        // Instanciando o controller no modo de teste (sem JavaFX)
        controller = new GameController(usuarioMock, sessaoMock, mapaMock, voltarMenuMock);
    }

    // ==========================================
    // 1. TESTES ESTRUTURAIS (100% MC/DC)
    // ==========================================

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC) e Dublê de Teste (Mock)
     * O QUE FAZ: Verifica a ramificação de falha da condição principal de movimento.
     * Simula o cenário onde o jogador tenta andar para uma posição inválida (ex: parede).
     * Garante que as coordenadas do jogador (xAtual, yAtual) não sejam alteradas.
     */
    @Test
    public void mcdc_PodeMoverFalso_NadaAcontece() {
        // Condição: mapa.podeMover(x,y) retorna FALSE
        when(mapaMock.podeMover(1, 0)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);

        // x e y devem continuar 0 (não andou)
        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC) e Dublê de Teste (Mock)
     * O QUE FAZ: Testa a primeira ramificação da lógica do Alçapão.
     * Simula o jogador caindo em um alçapão SEM possuir o item especial.
     * Garante que o jogador seja penalizado e o método voltarNivel() seja acionado.
     */
    @Test
    public void mcdc_CaiuNoAlcapao_SemItem_VoltaNivel() {
        // Condições: podeMover = TRUE, isAlcapao = TRUE, isTemItemEspecial = FALSE
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(true);
        when(sessaoMock.isTemItemEspecial()).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);

        // Comportamento esperado
        verify(sessaoMock, times(1)).voltarNivel();
        verify(sessaoMock, never()).avancarNivel();
    }

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC) e Dublê de Teste (Mock)
     * O QUE FAZ: Testa a segunda ramificação da lógica do Alçapão.
     * Simula o jogador caindo no alçapão COM o item especial em mãos.
     * Garante que o jogador utilize o item e o método avancarNivel() seja acionado.
     */
    @Test
    public void mcdc_CaiuNoAlcapao_ComItem_AvancaNivel() {
        // Condições: podeMover = TRUE, isAlcapao = TRUE, isTemItemEspecial = TRUE
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(true);
        when(sessaoMock.isTemItemEspecial()).thenReturn(true);

        controller.aplicarRegrasDeMovimento(1, 0);

        // Comportamento esperado
        verify(sessaoMock, times(1)).avancarNivel();
    }

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC) e Dublê de Teste (Mock)
     * O QUE FAZ: Valida a ramificação de coleta de itens comuns.
     * Simula um movimento válido para uma coordenada que contém uma moeda.
     * Garante que a moeda seja retirada do mapa e que 10 pontos sejam creditados ao usuário.
     */
    @Test
    public void mcdc_PodeMover_ComMoeda_SemCristal() {
        // Condições: podeMover = TRUE, isAlcapao = FALSE, temMoeda = TRUE, isItemEspecial = FALSE
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);

        // CORREÇÃO: Usando List em vez de Set
        List<Ponto> moedas = new ArrayList<>();
        moedas.add(new Ponto(1, 0));
        when(mapaMock.getMoedas()).thenReturn(moedas);

        when(mapaMock.isItemEspecial(1, 0)).thenReturn(false);

        controller.aplicarRegrasDeMovimento(1, 0);

        // Verifica se coletou e adicionou 10 pontos
        verify(mapaMock, times(1)).coletarMoeda(any(Ponto.class));
        verify(usuarioMock, times(1)).adicionarPontos(10);
        assertEquals(1, controller.xAtual); // Andou
    }

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC) e Dublê de Teste (Mock)
     * O QUE FAZ: Valida a ramificação de coleta do item especial.
     * Simula um movimento para uma casa que contém o cristal (e nenhuma moeda).
     * Garante que o status de "TemItemEspecial" na sessão seja atualizado para verdadeiro.
     */
    @Test
    public void mcdc_PodeMover_SemMoeda_ComCristal() {
        // Condições: podeMover = TRUE, isAlcapao = FALSE, temMoeda = FALSE, isItemEspecial = TRUE
        when(mapaMock.podeMover(1, 0)).thenReturn(true);
        when(mapaMock.isAlcapao(1, 0)).thenReturn(false);

        // CORREÇÃO: Passando uma lista vazia, pois o cenário é SEM moeda
        when(mapaMock.getMoedas()).thenReturn(new ArrayList<>());

        when(mapaMock.isItemEspecial(1, 0)).thenReturn(true);

        controller.aplicarRegrasDeMovimento(1, 0);

        // Verifica se pegou o cristal
        verify(sessaoMock, times(1)).setTemItemEspecial(true);
        verify(mapaMock, times(1)).coletarItemEspecial();
    }

    // ==========================================
    // 2. TESTES DE DOMÍNIO E FRONTEIRA
    // ==========================================

    /**
     * TIPO: Teste de Domínio e Fronteira
     * O QUE FAZ: Testa a transição de estado/fronteira temporal do jogador.
     * Força o jogador a estar em uma posição distante no mapa e chama o método carregarNivel().
     * Garante a regra de domínio de que todo novo nível DEVE reiniciar o jogador na coordenada (0, 0).
     */
    @Test
    public void dominio_ResetDeCoordenadasAoCarregarNivel() {
        // Simula que o personagem estava longe
        controller.xAtual = 10;
        controller.yAtual = 14;

        // Fronteira de estado: carregar o nível OBRIGA x e y a virarem 0
        controller.carregarNivel();

        assertEquals(0, controller.xAtual);
        assertEquals(0, controller.yAtual);
    }

    // ==========================================
    // 3. TESTES DE PROPRIEDADE (JQWIK)
    // ==========================================

    /**
     * TIPO: Teste de Propriedade (Property-Based Testing)
     * O QUE FAZ: Avalia uma regra (propriedade) universal do jogo contra milhares de cenários.
     * O Jqwik gera coordenadas X e Y totalmente aleatórias (incluindo números negativos e fora dos limites).
     * A propriedade garantida aqui é: independentemente do "lixo" ou input inválido passado
     * para a regra de movimento, as coordenadas do jogador NUNCA podem ficar negativas.
     */
    @Property
    public boolean propriedade_NuncaDeveEstarEmPosicaoInvalida(
            @ForAll @IntRange(min = -10, max = 20) int randomX,
            @ForAll @IntRange(min = -10, max = 20) int randomY) {

        // Dublê permitindo apenas movimentos válidos na regra do mapa (ex: 0 a 14)
        Mapa mapaReal = new Mapa(15, 15);
        GameController ctrlPropriedade = new GameController(
                new Usuario("teste", "123", false),
                new SessaoJogo(),
                mapaReal,
                () -> {}
        );
        ctrlPropriedade.modoTeste = true;

        // Tenta aplicar o movimento gerado aleatoriamente pelo Jqwik
        ctrlPropriedade.aplicarRegrasDeMovimento(randomX, randomY);

        // Propriedade universal: as coordenadas atuais do jogador NUNCA podem ser negativas
        // se a lógica do mapa estiver funcionando.
        return ctrlPropriedade.xAtual >= 0 && ctrlPropriedade.yAtual >= 0;
    }
}
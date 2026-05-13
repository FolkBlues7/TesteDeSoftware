package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class SessaoJogoTest {

    private SessaoJogo sessao;

    @BeforeEach
    public void setup() {
        // Criamos uma nova sessão zerada antes de cada teste
        sessao = new SessaoJogo();
    }

    // ==========================================
    // 1. TESTES DE ESTADO INICIAL
    // ==========================================

    /**
     * TIPO: Teste de Estado
     * O QUE FAZ: Verifica se o construtor inicializa a sessão corretamente no Nível 1,
     * sem itens e com a lista de mapas vazia.
     */
    @Test
    public void dominio_EstadoInicialDaSessao() {
        assertEquals(1, sessao.getNivelAtual(), "O jogo sempre deve começar no nível 1.");
        assertFalse(sessao.isTemItemEspecial(), "O jogador não deve começar com o item especial.");
        assertNull(sessao.getMapaDoNivelAtual(), "Como nenhum mapa foi salvo ainda, deve retornar nulo.");
    }

    /**
     * TIPO: Teste de Mutação (Setter/Getter)
     * O QUE FAZ: Garante que o jogador consegue pegar o item especial durante o nível.
     */
    @Test
    public void dominio_PodePegarItemEspecial() {
        sessao.setTemItemEspecial(true);
        assertTrue(sessao.isTemItemEspecial(), "Deveria registrar que o jogador pegou o item.");
    }

    // ==========================================
    // 2. TESTES ESTRUTURAIS (Transições de Nível)
    // ==========================================

    /**
     * TIPO: Teste Estrutural e de Estado
     * O QUE FAZ: Verifica o comportamento de avançar de fase.
     * Garante que o nível sobe em +1 e o item especial é consumido (false).
     */
    @Test
    public void estrutural_AvancarNivelConsomeItemEAvanca() {
        sessao.setTemItemEspecial(true); // Finge que o jogador pegou o item
        sessao.avancarNivel(); // Usa o item para passar de fase

        assertEquals(2, sessao.getNivelAtual(), "O nível deveria ter subido para 2.");
        assertFalse(sessao.isTemItemEspecial(), "O item especial deveria ser consumido ao passar de fase.");
    }

    /**
     * TIPO: Teste Estrutural (Limite / Boundary)
     * O QUE FAZ: Testa o caminho "IF > 1" do voltarNivel.
     * Se o jogador está no nível 2 ou mais e cai no alçapão, ele perde 1 nível e o item.
     */
    @Test
    public void estrutural_VoltarNivelDeFaseAvancadaDiminuiONivelEPerdeItem() {
        // Preparando o cenário: Colocando o jogador no Nível 2 com um item
        sessao.avancarNivel();
        sessao.setTemItemEspecial(true);

        // Ação: Caiu no alçapão
        sessao.voltarNivel();

        assertEquals(1, sessao.getNivelAtual(), "Deveria ter voltado para o nível 1.");
        assertFalse(sessao.isTemItemEspecial(), "Deveria perder o item ao cair no alçapão.");
    }

    /**
     * TIPO: Teste Estrutural (Limite / Boundary)
     * O QUE FAZ: Testa o caminho do voltarNivel quando o IF falha.
     * Se o jogador já está no Nível 1 e cai no alçapão, ele NÃO pode ir pro nível 0.
     */
    @Test
    public void estrutural_VoltarNivelNoNivelUmNaoVaiParaZero() {
        sessao.setTemItemEspecial(true);

        // Ação: Caiu no alçapão logo na primeira fase
        sessao.voltarNivel();

        assertEquals(1, sessao.getNivelAtual(), "O nível não pode ser menor que 1. Deve continuar no 1.");
        assertFalse(sessao.isTemItemEspecial(), "Ainda assim, deveria perder o item.");
    }

    // ==========================================
    // 3. TESTES DE INTEGRAÇÃO DE OBJETOS (Listas)
    // ==========================================

    /**
     * TIPO: Teste de Integração (Memória da Lista)
     * O QUE FAZ: Simula o fluxo real do jogo salvando mapas conforme o nível avança
     * e garante que o método getMapaDoNivelAtual() busca no índice certo.
     */
    @Test
    public void integracao_SalvarERecuperarMapasCorretamente() {
        // Criação de Mapas Falsos usando Mockito para não depender da classe Mapa real
        Mapa mapaNivel1 = Mockito.mock(Mapa.class);
        Mapa mapaNivel2 = Mockito.mock(Mapa.class);

        // Cenário Nível 1
        sessao.salvarMapa(mapaNivel1);
        assertEquals(mapaNivel1, sessao.getMapaDoNivelAtual(), "Deveria retornar o mapa do Nível 1.");

        // Cenário Nível 2
        sessao.avancarNivel();
        assertNull(sessao.getMapaDoNivelAtual(), "Acabou de avançar. O novo mapa ainda não foi salvo, deveria retornar null.");

        sessao.salvarMapa(mapaNivel2);
        assertEquals(mapaNivel2, sessao.getMapaDoNivelAtual(), "Agora deveria retornar o mapa do Nível 2.");

        // Verificando se a memória do Nível 1 ainda existe (se ele voltar de fase)
        sessao.voltarNivel();
        assertEquals(mapaNivel1, sessao.getMapaDoNivelAtual(), "Ao voltar para o Nível 1, deveria recuperar da memória o mapa antigo.");
    }
}
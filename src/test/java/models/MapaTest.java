package models;

import common.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapaTest {

    // ==========================================
    // 1. TESTES DE FRONTEIRA E LIMITES
    // ==========================================

    @Test
    void podeMover() {
        var linhas = 5;
        var colunas = 5;
        var obstaculos = new boolean[linhas][colunas];
        obstaculos[1][0] = true;
        var ponto = new Ponto(0, 1);
        var mapa = new Mapa(5, 5);

        // Valida movimentos válidos, colisões e limites externos
        mapa.gerarCenarioPredefinido(obstaculos, List.of(ponto));
        assertTrue(mapa.podeMover(0, 1));
        assertFalse(mapa.podeMover(1, 0));
        assertFalse(mapa.podeMover(-1, 0));
        assertFalse(mapa.podeMover(0, -1));
        assertFalse(mapa.podeMover(0, linhas + 1));
        assertFalse(mapa.podeMover(colunas + 1, 0));
    }

    @Test
    void deveRejeitarQuantidadeInvalidaDeMoedas() {
        var mapa = new Mapa(5, 5);
        mapa.setRandom(new RandomGenerator() {
            @Override public int nextInt(int b) { return 0; }
            @Override public double nextDouble() { return 0.5; }
        });

        // Teste de limite inferior, não pode haver números negativos para moedas.
        assertThrows(IllegalArgumentException.class, () -> mapa.gerarCenarioAleatorio(-1));

        // Teste de limite superior, não pode haver mais moedas que espaços!
        assertThrows(IllegalArgumentException.class, () -> mapa.gerarCenarioAleatorio(26),
                "Não deve permitir gerar mais de 3 moedas conforme REQ-01");
    }

    @Test
    void testesDeRobustezExtrema() {
        var mapa = new Mapa(5, 5);
        mapa.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());

        // --- TESTE DE NULL ---
        mapa.setRandom(null);
        assertThrows(NullPointerException.class, () -> {
            mapa.gerarCenarioAleatorio(1);
        }, "Deveria lançar NPE ao tentar gerar cenário sem um RandomGenerator configurado");

        // --- TESTE DE MAX_VALUE (Overflow e Fronteira) ---
        assertFalse(mapa.podeMover(Integer.MAX_VALUE, 0), "Coordenada X máxima deve ser inválida");
        assertFalse(mapa.podeMover(0, Integer.MAX_VALUE), "Coordenada Y máxima deve ser inválida");
        assertFalse(mapa.podeMover(Integer.MAX_VALUE, Integer.MAX_VALUE), "Coordenadas máximas devem ser inválidas");

        // Teste de valores negativos extremos
        assertFalse(mapa.podeMover(Integer.MIN_VALUE, 0), "Valores negativos extremos devem ser inválidos");
    }

    // ==========================================
    // 2. TESTES DE DOMÍNIO E FLUXO
    // ==========================================

    @Test
    void adicionarMovimento() {
        var linhas = 5;
        var colunas = 5;
        var obstaculos = new boolean[linhas][colunas];
        obstaculos[1][0] = true;
        var moeda = new Ponto(0, 2);
        var mapa = new Mapa(colunas, linhas);
        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>(List.of(moeda)));

        // Valida registro de rastro no trajeto
        mapa.adicionarMovimento(1, 0);
        assertEquals(1, mapa.getTrajeto().size());

        mapa.adicionarMovimento(0, 1);
        assertEquals(2, mapa.getTrajeto().size());

        // Valida coleta de moeda e conclusão da fase
        mapa.adicionarMovimento(0, 2);
        assertEquals(1, mapa.getMoedasColetadas());
        assertTrue(mapa.faseConcluida());
    }

    @Test
    void deveGarantirIdempotenciaNaColetaDeMoedas() {
        var mapa = new Mapa(5, 5);
        var moedaPos = new Ponto(1, 1);
        var obstaculos = new boolean[5][5];

        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>(List.of(moedaPos)));

        // Simula o jogador entrando na casa da moeda pela primeira vez
        mapa.adicionarMovimento(1, 1);
        assertEquals(1, mapa.getMoedasColetadas(), "Deveria coletar a moeda na primeira visita");

        // Simula o jogador saindo e voltando para a mesma casa da moeda
        mapa.adicionarMovimento(1, 2); // Saiu
        mapa.adicionarMovimento(1, 1); // Voltou

        assertEquals(1, mapa.getMoedasColetadas(), "Não deve coletar a mesma moeda mais de uma vez (Idempotência)");
    }

    @Test
    void deveRegistrarTrajetoMesmoAoVisitarMesmaCasa() {
        var mapa = new Mapa(5, 5);
        mapa.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());

        // Movimento: Origem -> (1,0) -> Origem
        mapa.adicionarMovimento(1, 0);
        mapa.adicionarMovimento(0, 0);

        assertEquals(3, mapa.getTrajeto().size(), "O trajeto deve crescer a cada movimento realizado pelo jogador");
    }

    @Test
    void naoDeveAdicionarMovimentoSeHouverObstaculo() {
        var linhas = 5;
        var colunas = 5;
        var obstaculos = new boolean[linhas][colunas];
        obstaculos[1][1] = true; // Parede no caminho

        var mapa = new Mapa(colunas, linhas);
        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>());

        if (mapa.podeMover(1, 1)) {
            mapa.adicionarMovimento(1, 1);
        }

        assertTrue(mapa.getTrajeto().size() == 1  , "O trajeto não deve registrar movimentos para células com obstáculos");
    }

    // ==========================================
    // 3. TESTES ESTRUTURAIS E ALGORÍTMICOS
    // ==========================================

    @Test
    void deveValidarAcessibilidadeComBFS() {
        var mapa = new Mapa(3, 3);
        var obstaculos = new boolean[3][3];

        // --- 1. CENÁRIO DE BLOQUEIO (Moeda cercada) ---
        obstaculos[1][2] = true;
        obstaculos[2][1] = true;
        var moedaBloqueada = new Ponto(2, 2);

        mapa.gerarCenarioPredefinido(obstaculos, List.of(moedaBloqueada));

        assertFalse(mapa.verificarAcessibilidade(mapa.getMoedas(), mapa.getObstaculos()),
                "O BFS deveria retornar false para uma moeda cercada por obstáculos");

        // --- 2. CENÁRIO DE SUCESSO (Caminho em "Z") ---
        var obstaculosZ = new boolean[3][3];
        obstaculosZ[0][1] = true;
        obstaculosZ[1][1] = true;

        mapa.gerarCenarioPredefinido(obstaculosZ, List.of(new Ponto(2, 2)));

        assertTrue(mapa.verificarAcessibilidade(mapa.getMoedas(), mapa.getObstaculos()),
                "O BFS deveria retornar true para um caminho tortuoso, mas possível");
    }

    /**
     * TIPO: Teste Estrutural (Mock)
     * O QUE FAZ: Substitui aquele código comentado gigante.
     * Usa o Mockito para forçar resultados específicos na geração aleatória,
     * garantindo que o método gera exatamente o que esperamos sem cair em loop infinito.
     */
    /*@Test
    void gerarCenarioAleatorioComMockito() {
        var mapa = new Mapa(5, 5);
        RandomGenerator mockRandom = Mockito.mock(RandomGenerator.class);

        // 1. Forçamos o double a sempre ser 0.1
        // Isso garante 0% de chance de gerar obstáculos. O mapa nascerá totalmente livre,
        // garantindo que o seu teste de BFS aprove o mapa de primeira e não entre em loop.
        Mockito.when(mockRandom.nextDouble()).thenReturn(0.1);

        // 2. Usamos um Random real para as coordenadas inteiras!
        // O "thenAnswer" permite que cada vez que o nextInt do Mock for chamado,
        // ele delegue o trabalho para um gerador real, evitando a repetição infinita.
        java.util.Random geradorReal = new java.util.Random();
        Mockito.when(mockRandom.nextInt(Mockito.anyInt())).thenAnswer(invocation -> {
            int limite = invocation.getArgument(0);
            return geradorReal.nextInt(limite);
        });

        mapa.setRandom(mockRandom);

        // Ação: Pede para gerar 3 moedas
        mapa.gerarCenarioAleatorio(3);

        // Asserções
        assertEquals(3, mapa.getMoedas().size(), "Deve gerar exatamente 3 moedas espalhadas pelo mapa livre.");
        assertEquals(5, mapa.getColunas());
        assertEquals(5, mapa.getLinhas());
        assertFalse(mapa.getTrajeto().isEmpty(), "O trajeto inicial (0,0) deve ser registrado no momento da geração.");
    }*/
}
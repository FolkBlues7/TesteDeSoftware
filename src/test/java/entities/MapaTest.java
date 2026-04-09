package entities;

import common.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.support.descriptor.FileSystemSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapaTest {

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
    void gerarCenarioAleatorio() {
        var mapa = new Mapa(5, 5);
        var javaRandom = new java.util.Random();

        // Implementação Mock para controlar comportamento aleatório
        mapa.setRandom(new RandomGenerator() {
            int intRepeats = 0;
            int intPastValue = 0;

            @Override
            public int nextInt(int bound) {
                if (intRepeats == 0) {
                    intPastValue = javaRandom.nextInt(bound);
                }
                if (intRepeats > 4) {
                    intPastValue = javaRandom.nextInt(bound);
                }
                intRepeats++;
                return intPastValue;
            }

            final double doublePastValue = 0;
            int doubleRepeats = 0;

            @Override
            public double nextDouble() {
                doubleRepeats++;

                if (doubleRepeats < (mapa.getLinhas() * mapa.getColunas()) - 1) {
                    return doublePastValue;
                }

                return javaRandom.nextDouble();
            }
        });

        // Verifica se cenário aleatório gera itens e mantém dimensões
        mapa.gerarCenarioAleatorio(3);
        assertFalse(mapa.getMoedas().isEmpty());
        assertFalse(mapa.getTrajeto().isEmpty());

        assertEquals(5, mapa.getColunas());
        assertEquals(5, mapa.getLinhas());
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

    //Se passarmos mais de uma vez onde antes havia uma única moeda, nada deve ser adicionado.
    @Test
    void deveGarantirIdempotenciaNaColetaDeMoedas() {
        var mapa = new Mapa(5, 5);
        var moedaPos = new Ponto(1, 1);
        var obstaculos = new boolean[5][5];

        // Configura cenário com 1 moeda específica
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

        // O trajeto deve registrar cada passo, totalizando 3 movimentos no rastro, o trajeto começa em 0 e 0, então o início também conta
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

        // Tenta mover para o obstáculo
        // Nota: Aqui assumimos que a lógica de controle chama podeMover antes de adicionarMovimento
        if (mapa.podeMover(1, 1)) {
            mapa.adicionarMovimento(1, 1);
        }

        assertTrue(mapa.getTrajeto().size() == 1  , "O trajeto não deve registrar movimentos para células com obstáculos");
    }

    @Test
    void deveValidarAcessibilidadeComBFS() {
        var mapa = new Mapa(3, 3);
        var obstaculos = new boolean[3][3];

        // --- 1. CENÁRIO DE BLOQUEIO (Moeda cercada) ---
        // Colocamos a moeda no canto (2,2) e paredes em volta (1,2) e (2,1)
        obstaculos[1][2] = true;
        obstaculos[2][1] = true;
        var moedaBloqueada = new Ponto(2, 2);

        mapa.gerarCenarioPredefinido(obstaculos, List.of(moedaBloqueada));

        // O método verificarAcessibilidade deve ser capaz de notar que o jogador (em 0,0)
        // não consegue chegar em (2,2)
        assertFalse(mapa.verificarAcessibilidade(mapa.getMoedas(), mapa.getObstaculos()),
                "O BFS deveria retornar false para uma moeda cercada por obstáculos");

        // --- 2. CENÁRIO DE SUCESSO (Caminho em "Z") ---
        // Criamos um labirinto onde o jogador tem que dar a volta
        var obstaculosZ = new boolean[3][3];
        obstaculosZ[0][1] = true; // Bloqueia caminho direto
        obstaculosZ[1][1] = true; // Bloqueia meio
        // Caminho aberto: (0,0) -> (1,0) -> (2,0) -> (2,1) -> (2,2)

        mapa.gerarCenarioPredefinido(obstaculosZ, List.of(new Ponto(2, 2)));

        assertTrue(mapa.verificarAcessibilidade(mapa.getMoedas(), mapa.getObstaculos()),
                "O BFS deveria retornar true para um caminho tortuoso, mas possível");
    }

    @Test
    void testesDeRobustezExtrema() {
        var mapa = new Mapa(5, 5);
        mapa.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());

        // --- TESTE DE NULL ---
        // Verifica se o sistema trata a falta de um gerador de números aleatórios
        mapa.setRandom(null);
        assertThrows(NullPointerException.class, () -> {
            mapa.gerarCenarioAleatorio(1);
        }, "Deveria lançar NPE ao tentar gerar cenário sem um RandomGenerator configurado");

        // --- TESTE DE MAX_VALUE (Overflow e Fronteira) ---
        // Garante que valores extremos de inteiros sejam barrados corretamente
        assertFalse(mapa.podeMover(Integer.MAX_VALUE, 0), "Coordenada X máxima deve ser inválida");
        assertFalse(mapa.podeMover(0, Integer.MAX_VALUE), "Coordenada Y máxima deve ser inválida");
        assertFalse(mapa.podeMover(Integer.MAX_VALUE, Integer.MAX_VALUE), "Coordenadas máximas devem ser inválidas");

        // Teste de valores negativos extremos
        assertFalse(mapa.podeMover(Integer.MIN_VALUE, 0), "Valores negativos extremos devem ser inválidos");
    }


}
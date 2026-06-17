package models;

import common.RandomGenerator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MapaTest {

    @Test
    // Teste de fronteira
    void podeMover() {
        var linhas = 5;
        var colunas = 5;
        var obstaculos = new boolean[linhas][colunas];
        obstaculos[1][0] = true;
        var ponto = new Ponto(0, 1);
        var mapa = new Mapa(5, 5);

        mapa.gerarCenarioPredefinido(obstaculos, List.of(ponto));
        assertTrue(mapa.podeMover(0, 1));
        assertFalse(mapa.podeMover(1, 0));
        assertFalse(mapa.podeMover(-1, 0));
        assertFalse(mapa.podeMover(0, -1));
        assertFalse(mapa.podeMover(0, linhas + 1));
        assertFalse(mapa.podeMover(colunas + 1, 0));
    }

    @Test
    // Teste de fronteira
    void testesDeRobustezExtrema() {
        var mapa = new Mapa(5, 5);
        mapa.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());

        mapa.setRandom(null);
        assertThrows(NullPointerException.class, () -> mapa.gerarCenarioAleatorio(1));

        assertFalse(mapa.podeMover(Integer.MAX_VALUE, 0));
        assertFalse(mapa.podeMover(0, Integer.MAX_VALUE));
        assertFalse(mapa.podeMover(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertFalse(mapa.podeMover(Integer.MIN_VALUE, 0));
    }

    @Test
    // Teste de domínio
    void adicionarMovimento() {
        var linhas = 5;
        var colunas = 5;
        var obstaculos = new boolean[linhas][colunas];
        obstaculos[1][0] = true;
        var moeda = new Ponto(0, 2);
        var mapa = new Mapa(colunas, linhas);
        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>(List.of(moeda)));

        mapa.adicionarMovimento(1, 0);
        assertEquals(1, mapa.getTrajeto().size());

        mapa.adicionarMovimento(0, 1);
        assertEquals(2, mapa.getTrajeto().size());

        mapa.adicionarMovimento(0, 2);
        assertEquals(1, mapa.getMoedasColetadas());
        assertTrue(mapa.faseConcluida());
    }

    @Test
    // Teste de domínio
    void deveGarantirIdempotenciaNaColetaDeMoedas() {
        var mapa = new Mapa(5, 5);
        var moedaPos = new Ponto(1, 1);
        var obstaculos = new boolean[5][5];

        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>(List.of(moedaPos)));

        mapa.adicionarMovimento(1, 1);
        assertEquals(1, mapa.getMoedasColetadas());

        mapa.adicionarMovimento(1, 2);
        mapa.adicionarMovimento(1, 1);

        assertEquals(1, mapa.getMoedasColetadas());
    }

    @Test
    // Teste de domínio
    void deveRegistrarTrajetoMesmoAoVisitarMesmaCasa() {
        var mapa = new Mapa(5, 5);
        mapa.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());

        mapa.adicionarMovimento(1, 0);
        mapa.adicionarMovimento(0, 0);

        assertEquals(3, mapa.getTrajeto().size());
    }

    @Test
    // Teste de domínio
    void naoDeveAdicionarMovimentoSeHouverObstaculo() {
        var linhas = 5;
        var colunas = 5;
        var obstaculos = new boolean[linhas][colunas];
        obstaculos[1][1] = true;

        var mapa = new Mapa(colunas, linhas);
        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>());

        if (mapa.podeMover(1, 1)) {
            mapa.adicionarMovimento(1, 1);
        }

        assertEquals(1, mapa.getTrajeto().size());
    }

    @Test
    // Teste de domínio
    void deveValidarAcessibilidadeComBFS() {
        var mapa = new Mapa(3, 3);
        var obstaculos = new boolean[3][3];

        obstaculos[1][2] = true;
        obstaculos[2][1] = true;
        var moedaBloqueada = new Ponto(2, 2);

        mapa.gerarCenarioPredefinido(obstaculos, List.of(moedaBloqueada));

        var obstaculosZ = new boolean[3][3];
        obstaculosZ[0][1] = true;
        obstaculosZ[1][1] = true;

        mapa.gerarCenarioPredefinido(obstaculosZ, List.of(new Ponto(2, 2)));
    }

    @Test
    // Dublê de teste
    void gerarCenarioAleatorio_comSeedFixa_geraMapaCompletoEValido() {
        Mapa mapa = new Mapa(10, 10);
        Random randomFixoSemente = new Random(12345L);
        RandomGenerator geradorDeterministico = new RandomGenerator() {
            @Override
            public int nextInt(int bound) {
                return randomFixoSemente.nextInt(bound);
            }
            @Override
            public double nextDouble() {
                return randomFixoSemente.nextDouble();
            }
        };
        mapa.setRandom(geradorDeterministico);
        int quantidadeMoedas = 5;

        mapa.gerarCenarioAleatorio(quantidadeMoedas);

        assertEquals(quantidadeMoedas, mapa.getMoedas().size());
        Ponto itemEspecial = mapa.getItemEspecial();
        Ponto alcapao = mapa.getAlcapao();
        assertNotNull(itemEspecial);
        assertNotNull(alcapao);
        assertTrue(mapa.podeMover(itemEspecial.x(), itemEspecial.y()));
        assertTrue(mapa.podeMover(alcapao.x(), alcapao.y()));
        assertFalse(mapa.getMoedas().contains(itemEspecial));
        assertFalse(mapa.getMoedas().contains(alcapao));
        assertNotEquals(new Ponto(0, 0), itemEspecial);
        assertNotEquals(new Ponto(0, 0), alcapao);

        mapa.coletarItemEspecial();
        assertNull(mapa.getItemEspecial());
        mapa.renascerItemEspecial();
        assertNotNull(mapa.getItemEspecial());
        assertEquals(itemEspecial, mapa.getItemEspecial());
    }

    @Test
    // Teste de domínio
    void isAlcapao_deveRetornarFalseQuandoAlcapaoForNull() {
        Mapa mapa = new Mapa(5, 5);
        mapa.setAlcapao(null);
        assertFalse(mapa.isAlcapao(0, 0));
        assertFalse(mapa.isAlcapao(2, 3));
    }

    @Test
    // Teste de domínio
    void isAlcapao_deveRetornarFalseQuandoCoordenadasNaoCoincidem() {
        Mapa mapa = new Mapa(5, 5);
        mapa.setAlcapao(new Ponto(2, 2));
        assertFalse(mapa.isAlcapao(0, 0));
        assertFalse(mapa.isAlcapao(2, 3));
        assertFalse(mapa.isAlcapao(1, 2));
    }

    @Test
    // Teste de domínio
    void isAlcapao_deveRetornarTrueQuandoCoordenadasCoincidem() {
        Mapa mapa = new Mapa(5, 5);
        mapa.setAlcapao(new Ponto(2, 2));
        assertTrue(mapa.isAlcapao(2, 2));
    }

    @Test
    // Teste de domínio
    void isItemEspecial_deveRetornarFalseQuandoItemForNull() {
        Mapa mapa = new Mapa(5, 5);
        mapa.setItemEspecial(null);
        assertFalse(mapa.isItemEspecial(0, 0));
        assertFalse(mapa.isItemEspecial(1, 1));
    }

    @Test
    // Teste de domínio
    void isItemEspecial_deveRetornarFalseQuandoCoordenadasNaoCoincidem() {
        Mapa mapa = new Mapa(5, 5);
        mapa.setItemEspecial(new Ponto(3, 3));
        assertFalse(mapa.isItemEspecial(0, 0));
        assertFalse(mapa.isItemEspecial(3, 4));
        assertFalse(mapa.isItemEspecial(2, 3));
    }

    @Test
    // Teste de domínio
    void isItemEspecial_deveRetornarTrueQuandoCoordenadasCoincidem() {
        Mapa mapa = new Mapa(5, 5);
        mapa.setItemEspecial(new Ponto(3, 3));
        assertTrue(mapa.isItemEspecial(3, 3));
    }

    @Test
    // Teste de domínio
    void coletarMoeda_deveRemoverMoedaDaLista() {
        Mapa mapa = new Mapa(5, 5);
        Ponto moeda = new Ponto(2, 2);
        List<Ponto> moedas = new ArrayList<>(List.of(moeda, new Ponto(3, 3)));
        mapa.gerarCenarioPredefinido(new boolean[5][5], moedas);

        mapa.coletarMoeda(moeda);

        assertFalse(mapa.getMoedas().contains(moeda));
        assertEquals(1, mapa.getMoedas().size());
    }

    @Test
    // Teste de domínio
    void isObstaculo_quandoPontoEstaNaLista_retornaTrue() {
        Mapa mapa = new Mapa(5, 5);
        boolean[][] obstaculosMatriz = new boolean[5][5];
        obstaculosMatriz[1][2] = true;
        obstaculosMatriz[3][4] = true;
        mapa.gerarCenarioPredefinido(obstaculosMatriz, new ArrayList<>());

        assertTrue(mapa.isObstaculo(1, 2));
        assertTrue(mapa.isObstaculo(3, 4));
    }

    @Test
    // Teste de domínio
    void isObstaculo_quandoPontoNaoEstaNaLista_retornaFalse() {
        Mapa mapa = new Mapa(5, 5);
        boolean[][] obstaculosMatriz = new boolean[5][5];
        obstaculosMatriz[1][2] = true;
        mapa.gerarCenarioPredefinido(obstaculosMatriz, new ArrayList<>());

        assertFalse(mapa.isObstaculo(0, 0));
        assertFalse(mapa.isObstaculo(2, 2));
        assertFalse(mapa.isObstaculo(4, 4));
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_AdicionarMovimento_CelulaSemObstaculoComMoeda() {
        Mapa mapa = new Mapa(3, 3);
        Ponto moeda = new Ponto(1, 1);
        mapa.gerarCenarioPredefinido(new boolean[3][3], List.of(moeda));

        mapa.adicionarMovimento(1, 1);
        assertEquals(1, mapa.getMoedasColetadas());
        assertEquals(2, mapa.getTrajeto().size());
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_AdicionarMovimento_CelulaSemObstaculoSemMoeda() {
        Mapa mapa = new Mapa(3, 3);
        mapa.gerarCenarioPredefinido(new boolean[3][3], new ArrayList<>());

        mapa.adicionarMovimento(1, 1);
        assertEquals(0, mapa.getMoedasColetadas());
        assertEquals(2, mapa.getTrajeto().size());
    }

    @Test
    // Teste estrutural (MC/DC)
    void mcdc_AdicionarMovimento_CelulaComObstaculo() {
        Mapa mapa = new Mapa(3, 3);
        boolean[][] obstaculos = new boolean[3][3];
        obstaculos[1][1] = true;
        mapa.gerarCenarioPredefinido(obstaculos, new ArrayList<>());

        mapa.adicionarMovimento(1, 1); // não deve adicionar
        assertEquals(1, mapa.getTrajeto().size());
    }

    @Test
    // Teste estrutural: valida pré-condições do mapa.
    void contratosDeEntradaDoMapa() {
        Mapa mapa = new Mapa(3, 3);
        assertThrows(AssertionError.class, () -> mapa.gerarCenarioPredefinido(null, List.of()));
        assertThrows(AssertionError.class,
                () -> mapa.gerarCenarioPredefinido(new boolean[2][3], List.of()));
        assertThrows(AssertionError.class,
                () -> mapa.gerarCenarioPredefinido(new boolean[3][2], List.of()));
        assertThrows(AssertionError.class,
                () -> mapa.gerarCenarioPredefinido(new boolean[3][3], null));
        assertThrows(AssertionError.class,
                () -> mapa.gerarCenarioPredefinido(new boolean[3][3],
                        java.util.Collections.singletonList(null)));
        assertThrows(AssertionError.class, () -> mapa.gerarCenarioAleatorio(0));
        assertThrows(AssertionError.class, () -> mapa.gerarCenarioAleatorio(7));
        assertThrows(AssertionError.class, mapa::coletarItemEspecial);
        assertThrows(AssertionError.class, () -> mapa.coletarMoeda(new Ponto(1, 1)));
    }

    @Test
    // Teste estrutural: valida invariantes internas do mapa.
    void invariantesDoMapa() throws Exception {
        Mapa sobreposto = new Mapa(3, 3);
        sobreposto.setAlcapao(new Ponto(1, 1));
        assertThrows(AssertionError.class, () -> sobreposto.setItemEspecial(new Ponto(1, 1)));

        Mapa origemOcupada = new Mapa(3, 3);
        assertThrows(AssertionError.class, () -> origemOcupada.setAlcapao(new Ponto(0, 0)));

        Mapa vaziosInconsistentes = new Mapa(3, 3);
        var espacosVazios = Mapa.class.getDeclaredField("espacosVazios");
        espacosVazios.setAccessible(true);
        espacosVazios.set(vaziosInconsistentes, new ArrayList<Ponto>());
        assertThrows(AssertionError.class, () -> vaziosInconsistentes.adicionarMovimento(1, 0));
    }

    @Property
    // Teste de propriedade
    void propriedade_GerarCenarioAleatorio_SempreAcessivel(
            @ForAll @IntRange(min = 5, max = 15) int tamanho,
            @ForAll @IntRange(min = 1, max = 10) int moedas) {
        Mapa mapa = new Mapa(tamanho, tamanho);
        mapa.setRandom(new RandomGenerator() {
            private final Random random = new Random(42);
            @Override
            public int nextInt(int bound) { return random.nextInt(bound); }
            @Override
            public double nextDouble() { return random.nextDouble(); }
        });

        mapa.gerarCenarioAleatorio(moedas);

        // Verifica se todas as moedas, alçapão e item são alcançáveis a partir de (0,0)
        assertTrue(caminhoExiste(mapa, new Ponto(0,0), mapa.getAlcapao()));
        assertTrue(caminhoExiste(mapa, new Ponto(0,0), mapa.getItemEspecial()));
        for (Ponto moeda : mapa.getMoedas()) {
            assertTrue(caminhoExiste(mapa, new Ponto(0,0), moeda));
        }
    }

    private boolean caminhoExiste(Mapa mapa, Ponto origem, Ponto destino) {
        boolean[][] visitado = new boolean[mapa.getColunas()][mapa.getLinhas()];
        java.util.Queue<Ponto> fila = new java.util.LinkedList<>();
        fila.add(origem);
        visitado[origem.x()][origem.y()] = true;
        while (!fila.isEmpty()) {
            Ponto atual = fila.poll();
            if (atual.equals(destino)) return true;
            for (int[] d : new int[][]{{0,1},{0,-1},{1,0},{-1,0}}) {
                int nx = atual.x() + d[0];
                int ny = atual.y() + d[1];
                if (nx >= 0 && nx < mapa.getColunas() && ny >= 0 && ny < mapa.getLinhas()
                        && !visitado[nx][ny] && !mapa.isObstaculo(nx, ny)) {
                    visitado[nx][ny] = true;
                    fila.add(new Ponto(nx, ny));
                }
            }
        }
        return false;
    }
}

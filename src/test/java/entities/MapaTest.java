package entities;

import common.RandomGenerator;
import org.junit.jupiter.api.Test;

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
        mapa.adicionarMovimento(1, 0);

        assertEquals(1, mapa.getTrajeto().size());

        mapa.adicionarMovimento(0, 1);
        assertEquals(2, mapa.getTrajeto().size());

        mapa.adicionarMovimento(0, 2);
        assertEquals(1, mapa.getMoedasColetadas());
        assertTrue(mapa.faseConcluida());
    }

    @Test
    void gerarCenarioAleatorio() {
        var mapa = new Mapa(5, 5);
        var javaRandom = new java.util.Random();

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

        mapa.gerarCenarioAleatorio(3);
        assertFalse(mapa.getMoedas().isEmpty());
        assertFalse(mapa.getTrajeto().isEmpty());

        assertEquals(5, mapa.getColunas());
        assertEquals(5, mapa.getLinhas());
    }
}
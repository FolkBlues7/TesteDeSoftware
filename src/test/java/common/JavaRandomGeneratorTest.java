package common;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JavaRandomGeneratorTest {

    private JavaRandomGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JavaRandomGenerator();
    }

    @Test
    // Teste de domínio
    void nextInt_alwaysReturnsValueWithinBounds() {
        int bound = 10;
        for (int i = 0; i < 1000; i++) {
            int result = generator.nextInt(bound);
            assertThat(result).isNotNegative().isLessThan(bound);
        }
    }

    @Test
    // Teste de domínio
    void nextDouble_alwaysReturnsValueBetweenZeroAndOne() {
        for (int i = 0; i < 1000; i++) {
            double result = generator.nextDouble();
            assertThat(result).isNotNegative().isLessThan(1.0);
        }
    }

    @Test
    // Teste de fronteira
    void nextInt_boundZero_throwsException() {
        assertThatThrownBy(() -> generator.nextInt(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    // Teste de propriedade
    void nextInt_anyBound_returnsValueInRange(@ForAll @IntRange(min = 1, max = 1000) int bound) {
        JavaRandomGenerator generator = new JavaRandomGenerator();
        int result = generator.nextInt(bound);
        assertThat(result).isBetween(0, bound - 1);
    }

    @Property
    // Teste de propriedade
    void nextDouble_alwaysBetweenZeroAndOne(@ForAll int seed) {
        JavaRandomGenerator generator = new JavaRandomGenerator();
        double result = generator.nextDouble();
        assertThat(result).isBetween(0.0, 1.0);
    }
}
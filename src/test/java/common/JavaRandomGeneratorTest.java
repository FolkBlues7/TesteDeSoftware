package common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JavaRandomGeneratorTest {

    private JavaRandomGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JavaRandomGenerator();
    }

    @Test
    void nextInt_alwaysReturnsValueWithinBounds() {
        int bound = 10;

        for (int i = 0; i < 1000; i++) {
            int result = generator.nextInt(bound);

            assertThat(result)
                    .isNotNegative()
                    .isLessThan(bound);
        }
    }

    @Test
    void nextDouble_alwaysReturnsValueBetweenZeroAndOne() {
        for (int i = 0; i < 1000; i++) {
            double result = generator.nextDouble();

            assertThat(result)
                    .isNotNegative()
                    .isLessThan(1.0);
        }
    }
}
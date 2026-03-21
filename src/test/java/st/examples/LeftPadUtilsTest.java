package st.examples;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.of;
import java.util.stream.Stream;
import static st.examples.LeftPadUtils.leftPad;

public class LeftPadUtilsTest {
    @ParameterizedTest
    @MethodSource("generator")
    void test(String originalStr, int size, String padString, String expectedStr) {
        assertThat(leftPad(originalStr, size, padString)).isEqualTo(expectedStr);
    }
    static Stream<Arguments> generator() {
        return Stream.of(
                of(null, 10, "-", null), // T1: str é null
                of("", 5, "-", "-----"), // T2: str é vazia
                of("abc", -1, "-", "abc"), // T3: size negativo
                of("abc", 5, null, "  abc"), // T4: padStr é null
                of("abc", 5, "", "  abc"), // T5: padStr é vazia
                of("abc", 5, "-", "--abc"), // T6: padStr tem um único caracter
                of("abc", 3, "-", "abc"), // T7: size == len(str)
                of("abc", 0, "-", "abc"), // T8: size == 0
                of("abc", 2, "-", "abc") // T9: size < len(str)
//                of("abc", 5, "--", "--abc"), // T10: len(padStr) == espaço restante em str
//                of("abc", 5, "---", "--abc"), // T11: len(padStr) > espaço restante em str
//                of("abc", 5, "-", "--abc") // T12: len(padStr) < espaço restante em str

        );
    }
}

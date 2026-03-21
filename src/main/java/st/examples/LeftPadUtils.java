package st.examples;

public class LeftPadUtils {

    private static final String SPACE = " ";

    private static boolean isEmpty(final CharSequence cs) {

        return cs == null || cs.isEmpty();
    }

    /**
     * Left pad a String with a specified String.
     * Pad to a size of {@code size}.
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padStr  the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) { // If the string to pad is null, we return null right away
            return null;
        }
        if (isEmpty(padStr)) { // If the pad string is null or empty, we make it a space
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) { // There is no need to pad this string
            return str; // returns original String when possible
        }

        if (pads == padLen) {  // If the number of characters to pad matches the size of the pad string, we concatenate it
            return padStr.concat(str);
        } else if (pads < padLen) { // If we cannot fit the entire pad string, we add only the part that fits
            return padStr.substring(0, pads).concat(str);
        } else { // We have to add the pad string more than once. We go character by character until the string is fully padded
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

}
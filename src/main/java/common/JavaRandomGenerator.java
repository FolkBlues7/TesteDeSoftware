package common;

public class JavaRandomGenerator implements RandomGenerator {
    private final java.util.Random random = new java.util.Random();

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }
}

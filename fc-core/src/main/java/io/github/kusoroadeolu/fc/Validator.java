package io.github.kusoroadeolu.fc;

final class Validator {
    private Validator(){}

    static int ensureGreaterThanOrEqualToZero(int i, String prefix) {
        if (i <= 0) throw new IllegalArgumentException(prefix + " cannot be less than or equal to zero");
        return i;
    }
}

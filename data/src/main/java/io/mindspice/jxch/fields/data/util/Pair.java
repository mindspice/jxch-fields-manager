package io.mindspice.jxch.fields.data.util;

public record Pair<U, V>(U first, V second) {
    public static <U, V> Pair<U, V> of(U obj1, V obj2) {
        return new Pair<>(obj1, obj2);
    }
}

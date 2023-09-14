package io.mindspice.jxch.fields.data.enums;

public enum SpInterval {
    _64(64),
    _384(384),
    _1536(1536),
    _4608(4608),
    _9216(9216);
    public final int value;

    SpInterval(int i) { value = i; }
}

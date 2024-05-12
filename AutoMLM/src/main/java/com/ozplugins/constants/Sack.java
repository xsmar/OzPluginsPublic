package com.ozplugins.constants;

import lombok.Getter;

@Getter
public enum Sack {
    REGULAR(81),
    UPGRADED(162);

    private final int size;

    Sack(int size) {
        this.size = size;
    }
}

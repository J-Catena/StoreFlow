package com.jcatena.storeflow.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Quantities {

    public static final int SCALE = 3;

    private Quantities() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}

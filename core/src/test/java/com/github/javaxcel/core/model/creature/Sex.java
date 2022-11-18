package com.github.javaxcel.core.model.creature;

import com.github.javaxcel.core.TestUtils;

public enum Sex {

    MALE, INTERSEX, FEMALE;

    public static Sex newRandom() {
        int randomInt = TestUtils.getRandom().nextInt(100);
        return randomInt < 50 ? FEMALE // 50%
                : randomInt > 50 ? MALE // 49%
                : INTERSEX; // 1%
    }

}

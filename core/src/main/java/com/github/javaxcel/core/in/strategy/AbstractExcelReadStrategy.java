package com.github.javaxcel.core.in.strategy;

import java.util.Objects;

public abstract class AbstractExcelReadStrategy implements ExcelReadStrategy {

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractExcelReadStrategy)) return false;

        AbstractExcelReadStrategy that = (AbstractExcelReadStrategy) obj;
        return Objects.equals(this.getClass(), that.getClass());
    }

}

package com.github.javaxcel.core.out.strategy;

import java.util.Objects;

/**
 * Abstract strategy for Excel writer
 *
 * @since 0.8.0
 */
public abstract class AbstractExcelWriteStrategy implements ExcelWriteStrategy {

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractExcelWriteStrategy)) {
            return false;
        }

        AbstractExcelWriteStrategy that = (AbstractExcelWriteStrategy) obj;
        return Objects.equals(this.getClass(), that.getClass());
    }

}

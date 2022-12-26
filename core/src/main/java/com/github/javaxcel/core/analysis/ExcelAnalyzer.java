package com.github.javaxcel.core.analysis;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Analyzer for preparing the fields to handle for Excel
 *
 * @since 0.9.0
 */
public interface ExcelAnalyzer {

    /**
     * Analyzes the fields to handle for Excel.
     *
     * @param fields    targeted fields
     * @param arguments optional arguments
     * @return analyses of the fields
     */
    List<ExcelAnalysis> analyze(List<Field> fields, Object... arguments);

}

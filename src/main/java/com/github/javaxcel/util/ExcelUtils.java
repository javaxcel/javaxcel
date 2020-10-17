package com.github.javaxcel.util;

import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.exception.UnsupportedWorkbookException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * Gets range of the sheets.
     *
     * @param workbook excel workbook
     * @return range that from 0 to (the number of sheets - 1)
     * @see Workbook#getNumberOfSheets()
     */
    public static int[] getSheetRange(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets()).toArray();
    }

    public static List<Sheet> getSheets(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets())
                .mapToObj(workbook::getSheetAt)
                .collect(toList());
    }

    /**
     * Returns the number of models in a sheet.
     *
     * <p> This excludes header row.
     * In other words, this returns the total number of rows minus 1.
     *
     * @param sheet sheet
     * @return the number of models
     */
    public static int getNumOfModels(Sheet sheet) {
        if (sheet instanceof SXSSFSheet) throw new UnsupportedWorkbookException();
        return Math.max(0, sheet.getPhysicalNumberOfRows() - 1);
    }

    public static long getNumOfModels(Workbook workbook) {
        if (workbook instanceof SXSSFWorkbook) throw new UnsupportedWorkbookException();
        return getSheets(workbook).stream().mapToInt(ExcelUtils::getNumOfModels).sum();
    }

    public static int getMaxRows(Workbook workbook) {
        return workbook instanceof HSSFWorkbook
                ? SpreadsheetVersion.EXCEL97.getMaxRows()
                : SpreadsheetVersion.EXCEL2007.getMaxRows();
    }

    public static <T> T instantiate(Class<T> type) {
        // Allows only constructor without parameter. TODO: write it in javadoc.
        Constructor<T> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new NoTargetedConstructorException(e, type);
        }
        constructor.setAccessible(true);

        // Instantiates new model and sets up data into the model's fields.
        T model;
        try {
            model = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", type.getName()));
        }

        return model;
    }

}

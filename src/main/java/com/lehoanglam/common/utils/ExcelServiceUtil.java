package com.yes4all.common.utils;

import com.yes4all.common.errors.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class ExcelServiceUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelServiceUtil.class);

    // private constructor
    private ExcelServiceUtil() {}

    public static byte[] generateExcelFile(String fileName, List<?> data, Map<String, String> headerMap) {
        byte[] resource;
        StopWatch stopwatch = DateUtil.initStopWatch();
        log.info("START Generate excel file");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet(fileName);

            // Creating header
            createHeaders(workbook, sheet, headerMap);

            // Creating data rows for each item
            addExcelCell(sheet, data, headerMap, workbook);

            // Resize the columns to fit the content
            sheet.trackAllColumnsForAutoSizing();
            for (int i = 0; i < headerMap.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            log.info("END generate excel file in {}", DateUtil.calculateTime(stopwatch));
            resource = byteArrayOutputStream.toByteArray();

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            log.error("Could not created excel file");
            throw new BusinessException("Could not created excel file");
        }
        return resource;
    }

    private static void addExcelCell(Sheet sheet, List<?> contents, Map<String, String> headers, SXSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);

        int rowIndex = 1;
        for (Object item : contents) {
            Row dataRow = sheet.createRow(rowIndex++);
            int columnIndex = 0;
            for (String property : headers.keySet()) {
                try {
                    // Use reflection to get the property value
                    Field field = item.getClass().getDeclaredField(property);
                    field.setAccessible(true);
                    Object value = field.get(item);
                    // Create a new cell and set the value
                    createCell(dataRow, columnIndex++, value, cellStyle);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to write data to Excel file.", e);
                }
            }
        }
    }

    private static void createHeaders(Workbook workbook, Sheet sheet, Map<String, String> headers) {
        // create header styles
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // create first row
        Row row = sheet.createRow(0);
        // add header value
        int columnCount = 0;
        for (String header : headers.values()) {
            createCell(row, columnCount++, header, headerCellStyle);
        }
    }

    private static void createCell(Row row, int index, Object value, CellStyle cellStyle) {
        Cell cell = row.createCell(index);
        cell.setCellStyle(cellStyle);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        }
    }
}

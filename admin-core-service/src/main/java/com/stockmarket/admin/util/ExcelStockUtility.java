package com.stockmarket.admin.util;

import com.stockmarket.admin.dto.ApiDtos.StockRequest;
import com.stockmarket.admin.exception.CustomExceptions.InvalidExcelFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelStockUtility {

    private static final Logger log = LoggerFactory.getLogger(ExcelStockUtility.class);
    private static final String[] HEADERS = {"Symbol", "CompanyName", "Sector", "LatestPrice", "ExchangeId"};

    public byte[] generateStocksExcel(List<StockRequest> stocks) {
        log.info("Generating Excel workbook for {} stock rows", stocks.size());
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Stocks");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }

            int rowIdx = 1;
            for (StockRequest s : stocks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.symbol());
                row.createCell(1).setCellValue(s.companyName());
                row.createCell(2).setCellValue(s.sector());
                row.createCell(3).setCellValue(s.latestPrice().doubleValue());
                row.createCell(4).setCellValue(s.exchangeId());
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Excel workbook", e);
            throw new InvalidExcelFormatException("Unable to export stocks to Excel");
        }
    }

    public List<StockRequest> parseStocksExcel(InputStream inputStream) {
        log.info("Parsing uploaded Excel file for bulk stock import");
        List<StockRequest> parsed = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new InvalidExcelFormatException("Excel workbook has no sheets");
            }
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                String symbol = getStringValue(row.getCell(0));
                String companyName = getStringValue(row.getCell(1));
                String sector = getStringValue(row.getCell(2));
                BigDecimal latestPrice = getDecimalValue(row.getCell(3));
                Long exchangeId = getLongValue(row.getCell(4));

                parsed.add(new StockRequest(symbol, companyName, sector, latestPrice, exchangeId));
            }
            log.info("Successfully parsed {} stock rows from Excel", parsed.size());
            return parsed;
        } catch (InvalidExcelFormatException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error parsing Excel file", e);
            throw new InvalidExcelFormatException("Malformed Excel file or invalid column values: " + e.getMessage());
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 5; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) throw new InvalidExcelFormatException("Missing required text cell");
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> throw new InvalidExcelFormatException("Expected string cell");
        };
    }

    private BigDecimal getDecimalValue(Cell cell) {
        if (cell == null) throw new InvalidExcelFormatException("Missing price cell");
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> new BigDecimal(cell.getStringCellValue().trim());
            default -> throw new InvalidExcelFormatException("Expected numeric price cell");
        };
    }

    private Long getLongValue(Cell cell) {
        if (cell == null) throw new InvalidExcelFormatException("Missing exchangeId cell");
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> Long.parseLong(cell.getStringCellValue().trim());
            default -> throw new InvalidExcelFormatException("Expected numeric exchangeId cell");
        };
    }
}

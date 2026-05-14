package io.orphea.dataset.library.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "ExcelService")
@Component
@RequiredArgsConstructor
public class ExcelService {

    public static List<MultipartFile> splitExcelFile(MultipartFile multipartFile, List<String> allowedSheetNames) throws IOException {
        List<MultipartFile> multipartFiles = new ArrayList<>();

        // Determine file format (XLS or XLSX)
        Workbook workbook = getWorkbook(multipartFile.getInputStream(), multipartFile.getOriginalFilename());

        // Iterate through each sheet and create a new Excel file for each allowed sheet
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();

            // Check if the sheet name is in the allowed list
            if (allowedSheetNames.contains(sheetName)) {
                // Create a new workbook for the single sheet
                Workbook singleSheetWorkbook = workbook instanceof XSSFWorkbook
                        ? new XSSFWorkbook()
                        : new HSSFWorkbook();
                Sheet newSheet = singleSheetWorkbook.createSheet(sheetName);

                // Copy the content from the original sheet to the new sheet
                copySheet(sheet, newSheet);

                // Convert the workbook to a MultipartFile
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                singleSheetWorkbook.write(byteArrayOutputStream);
                byteArrayOutputStream.close();

                MultipartFile file = new MockMultipartFile(
                        sheetName + ".xls",
                        sheetName + (workbook instanceof XSSFWorkbook ? ".xlsx" : ".xls"),
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        new ByteArrayInputStream(byteArrayOutputStream.toByteArray())
                );

                multipartFiles.add(file);
            }
        }

        // Close the original workbook
        workbook.close();

        return multipartFiles;
    }

    private static Workbook getWorkbook(InputStream inputStream, String originalFilename) throws IOException {
        IOUtils.setByteArrayMaxOverride(300_000_000);
        if (originalFilename.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (originalFilename.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + originalFilename);
        }
    }

    private static void copySheet(Sheet originalSheet, Sheet newSheet) {
        // Copy rows and cells
        for (int i = 0; i <= originalSheet.getLastRowNum(); i++) {
            Row originalRow = originalSheet.getRow(i);
            Row newRow = newSheet.createRow(i);

            if (originalRow != null) {
                for (int j = 0; j < originalRow.getLastCellNum(); j++) {
                    Cell originalCell = originalRow.getCell(j);
                    Cell newCell = newRow.createCell(j);

                    if (originalCell != null) {
                        copyCell(originalCell, newCell);
                    }
                }
            }
        }
    }

    private static void copyCell(Cell originalCell, Cell newCell) {
        switch (originalCell.getCellType()) {
            case STRING:
                newCell.setCellValue(originalCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(originalCell)) {
                    newCell.setCellValue(originalCell.getDateCellValue());
                } else {
                    newCell.setCellValue(originalCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                newCell.setCellValue(originalCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(originalCell.getCellFormula());
                break;
            case BLANK:
                newCell.setBlank();
                break;
            case ERROR:
                newCell.setCellErrorValue(originalCell.getErrorCellValue());
                break;
            default:
                break;
        }
    }


    public List<String> getExcelSheetNames(MultipartFile excelFile) {
        List<String> sheetNames = new ArrayList<>();
        try {
            Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sheetNames;
    }

    public MultipartFile convertExcelToCsv(MultipartFile multipartFile, String sheetName) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheet(sheetName);

        // Detect start row and column
        int startRow = findStartRow(sheet);
        int startCol = findStartColumn(sheet, startRow);

        // Process the sheet and create CSV
        StringBuilder csvData = new StringBuilder();
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);

            if (row != null) {
                for (int j = startCol; j <= row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String cellValue = getCellValueAsString(cell, workbook);
                        csvData.append(cellValue).append(",");
                    } else {
                        csvData.append(",");
                    }
                }
                csvData.deleteCharAt(csvData.length() - 1);
                csvData.append("\n");
            }
        }

        workbook.close();

        byte[] csvDataBytes = csvData.toString().getBytes();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
        String formattedDateTime = now.format(formatter);

        int lastDotIndex = multipartFile.getOriginalFilename().lastIndexOf(".");

        return new MockMultipartFile(multipartFile.getOriginalFilename() + ".csv",
                multipartFile.getOriginalFilename().substring(0, lastDotIndex) + "-" + sheetName + "-" + formattedDateTime + ".csv", "text/csv", csvDataBytes);
    }

    private int findStartRow(Sheet sheet) {
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row != null) {
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        return row.getRowNum();
                    }
                }
            }
        }
        return sheet.getFirstRowNum();
    }

    private int findStartColumn(Sheet sheet, int startRow) {
        int minStartCol = Integer.MAX_VALUE;
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row != null) {
                int firstCell = row.getFirstCellNum();
                if (firstCell >= 0 && firstCell < minStartCol) {
                    minStartCol = firstCell;
                }
            }
        }
        return minStartCol >= 0 ? minStartCol : 0;
    }

    private String getCellValueAsString(Cell cell, Workbook workbook) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case FORMULA:
                try {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellEvaluated = evaluator.evaluate(cell);
                    return String.valueOf(cellEvaluated.getNumberValue());
                } catch (Exception e) {
                    return "Formula Parse Error";
                }
            case STRING:
                return cell.getStringCellValue();
            default:
                return "";
        }
    }
}


package org.cherubim.excel.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.cherubim.common.util.DateUtil;
import org.cherubim.common.util.StringUtil;
import org.cherubim.excel.common.Constant;
import org.cherubim.excel.entity.ExcelEntity;
import org.cherubim.excel.entity.ExcelPropertyEntity;
import org.cherubim.excel.function.ExportFunction;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.cherubim.excel.common.Constant.CHARSET;
import static org.cherubim.excel.common.Constant.FILE_PATH;

/**
 * 导出具体实现类
 *
 * @author huangxiaohu
 */
@Slf4j
public class ExcelWriter {

    private Integer rowAccessWindowSize;
    private ExcelEntity excelEntity;
    private Integer pageSize;
    private Integer nullCellCount = 0;
    private Integer recordCountPerSheet;
    private XSSFCellStyle headCellStyle;
    private Map<Integer, Integer> columnWidthMap = new HashMap<Integer, Integer>();

    public ExcelWriter(ExcelEntity excelEntity, Integer pageSize, Integer rowAccessWindowSize, Integer recordCountPerSheet) {
        this.excelEntity = excelEntity;
        this.pageSize = pageSize;
        this.rowAccessWindowSize = rowAccessWindowSize;
        this.recordCountPerSheet = recordCountPerSheet;
    }

    public ExcelWriter(ExcelEntity excelEntity) {
        this.excelEntity = excelEntity;
    }

    public void generateCsv(String email) {


        try {
            File path = new File(FILE_PATH + email);

            List<File> fileList = new ArrayList<File>();
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files.length <= 0) {
                    return;
                }
                for (File file : files) {
                    fileList.add(file);
                }
                final List<File> collect = fileList.stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
                File csvFile = new File(FILE_PATH + email + File.separator + excelEntity.getFileName() + ".csv");

                if (!csvFile.exists()) {
                    csvFile.createNewFile();
                }
                Appendable printWriter = new PrintWriter(csvFile, CHARSET);
                final List<String> excelColNames = excelEntity.getPropertyList().stream().map(ExcelPropertyEntity::getColumnName).collect(Collectors.toList());
                CSVPrinter csvPrinter = CSVFormat.EXCEL.withHeader(excelColNames.toArray(new String[excelColNames.size()])).print(printWriter);

                csvPrinter.flush();
                csvPrinter.close();
                for (File file : collect) {
                    if (file.getName().endsWith("csv")) {
                        byte[] bytes = FileUtils.readFileToByteArray(file);
                        FileUtils.writeByteArrayToFile(csvFile, bytes, true);
                    }
                    if (!file.getName().contains(excelEntity.getFileName())) {
                        file.delete();
                    }
                }
            }

        } catch (Exception e) {
            log.error("write into file error:{}", e.toString());
        }

    }

    /**
     * @param param
     * @param exportFunction
     * @param <P>
     * @param <T>
     * @return
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ParseException
     * @throws IllegalAccessException
     */
    public <P, T> SXSSFWorkbook generateWorkbook(P param, ExportFunction<P, T> exportFunction) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(rowAccessWindowSize);
        int sheetNo = 1;
        int rowNum = 1;
        List<ExcelPropertyEntity> propertyList = excelEntity.getPropertyList();
        //初始化第一行
        SXSSFSheet sheet = generateHeader(workbook, propertyList, excelEntity.getFileName());

        //生成其他行
        int firstPageNo = 1;
        while (true) {
            List<T> data = exportFunction.pageQuery(param, firstPageNo, pageSize);
            if (data == null || data.isEmpty()) {
                if (rowNum != 1) {
                    if (Constant.OPEN_CELL_STYLE) {
                        sizeColumnWidth(sheet, propertyList.size());
                    }
                }
                log.warn("查询结果为空,结束查询!");
                break;
            }
            int dataSize = data.size();
            for (int i = 1; i <= dataSize; i++, rowNum++) {
                T queryResult = data.get(i - 1);
                Object convertResult = exportFunction.convert(queryResult);
                if (rowNum > Constant.MAX_RECORD_COUNT_PEER_SHEET) {
                    if (Constant.OPEN_CELL_STYLE) {
                        sizeColumnWidth(sheet, propertyList.size());
                    }
                    sheet = generateHeader(workbook, propertyList, excelEntity.getFileName() + "_" + sheetNo);
                    sheetNo++;
                    rowNum = 1;
                    columnWidthMap.clear();
                }
                SXSSFRow row = sheet.createRow(rowNum);
                for (int j = 0; j < propertyList.size(); j++) {
                    SXSSFCell cell = row.createCell(j);
                    buildCellValue(cell, convertResult, propertyList.get(j));
                    calculateColumnWidth(cell, j);
                }
                if (nullCellCount == propertyList.size()) {
                    log.warn("忽略一行空数据!");
                    sheet.removeRow(row);
                    rowNum--;
                }
                nullCellCount = 0;

            }
            if (data.size() < pageSize) {
                sizeColumnWidth(sheet, propertyList.size());
                log.warn("查询结果数量小于pageSize,结束查询!");
                break;
            }
            firstPageNo++;
        }
        return workbook;
    }

    /**
     * 构建模板Excel
     *
     * @return
     */
    public SXSSFWorkbook generateTemplateWorkbook() {
        SXSSFWorkbook workbook = new SXSSFWorkbook(rowAccessWindowSize);

        List<ExcelPropertyEntity> propertyList = excelEntity.getPropertyList();
        SXSSFSheet sheet = generateHeader(workbook, propertyList, excelEntity.getFileName());

        SXSSFRow row = sheet.createRow(1);
        for (int j = 0; j < propertyList.size(); j++) {
            SXSSFCell cell = row.createCell(j);
            cell.setCellValue(propertyList.get(j).getTemplateCellValue());
            calculateColumnWidth(cell, j);
        }
        sizeColumnWidth(sheet, propertyList.size());
        return workbook;
    }

    /**
     * 构建多Sheet Excel
     *
     * @param param
     * @param exportFunction
     * @param <R>
     * @param <T>
     * @return
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ParseException
     * @throws IllegalAccessException
     */
    public <R, T> SXSSFWorkbook generateMultiSheetWorkbook(R param, ExportFunction<R, T> exportFunction) throws Exception {
        int pageNo = 1;
        int sheetNo = 1;
        int rowNum = 1;
        SXSSFWorkbook workbook = new SXSSFWorkbook(rowAccessWindowSize);
        List<ExcelPropertyEntity> propertyList = excelEntity.getPropertyList();
        SXSSFSheet sheet = generateHeader(workbook, propertyList, excelEntity.getFileName());

        while (true) {
            List<T> data = exportFunction.pageQuery(param, pageNo, pageSize);
            if (data == null || data.isEmpty()) {
                if (rowNum != 1) {
                    sizeColumnWidth(sheet, propertyList.size());
                }
                log.warn("查询结果为空,结束查询!");
                break;
            }
            for (int i = 1; i <= data.size(); i++, rowNum++) {
                T queryResult = data.get(i - 1);
                Object convertResult = exportFunction.convert(queryResult);
                if (rowNum > recordCountPerSheet) {
                    sizeColumnWidth(sheet, propertyList.size());
                    sheet = generateHeader(workbook, propertyList, excelEntity.getFileName() + "_" + sheetNo);
                    sheetNo++;
                    rowNum = 1;
                    columnWidthMap.clear();
                }
                SXSSFRow bodyRow = sheet.createRow(rowNum);
                for (int j = 0; j < propertyList.size(); j++) {
                    SXSSFCell cell = bodyRow.createCell(j);
                    buildCellValue(cell, convertResult, propertyList.get(j));
                    calculateColumnWidth(cell, j);
                }
                if (nullCellCount == propertyList.size()) {
                    log.warn("忽略一行空数据!");
                    sheet.removeRow(bodyRow);
                    rowNum--;
                }
                nullCellCount = 0;
            }
            if (data.size() < pageSize) {
                sizeColumnWidth(sheet, propertyList.size());
                log.warn("查询结果数量小于pageSize,结束查询!");
                break;
            }
            pageNo++;
        }
        return workbook;
    }

    /**
     * 自动适配中文单元格
     *
     * @param sheet
     * @param columnSize
     */
    private void sizeColumnWidth(SXSSFSheet sheet, Integer columnSize) {
        if (Constant.OPEN_AUTO_COLUMN_WIDTH) {
            for (int j = 0; j < columnSize; j++) {
                if (columnWidthMap.get(j) != null) {
                    sheet.setColumnWidth(j, columnWidthMap.get(j) * 256);
                }
            }
        }
    }

    /**
     * 自动适配中文单元格
     *
     * @param cell
     * @param columnIndex
     */
    private void calculateColumnWidth(SXSSFCell cell, Integer columnIndex) {
        if (Constant.OPEN_AUTO_COLUMN_WIDTH) {
            String cellValue = cell.getStringCellValue();
            int length = cellValue.getBytes().length;
            length += (int) Math.ceil((double) ((cellValue.length() * 3 - length) / 2) * 0.1D);
            length = Math.max(length, Constant.CHINESES_ATUO_SIZE_COLUMN_WIDTH_MIN);
            length = Math.min(length, Constant.CHINESES_ATUO_SIZE_COLUMN_WIDTH_MAX);
            if (columnWidthMap.get(columnIndex) == null || columnWidthMap.get(columnIndex) < length) {
                columnWidthMap.put(columnIndex, length);
            }
        }
    }

    /**
     * 初始化第一行的属性
     *
     * @param workbook
     * @param propertyList
     * @param sheetName
     * @return
     */
    private SXSSFSheet generateHeader(SXSSFWorkbook workbook, List<ExcelPropertyEntity> propertyList, String sheetName) {
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        SXSSFRow headerRow = sheet.createRow(0);
        if (Constant.OPEN_CELL_STYLE) {
            headerRow.setHeight((short) 600);
            CellStyle headCellStyle = getHeaderCellStyle(workbook);
        }
        for (int i = 0; i < propertyList.size(); i++) {
            SXSSFCell cell = headerRow.createCell(i);
            if (Constant.OPEN_CELL_STYLE) {
                cell.setCellStyle(headCellStyle);
            }
            cell.setCellValue(propertyList.get(i).getColumnName());
            calculateColumnWidth(cell, i);
        }
        return sheet;
    }

    /**
     * 构造 除第一行以外的其他行的列值
     *
     * @param cell
     * @param entity
     * @param property
     */
    private void buildCellValue(SXSSFCell cell, Object entity, ExcelPropertyEntity property) throws Exception {
        Field field = property.getFieldEntity();
        Object cellValue = field.get(entity);
        if (StringUtil.isBlank(cellValue) || "0".equals(cellValue.toString()) || "0.0".equals(cellValue.toString()) || "0.00".equals(cellValue.toString())) {
            nullCellCount++;
        }
        if (cellValue == null) {
            cell.setCellValue("");
        } else if (cellValue instanceof BigDecimal) {
            if (-1 == property.getScale()) {
                cell.setCellValue(cellValue.toString());
            } else {
                cell.setCellValue((((BigDecimal) cellValue).setScale(property.getScale(), property.getRoundingMode())).toString());
            }
        } else if (cellValue instanceof Date) {
            cell.setCellValue(DateUtil.format(property.getDateFormat(), (Date) cellValue));
        } else {
            cell.setCellValue(cellValue.toString());
        }
    }

    public CellStyle getHeaderCellStyle(SXSSFWorkbook workbook) {
        if (headCellStyle == null) {
            headCellStyle = workbook.getXSSFWorkbook().createCellStyle();
            headCellStyle.setBorderTop(BorderStyle.NONE);
            headCellStyle.setBorderRight(BorderStyle.NONE);
            headCellStyle.setBorderBottom(BorderStyle.NONE);
            headCellStyle.setBorderLeft(BorderStyle.NONE);
            headCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFColor color = new XSSFColor(new java.awt.Color(217, 217, 217), new DefaultIndexedColorMap());
            headCellStyle.setFillForegroundColor(color);
            headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setFontName("微软雅黑");
            font.setColor(IndexedColors.ROYAL_BLUE.index);
            font.setBold(true);
            headCellStyle.setFont(font);
            headCellStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));
        }
        return headCellStyle;
    }
}

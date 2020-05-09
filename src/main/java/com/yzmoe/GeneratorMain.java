package com.yzmoe;

import org.apache.poi.ss.formula.functions.Index;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorMain {
    
    static String url = "jdbc:mysql://localhost:3306/cloud";
    static String user  = "root";
    static String pass = "12345678";
    static String db ="UNKNOWN";
    static String outfile = "out.xlsx";


    static int marginTop = 1;
    static int marginLeft = 1;
    static String[] fixedLabels = {"序号", "列名", "数据类型", "长度", "小数位", "标志", "主键", "允许空", "默认值", "说明"};

    public static void main(String[] args) throws SQLException {

        if(args.length<5){
            System.out.println("使用说明：\n" +
                    "    java -jar generator.jar url=jdbc:mysql://localhost:3306 user=root pass=1234 db=mydb out=/home/user/out.xlsx");
            return;
        }else{
            for (String arg : args) {
                if(arg.startsWith("url=")){
                    url = arg.substring(4);
                }else if(arg.startsWith("user=")){
                    user = arg.substring(5);
                }else if(arg.startsWith("pass=")){
                    pass = arg.substring(5);
                }else if(arg.startsWith("db=")){
                    db = arg.substring(3);
                }else if(arg.startsWith("out=")){
                    outfile = arg.substring(4);
                }else{
                    System.err.println("Invalid arg: "+ arg);
                    return;
                }
            }
        }
        AnalysisDatabase a = new AnalysisDatabase(url, user, pass);

        List<AnalysisDatabase.TableInfo> tablesInfo = a.getTableInfoByDatabase(db);
        for (AnalysisDatabase.TableInfo tableInfo : tablesInfo) {
            tableInfo.colInfos = a.getColInfoByTable(db, tableInfo.tableName);
            System.out.println("获取表"+ tableInfo.tableName +"成功, 列数: "+ tableInfo.colInfos.size());
        }
        createExcel(outfile, db,tablesInfo);
    }

    public static void createExcel(String fileDir, String databaseName, List<AnalysisDatabase.TableInfo> infos){

        //创建workbook
        XSSFWorkbook hWorkbook = new XSSFWorkbook();
        //新建文件
        FileOutputStream fileOutputStream = null;
        XSSFRow row = null;
        try {
            XSSFSheet sheet = hWorkbook.createSheet(databaseName);
            sheet.setColumnWidth(marginLeft+1, 6000);
            sheet.setColumnWidth(marginLeft+2, 4000);
            sheet.setColumnWidth(marginLeft+3, 3000);
            sheet.setColumnWidth(marginLeft+8, 5000);
            sheet.setColumnWidth(marginLeft+9, 10000);

            CellStyle centerStyle = hWorkbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle grayBackground = hWorkbook.createCellStyle();
            grayBackground.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            grayBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            grayBackground.setBorderBottom(BorderStyle.THIN);
            grayBackground.setBorderTop(BorderStyle.THIN);
            grayBackground.setBorderLeft(BorderStyle.THIN);
            grayBackground.setBorderRight(BorderStyle.THIN);

            CellStyle labelStyle = hWorkbook.createCellStyle();
            labelStyle.cloneStyleFrom(grayBackground);
            labelStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle fillStyle = hWorkbook.createCellStyle();
            fillStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            fillStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


            int startRow = marginTop;

            for (int i = 0; i < infos.size(); i++) {
                AnalysisDatabase.TableInfo tableInfo = infos.get(i);
                List<AnalysisDatabase.ColInfo> colInfos = tableInfo.colInfos;

                XSSFRow rowfill = sheet.createRow(startRow);
                XSSFCell cellfill = rowfill.createCell(marginLeft);
                cellfill.setCellStyle(fillStyle);
                sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,marginLeft, marginLeft+9));
                startRow++;

                // 表名行
                XSSFRow row1 = sheet.createRow(startRow);
                XSSFCell cell = row1.createCell(marginLeft);
                cell.setCellValue("表名");
                XSSFCell cell2 = row1.createCell(marginLeft+1);
                cell2.setCellValue(tableInfo.tableName + "("+ tableInfo.tableComment+")");
                cell2.setCellStyle(centerStyle);
                sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, marginLeft + 1, marginLeft + 9));
                startRow++;
                //表头行
                XSSFRow row2 = sheet.createRow(startRow++);
                for (int i1 = 0; i1 < fixedLabels.length; i1++) {
                    XSSFCell cell1 = row2.createCell(marginLeft + i1);
                    cell1.setCellValue(fixedLabels[i1]);
                    cell1.setCellStyle(labelStyle);
                }
                int j = 1;
                for (AnalysisDatabase.ColInfo colInfo : colInfos) {
                    XSSFRow row3 = sheet.createRow(startRow++);
                    int colIndex = marginLeft;
                    XSSFCell cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(j);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.colName);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.colType);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.length);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.decimalDigits);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.flag);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.primaryKey?"是":"否");
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.nullable?"是":"否");
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.defaultValue);
                    cell1.setCellStyle(grayBackground);

                    cell1 = row3.createCell(colIndex++);
                    cell1.setCellValue(colInfo.comment);
                    cell1.setCellStyle(grayBackground);

                    j++;
                }
                startRow++;
            }

            fileOutputStream = new FileOutputStream(fileDir);
            hWorkbook.write(fileOutputStream);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }



}

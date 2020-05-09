package com.yzmoe;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AnalysisDatabase  implements AutoCloseable{

    private Connection conn = null;
    
    public static class TableInfo{
        public String tableName;
        public String tableComment;
        public List<ColInfo> colInfos;
    }
    
    public static class ColInfo{
        public String colName;
        public String colType;
        public Integer length;
        public Integer decimalDigits;
        public String flag;
        public Boolean primaryKey;
        public Boolean nullable;
        public String defaultValue;
        public String comment;

        @Override
        public String toString() {
            return "ColInfo{" +
                    "colName='" + colName + '\'' +
                    ", colType='" + colType + '\'' +
                    ", length=" + length +
                    ", decimalDigits=" + decimalDigits +
                    ", flag='" + flag + '\'' +
                    ", primaryKey=" + primaryKey +
                    ", nullable=" + nullable +
                    ", defaultValue='" + defaultValue + '\'' +
                    ", comment='" + comment + '\'' +
                    '}';
        }
    }
    
    public AnalysisDatabase(String url,String user, String pass) throws SQLException {
        conn = java.sql.DriverManager.getConnection(url, user, pass);
    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *
     * @param database 查询的数据库，为空表示查询所有数据库
     * @return 表名以及表注释
     * @throws SQLException
     */
    public List<TableInfo> getTableInfoByDatabase(String database) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        ResultSet rs = dbMetaData.getTables(database, null, null,new String[] { "TABLE" });
        int cols = rs.getMetaData().getColumnCount();
        List<TableInfo> list= new ArrayList<>();
        while (rs.next()) {// ///TABLE_TYPE/REMARKS
            TableInfo tableInfo = new TableInfo();
            tableInfo.tableName = rs.getString("TABLE_NAME");
            tableInfo.tableComment = rs.getString("REMARKS");
            list.add(tableInfo);

//            for (int i = 1; i <= cols; i++) {
//                String name = rs.getMetaData().getColumnName(i);
//                String string = rs.getString(i);
//                System.out.println( name +": "+ string);
//            }
//            System.out.println("--------------------------");
        }
        return list;
    }
    
    public List<ColInfo> getColInfoByTable(String database,String tableName) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();
        ResultSet columns = dbMetaData.getColumns(database, null,tableName, null);

        ResultSet pks = dbMetaData.getPrimaryKeys(database, null, tableName);
        boolean next = pks.next();
        String pk = "";
        if(next){
            pk = pks.getString("COLUMN_NAME");
        }

        List<ColInfo> list = new ArrayList<>();
        while(columns.next()){

            ColInfo colInfo = new ColInfo();
            colInfo.colName= columns.getString("COLUMN_NAME");
            colInfo.colType = columns.getString("TYPE_NAME");
            colInfo.length = columns.getInt("COLUMN_SIZE");
            colInfo.decimalDigits = columns.getInt("DECIMAL_DIGITS");
            colInfo.nullable = "YES".equals(columns.getString("IS_NULLABLE"));
            colInfo.defaultValue = columns.getString("COLUMN_DEF");
            colInfo.comment = columns.getString("REMARKS");
            boolean autoincrement = "YES".equals( columns.getString("IS_AUTOINCREMENT"));
            colInfo.flag = autoincrement?"自增":"";
            colInfo.primaryKey = colInfo.colName.equals(pk);
            list.add(colInfo);
        }
        return list;
    }
    
}


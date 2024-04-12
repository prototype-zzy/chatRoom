package db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class DBOperator {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static String DB_URL;
    static String USER;
    static String PASS;
    private Connection conn;
    private Statement stmt;

    public DBOperator() throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(Paths.get("config.properties")));
        DB_URL = properties.getProperty("db");
        USER = properties.getProperty("dbuser");
        PASS = properties.getProperty("dbpassword");

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("Connecting database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
//            // 执行查询
//            System.out.println(" 实例化Statement对象...");
//            stmt = conn.createStatement();
//            String sql = "SELECT id, name, password FROM user";
//            ResultSet rs = stmt.executeQuery(sql);
//
//            // 展开结果集数据库
//            while(rs.next()){
//                // 通过字段检索
//                int id  = rs.getInt("id");
//                String name = rs.getString("name");
//                String pass = rs.getString("password");
//
//                // 输出数据
//                System.out.print("ID: " + id);
//                System.out.print("name: " + name);
//                System.out.print("password: " + pass);
//                System.out.print("\n");
//            }
//            // 完成后关闭
//            rs.close();
//            stmt.close();
//            conn.close();
        } catch(SQLException | ClassNotFoundException se){
            // 处理 JDBC 错误
            se.printStackTrace();
            System.err.println("ERROR Connect Database");
//        } finally{
//            // 关闭资源
//            try{
//                if(stmt!=null) stmt.close();
//            } catch(SQLException se2){
//            }// 什么都不做
//            try{
//                if(conn!=null) conn.close();
//            } catch(SQLException se){
//                se.printStackTrace();
//            }
        }
    }

    public User getUserByName(String name) {
        String sql = "SELECT id, name, password FROM user WHERE name = '" + name + "'";
        User user = null;
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int userId = rs.getInt("id");
                String pass = rs.getString("password");
                user = new User(userId, name, pass);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;

    }

    public List<History> getRecentHistory() {
        return getHistory(0, 30);
    }

    public List<History> getHistory(int pageNum, int pageSize){
        String sql = String.format("SELECT * FROM history ORDER BY time DESC LIMIT %d, %d;", (pageNum) * pageSize, pageSize);
        System.out.println("query for history");
        List<History> histories = new ArrayList<>(pageSize);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp time = rs.getTimestamp("time");
                String sender = rs.getString("sender");
                String content = rs.getString("content");
                histories.add(new History(id, time, sender, content));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Collections.reverse(histories);
        return histories;
    }

    public int insertHistory(String sender, String content) {
        String sql = String.format("INSERT INTO history (sender, content) VALUES ('%s', '%s')", sender, content);
        System.out.println("insert history");
        try {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

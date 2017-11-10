/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chattcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author andrea
 */
public class SQLHelper {

    private Connection connection;
    private Statement statement;

    public SQLHelper() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:ChatTcpDB.sqlite");
            this.statement = connection.createStatement();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public boolean login(String username, String password) {
        if (!username.equals("")) {
            try {
                String query = "select * from CredenzialiAccesso where username='" + username + "';";
                ResultSet rs = statement.executeQuery(query);
                if (rs.next()) {
                    String name = rs.getString("username");
                    String pass = rs.getString("password");
                    return name.equals(username) && password.equals(pass);
                } else {
                    return false;
                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                closeConnection();
            }
        }
        return false;
    }

    public boolean exist(String username) {
        if (!username.equals("")) {
            try {
                String query = "select * from CredenzialiAccesso where username='" + username + "';";
                ResultSet rs = statement.executeQuery(query);
                return rs.next();

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                closeConnection();
            }
        }
        return false;
    }

    public void addUser(String username, String password) {
        try {
            String sql = "insert into CredenzialiAccesso values('" + username + "','" + password + "')";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

}

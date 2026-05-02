package dao;

import db.DBConnection;
import java.sql.*;

public class UserDAO {

    public String authenticate(String username, String password) {
        String role = null;

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT role FROM users WHERE username=? AND password_hash=?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return role;
    }
}
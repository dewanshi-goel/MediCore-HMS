package dao;

import db.DBConnection;
import java.sql.*;
import java.util.*;

public class PatientDAO {

    // Returns list of "patient_id|name" strings matching the search term
    public List<String> searchByName(String query) {
        List<String> results = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT patient_id, name FROM patients WHERE name LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("patient_id") + "|" + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
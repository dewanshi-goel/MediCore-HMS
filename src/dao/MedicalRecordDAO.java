package dao;

import db.DBConnection;
import java.sql.*;
import java.util.*;

public class MedicalRecordDAO {

    // ✅ ADD RECORD
    public boolean addRecord(String recordId, String patientId, String doctorId,
                             String apptId, String diagnosis,
                             String prescription, String notes) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO medical_records VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE())";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, recordId);
            ps.setString(2, patientId);
            ps.setString(3, doctorId);
            ps.setString(4, apptId);
            ps.setString(5, diagnosis);
            ps.setString(6, prescription);
            ps.setString(7, notes);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ GET RECORDS BY PATIENT
    public List<String> getRecordsByPatient(String patientId) {

        List<String> records = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM medical_records WHERE patient_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, patientId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String rec = rs.getString("diagnosis") + " | "
                           + rs.getString("prescription") + " | "
                           + rs.getDate("created_at");
                records.add(rec);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }
}
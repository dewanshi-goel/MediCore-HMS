package dao;

import db.DBConnection;
import java.sql.*;

public class AppointmentDAO {

    public boolean bookAppointment(String apptId, String patientId, String doctorId, String datetime, String type) {

        try {
            Connection conn = DBConnection.getConnection();

            // Convert the incoming string "2026-05-01 10:30:00" to a proper Timestamp
            Timestamp ts = Timestamp.valueOf(datetime); // expects "yyyy-MM-dd HH:mm:ss"

            // STEP 1: CHECK FOR CONFLICT
            String checkSql = "SELECT * FROM appointments WHERE doctor_id=? AND appt_datetime=? AND status!='cancelled'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, doctorId);
            checkStmt.setTimestamp(2, ts);  // use setTimestamp, not setString

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false; // conflict
            }

            // STEP 2: INSERT
            String insertSql = "INSERT INTO appointments VALUES (?, ?, ?, ?, 'pending', ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, apptId);
            insertStmt.setString(2, patientId);
            insertStmt.setString(3, doctorId);
            insertStmt.setTimestamp(4, ts); // use setTimestamp here too
            insertStmt.setString(5, type);

            insertStmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
package server;

import com.sun.net.httpserver.*;

import dao.AppointmentDAO;
import dao.MedicalRecordDAO;
import dao.PatientDAO;
import dao.UserDAO;
import db.DBConnection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class MainServer {

    private static String fetchAppointments(String patientId, String doctorId) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql;
        PreparedStatement stmt;

        if (patientId != null) {
            sql = "SELECT a.appt_id, p.name, d.name, a.appt_datetime, a.status, a.type " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "WHERE a.patient_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, patientId);
        } else if (doctorId != null) {
            sql = "SELECT a.appt_id, p.name, d.name, a.appt_datetime, a.status, a.type " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "WHERE a.doctor_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, doctorId);
        } else {
            sql = "SELECT a.appt_id, p.name, d.name, a.appt_datetime, a.status, a.type " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id";
            stmt = conn.prepareStatement(sql);
        }

        ResultSet rs = stmt.executeQuery();
        StringBuilder sb = new StringBuilder();

        while (rs.next()) {
            sb.append(rs.getString(1)).append("|")
            .append(rs.getString(2)).append("|")
            .append(rs.getString(3)).append("|")
            .append(rs.getString(4)).append("|")
            .append(rs.getString(5)).append("|")
            .append(rs.getString(6)).append("##");
        }

        rs.close();
        stmt.close();
        return sb.toString();
    }
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // ── LOGIN ──
        server.createContext("/login", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());

                String[] parts = body.split("&");
                String username = parts[0].split("=")[1];
                String password = parts[1].split("=")[1];

                UserDAO dao = new UserDAO();
                String role = dao.authenticate(username, password);

                String response = (role != null) ? role : "invalid";

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        // ── BOOK APPOINTMENT ──
        server.createContext("/book", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());

                String[] parts = body.split("&");
                String patientId = URLDecoder.decode(parts[0].split("=")[1], "UTF-8");
                String doctorId  = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");
                String datetime  = URLDecoder.decode(parts[2].split("=")[1], "UTF-8");
                String type      = URLDecoder.decode(parts[3].split("=")[1], "UTF-8");

                System.out.println("Decoded datetime: '" + datetime + "'");

                String apptId = "A" + String.format("%06d", (int)(Math.random() * 1000000));

                AppointmentDAO dao = new AppointmentDAO();
                boolean success = dao.bookAppointment(apptId, patientId, doctorId, datetime, type);

                String response = success ? "success" : "conflict";

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        // ── ADD MEDICAL RECORD ──
        server.createContext("/addRecord", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());

                String patientId = "", doctorId = "", apptId = "";
                String diagnosis = "", prescription = "", notes = "";

                String[] params = body.split("&");
                for (String param : params) {
                    String[] keyVal = param.split("=");
                    if (keyVal.length < 2) continue;
                    String key   = keyVal[0];
                    String value = URLDecoder.decode(keyVal[1], "UTF-8");

                    if (key.equals("patientId"))    patientId    = value;
                    if (key.equals("doctorId"))     doctorId     = value;
                    if (key.equals("apptId"))       apptId       = value;
                    if (key.equals("diagnosis"))    diagnosis    = value;
                    if (key.equals("prescription")) prescription = value;
                    if (key.equals("notes"))        notes        = value;
                }

                String recordId = "R" + String.format("%03d", (int)(Math.random() * 1000));

                MedicalRecordDAO dao = new MedicalRecordDAO();
                boolean success = dao.addRecord(recordId, patientId, doctorId,
                                                apptId, diagnosis, prescription, notes);

                String response = success ? "success" : "error";

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        // ── GET MEDICAL RECORDS ──
        server.createContext("/getRecords", exchange -> {
            String query     = exchange.getRequestURI().getQuery();
            String patientId = query.split("=")[1];

            MedicalRecordDAO dao = new MedicalRecordDAO();
            List<String> records = dao.getRecordsByPatient(patientId);

            StringBuilder response = new StringBuilder();
            for (String rec : records) {
                response.append(rec.replace("\n", "\\n"));
                response.append("##");
            }

            String resp = response.toString();
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, resp.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        });

        // ── GET APPOINTMENTS ──
        server.createContext("/getAppointments", exchange -> {
            String query     = exchange.getRequestURI().getQuery();
            String patientId = null;
            String doctorId  = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] kv = param.split("=");
                    if (kv.length < 2) continue;
                    if (kv[0].equals("patientId")) patientId = URLDecoder.decode(kv[1], "UTF-8");
                    if (kv[0].equals("doctorId"))  doctorId  = URLDecoder.decode(kv[1], "UTF-8");
                }
            }

            final String finalDoctorId = doctorId;
            try {
                String resp = fetchAppointments(patientId, finalDoctorId);
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(resp.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String err = "error";
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, err.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(err.getBytes());
                os.close();
            }
        });

        // ── SEARCH PATIENT ──
        server.createContext("/searchPatient", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            String query = exchange.getRequestURI().getQuery();
            String searchTerm = "";

            if (query != null && query.startsWith("name=")) {
                searchTerm = URLDecoder.decode(query.split("=")[1], "UTF-8");
            }

            PatientDAO dao = new PatientDAO();
            List<String> patients = dao.searchByName(searchTerm);

            StringBuilder sb = new StringBuilder();
            for (String p : patients) {
                sb.append(p).append("##");
            }

            String resp = sb.toString();
            exchange.sendResponseHeaders(200, resp.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        });

        // ── GET ALL DOCTORS ──
        server.createContext("/getDoctors", exchange -> {
            try {
                Connection conn = DBConnection.getConnection();
                String sql = "SELECT doctor_id, name, specialization, phone, available_days FROM doctors";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("|")
                    .append(rs.getString(2)).append("|")
                    .append(rs.getString(3)).append("|")
                    .append(rs.getString(4)).append("|")
                    .append(rs.getString(5)).append("##");
                }
                rs.close();
                stmt.close();
                String resp = sb.toString();
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(resp.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String err = "error";
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, err.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(err.getBytes());
                os.close();
            }
        });

        // ── GET ALL PATIENTS ──
        server.createContext("/getPatients", exchange -> {
            try {
                Connection conn = DBConnection.getConnection();
                String sql = "SELECT patient_id, name, phone FROM patients";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("|")
                    .append(rs.getString(2)).append("|")
                    .append(rs.getString(3)).append("##");
                }
                rs.close();
                stmt.close();
                String resp = sb.toString();
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(resp.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String err = "error";
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, err.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(err.getBytes());
                os.close();
            }
        });

        // ── GET SLOTS ──
        server.createContext("/getSlots", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String doctorId = null, date = null;
                for (String param : query.split("&")) {
                    String[] kv = param.split("=");
                    if (kv.length < 2) continue;
                    if (kv[0].equals("doctorId")) doctorId = URLDecoder.decode(kv[1], "UTF-8");
                    if (kv[0].equals("date"))     date     = URLDecoder.decode(kv[1], "UTF-8");
                }

                Connection conn = DBConnection.getConnection();

                // Get doctor's available days
                String daysSql = "SELECT available_days FROM doctors WHERE doctor_id = ?";
                PreparedStatement daysStmt = conn.prepareStatement(daysSql);
                daysStmt.setString(1, doctorId);
                ResultSet daysRs = daysStmt.executeQuery();
                String availableDays = daysRs.next() ? daysRs.getString(1) : "";
                daysRs.close(); daysStmt.close();

                // Get already booked slots for that doctor on that date
                String slotsSql = "SELECT appt_datetime FROM appointments " +
                                "WHERE doctor_id = ? AND DATE(appt_datetime) = ? " +
                                "AND status != 'cancelled'";
                PreparedStatement slotsStmt = conn.prepareStatement(slotsSql);
                slotsStmt.setString(1, doctorId);
                slotsStmt.setString(2, date);
                ResultSet slotsRs = slotsStmt.executeQuery();

                StringBuilder bookedSlots = new StringBuilder();
                while (slotsRs.next()) {
                    String dt = slotsRs.getString(1); // "2026-06-10 10:00:00"
                    String time = dt.substring(11, 16); // "10:00"
                    bookedSlots.append(time).append(",");
                }
                slotsRs.close(); slotsStmt.close();

                String resp = availableDays + "|" + bookedSlots.toString();
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(resp.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String err = "error";
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, err.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(err.getBytes());
                os.close();
            }
        });

        // ── UPDATE APPOINTMENT STATUS ──
        server.createContext("/updateStatus", exchange -> {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                String apptId = null, status = null;
                for (String param : body.split("&")) {
                    String[] kv = param.split("=");
                    if (kv.length < 2) continue;
                    if (kv[0].equals("apptId"))  apptId = URLDecoder.decode(kv[1], "UTF-8");
                    if (kv[0].equals("status"))  status = URLDecoder.decode(kv[1], "UTF-8");
                }

                Connection conn = DBConnection.getConnection();
                String sql = "UPDATE appointments SET status = ? WHERE appt_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, status);
                stmt.setString(2, apptId);
                int rows = stmt.executeUpdate();
                stmt.close();

                String resp = rows > 0 ? "success" : "error";
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(resp.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String err = "error";
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, err.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(err.getBytes());
                os.close();
            }
        });

        server.start();
        System.out.println("Server started at http://localhost:8000");
    }
}
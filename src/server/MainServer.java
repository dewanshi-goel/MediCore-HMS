package server;

import com.sun.net.httpserver.*;

import dao.AppointmentDAO;
import dao.MedicalRecordDAO;
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

    // ── Moved outside lambda to avoid checked exception problem ──
    private static String fetchAppointments(String patientId) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = (patientId != null)
            ? "SELECT a.appt_id, p.name, d.name, a.appt_datetime, a.status, a.type " +
              "FROM appointments a " +
              "JOIN patients p ON a.patient_id = p.patient_id " +
              "JOIN doctors d ON a.doctor_id = d.doctor_id " +
              "WHERE a.patient_id = ?"
            : "SELECT a.appt_id, p.name, d.name, a.appt_datetime, a.status, a.type " +
              "FROM appointments a " +
              "JOIN patients p ON a.patient_id = p.patient_id " +
              "JOIN doctors d ON a.doctor_id = d.doctor_id";

        PreparedStatement stmt = conn.prepareStatement(sql);
        if (patientId != null) stmt.setString(1, patientId);

        ResultSet rs = stmt.executeQuery();
        StringBuilder sb = new StringBuilder();

        while (rs.next()) {
            sb.append(rs.getString(1)).append("|")   // appt_id
              .append(rs.getString(2)).append("|")   // patient name
              .append(rs.getString(3)).append("|")   // doctor name
              .append(rs.getString(4)).append("|")   // datetime
              .append(rs.getString(5)).append("|")   // status
              .append(rs.getString(6)).append("##"); // type
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
            String patientId = (query != null && query.contains("patientId"))
                               ? query.split("=")[1] : null;
            try {
                String resp = fetchAppointments(patientId);
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
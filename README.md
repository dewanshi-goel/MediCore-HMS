# 🏥 MediCore HMS — Hospital Management System

> A full-stack, database-driven Hospital Management System
> Manages patients, doctors, appointments, and medical records through a role-based web interface backed by a live MySQL database.

---

## 📋 Table of Contents
- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Role-Based Access](#role-based-access)
- [Features](#features)
- [Database Schema](#database-schema)
- [Setup Instructions](#setup-instructions)
- [Project Structure](#project-structure)
- [Key Implementation Details](#key-implementation-details)
- [Login Credentials](#login-credentials)

---

## 📌 About the Project

MediCore HMS is a three-tier hospital management application that simulates real hospital operations across three user roles — **Admin**, **Doctor**, and **Patient**.

It was built to demonstrate practical application of relational database concepts including:

- Schema design and normalization (3NF)
- Foreign key constraints and referential integrity
- Prepared statements and SQL injection prevention
- Conflict detection and transactional booking logic
- Role-based access control with session management

All data is stored and fetched live from MySQL — there is no hardcoded mock data in production flows.

---

## 🛠 Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Database | MySQL 8.0 | Data storage, constraints, relationships |
| Backend | Java (Core + JDBC) | Business logic, SQL execution, HTTP endpoints |
| HTTP Server | `com.sun.net.httpserver` | Lightweight built-in Java HTTP server |
| Frontend | HTML5 + CSS3 | UI layout, styling, responsive design |
| Scripting | Vanilla JavaScript | Fetch API, DOM manipulation, session handling |
| DB Driver | MySQL Connector/J | JDBC bridge between Java and MySQL |

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────┐
│                  PRESENTATION TIER                  │
│   index.html · admin.html · doctor.html             │
│   patient.html · style.css · script.js              │
└─────────────────────┬───────────────────────────────┘
                      │ HTTP Fetch API (POST/GET)
                      │ URL-encoded bodies
                      ▼
┌─────────────────────────────────────────────────────┐
│                    LOGIC TIER                       │
│   MainServer.java — port 8000                       │
│                                                     │
│   /login          /book            /getSlots        │
│   /addRecord      /getRecords      /updateStatus    │
│   /getAppointments                                  │
│   /searchPatient  /getDoctors      /getPatients     │
└─────────────────────┬───────────────────────────────┘
                      │ JDBC PreparedStatement
                      ▼
┌─────────────────────────────────────────────────────┐
│                     DATA TIER                       │
│   MySQL — hospital_db                               │
│   users · patients · doctors · appointments         │
│   medical_records · billing                         │
└─────────────────────────────────────────────────────┘
```

---

## 👥 Role-Based Access

```
                    ┌─────────────┐
                    │  LOGIN PAGE │
                    └──────┬──────┘
                           │ authenticated via MySQL
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌─────────────┐ ┌───────────────┐ ┌──────────────┐
    │    ADMIN    │ │    DOCTOR     │ │   PATIENT    │
    └──────┬──────┘ └───────┬───────┘ └──────┬───────┘
           │                │                 │
    ┌──────▼──────┐  ┌──────▼───────────┐  ┌──────▼──────┐
    │ • Dashboard │  │ • Appointments   │  │ • Book Appt │
    │ • Patients  │  │   (with manage)  │  │   (slots)   │
    │ • Doctors   │  │ • Write Rx       │  │ • My Records│
    │ • All Appts │  │ • Medical History│  └─────────────┘
    └─────────────┘  └──────────────────┘
```

---

## ✨ Features

### 🛡️ Admin
- Live dashboard — patient count, doctor count, and appointment count all fetched from MySQL in real time
- View all registered patients with ID, name, and phone
- View all doctors with specialization, phone, and available days
- View all appointments with color-coded status badges — Pending / Scheduled / Completed / Cancelled

### 👨‍⚕️ Doctor
- View all appointments assigned to them, fetched live from the database
- **Appointment lifecycle management**:
  - Accept pending appointments → status becomes `scheduled`
  - Cancel pending appointments → status becomes `cancelled`
  - Mark scheduled appointments as complete → status becomes `completed`
- **Write Prescription** — only available for `scheduled` appointments:
  - Search patients by name (live search with dropdown)
  - Fill structured record: diagnosis, medicines, do's, don'ts, investigations, follow-up, fees
- **Medical History** — view a patient's full record history as formatted cards, color-coded by medicine / do's / don'ts
- Filter appointments by patient name using the search bar
- Back button on all sub-pages to return to appointments

### 🧑 Patient
- **Smart appointment booking**:
  - Select a doctor — their available days are shown immediately
  - Date picker validates the selected day against the doctor's schedule — invalid days show an error
  - Time slots (10:00 AM – 5:00 PM, 30-min intervals) rendered as clickable buttons
  - Already-booked slots are greyed out and non-clickable
  - Booking creates a `pending` appointment awaiting doctor acceptance
- View all past medical records as formatted prescription cards with diagnosis, medicines, do's and don'ts

### 🔐 All Roles
- Secure login with role-based validation against MySQL
- Session stored in `sessionStorage` — cleared on logout
- Every page calls `requireLogin(role)` on load — mismatched or missing sessions redirect to login
- Toast notifications for all actions (success / error)
- Color-coded status badges throughout (amber = scheduled, green = completed, red = cancelled, blue = pending)

---

## 🗄 Database Schema

```sql
hospital_db
├── users           (user_id PK, username, password_hash, role ENUM['admin','doctor','patient'])
├── patients        (patient_id PK, user_id FK, name, dob, gender, blood_group, phone, address, doctor_id FK)
├── doctors         (doctor_id PK, user_id FK, name, specialization, phone, available_days)
├── appointments    (appt_id PK, patient_id FK, doctor_id FK, appt_datetime DATETIME,
│                    status ENUM['pending','scheduled','completed','cancelled'], type ENUM['regular','emergency'])
├── medical_records (record_id PK, patient_id FK, doctor_id FK, appt_id FK,
│                    diagnosis, prescription, notes, created_at DATE)
└── billing         (bill_id PK, patient_id FK, appt_id FK, amount DECIMAL, payment_status ENUM, generated_at DATE)
```

All tables are in **Third Normal Form (3NF)**.  
Foreign keys enforce referential integrity across all related tables.

---

## ⚙️ Setup Instructions

### Prerequisites
- JDK 17 or higher
- MySQL 8.0
- MySQL Workbench (or any MySQL client)
- VS Code with Live Server extension

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/HospitalMS.git
cd HospitalMS
```

### 2. Database Setup
Open MySQL Workbench and run in order:
```sql
-- Creates hospital_db and all 6 tables
source database/schema.sql

-- Inserts default users, doctors, patients, and appointments
source database/seed.sql
```

### 3. Configure DB Connection
Open `src/db/DBConnection.java` and update:
```java
private static final String URL  = "jdbc:mysql://localhost:3306/hospital_db";
private static final String USER = "root";          // your MySQL username
private static final String PASS = "yourpassword";  // your MySQL password
```

### 4. Add MySQL Connector
- Download `mysql-connector-j-x.x.x.jar` from [MySQL Downloads](https://dev.mysql.com/downloads/connector/j/)
- Place it inside the `lib/` folder

### 5. Compile and Run the Java Server

**Windows:**
```bash
javac -cp lib/mysql-connector-j.jar src/**/*.java -d out/
java -cp "lib/mysql-connector-j.jar;out" server.MainServer
```

**Mac / Linux:**
```bash
javac -cp lib/mysql-connector-j.jar src/**/*.java -d out/
java -cp "lib/mysql-connector-j.jar:out" server.MainServer
```

Server starts at `http://localhost:8000` — keep this terminal open.

### 6. Open the Frontend
- Open `index.html` with **VS Code Live Server** (right-click → Open with Live Server)
- Runs at `http://127.0.0.1:5500`

---

## 📁 Project Structure

```
HospitalMS/
├── src/
│   ├── server/
│   │   └── MainServer.java        # HTTP server — all API endpoints
│   ├── dao/
│   │   ├── UserDAO.java           # Authentication
│   │   ├── AppointmentDAO.java    # Booking + conflict detection
│   │   ├── MedicalRecordDAO.java  # Record insert + fetch
│   │   └── PatientDAO.java        # Patient search
│   └── db/
│       └── DBConnection.java      # JDBC singleton connection
├── web/
│   ├── index.html                 # Login page
│   ├── admin.html                 # Admin dashboard
│   ├── doctor.html                # Doctor dashboard
│   ├── patient.html               # Patient dashboard
│   ├── style.css                  # Shared stylesheet (maroon + beige theme)
│   └── script.js                  # Shared JS utilities
├── database/
│   ├── schema.sql                 # CREATE TABLE statements
│   └── seed.sql                   # Default data inserts
├── screenshots/                   # UI screenshots
├── .gitignore
└── README.md
```

---

## 🔍 Key Implementation Details

**Appointment Slot System**  
Patients cannot freely pick any date and time. The frontend fetches the selected doctor's `available_days` from the backend, validates the chosen date against those days client-side, then fetches already-booked slots for that date. Time slots are rendered as buttons — booked ones are greyed out. This prevents both invalid-day bookings and double-bookings.

**Appointment Lifecycle**  
New bookings start with status `pending`. The doctor must explicitly Accept (→ `scheduled`) or Cancel (→ `cancelled`) each request. After consultation, the doctor marks it Complete (→ `completed`). Write Prescription is only available on `scheduled` appointments.

**Double Booking Prevention**  
Before every insert, `AppointmentDAO` runs a `SELECT` to check for an existing non-cancelled appointment at the same `doctor_id` + `appt_datetime`. If found, the server returns `"conflict"` and no insert is attempted.

**Live Data Throughout**  
All three dashboards fetch data from MySQL at runtime — no hardcoded mock data remains in any production flow. Patient lists, doctor dropdowns, appointment tables, and record counts all reflect the live database state.

**SQL Injection Prevention**  
Every query uses `PreparedStatement` with `?` placeholders. No string concatenation is used in SQL anywhere in the codebase.

**URL Encoding**  
Prescription data contains special characters (`#`, newlines, `:`). All POST values are encoded with `encodeURIComponent()` on the frontend and decoded with `URLDecoder.decode()` in Java before processing.

**Session Management**  
After login, `{ username, role }` is stored in `sessionStorage`. Every page calls `requireLogin(expectedRole)` on load — missing or mismatched sessions redirect to login immediately.

**CORS**  
Every endpoint adds `Access-Control-Allow-Origin: *` before `sendResponseHeaders()` so the Live Server frontend (port 5500) can communicate freely with the Java backend (port 8000).

---

## 🔑 Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Doctor | `doctor1` | `doc123` |
| Patient | `patient1` | `pat123` |

---

## 👩‍💻 Developed By

**Dewanshi S. Goel**  
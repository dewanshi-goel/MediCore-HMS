CREATE DATABASE hospital_db;
USE hospital_db;

-- USERS
CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('admin','doctor','patient') NOT NULL
);

-- PATIENTS
CREATE TABLE patients (
  patient_id VARCHAR(10) PRIMARY KEY,
  user_id INT,
  name VARCHAR(100),
  dob DATE,
  gender VARCHAR(10),
  blood_group VARCHAR(5),
  phone VARCHAR(15) UNIQUE,
  address TEXT,
  doctor_id VARCHAR(10),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- DOCTORS
CREATE TABLE doctors (
  doctor_id VARCHAR(10) PRIMARY KEY,
  user_id INT,
  name VARCHAR(100),
  specialization VARCHAR(100),
  phone VARCHAR(15),
  available_days VARCHAR(50),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- APPOINTMENTS
CREATE TABLE appointments (
  appt_id VARCHAR(10) PRIMARY KEY,
  patient_id VARCHAR(10),
  doctor_id VARCHAR(10),
  appt_datetime DATETIME,
  status ENUM('scheduled','completed','cancelled'),
  type ENUM('regular','emergency'),
  FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- MEDICAL RECORDS
CREATE TABLE medical_records (
  record_id VARCHAR(10) PRIMARY KEY,
  patient_id VARCHAR(10),
  doctor_id VARCHAR(10),
  appt_id VARCHAR(10),
  diagnosis TEXT,
  prescription TEXT,
  notes TEXT,
  created_at DATE,
  FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- BILLING
CREATE TABLE billing (
  bill_id VARCHAR(10) PRIMARY KEY,
  patient_id VARCHAR(10),
  appt_id VARCHAR(10),
  amount DECIMAL(10,2),
  payment_status ENUM('paid','pending'),
  generated_at DATE,
  FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);
# MediCore HMS — Hospital Management System

A full-stack DBMS project built with Java (Core + JDBC), MySQL, and HTML/CSS/JS.

## Setup Instructions

### 1. Database Setup
- Open MySQL Workbench
- Run the file `database/schema.sql` — this creates the database and all tables
- Run `database/seed.sql` — this inserts the default users, doctor, and patient

### 2. Backend Setup
- Make sure JDK 17+ is installed
- Add `mysql-connector-j.jar` to the `lib/` folder
- Compile: `javac -cp lib/mysql-connector-j.jar src/**/*.java`
- Run: `java -cp lib/mysql-connector-j.jar:src server.MainServer`

### 3. Frontend Setup
- Open `web/index.html` in a browser (or use VS Code Live Server on port 5500)
- Java server must be running on port 8000

## Credentials
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Doctor | doctor1 | doc123 |
| Patient | patient1 | pat123 |
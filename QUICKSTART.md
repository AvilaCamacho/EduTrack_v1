# Quick Start Guide - EduTrack v1

## 5-Minute Setup

### Prerequisites
- Java 17+ installed
- Maven installed
- Oracle Cloud database created

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/AvilaCamacho/EduTrack_v1.git
   cd EduTrack_v1
   ```

2. **Add Oracle Wallet**
   - Download your Oracle Cloud wallet
   - Extract all files to: `src/main/resources/wallet/`

3. **Configure Database**
   Edit `src/main/resources/database.properties`:
   ```properties
   db.username=ADMIN
   db.password=your_password
   db.tns.alias=your_dbname_high
   ```

4. **Setup Database Schema**
   ```bash
   sqlplus ADMIN/your_password@your_dbname_high @database_setup.sql
   ```
   Or use Oracle Cloud Console SQL Developer Web

5. **Run the Application**
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

6. **Login**
   - Teacher: `maestro1` / `password123`
   - Student: `alumno1` / `password123`

## Default Test Users

### Teacher Account
```
Username: maestro1
Password: password123
```

### Student Accounts
```
Username: alumno1 | alumno2 | alumno3
Password: password123 (all)
```

## Key Features Quick Access

### As Teacher
1. Create Group → "Crear Grupo" button
2. Add Students → Select group → "Agregar Alumno"
3. Take Attendance → Select group → "Pasar Lista"
4. Remove Student → Select student → "Eliminar Alumno"

### As Student
1. View Groups → Left panel (automatic)
2. View Attendance → Right panel (automatic)

## Common Commands

```bash
# Compile only
mvn clean compile

# Run application
mvn javafx:run

# Create package
mvn clean package

# Clean build artifacts
mvn clean
```

## Architecture Overview

```
EduTrack_v1/
├── Model Layer
│   ├── User (Teacher/Student)
│   ├── Group
│   ├── GroupStudent
│   └── Attendance
├── Database Layer (DAO)
│   ├── DatabaseConnection (Oracle Wallet)
│   ├── UserDAO
│   ├── GroupDAO
│   ├── GroupStudentDAO
│   └── AttendanceDAO
├── Controller Layer
│   ├── LoginController
│   ├── TeacherDashboardController
│   └── StudentDashboardController
└── View Layer (FXML + CSS)
    ├── Login.fxml
    ├── TeacherDashboard.fxml
    ├── StudentDashboard.fxml
    └── style.css
```

## Database Schema

```sql
USERS
├── id (PK)
├── username (UNIQUE)
├── password
├── user_type (TEACHER/STUDENT)
├── full_name
└── email

GROUPS
├── id (PK)
├── name
├── description
├── teacher_id (FK → USERS)
└── created_date

GROUP_STUDENTS
├── id (PK)
├── group_id (FK → GROUPS)
├── student_id (FK → USERS)
└── enrolled_date

ATTENDANCE
├── id (PK)
├── group_id (FK → GROUPS)
├── student_id (FK → USERS)
├── attendance_date
└── present (0/1)
```

## Next Steps

1. Change default passwords
2. Add more users
3. Customize CSS colors
4. Configure database backup
5. Review security settings

For detailed instructions, see:
- `README.md` - Complete documentation (English)
- `GUIA_INSTALACION.md` - Complete guide (Spanish)

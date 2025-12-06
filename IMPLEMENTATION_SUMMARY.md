# EduTrack v1 - Implementation Summary

## 🎯 Project Overview

**EduTrack** is a complete desktop application built with JavaFX that connects to Oracle Cloud Database for educational management.

### Key Technologies
- **Java 17** - Programming language
- **JavaFX 21.0.1** - UI Framework
- **Oracle Cloud Database** - Cloud-based database with wallet authentication
- **Maven** - Build and dependency management
- **CSS** - Custom styling

---

## ✅ Implemented Features

### 🔐 Authentication System
- Login screen with username/password authentication
- Two user types: Teacher (Maestro) and Student (Alumno)
- Session management with singleton pattern
- Automatic redirection to appropriate dashboard

### 👨‍🏫 Teacher Dashboard
Complete functionality for educators:
- ✅ **Create Groups**: Teachers can create unlimited groups with name and description
- ✅ **Delete Groups**: Remove groups that are no longer needed
- ✅ **Add Students**: Add students from the system to any group
- ✅ **Remove Students**: Remove students from groups
- ✅ **Take Attendance**: Pass list with checkbox interface for each student
- ✅ **View Students**: See all students enrolled in each group

### 👨‍🎓 Student Dashboard
View-only interface for students:
- ✅ **View Groups**: See all groups the student is enrolled in
- ✅ **View Attendance**: See complete attendance history with dates and status

---

## 📁 Project Structure

```
EduTrack_v1/
│
├── src/main/java/com/edutrack/
│   ├── Main.java                           # Application entry point
│   │
│   ├── controller/                         # UI Controllers
│   │   ├── LoginController.java           # Login screen logic
│   │   ├── TeacherDashboardController.java # Teacher functionality
│   │   └── StudentDashboardController.java # Student functionality
│   │
│   ├── database/                           # Data Access Layer
│   │   ├── DatabaseConnection.java        # Oracle Cloud wallet connection
│   │   ├── UserDAO.java                   # User data operations
│   │   ├── GroupDAO.java                  # Group data operations
│   │   ├── GroupStudentDAO.java           # Group-Student relationships
│   │   └── AttendanceDAO.java             # Attendance tracking
│   │
│   ├── model/                              # Domain Models
│   │   ├── User.java                      # User entity (Teacher/Student)
│   │   ├── Group.java                     # Group entity
│   │   ├── GroupStudent.java              # Many-to-many relationship
│   │   └── Attendance.java                # Attendance records
│   │
│   └── util/                               # Utilities
│       └── SessionManager.java            # User session management
│
├── src/main/resources/
│   ├── fxml/                               # UI Layouts
│   │   ├── Login.fxml                     # Login screen
│   │   ├── TeacherDashboard.fxml          # Teacher interface
│   │   └── StudentDashboard.fxml          # Student interface
│   │
│   ├── css/
│   │   └── style.css                      # Custom styling
│   │
│   ├── wallet/                             # Oracle Cloud Wallet
│   │   └── README.md                      # Wallet setup instructions
│   │
│   └── database.properties                 # Database configuration
│
├── database_setup.sql                      # Database schema and sample data
├── pom.xml                                 # Maven configuration
├── README.md                               # English documentation
├── GUIA_INSTALACION.md                     # Spanish setup guide
└── QUICKSTART.md                           # Quick reference
```

---

## 🗄️ Database Schema

### USERS Table
Stores all system users (teachers and students)
- `id` (Primary Key)
- `username` (Unique)
- `password`
- `user_type` (TEACHER/STUDENT)
- `full_name`
- `email`
- `created_date`

### GROUPS Table
Stores class/group information
- `id` (Primary Key)
- `name`
- `description`
- `teacher_id` (Foreign Key → USERS)
- `created_date`

### GROUP_STUDENTS Table
Many-to-many relationship between groups and students
- `id` (Primary Key)
- `group_id` (Foreign Key → GROUPS)
- `student_id` (Foreign Key → USERS)
- `enrolled_date`

### ATTENDANCE Table
Records daily attendance
- `id` (Primary Key)
- `group_id` (Foreign Key → GROUPS)
- `student_id` (Foreign Key → USERS)
- `attendance_date`
- `present` (Boolean: 1=Present, 0=Absent)

---

## 🎨 User Interface Design

### Color Scheme
- **Primary Gradient**: Purple to Blue (#667eea → #764ba2)
- **Success/Primary Actions**: Green (#4CAF50)
- **Danger/Delete Actions**: Red (#e74c3c)
- **Info Actions**: Blue (#3498db)
- **Background**: Light gray (#f5f5f5)
- **Text**: Dark gray (#333)

### UI Components
- Custom styled buttons with hover effects
- Responsive tables and lists
- Modern card-based layout
- Gradient headers
- Shadow effects for depth

---

## 🔧 Technical Implementation

### Design Patterns Used
1. **Singleton Pattern**: DatabaseConnection, SessionManager
2. **DAO Pattern**: All database operations
3. **MVC Pattern**: Separation of Model, View (FXML), Controller

### Database Connection
- Uses Oracle Cloud wallet for secure authentication
- Connection pooling ready
- Automatic reconnection on connection loss
- TNS-based connection string

### Security Features
- Password-based authentication
- Session management
- Wallet-based database encryption
- SQL injection prevention (PreparedStatements)

---

## 📦 Dependencies

### Core Dependencies
```xml
<!-- JavaFX -->
- javafx-controls: 21.0.1
- javafx-fxml: 21.0.1

<!-- Oracle JDBC -->
- ojdbc8: 21.9.0.0
- oraclepki: 21.9.0.0
- osdt_cert: 21.9.0.0
- osdt_core: 21.9.0.0
```

---

## 🚀 Quick Start Commands

```bash
# Compile the project
mvn clean compile

# Run the application
mvn javafx:run

# Package the application
mvn clean package

# The executable JAR will be in:
# target/edutrack-v1-1.0-SNAPSHOT.jar
```

---

## 📋 Default Test Users

### Teacher Account
```
Username: maestro1
Password: password123
Full Name: Prof. Juan García
Email: juan.garcia@edutrack.com
```

### Student Accounts
```
Username: alumno1
Password: password123
Full Name: María López

Username: alumno2
Password: password123
Full Name: Carlos Rodríguez

Username: alumno3
Password: password123
Full Name: Ana Martínez
```

---

## 🔄 Application Flow

```
1. Application Start
   ↓
2. Database Configuration (wallet + credentials)
   ↓
3. Login Screen
   ↓
4. Authentication
   ↓
5a. Teacher Dashboard              5b. Student Dashboard
    - Manage Groups                    - View Groups
    - Manage Students                  - View Attendance
    - Take Attendance
```

---

## 📝 Setup Requirements

### Required Files in `src/main/resources/wallet/`
1. `cwallet.sso` - Oracle Cloud wallet
2. `ewallet.p12` - Encrypted wallet
3. `tnsnames.ora` - TNS connection definitions
4. `sqlnet.ora` - SQL*Net configuration
5. `ojdbc.properties` - JDBC properties
6. Certificate files (truststore.jks, keystore.jks)

### Required Configuration
Update `src/main/resources/database.properties`:
```properties
db.wallet.path=src/main/resources/wallet
db.username=YOUR_USERNAME
db.password=YOUR_PASSWORD
db.tns.alias=YOUR_TNS_ALIAS
```

---

## ✨ Key Features Highlights

### For Teachers
- **Intuitive Group Management**: Create and organize students into groups
- **Easy Attendance**: Simple checkbox interface for taking attendance
- **Student Management**: Add/remove students with a few clicks
- **Real-time Updates**: All changes reflect immediately in the UI

### For Students
- **Clear Overview**: See all enrolled groups at a glance
- **Attendance History**: Track attendance records with dates
- **Read-Only Interface**: Simple, distraction-free design

### For Administrators
- **Secure Authentication**: Oracle Cloud wallet encryption
- **Scalable Architecture**: Clean separation of concerns
- **Easy Maintenance**: Well-documented code and structure
- **Database Integrity**: Foreign key constraints and validation

---

## 📚 Documentation Files

1. **README.md** - Complete English documentation
2. **GUIA_INSTALACION.md** - Detailed Spanish setup guide
3. **QUICKSTART.md** - Quick reference guide
4. **database_setup.sql** - Database initialization script
5. **src/main/resources/wallet/README.md** - Wallet setup instructions

---

## 🎓 Learning Resources

The code includes examples of:
- JavaFX UI design with FXML
- Oracle Cloud database integration
- DAO pattern implementation
- Session management
- CSS styling for JavaFX
- Maven project structure
- Oracle wallet authentication

---

## 🔐 Security Considerations

### Implemented
- ✅ Oracle Cloud wallet authentication
- ✅ Prepared statements (SQL injection prevention)
- ✅ Session management
- ✅ Wallet files excluded from git (.gitignore)

### Recommended for Production
- ⚠️ Encrypt passwords in database (currently plain text)
- ⚠️ Implement password strength requirements
- ⚠️ Add session timeout
- ⚠️ Implement audit logging
- ⚠️ Add rate limiting for login attempts

---

## 📈 Future Enhancements

Potential features for future versions:
- Export attendance to CSV/Excel
- Email notifications
- Multi-language support
- Report generation
- Student performance analytics
- Parent portal
- Mobile app version
- Cloud file storage integration

---

## 🏆 Project Status

**Status**: ✅ **COMPLETE AND READY FOR DEPLOYMENT**

All core requirements have been implemented:
- ✅ JavaFX desktop application
- ✅ Oracle Cloud database connection via wallet
- ✅ Login system with two user types
- ✅ Different interfaces for Teacher and Student
- ✅ Teacher features (groups, students, attendance)
- ✅ CSS styling
- ✅ Complete documentation

---

## 📞 Support

For questions or issues:
1. Check the documentation files
2. Review the database_setup.sql script
3. Verify Oracle Cloud wallet configuration
4. Ensure Java 17+ and Maven are installed

---

## 📄 License

This project is open source and available under the MIT License.

---

**Built with ❤️ for educational management**

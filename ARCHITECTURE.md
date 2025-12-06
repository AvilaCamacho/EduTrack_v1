# EduTrack v1 - Architecture Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌───────────────────┐  ┌─────────────────┐ │
│  │   Login.fxml │  │ TeacherDashboard  │  │ StudentDashboard│ │
│  │              │  │      .fxml        │  │     .fxml       │ │
│  │  [Username]  │  │                   │  │                 │ │
│  │  [Password]  │  │  ┌─────────────┐  │  │  ┌───────────┐ │ │
│  │  [  Login  ] │  │  │   Groups    │  │  │  │  Groups   │ │ │
│  │              │  │  │  ListView   │  │  │  │ ListView  │ │ │
│  └──────────────┘  │  └─────────────┘  │  │  └───────────┘ │ │
│         │          │  ┌─────────────┐  │  │  ┌───────────┐ │ │
│         │          │  │  Students   │  │  │  │Attendance │ │ │
│         │          │  │ TableView   │  │  │  │TableView  │ │ │
│         │          │  └─────────────┘  │  │  └───────────┘ │ │
│         │          │  [Create] [Add]   │  │                 │ │
│         │          │  [Delete] [List]  │  │                 │ │
│         │          └───────────────────┘  └─────────────────┘ │
│         │                    │                      │          │
└─────────┼────────────────────┼──────────────────────┼──────────┘
          │                    │                      │
          │              style.css (Styling)          │
          │                    │                      │
┌─────────▼────────────────────▼──────────────────────▼──────────┐
│                        CONTROLLER LAYER                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ LoginController  │  │TeacherDashboard  │  │StudentDashb. │ │
│  │                  │  │   Controller     │  │  Controller  │ │
│  │ - handleLogin()  │  │- handleCreate    │  │- loadGroups()│ │
│  │ - openDashboard()│  │  Group()         │  │- loadAttend. │ │
│  │                  │  │- handleAdd       │  │              │ │
│  │                  │  │  Student()       │  │              │ │
│  │                  │  │- handleTake      │  │              │ │
│  │                  │  │  Attendance()    │  │              │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
│           │                     │                     │         │
└───────────┼─────────────────────┼─────────────────────┼─────────┘
            │                     │                     │
            │         ┌───────────▼─────────┐           │
            │         │  SessionManager     │           │
            └────────►│  (Singleton)        │◄──────────┘
                      │  - currentUser      │
                      │  - setCurrentUser() │
                      │  - getCurrentUser() │
                      └─────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                          DATA ACCESS LAYER                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐   │
│  │   UserDAO    │  │   GroupDAO   │  │ GroupStudentDAO    │   │
│  │              │  │              │  │                    │   │
│  │ authenticate │  │getGroupsBy   │  │getStudentsByGroup │   │
│  │   ()         │  │  Teacher()   │  │   ()              │   │
│  │getAllStudents│  │getGroupsBy   │  │addStudentToGroup  │   │
│  │   ()         │  │  Student()   │  │   ()              │   │
│  │getUserById() │  │createGroup() │  │removeStudentFrom  │   │
│  │              │  │deleteGroup() │  │  Group()          │   │
│  └──────────────┘  └──────────────┘  └────────────────────┘   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │               AttendanceDAO                              │  │
│  │                                                          │  │
│  │  getAttendanceByGroup() | getAttendanceByStudent()      │  │
│  │  recordAttendance()     | updateAttendance()            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            │                                    │
└────────────────────────────┼────────────────────────────────────┘
                             │
                  ┌──────────▼──────────┐
                  │  DatabaseConnection │
                  │    (Singleton)      │
                  │                     │
                  │ - getConnection()   │
                  │ - configure()       │
                  │ - connect()         │
                  │ - disconnect()      │
                  └──────────┬──────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                        DATABASE LAYER                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│               ┌─────────────────────────────┐                  │
│               │   Oracle Cloud Database     │                  │
│               │   (Autonomous Database)     │                  │
│               └─────────────────────────────┘                  │
│                             │                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │  USERS   │  │  GROUPS  │  │  GROUP_  │  │ATTENDANCE│      │
│  │          │  │          │  │ STUDENTS │  │          │      │
│  │ id       │  │ id       │  │ id       │  │ id       │      │
│  │ username │  │ name     │  │ group_id │  │ group_id │      │
│  │ password │  │ desc     │  │ student  │  │ student  │      │
│  │ user_type│  │ teacher  │  │   _id    │  │   _id    │      │
│  │ fullname │  │   _id    │  │ enrolled │  │ date     │      │
│  │ email    │  │ created  │  │   _date  │  │ present  │      │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                             ▲
                             │
                   ┌─────────┴─────────┐
                   │  Oracle Wallet    │
                   │  Authentication   │
                   │                   │
                   │ - cwallet.sso     │
                   │ - ewallet.p12     │
                   │ - tnsnames.ora    │
                   │ - sqlnet.ora      │
                   │ - certificates    │
                   └───────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                          MODEL LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐        │
│  │    User     │  │    Group    │  │  GroupStudent   │        │
│  │             │  │             │  │                 │        │
│  │ + id        │  │ + id        │  │ + id            │        │
│  │ + username  │  │ + name      │  │ + groupId       │        │
│  │ + password  │  │ + desc      │  │ + studentId     │        │
│  │ + userType  │  │ + teacherId │  │ + studentName   │        │
│  │ + fullName  │  │ + created   │  │                 │        │
│  │ + email     │  │             │  │                 │        │
│  └─────────────┘  └─────────────┘  └─────────────────┘        │
│                                                                 │
│  ┌─────────────────────────────────────────────────┐           │
│  │              Attendance                         │           │
│  │                                                 │           │
│  │ + id          + attendanceDate                 │           │
│  │ + groupId     + present (boolean)              │           │
│  │ + studentId   + studentName                    │           │
│  │ + getStatus() returns "Presente"/"Ausente"     │           │
│  └─────────────────────────────────────────────────┘           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagrams

### Login Flow
```
User Input (Username/Password)
    │
    ▼
LoginController.handleLogin()
    │
    ▼
UserDAO.authenticate()
    │
    ▼
DatabaseConnection.getConnection()
    │
    ▼
Oracle Cloud Database (via Wallet)
    │
    ▼
User Object (or null)
    │
    ▼
SessionManager.setCurrentUser()
    │
    ▼
Load Dashboard (Teacher or Student)
```

### Teacher: Take Attendance Flow
```
Teacher selects Group
    │
    ▼
TeacherDashboardController.handleTakeAttendance()
    │
    ▼
GroupStudentDAO.getStudentsByGroup()
    │
    ▼
Display CheckBox List
    │
    ▼
Teacher marks attendance (checkboxes)
    │
    ▼
AttendanceDAO.recordAttendance() (for each student)
    │
    ▼
Oracle Cloud Database
    │
    ▼
Success Message
```

### Student: View Attendance Flow
```
Student logs in
    │
    ▼
StudentDashboardController.initialize()
    │
    ▼
AttendanceDAO.getAttendanceByStudent()
    │
    ▼
Oracle Cloud Database
    │
    ▼
List<Attendance>
    │
    ▼
Display in TableView (Date, Status)
```

## Component Dependencies

```
Main.java
  │
  ├─► DatabaseConnection (configure)
  │
  └─► Login.fxml
        │
        └─► LoginController
              │
              ├─► UserDAO
              │     └─► DatabaseConnection
              │
              └─► SessionManager
                    │
                    ├─► TeacherDashboard.fxml
                    │     │
                    │     └─► TeacherDashboardController
                    │           │
                    │           ├─► GroupDAO
                    │           ├─► GroupStudentDAO
                    │           ├─► AttendanceDAO
                    │           └─► UserDAO
                    │
                    └─► StudentDashboard.fxml
                          │
                          └─► StudentDashboardController
                                │
                                ├─► GroupDAO
                                └─► AttendanceDAO
```

## Technology Stack

```
┌─────────────────────────────────────────┐
│         Application Layer               │
│                                         │
│  Java 17 + JavaFX 21.0.1 + Maven       │
└─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│         Middleware Layer                │
│                                         │
│  Oracle JDBC 21.9.0.0                  │
│  Oracle PKI (Wallet Support)           │
└─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│         Database Layer                  │
│                                         │
│  Oracle Autonomous Database (Cloud)     │
└─────────────────────────────────────────┘
```

## Security Architecture

```
Application
    │
    ▼
Oracle Wallet Files (Local)
    │
    ├─► cwallet.sso (Secure credentials)
    ├─► ewallet.p12 (Encrypted wallet)
    ├─► Certificates (SSL/TLS)
    └─► tnsnames.ora (Connection config)
    │
    ▼
Encrypted Connection (SSL/TLS)
    │
    ▼
Oracle Cloud Database
    │
    └─► Autonomous Database (Encrypted at rest)
```

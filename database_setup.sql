-- EduTrack Database Schema
-- Oracle Cloud Database Setup Script

-- NOTE: Este archivo contiene el DDL para crear las tablas usadas por la aplicación
-- y al final incluye un script de migración (respaldo + normalización + deduplicación + índices)
-- Ejecuta en un entorno de pruebas primero. HAZ BACKUP antes de producción.

-- Create Users table
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    user_type VARCHAR2(20) NOT NULL CHECK (user_type IN ('TEACHER', 'STUDENT')),
    full_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    created_date DATE DEFAULT SYSDATE
);

-- Create sequence for users
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER users_bir
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
        SELECT users_seq.NEXTVAL INTO :new.id FROM dual;
    END IF;
END;
/

-- Create Groups table (typo corrected: use VARCHAR2)
CREATE TABLE groups (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    description VARCHAR2(500),
    teacher_id NUMBER NOT NULL,
    created_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create sequence for groups
CREATE SEQUENCE groups_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER groups_bir
BEFORE INSERT ON groups
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
        SELECT groups_seq.NEXTVAL INTO :new.id FROM dual;
    END IF;
END;
/

-- Create Group Students table (many-to-many relationship)
CREATE TABLE group_students (
    id NUMBER PRIMARY KEY,
    group_id NUMBER NOT NULL,
    student_id NUMBER NOT NULL,
    enrolled_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_group_student UNIQUE (group_id, student_id)
);

-- Create sequence for group_students
CREATE SEQUENCE group_students_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER group_students_bir
BEFORE INSERT ON group_students
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
        SELECT group_students_seq.NEXTVAL INTO :new.id FROM dual;
    END IF;
END;
/

-- Create Attendance table
CREATE TABLE attendance (
    id NUMBER PRIMARY KEY,
    group_id NUMBER NOT NULL,
    student_id NUMBER NOT NULL,
    attendance_date DATE DEFAULT SYSDATE,
    present NUMBER(1) DEFAULT 1 CHECK (present IN (0, 1)),
    CONSTRAINT fk_attendance_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create sequence for attendance
CREATE SEQUENCE attendance_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER attendance_bir
BEFORE INSERT ON attendance
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
        SELECT attendance_seq.NEXTVAL INTO :new.id FROM dual;
    END IF;
END;
/

-- Insert sample data
-- Sample Teacher
INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('maestro1', 'password123', 'TEACHER', 'Prof. Juan García', 'juan.garcia@edutrack.com');

-- Sample Students
INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('alumno1', 'password123', 'STUDENT', 'María López', 'maria.lopez@edutrack.com');

INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('alumno2', 'password123', 'STUDENT', 'Carlos Rodríguez', 'carlos.rodriguez@edutrack.com');

INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('alumno3', 'password123', 'STUDENT', 'Ana Martínez', 'ana.martinez@edutrack.com');

COMMIT;


/* ------------------------------------------------------------------
   MIGRATION: respaldo + normalización + deduplicación + constraints
   Ejecuta estos bloques en orden en un entorno de pruebas antes de producción.
   El script asume que la tabla `attendance` existe (creada arriba) y que
   tienes privilegios para crear tablas, índices y alterar tablas.
   ------------------------------------------------------------------ */

-- EduTrack Database Schema
-- Oracle Cloud Database Setup Script

-- Create Users table
CREATE TABLE users (
                       id NUMBER PRIMARY KEY,
                       username VARCHAR2(50) UNIQUE NOT NULL,
                       password VARCHAR2(255) NOT NULL,
                       user_type VARCHAR2(20) NOT NULL CHECK (user_type IN ('TEACHER', 'STUDENT')),
                       full_name VARCHAR2(100) NOT NULL,
                       email VARCHAR2(100) UNIQUE NOT NULL,
                       created_date DATE DEFAULT SYSDATE
);

-- Create sequence for users
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER users_bir
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
SELECT users_seq.NEXTVAL INTO :new.id FROM dual;
END IF;
END;
/

-- Create Groups table
CREATE TABLE groups (
                        id NUMBER PRIMARY KEY,
                        name VARCHAR2(100) NOT NULL,
                        description VARCHAR2(500),
                        teacher_id NUMBER NOT NULL,
                        created_date DATE DEFAULT SYSDATE,
                        CONSTRAINT fk_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create sequence for groups
CREATE SEQUENCE groups_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER groups_bir
BEFORE INSERT ON groups
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
SELECT groups_seq.NEXTVAL INTO :new.id FROM dual;
END IF;
END;
/

-- Create Group Students table (many-to-many relationship)
CREATE TABLE group_students (
                                id NUMBER PRIMARY KEY,
                                group_id NUMBER NOT NULL,
                                student_id NUMBER NOT NULL,
                                enrolled_date DATE DEFAULT SYSDATE,
                                CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
                                CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                                CONSTRAINT unique_group_student UNIQUE (group_id, student_id)
);

-- Create sequence for group_students
CREATE SEQUENCE group_students_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER group_students_bir
BEFORE INSERT ON group_students
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
SELECT group_students_seq.NEXTVAL INTO :new.id FROM dual;
END IF;
END;
/

-- Create Attendance table
CREATE TABLE attendance (
                            id NUMBER PRIMARY KEY,
                            group_id NUMBER NOT NULL,
                            student_id NUMBER NOT NULL,
                            attendance_date DATE DEFAULT SYSDATE,
                            present NUMBER(1) DEFAULT 1 CHECK (present IN (0, 1)),
                            CONSTRAINT fk_attendance_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
                            CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create sequence for attendance
CREATE SEQUENCE attendance_seq START WITH 1 INCREMENT BY 1;

-- Create trigger for auto-increment
CREATE OR REPLACE TRIGGER attendance_bir
BEFORE INSERT ON attendance
FOR EACH ROW
BEGIN
    IF :new.id IS NULL THEN
SELECT attendance_seq.NEXTVAL INTO :new.id FROM dual;
END IF;
END;
/

-- Insert sample data
-- Sample Teacher
INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('maestro1', 'password123', 'TEACHER', 'Prof. Juan García', 'juan.garcia@edutrack.com');

-- Sample Students
INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('alumno1', 'password123', 'STUDENT', 'María López', 'maria.lopez@edutrack.com');

INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('alumno2', 'password123', 'STUDENT', 'Carlos Rodríguez', 'carlos.rodriguez@edutrack.com');

INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('alumno3', 'password123', 'STUDENT', 'Ana Martínez', 'ana.martinez@edutrack.com');

COMMIT;
 -- MIGRATION COMPLETA. Revisa resultados y borra attendance_backup solo cuando confirmes.

/*
Opcional: si prefieres no añadir una constraint y en su lugar usar un índice único por función,
puedes comentar el ALTER TABLE anterior y descomentar la siguiente instrucción.
Esto requiere que no existan duplicados al momento de crear el índice.

-- CREATE UNIQUE INDEX ux_attendance_group_student_trunc
-- ON attendance (group_id, student_id, TRUNC(attendance_date));

*/

-- Consultas útiles post-migración (ejemplos):
-- Mostrar últimas filas de attendance
-- SELECT * FROM attendance ORDER BY attendance_date DESC, group_id, student_id FETCH FIRST 50 ROWS ONLY;

-- Obtener fechas disponibles para un grupo (usa en la app):
-- SELECT DISTINCT TRUNC(attendance_date) as d
-- FROM attendance
-- WHERE group_id = :GROUP_ID
-- ORDER BY TRUNC(attendance_date) DESC;

-- Consultar asistencia de un grupo en una fecha:
-- SELECT a.*, u.full_name
-- FROM attendance a
-- JOIN users u ON a.student_id = u.id
-- WHERE a.group_id = :GROUP_ID
--   AND TRUNC(a.attendance_date) = TRUNC(TO_DATE(:YYYYMMDD,'YYYYMMDD'))
-- ORDER BY u.full_name;
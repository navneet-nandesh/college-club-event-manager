CREATE DATABASE IF NOT EXISTS clubevent_db;
USE clubevent_db;

DROP TABLE IF EXISTS volunteer_assignments;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS execom_members;
DROP TABLE IF EXISTS students;

CREATE TABLE students (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    password VARCHAR(50),
    department VARCHAR(50),
    year INT
);

CREATE TABLE execom_members (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    password VARCHAR(50),
    designation VARCHAR(50)
);

CREATE TABLE events (
    eventId VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100),
    date VARCHAR(50),
    venue VARCHAR(100),
    maxSeats INT
);

CREATE TABLE registrations (
    regId VARCHAR(20) PRIMARY KEY,
    studentId VARCHAR(20),
    eventId VARCHAR(20),
    attended BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (studentId) REFERENCES students(id),
    FOREIGN KEY (eventId) REFERENCES events(eventId)
);

CREATE TABLE volunteer_assignments (
    volId VARCHAR(20) PRIMARY KEY,
    memberId VARCHAR(20),
    eventId VARCHAR(20),
    role VARCHAR(100),
    FOREIGN KEY (memberId) REFERENCES execom_members(id),
    FOREIGN KEY (eventId) REFERENCES events(eventId)
);

-- Insert Sample Students
INSERT INTO students VALUES ('STU-101', 'Alex Turner', 'alex@college.edu', 'student123', 'Computer Science', 3);
INSERT INTO students VALUES ('STU-102', 'Maya Patel', 'maya@college.edu', 'student123', 'Information Technology', 2);
INSERT INTO students VALUES ('STU-103', 'Liam Chen', 'liam@college.edu', 'student123', 'Mechanical', 4);

-- Insert Sample Execom Members
INSERT INTO execom_members VALUES ('EXE-001', 'Sarah Jenkins', 'sarah@college.edu', 'admin123', 'Club President');
INSERT INTO execom_members VALUES ('EXE-002', 'David Miller', 'david@college.edu', 'admin123', 'Technical Lead');

-- Insert Sample Events
INSERT INTO events VALUES ('EVT-101', 'Tech Summit 2026', '2026-11-15', 'Main Auditorium', 30);
INSERT INTO events VALUES ('EVT-102', 'Web Dev Workshop', '2026-11-20', 'Lab 1', 25);
INSERT INTO events VALUES ('EVT-103', 'Cultural Night', '2026-12-05', 'Open Stage', 100);
INSERT INTO events VALUES ('EVT-104', 'Coding Contest', '2026-11-08', 'Hall A', 2);

-- Pre-registered: Alex in EVT-101 (to test AlreadyRegisteredException)
INSERT INTO registrations VALUES ('REG-1001', 'STU-101', 'EVT-101', TRUE);
-- EVT-104 filled with 2 students (to test EventFullException)
INSERT INTO registrations VALUES ('REG-1002', 'STU-102', 'EVT-104', FALSE);
INSERT INTO registrations VALUES ('REG-1003', 'STU-103', 'EVT-104', FALSE);

-- Insert Volunteer Assignment
INSERT INTO volunteer_assignments VALUES ('VOL-501', 'EXE-001', 'EVT-101', 'Coordinator');
INSERT INTO volunteer_assignments VALUES ('VOL-502', 'EXE-002', 'EVT-104', 'Judge');

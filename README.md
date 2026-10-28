# College Club Event Manager (Java OOP + MySQL)

A simple, beginner-friendly College Club Event Management project that directly connects to **MySQL** and implements the architecture outline.

---

## 🚀 How to Run the Program

In your terminal or PowerShell, run:

```powershell
java -cp "bin;lib\mysql-connector-j.jar" clubevent.Main
```

*(Or open [src/clubevent/Main.java](file:///c:/Users/navne/clubevent/src/clubevent/Main.java) in VS Code and click the **Run** button right above `public static void main`!)*

---

## 🔄 What Happens When You Run [Main.java](file:///c:/Users/navne/clubevent/src/clubevent/Main.java)

1. **Automatic Database Traversal (Console Output)**:
   It connects to MySQL (`clubevent_db` with password `qwerty123`) and prints:
   - **All Events**: Event ID, Name, Date, Venue, Max Seats, Seats Left.
   - **All Registrations & Participants**: Student Name, Department, Event Name, Attended Status.
   - **All Volunteer Assignments**: Execom Member Name, Role, Event Name.

2. **Basic GUI (Login -> Dashboard -> Participants)**:
   - **[LoginFrame](file:///c:/Users/navne/clubevent/src/clubevent/LoginFrame.java)** opens with simple background color and 1-click demo buttons.
   - Logging in as **Student** opens **[StudentFrame](file:///c:/Users/navne/clubevent/src/clubevent/StudentFrame.java)** to register for events (handles `AlreadyRegisteredException` & `EventFullException`).
   - Logging in as **Execom Member** opens **[ExecomFrame](file:///c:/Users/navne/clubevent/src/clubevent/ExecomFrame.java)** to create events, assign volunteers, and open **[ParticipantsFrame](file:///c:/Users/navne/clubevent/src/clubevent/ParticipantsFrame.java)** to mark attendance.
   - Each screen also has a **"Traverse Console"** button to re-run the database traversal anytime!

---

## 📊 File Line Counts (All strictly under 185 lines)

| File | Lines | Purpose |
|---|---|---|
| [Authenticable.java](file:///c:/Users/navne/clubevent/src/clubevent/Authenticable.java) | 6 | `<<interface>>` declaring `login()` method |
| [InvalidLoginException.java](file:///c:/Users/navne/clubevent/src/clubevent/InvalidLoginException.java) | 7 | Custom exception for failed login |
| [EventFullException.java](file:///c:/Users/navne/clubevent/src/clubevent/EventFullException.java) | 7 | Custom exception when event has 0 seats left |
| [AlreadyRegisteredException.java](file:///c:/Users/navne/clubevent/src/clubevent/AlreadyRegisteredException.java) | 7 | Custom exception for duplicate registration |
| [Person.java](file:///c:/Users/navne/clubevent/src/clubevent/Person.java) | 22 | `<<abstract>>` class with protected `# id, name`, `# email, password` |
| [Student.java](file:///c:/Users/navne/clubevent/src/clubevent/Student.java) | 33 | Child class of `Person` (`department`, `year`) |
| [ExecomMember.java](file:///c:/Users/navne/clubevent/src/clubevent/ExecomMember.java) | 30 | Child class of `Person` (`designation`) |
| [Event.java](file:///c:/Users/navne/clubevent/src/clubevent/Event.java) | 36 | Event class with `seatsLeft()` calculation |
| [Registration.java](file:///c:/Users/navne/clubevent/src/clubevent/Registration.java) | 21 | Links `Student` and `Event` (`regId`, `attended`) |
| [VolunteerAssignment.java](file:///c:/Users/navne/clubevent/src/clubevent/VolunteerAssignment.java) | 20 | Links `ExecomMember` and `Event` (`volId`, `role`) |
| [DBConnection.java](file:///c:/Users/navne/clubevent/src/clubevent/DBConnection.java) | 21 | Connects to MySQL (`root` / `qwerty123`) |
| [DBManager.java](file:///c:/Users/navne/clubevent/src/clubevent/DBManager.java) | 184 | SQL operations and `traverseAll()` method |
| [LoginFrame.java](file:///c:/Users/navne/clubevent/src/clubevent/LoginFrame.java) | 62 | Basic login screen |
| [StudentFrame.java](file:///c:/Users/navne/clubevent/src/clubevent/StudentFrame.java) | 93 | Basic student portal |
| [ExecomFrame.java](file:///c:/Users/navne/clubevent/src/clubevent/ExecomFrame.java) | 123 | Basic execom operations portal |
| [ParticipantsFrame.java](file:///c:/Users/navne/clubevent/src/clubevent/ParticipantsFrame.java) | 92 | Basic participant attendance screen |
| [Main.java](file:///c:/Users/navne/clubevent/src/clubevent/Main.java) | 18 | Entry point that traverses and launches GUI |

---

## 🔑 Login Accounts (Pre-loaded in MySQL)

| Role | Email | Password | Pre-seeded Test Case |
|---|---|---|---|
| **Student** | `alex@college.edu` | `student123` | Already registered for `EVT-101` (*tests `AlreadyRegisteredException`*) |
| **Execom Member** | `sarah@college.edu` | `admin123` | Club President |

*(Or click the **Student Demo** / **Execom Demo** buttons on the login screen to fill in credentials with 1 click!)*

# College Club Event Manager (Java OOP + MySQL)

A simplified, beginner-level College Club Event Management project consolidated into a single self-contained file: **`ClubEventManager.java`**. Strictly aligned with lecture presentation topics: **Layout Managers**, **Exception Handling**, **Event Handling (Delegation Event Model)**, and **Swing**.

---

## 🚀 How to Compile and Run

In your terminal or PowerShell, run:

```powershell
javac -d bin -cp "lib\mysql-connector-j.jar" src\clubevent\ClubEventManager.java
java -cp "bin;lib\mysql-connector-j.jar" clubevent.ClubEventManager
```

---

## 🧭 Application Flow

1. **Role Selection (`RoleSelectFrame`)**:
   - The app starts by asking whether you want to enter the **Student Portal** or **Execom Portal**.
2. **Dedicated Logins**:
   - **Student Login (`StudentLoginFrame`)**: Login specifically for students against the `students` table. Includes a "Register" button to open the student registration form.
   - **Execom Member Login (`ExecomLoginFrame`)**: Login specifically for club execom members against the `execom_members` table. Includes a "Register" button to open the execom registration form.
3. **User Registration**:
   - **Student Registration (`StudentRegisterFrame`)**: Register new students with ID, Name, Email, Password, Department, and Year.
   - **Execom Registration (`ExecomRegisterFrame`)**: Register new execom members with ID, Name, Email, Password, and Designation.
4. **Dashboards**:
   - **Student Dashboard (`StudentFrame`)**: View available events, seats left, register for events, and view personal profile info.
   - **Execom Dashboard (`ExecomFrame`)**: Create new events, view registered participants, toggle attendance, assign volunteers, and view profile info.

---

## 📂 Project Structure

All source code is cleanly structured inside a single beginner-friendly file:
* **`src/clubevent/ClubEventManager.java`**:
  * **Section 1: Interfaces & Custom Exceptions**
    * `Authenticable` (`login` interface)
    * `InvalidLoginException`, `EventFullException`, `AlreadyRegisteredException` (subclasses of `Exception`)
  * **Section 2: OOP Models**
    * `Person` (abstract base class)
    * `Student` & `ExecomMember` (inheritance & method overriding)
    * `Event` (seat availability tracking)
  * **Section 3: Database Helper**
    * `DBManager` (clean JDBC connection, authentication, registration, queries)
  * **Section 4: Swing GUI Frames**
    * `RoleSelectFrame` (initial landing window to select role)
    * `StudentLoginFrame` & `StudentRegisterFrame` (student authentication and registration)
    * `ExecomLoginFrame` & `ExecomRegisterFrame` (execom authentication and registration)
    * `StudentFrame` (events table with dedicated `View Info` and `Register for Event` buttons)
    * `ExecomFrame` (event management, volunteer assignment, attendance tracking)
    * `ParticipantsFrame` (participant attendance toggle)
  * **Section 5: Main Entry Point**
    * `ClubEventManager` (launches GUI via `SwingUtilities.invokeLater`)

---

## 🔑 Sample Pre-loaded Accounts (from `init.sql`)

| Role | Email | Password | Details |
|---|---|---|---|
| **Student** | `alex@college.edu` | `student123` | Already registered for `EVT-101` (*tests `AlreadyRegisteredException`*) |
| **Execom Member** | `sarah@college.edu` | `admin123` | Club President (can create events & assign volunteers) |

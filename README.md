# College Club Event Manager (Java OOP + MySQL)

A simplified, beginner-level College Club Event Management project consolidated into a single self-contained file: **`ClubEventManager.java`**. Strictly aligned with lecture presentation topics: **Layout Managers**, **Exception Handling**, **Event Handling (Delegation Event Model)**, and **Swing**.

---

## 🚀 How to Compile and Run

In your terminal or PowerShell, run:

```powershell
javac -d bin -cp "lib\mysql-connector-j.jar" src\clubevent\ClubEventManager.java
java -cp "bin;lib\mysql-connector-j.jar" clubevent.ClubEventManager
```

*(Or open [src/clubevent/ClubEventManager.java](file:///c:/Users/navne/Downloads/college-club-event-manager-main/college-club-event-manager-main/src/clubevent/ClubEventManager.java) in VS Code or your IDE and click **Run**!)*

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
    * `DBManager` (clean JDBC connection and queries)
  * **Section 4: Swing GUI Frames**
    * `LoginFrame` (compact email/password form with `GridLayout`, `BorderLayout`, `FlowLayout`)
    * `StudentFrame` (events table with dedicated `View Info` and `Register for Event` buttons)
    * `ExecomFrame` (event management and volunteer assignment)
    * `ParticipantsFrame` (attendance tracker)
  * **Section 5: Main Entry Point**
    * `ClubEventManager` (launches GUI via `SwingUtilities.invokeLater`)

---

## 🔑 Login Accounts (Pre-loaded in MySQL)

| Role | Email | Password | Details |
|---|---|---|---|
| **Student** | `alex@college.edu` | `student123` | Already registered for `EVT-101` (*tests `AlreadyRegisteredException`*) |
| **Execom Member** | `sarah@college.edu` | `admin123` | Club President (can create events & assign volunteers) |

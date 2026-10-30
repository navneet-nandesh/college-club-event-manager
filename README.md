# College Club Event Manager (Java OOP + MySQL)

A simplified, beginner-friendly College Club Event Management project strictly aligned with lecture topics: **Layout Managers**, **Exception Handling**, **Event Handling (Delegation Event Model)**, and **Swing**.

---

## 🚀 How to Run the Program

In your terminal or PowerShell, run:

```powershell
javac -d bin -cp "lib\mysql-connector-j.jar" src\clubevent\*.java
java -cp "bin;lib\mysql-connector-j.jar" clubevent.Main
```

*(Or open [src/clubevent/Main.java](file:///c:/Users/navne/Downloads/college-club-event-manager-main/college-club-event-manager-main/src/clubevent/Main.java) and run it directly in your IDE!)*

---

## 📚 Concepts Implemented Directly from PPTs

### 1. Layout Managers (`java.awt.*`)
- **`BorderLayout`**: Five regions (`NORTH`, `SOUTH`, `CENTER`) used to structure top-level frames (`LoginFrame`, `StudentFrame`, `ExecomFrame`, `ParticipantsFrame`).
- **`FlowLayout`**: Standard centered component alignment for headers and button toolbars (`FlowLayout.CENTER`).
- **`GridLayout`**: Used for compact credential input fields in `LoginFrame` (`GridLayout(2, 2, 8, 8)`).
- **`ScrollPaneLayout`**: Used via `JScrollPane` to house event and participant tables.

### 2. Exception Handling Fundamentals
- **Checked vs Unchecked Exceptions**:
  - Checked: Custom exceptions `InvalidLoginException`, `EventFullException`, `AlreadyRegisteredException` subclassing `Exception`, plus `SQLException` and `ClassNotFoundException`.
  - Unchecked: `NumberFormatException` when parsing numerical seats in event creation.
- **5 Core Keywords**:
  - `try` & `catch`: Guarding and catching runtime errors.
  - `throw`: Explicit exception instantiation (`throw new InvalidLoginException(...)`).
  - `throws`: Method declarations specifying thrown exceptions.
  - `finally`: Guaranteed cleanup of database resources (`ResultSet`, `Statement`, `Connection`).
- **Multiple Catch Clauses**:
  Subclass exceptions are caught before general `Exception` handlers to prevent unreachable code errors.

### 3. Event Handling (Delegation Event Model)
- All GUI frames implement `ActionListener`.
- Buttons register their listener using `addActionListener(this)`.
- Handled in `actionPerformed(ActionEvent e)` using `if (e.getSource() == ...)` to identify which control triggered the event.
- Zero lambda expressions or external dependencies—strictly standard delegation event model.

### 4. Swing Components
- **Top-Level Container**: `JFrame` (`setSize`, `setLocationRelativeTo`, `setDefaultCloseOperation`, `getContentPane`).
- **Lightweight Containers**: `JPanel` arranged cleanly with layout managers.
- **Controls**: `JLabel`, `JTextField(10)`, `JPasswordField(10)`, `JButton`, `JScrollPane`, `JTable`.
- Launched via `SwingUtilities.invokeLater(new Runnable() { public void run() { ... } });`.

---

## 🔑 Login Accounts (Pre-loaded in MySQL)

| Role | Email | Password | Details |
|---|---|---|---|
| **Student** | `alex@college.edu` | `student123` | Already registered for `EVT-101` (*tests `AlreadyRegisteredException`*) |
| **Execom Member** | `sarah@college.edu` | `admin123` | Club President (can create events & assign volunteers) |

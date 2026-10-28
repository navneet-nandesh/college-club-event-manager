package clubevent;

public class Student extends Person implements Authenticable {
    private String department;
    private int year;

    public Student(String id, String name, String email, String password, String department, int year) {
        super(id, name, email, password);
        this.department = department;
        this.year = year;
    }

    public String getDepartment() { return department; }
    public int getYear() { return year; }

    @Override
    public void displayInfo() {
        System.out.println("Student: " + name + " | Dept: " + department + " | Year: " + year);
    }

    @Override
    public boolean login(String inputEmail, String inputPassword) throws InvalidLoginException {
        if (this.email.equalsIgnoreCase(inputEmail) && this.password.equals(inputPassword)) {
            return true;
        }
        throw new InvalidLoginException("Incorrect email or password for " + this.name);
    }

    @Override
    public boolean login() throws InvalidLoginException {
        return true;
    }
}

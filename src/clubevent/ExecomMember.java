package clubevent;

public class ExecomMember extends Person implements Authenticable {
    private String designation;

    public ExecomMember(String id, String name, String email, String password, String designation) {
        super(id, name, email, password);
        this.designation = designation;
    }

    public String getDesignation() { return designation; }

    @Override
    public void displayInfo() {
        System.out.println("Execom Member: " + name + " | Designation: " + designation);
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

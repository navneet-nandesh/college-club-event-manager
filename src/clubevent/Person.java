package clubevent;

public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String password;

    public Person(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public abstract void displayInfo();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

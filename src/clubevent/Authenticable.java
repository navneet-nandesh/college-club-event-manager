package clubevent;

public interface Authenticable {
    boolean login(String email, String password) throws InvalidLoginException;
    boolean login() throws InvalidLoginException;
}

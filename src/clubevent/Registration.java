package clubevent;

public class Registration {
    private String regId;
    private boolean attended;
    private Student student;
    private Event event;

    public Registration(String regId, Student student, Event event, boolean attended) {
        this.regId = regId;
        this.student = student;
        this.event = event;
        this.attended = attended;
    }

    public String getRegId() { return regId; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public Student getStudent() { return student; }
    public Event getEvent() { return event; }
}

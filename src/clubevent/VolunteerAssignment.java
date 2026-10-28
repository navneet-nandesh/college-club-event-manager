package clubevent;

public class VolunteerAssignment {
    private String volId;
    private String role;
    private ExecomMember member;
    private Event event;

    public VolunteerAssignment(String volId, ExecomMember member, Event event, String role) {
        this.volId = volId;
        this.member = member;
        this.event = event;
        this.role = role;
    }

    public String getVolId() { return volId; }
    public String getRole() { return role; }
    public ExecomMember getMember() { return member; }
    public Event getEvent() { return event; }
}

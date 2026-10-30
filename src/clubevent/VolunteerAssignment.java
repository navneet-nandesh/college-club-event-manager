package clubevent;

public class VolunteerAssignment {
    private String volId;
    private ExecomMember member;
    private Event event;
    private String role;

    public VolunteerAssignment(String volId, ExecomMember member, Event event, String role) {
        this.volId = volId;
        this.member = member;
        this.event = event;
        this.role = role;
    }

    public String getVolId() { return volId; }
    public ExecomMember getMember() { return member; }
    public Event getEvent() { return event; }
    public String getRole() { return role; }
}

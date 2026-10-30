package clubevent;

public class Event {
    private String eventId;
    private String name;
    private String date;
    private String venue;
    private int maxSeats;
    private int registeredCount;

    public Event(String eventId, String name, String date, String venue, int maxSeats) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.maxSeats = maxSeats;
        this.registeredCount = 0;
    }

    public int seatsLeft() {
        int left = maxSeats - registeredCount;
        if (left < 0) {
            return 0;
        }
        return left;
    }

    public String getEventId() { return eventId; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getVenue() { return venue; }
    public int getMaxSeats() { return maxSeats; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int count) { this.registeredCount = count; }

    @Override
    public String toString() {
        return name + " (" + eventId + ")";
    }
}

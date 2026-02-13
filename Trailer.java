/**
 * The Trailer – a permanent room where players start each day.
 * Has no scene cards, takes, or roles; serves purely as a resting location.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
public class Trailer extends Room {

    public Trailer() {
        super("Trailer");
    }

    @Override
    public String getStatusDescription() {
        return "Trailer";
    }
}

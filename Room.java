/**
 * Abstract base class representing any location on the Deadwood board.
 * All rooms (Sets, Trailer, CastingOffice) extend this class.
 * Encapsulates shared neighbor-connectivity logic (Liskov Substitution Principle).
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.ArrayList;
import java.util.List;

public abstract class Room {

    protected String name;
    protected List<Room> neighbors;

    /**
     * Constructs a Room with the given name.
     * @param name The display name of the room
     */
    public Room(String name) {
        this.name = name;
        this.neighbors = new ArrayList<>();
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    /** @return the room's name */
    public String getName() { return name; }

    /** @return an unmodifiable view of adjacent rooms */
    public List<Room> getNeighbors() { return neighbors; }

    /**
     * Registers an adjacent room (bidirectional setup done by Board).
     * @param r The neighboring room
     */
    public void addNeighbor(Room r) { neighbors.add(r); }

    /**
     * Checks whether a room with the given name is directly adjacent.
     * Case-insensitive comparison.
     * @param roomName Name to check
     * @return true if that room is a neighbor
     */
    public boolean isAdjacentTo(String roomName) {
        for (Room r : neighbors) {
            if (r.getName().equalsIgnoreCase(roomName)) return true;
        }
        return false;
    }

    /**
     * Retrieves a neighbor room by name (case-insensitive).
     * @param roomName Name of the desired neighbor
     * @return The Room object, or null if not adjacent
     */
    public Room getNeighborByName(String roomName) {
        for (Room r : neighbors) {
            if (r.getName().equalsIgnoreCase(roomName)) return r;
        }
        return null;
    }

    // ── Abstract Methods ─────────────────────────────────────────────────────

    /**
     * Returns a short status description suitable for the 'where' command.
     * @return Location description string
     */
    public abstract String getStatusDescription();
}

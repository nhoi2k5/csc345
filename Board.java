/**
 * The Board aggregates all rooms on the Deadwood board and provides
 * lookup utilities for the GameManager.
 *
 * Aggregation: Board holds references to Room objects but does not
 * exclusively own them (they are created by XMLParser and registered here).
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    /** All rooms keyed by lower-case name for fast lookup */
    private final Map<String, Room> rooms;

    /** Convenience lists for each room sub-type */
    private final List<Set>  sets;
    private Trailer      trailer;
    private CastingOffice office;

    public Board() {
        rooms = new HashMap<>();
        sets  = new ArrayList<>();
    }

    // ── Room registration ─────────────────────────────────────────────────────

    /**
     * Registers a room with the board.
     * Automatically updates the typed convenience references.
     * @param r Room to register
     */
    public void addRoom(Room r) {
        rooms.put(r.getName().toLowerCase(), r);
        if      (r instanceof Set)           sets.add((Set) r);
        else if (r instanceof Trailer)       trailer = (Trailer) r;
        else if (r instanceof CastingOffice) office  = (CastingOffice) r;
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * Retrieves a room by name (case-insensitive).
     * @param name The room name
     * @return Room or null if no room has that name
     */
    public Room getRoom(String name) {
        return rooms.get(name.toLowerCase());
    }

    /** @return all filming set rooms */
    public List<Set>   getSets()    { return sets;    }

    /** @return the Trailer room */
    public Trailer     getTrailer() { return trailer; }

    /** @return the CastingOffice room */
    public CastingOffice getOffice(){ return office;  }

    // ── Day-management helpers ────────────────────────────────────────────────

    /**
     * Counts how many sets currently have active (non-wrapped) scenes.
     * Used to detect end-of-day condition (≤1 active scene remaining).
     */
    public int countActiveSets() {
        int count = 0;
        for (Set s : sets) if (!s.isWrapped()) count++;
        return count;
    }

    /** Calls resetForNewDay() on every set. */
    public void resetAllSets() {
        for (Set s : sets) s.resetForNewDay();
    }
}

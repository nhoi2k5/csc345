/**
 * Represents a filming location (Set) on the Deadwood board.
 *
 * A Set holds:
 *  - A collection of takes (shot counters) that are removed as players act
 *  - Extra (off-card) roles permanently associated with the location
 *  - A reference to the active SceneCard placed at the start of the day
 *
 * The scene is "wrapped" when all shot counters have been removed, at
 * which point the budget-payout is triggered and the card is discarded.
 *
 * Composition relationship: a Set owns its Takes and extra Roles.
 * Association: a Set holds a reference to a SceneCard (owned by the deck).
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.ArrayList;
import java.util.List;

public class Set extends Room {

    private final List<Take>   takes;   // shot counters (composed)
    private final List<Role>   extras;  // off-card (extra) roles (composed)
    private SceneCard          activeCard;
    private boolean            wrapped;

    /**
     * @param name Display name of this filming location
     */
    public Set(String name) {
        super(name);
        this.takes    = new ArrayList<>();
        this.extras   = new ArrayList<>();
        this.wrapped  = false;
        this.activeCard = null;
    }

    // ── Scene card management ─────────────────────────────────────────────────

    /**
     * Places a new scene card on this set and resets all takes.
     * Called at the start of each day.
     * @param card The SceneCard to place here
     */
    public void setActiveCard(SceneCard card) {
        this.activeCard = card;
        this.wrapped    = false;
        for (Take t : takes) t.reset();
    }

    /** @return the current SceneCard, or null if no card is placed */
    public SceneCard getActiveCard() { return activeCard; }

    // ── Shot-counter (take) management ────────────────────────────────────────

    /** Adds a take to this set (done during board setup). */
    public void addTake(Take t) { takes.add(t); }

    /** @return all take objects for this set */
    public List<Take> getTakes() { return takes; }

    /** @return true if at least one shot counter remains on the board */
    public boolean hasShotCounters() {
        for (Take t : takes) if (t.isActive()) return true;
        return false;
    }

    /** @return how many shot counters are still active */
    public int countActiveTakes() {
        int count = 0;
        for (Take t : takes) if (t.isActive()) count++;
        return count;
    }

    /**
     * Removes exactly one active shot counter (highest-numbered first).
     * @return true if the scene wrapped (no more counters), false otherwise
     */
    public boolean removeOneTake() {
        for (Take t : takes) {
            if (t.isActive()) {
                t.setActive(false);
                break;
            }
        }
        if (!hasShotCounters()) {
            wrapped = true;
            return true; // scene wraps
        }
        return false;
    }

    // ── Role management ───────────────────────────────────────────────────────

    /** Adds an off-card extra role (done during board setup). */
    public void addExtra(Role r) { extras.add(r); }

    /** @return all off-card (extra) roles at this set */
    public List<Role> getExtras() { return extras; }

    /**
     * @return true when this set has no active scene or the scene is wrapped
     */
    public boolean isWrapped() { return wrapped || activeCard == null; }

    /**
     * Returns all roles (extras + on-card) available at this set.
     * On-card roles are only included when a non-wrapped scene is active.
     */
    public List<Role> getAllRoles() {
        List<Role> all = new ArrayList<>(extras);
        if (activeCard != null && !wrapped) {
            all.addAll(activeCard.getRoles());
        }
        return all;
    }

    /**
     * Collects all players currently working any role at this set.
     * @return list of active players at this set
     */
    public List<Player> getActivePlayers() {
        List<Player> players = new ArrayList<>();
        for (Role r : getAllRoles()) {
            if (r.getOccupiedBy() != null) players.add(r.getOccupiedBy());
        }
        return players;
    }

    // ── Day-reset ─────────────────────────────────────────────────────────────

    /**
     * Resets this set for the start of a new day.
     * Clears the scene card, reactivates takes, frees all extra roles.
     */
    public void resetForNewDay() {
        if (activeCard != null) activeCard.resetRoles();
        activeCard = null;
        wrapped    = false;
        for (Take t : takes)  t.reset();
        for (Role r : extras) r.setOccupiedBy(null);
    }

    // ── Room interface ────────────────────────────────────────────────────────

    @Override
    public String getStatusDescription() {
        if (wrapped || activeCard == null) {
            return name + " (wrapped)";
        }
        return name + " shooting " + activeCard.getName()
               + " scene " + activeCard.getSceneNumber();
    }
}

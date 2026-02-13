/**
 * Represents a single "take" (shot counter) on a Set.
 * When all takes are removed the scene wraps.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
public class Take {

    private final int number; // ordinal label (1, 2, 3 …)
    private boolean active;   // true = shot counter is still on board

    /**
     * Creates an active take.
     * @param number The take's label number
     */
    public Take(int number) {
        this.number = number;
        this.active = true;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    /** @return this take's label number */
    public int getNumber() { return number; }

    /** @return true if the shot counter is still on the board */
    public boolean isActive() { return active; }

    /** Sets whether the shot counter is present. */
    public void setActive(boolean active) { this.active = active; }

    /** Restores this take to its initial active state (new day setup). */
    public void reset() { this.active = true; }

    @Override
    public String toString() {
        return "Take " + number + (active ? " [active]" : " [used]");
    }
}

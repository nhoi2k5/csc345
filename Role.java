/**
 * Represents a role (part) that a player can work.
 *
 * There are two kinds of role:
 *   - On-card (starring) roles live inside a SceneCard and are revealed
 *     only when a set has an active scene.  Successful acting earns credits.
 *   - Off-card (extra) roles are permanently attached to a Set and are only
 *     accessible while the set has an active, non-wrapped scene.
 *     Successful acting earns dollars.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
public class Role {

    private final String  name;
    private final int     level;   // minimum player rank required
    private final String  line;    // flavour quote
    private final boolean onCard;  // true = starring, false = extra

    private Player occupiedBy;     // null when available

    /**
     * @param name   Role name as it appears in the XML
     * @param level  Minimum player rank required to take this role
     * @param line   The character's signature line
     * @param onCard true for scene-card roles, false for set extras
     */
    public Role(String name, int level, String line, boolean onCard) {
        this.name       = name;
        this.level      = level;
        this.line       = line;
        this.onCard     = onCard;
        this.occupiedBy = null;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getName()       { return name;       }
    public int    getLevel()      { return level;      }
    public String getLine()       { return line;       }
    public boolean isOnCard()     { return onCard;     }
    public Player  getOccupiedBy(){ return occupiedBy; }

    /** Assigns (or clears) the player currently working this role. */
    public void setOccupiedBy(Player p) { this.occupiedBy = p; }

    /** @return true if no player is currently working this role */
    public boolean isAvailable() { return occupiedBy == null; }

    @Override
    public String toString() {
        String tag = onCard ? "[starring]" : "[extra]";
        return String.format("%s (level %d) %s", name, level, tag);
    }
}

/**
 * Represents a single player in the Deadwood game.
 *
 * Tracks the player's resources (dollars, credits, rank), their
 * current location and role, rehearsal chips, and per-turn action
 * flags that are reset at the start of each turn.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
public class Player {

    // ── Identity ──────────────────────────────────────────────────────────────
    private final String name;   // e.g. "blue", "red"

    // ── Resources ────────────────────────────────────────────────────────────
    private int rank;
    private int dollars;
    private int credits;

    // ── Role / Location ───────────────────────────────────────────────────────
    private Room   location;
    private Role   currentRole;
    private int    rehearsalChips; // bonus to acting dice roll

    // ── Per-turn flags (reset by resetTurnState()) ────────────────────────────
    private boolean hasMoved;
    private boolean hasTakenRole;
    private boolean hasActed;
    private boolean hasRehearsed;

    /**
     * Creates a new player starting in the Trailer with rank 1.
     *
     * @param name           Display name / colour of the player
     * @param startingDollars Initial dollar amount (varies with player count)
     */
    public Player(String name, int startingDollars) {
        this.name           = name;
        this.rank           = 1;
        this.dollars        = startingDollars;
        this.credits        = 0;
        this.rehearsalChips = 0;
        this.currentRole    = null;
        resetTurnState();
    }

    // ── Turn-state management ─────────────────────────────────────────────────

    /** Resets all per-turn action flags. Called at the start of each turn. */
    public void resetTurnState() {
        hasMoved     = false;
        hasTakenRole = false;
        hasActed     = false;
        hasRehearsed = false;
    }

    // ── Resource mutators ─────────────────────────────────────────────────────

    public void addDollars(int d)  { dollars += d; }
    public void removeDollars(int d) { dollars -= d; }
    public void addCredits(int c)  { credits += c; }
    public void removeCredits(int c) { credits -= c; }
    public void addRehearsalChip() { rehearsalChips++; }
    public void resetRehearsalChips() { rehearsalChips = 0; }

    // ── Role / location management ────────────────────────────────────────────

    /**
     * Assigns this player to a role and sets acting-related state.
     * @param r The role to take (null to clear current role)
     */
    public void setCurrentRole(Role r) {
        this.currentRole    = r;
        this.rehearsalChips = 0; // chips reset whenever role changes
    }

    // ── Convenience ───────────────────────────────────────────────────────────

    /** @return true if the player is currently working a role */
    public boolean isWorking() { return currentRole != null; }

    /**
     * Computes the final score at game end.
     * Score = dollars + credits + rank
     */
    public int getScore() { return dollars + credits + rank; }

    // ── Getters / setters ──────────────────────────────────────────────────────

    public String  getName()            { return name;            }
    public int     getRank()            { return rank;            }
    public void    setRank(int r)       { rank = r;               }
    public int     getDollars()         { return dollars;         }
    public int     getCredits()         { return credits;         }
    public Room    getLocation()        { return location;        }
    public void    setLocation(Room r)  { location = r;           }
    public Role    getCurrentRole()     { return currentRole;     }
    public int     getRehearsalChips()  { return rehearsalChips;  }

    public boolean hasMoved()           { return hasMoved;        }
    public boolean hasTakenRole()       { return hasTakenRole;    }
    public boolean hasActed()           { return hasActed;        }
    public boolean hasRehearsed()       { return hasRehearsed;    }

    public void setHasMoved(boolean b)      { hasMoved = b;      }
    public void setHasTakenRole(boolean b)  { hasTakenRole = b;  }
    public void setHasActed(boolean b)      { hasActed = b;      }
    public void setHasRehearsed(boolean b)  { hasRehearsed = b;  }

    /**
     * Returns a summary line matching the 'who' command format.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("player ").append(name);
        sb.append(" ($").append(dollars);
        sb.append(", ").append(credits).append("cr");
        sb.append(", rank ").append(rank).append(")");
        if (currentRole != null) {
            sb.append("\n  Working: ").append(currentRole.getName());
            sb.append(", \"").append(currentRole.getLine()).append("\"");
            sb.append("  [rehearsal chips: ").append(rehearsalChips).append("]");
        }
        return sb.toString();
    }
}

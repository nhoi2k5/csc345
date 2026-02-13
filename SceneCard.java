/**
 * Represents a scene card from the cards.xml deck.
 * A SceneCard carries the film's title, budget, scene number,
 * descriptive flavour text, and the starring (on-card) roles.
 *
 * Cards are placed face-down on Sets at the start of each day and
 * flipped over (becoming "active") only when the first player takes
 * an on-card role at that set.  Once all shot-counters (takes) are
 * removed the card is "wrapped" and discarded for the day.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.ArrayList;
import java.util.List;

public class SceneCard {

    private final String name;
    private final int    budget;
    private final int    sceneNumber;
    private final String description;
    private final List<Role> roles;

    /**
     * @param name        Film title
     * @param budget      Scene budget (number of success dice rolled on wrap)
     * @param sceneNumber Scene identifier
     * @param description Flavour text for the scene
     */
    public SceneCard(String name, int budget, int sceneNumber, String description) {
        this.name        = name;
        this.budget      = budget;
        this.sceneNumber = sceneNumber;
        this.description = description;
        this.roles       = new ArrayList<>();
    }

    // ── Mutators ─────────────────────────────────────────────────────────────

    /** Adds an on-card (starring) role to this scene. */
    public void addRole(Role r) { roles.add(r); }

    /**
     * Clears all player assignments for every on-card role.
     * Called when a scene wraps or a new day begins.
     */
    public void resetRoles() {
        for (Role r : roles) r.setOccupiedBy(null);
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String     getName()       { return name;        }
    public int        getBudget()     { return budget;      }
    public int        getSceneNumber(){ return sceneNumber; }
    public String     getDescription(){ return description; }
    public List<Role> getRoles()      { return roles;       }

    @Override
    public String toString() {
        return name + " (scene " + sceneNumber + ", budget " + budget + ")";
    }
}

/**
 * The Casting Office – a permanent room where players can spend
 * dollars or credits to upgrade their rank (1–6).
 *
 * Aggregation: owns a list of Upgrade objects loaded from board.xml.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.ArrayList;
import java.util.List;

public class CastingOffice extends Room {

    private final List<Upgrade> upgrades;

    public CastingOffice() {
        super("office");
        this.upgrades = new ArrayList<>();
    }

    // ── Upgrade management ────────────────────────────────────────────────────

    /** Adds an upgrade option (called during XML parsing). */
    public void addUpgrade(Upgrade u) { upgrades.add(u); }

    /** @return all available upgrade options */
    public List<Upgrade> getUpgrades() { return upgrades; }

    /**
     * Finds the upgrade option for the given target level and currency.
     * @param level    Target rank (2–6)
     * @param currency "dollar" or "credit"
     * @return matching Upgrade or null if not found
     */
    public Upgrade findUpgrade(int level, String currency) {
        for (Upgrade u : upgrades) {
            if (u.getLevel() == level
                    && u.getCurrency().equalsIgnoreCase(currency)) {
                return u;
            }
        }
        return null;
    }

    // ── Room interface ────────────────────────────────────────────────────────

    @Override
    public String getStatusDescription() {
        return "Casting Office";
    }
}

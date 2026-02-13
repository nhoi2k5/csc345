/**
 * Represents a single rank-upgrade option available at the Casting Office.
 * An upgrade has a target rank level, a currency type ("dollar" or "credit"),
 * and an associated cost.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
public class Upgrade {

    private final int    level;    // target rank (2-6)
    private final String currency; // "dollar" or "credit"
    private final int    amount;   // cost in that currency

    /**
     * @param level    Target rank level this upgrade unlocks
     * @param currency Payment type: "dollar" or "credit"
     * @param amount   Cost in the specified currency
     */
    public Upgrade(int level, String currency, int amount) {
        this.level    = level;
        this.currency = currency;
        this.amount   = amount;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    /** @return target rank */
    public int    getLevel()    { return level;    }
    /** @return "dollar" or "credit" */
    public String getCurrency() { return currency; }
    /** @return cost in chosen currency */
    public int    getAmount()   { return amount;   }

    @Override
    public String toString() {
        return String.format("  Rank %d : %2d %s%s", level, amount, currency,
                             amount == 1 ? "" : "s");
    }
}

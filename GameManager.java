/**
 * GameManager controls the entire lifecycle of a Deadwood game:
 * setup, per-turn command processing, end-of-day and end-of-game logic.
 *
 * Cohesion: High (Functional / Communicational) – every method exists to
 *   serve the single purpose of managing game state.
 * Coupling: Medium – GameManager depends on model classes (Player, Board,
 *   Set, Role, …) through their public interfaces, not internals.
 *
 * SOLID notes:
 *   S – GameManager handles state; Deadwood.java handles I/O (separated).
 *   O – New game phases can be added without modifying existing logic.
 *   D – GameManager depends on Room abstraction, not concrete subclasses.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameManager {

    // ── Game configuration ────────────────────────────────────────────────────
    private static final String[] PLAYER_COLORS =
        {"blue", "cyan", "green", "orange", "pink", "red", "violet", "yellow"};
    private static final int TOTAL_SETS = 10;

    // ── Core state ────────────────────────────────────────────────────────────
    private Board            board;
    private List<SceneCard>  deck;
    private List<Player>     players;
    private int              currentPlayerIndex;
    private int              currentDay;
    private int              totalDays;
    private boolean          gameOver;
    private final Random     rng = new Random();

    /** @return true if the game has ended */
    public boolean isGameOver() { return gameOver; }

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Initialises the game for the given number of players.
     * Parses XML, creates players, and starts day 1.
     *
     * @param numPlayers 2–8 inclusive
     * @param boardFile  Path to board.xml
     * @param cardsFile  Path to cards.xml
     * @throws Exception on XML parse failure
     */
    public void setup(int numPlayers, String boardFile, String cardsFile)
            throws Exception {

        // ── Parse XML ────────────────────────────────────────────────────────
        XMLParser parser = new XMLParser();
        board = parser.parseBoard(boardFile);
        deck  = parser.parseCards(cardsFile);

        // ── Player count rules ────────────────────────────────────────────────
        totalDays = (numPlayers <= 3) ? 3 : 4;

        int startDollars = 0;
        if      (numPlayers == 5) startDollars = 1;
        else if (numPlayers == 6) startDollars = 2;
        else if (numPlayers >= 7) startDollars = 3;

        // ── Create players ────────────────────────────────────────────────────
        players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(PLAYER_COLORS[i], startDollars);
            p.setLocation(board.getTrailer());
            players.add(p);
        }

        currentPlayerIndex = 0;
        currentDay         = 1;
        gameOver           = false;

        System.out.println("=== Welcome to Deadwood! ===");
        System.out.println("Players: " + numPlayers + "  |  Days: " + totalDays);
        startDay();
    }

    // ── Day management ─────────────────────────────────────────────────────────

    /** Sets up a new day: shuffles deck, deals cards to sets, moves players. */
    private void startDay() {
        System.out.println("\n--- Day " + currentDay + " begins ---");

        // Reset all sets
        board.resetAllSets();

        // Shuffle deck and deal one card to each set
        Collections.shuffle(deck, rng);
        List<Set> sets = board.getSets();
        for (int i = 0; i < sets.size() && i < deck.size(); i++) {
            sets.get(i).setActiveCard(deck.get(i));
        }

        // Move all players to trailer and clear role state
        for (Player p : players) {
            p.setLocation(board.getTrailer());
            if (p.getCurrentRole() != null) {
                p.getCurrentRole().setOccupiedBy(null);
                p.setCurrentRole(null);
            }
            p.resetRehearsalChips();
            p.resetTurnState();
        }

        System.out.println("Scene cards dealt. All players return to the Trailer.");
        announceActivePlayer();
    }

    /**
     * Wraps up the current day and either starts the next one or ends the game.
     */
    private void endDay() {
        System.out.println("\n--- Day " + currentDay + " ends ---");

        // Free any players still on roles (no payout for unfinished scenes)
        for (Player p : players) {
            if (p.getCurrentRole() != null) {
                p.getCurrentRole().setOccupiedBy(null);
                p.setCurrentRole(null);
                p.resetRehearsalChips();
            }
        }

        if (currentDay >= totalDays) {
            endGame();
        } else {
            currentDay++;
            currentPlayerIndex = 0;
            startDay();
        }
    }

    /** Prints final scores and declares a winner. */
    private void endGame() {
        gameOver = true;
        System.out.println("\n============================");
        System.out.println("         GAME OVER          ");
        System.out.println("============================");
        System.out.println("Final Scores (dollars + credits + rank):");

        int     highScore  = -1;
        Player  winner     = null;

        for (Player p : players) {
            int score = p.getScore();
            System.out.printf("  %-8s : $%2d + %2dcr + rank %d = %d%n",
                p.getName(), p.getDollars(), p.getCredits(), p.getRank(), score);
            if (score > highScore) {
                highScore = score;
                winner    = p;
            }
        }

        System.out.println("\nWinner: " + winner.getName()
                           + " with " + highScore + " points!");
    }

    // ── Turn management ───────────────────────────────────────────────────────

    /** @return the player whose turn it currently is */
    public Player activePlayer() {
        return players.get(currentPlayerIndex);
    }

    /** @return a copy of the player list (read-only from outside) */
    public List<Player> getPlayers() { return players; }

    private void announceActivePlayer() {
        System.out.println("\nActive player: " + activePlayer().getName());
    }

    // ── Command handlers ──────────────────────────────────────────────────────

    /** who – prints current player info */
    public void cmdWho() {
        System.out.println(activePlayer());
    }

    /** where – prints current player location */
    public void cmdWhere() {
        Player p    = activePlayer();
        Room   loc  = p.getLocation();
        String desc = loc.getStatusDescription();
        if (p.isWorking()) {
            System.out.println(p.getName() + " is in " + desc
                + " working " + p.getCurrentRole().getName()
                + ", \"" + p.getCurrentRole().getLine() + "\"");
        } else {
            System.out.println(p.getName() + " is in " + desc);
        }
    }

    /** board – prints every player's location */
    public void cmdBoard() {
        System.out.println("--- All Players ---");
        for (Player p : players) {
            String marker = p == activePlayer() ? " *" : "";
            System.out.println("  " + p.getName() + marker
                + " -> " + p.getLocation().getStatusDescription()
                + (p.isWorking() ? " [" + p.getCurrentRole().getName() + "]" : ""));
        }
    }

    /**
     * move – moves the active player to an adjacent room.
     * Only allowed if the player is not currently working a role and has
     * not already moved this turn.
     *
     * @param roomName Destination room name (case-insensitive)
     */
    public void cmdMove(String roomName) {
        Player p = activePlayer();

        if (p.isWorking()) {
            System.out.println("Cannot move while working a role.");
            return;
        }
        if (p.hasMoved()) {
            System.out.println("You have already moved this turn.");
            return;
        }

        Room current = p.getLocation();
        Room dest    = current.getNeighborByName(roomName);

        if (dest == null) {
            // Try board-wide lookup in case player typed exact name
            dest = board.getRoom(roomName.toLowerCase());
            if (dest == null || !current.isAdjacentTo(dest.getName())) {
                System.out.println("Cannot move to \"" + roomName
                    + "\". Adjacent rooms: " + neighborNames(current));
                return;
            }
        }

        p.setLocation(dest);
        p.setHasMoved(true);
        System.out.println(p.getName() + " moves to " + dest.getStatusDescription());
    }

    /**
     * work – active player takes a role at their current location.
     * Player must not already be working a role and must meet the role's
     * rank requirement.  The set must have an active, non-wrapped scene.
     *
     * @param roleName Name of the desired role (case-insensitive)
     */
    public void cmdWork(String roleName) {
        Player p = activePlayer();

        if (p.isWorking()) {
            System.out.println("You are already working a role.");
            return;
        }
        if (p.hasTakenRole()) {
            System.out.println("You have already taken a role this turn.");
            return;
        }

        Room loc = p.getLocation();
        if (!(loc instanceof Set)) {
            System.out.println("You can only take a role on a filming Set.");
            return;
        }

        Set set = (Set) loc;
        if (set.isWrapped()) {
            System.out.println("The scene at " + set.getName()
                + " has already wrapped – no roles available.");
            return;
        }

        // Find the role by name (case-insensitive)
        Role found = null;
        for (Role r : set.getAllRoles()) {
            if (r.getName().equalsIgnoreCase(roleName) && r.isAvailable()) {
                found = r;
                break;
            }
        }

        if (found == null) {
            System.out.println("Role \"" + roleName
                + "\" not found or not available. Available roles:");
            listRoles(set);
            return;
        }

        if (p.getRank() < found.getLevel()) {
            System.out.println("Your rank (" + p.getRank()
                + ") is too low for this role (requires " + found.getLevel() + ").");
            return;
        }

        // Assign the role
        found.setOccupiedBy(p);
        p.setCurrentRole(found);
        p.setHasTakenRole(true);

        String roleType = found.isOnCard() ? "starring" : "extra";
        System.out.println(p.getName() + " takes the " + roleType + " role: "
            + found.getName() + " (level " + found.getLevel() + ")");
        System.out.println("  Line: \"" + found.getLine() + "\"");
    }

    /**
     * act – the active player attempts to act on their current role.
     * Rolls 1d6 + rehearsal chips.
     * Success (roll ≥ budget): removes a take, earns reward.
     *   - Starring role: +2 credits
     *   - Extra role:    +$1
     * If the final take is removed, the scene wraps and the budget payout
     * is distributed to starring-role players.
     */
    public void cmdAct() {
        Player p = activePlayer();

        if (!p.isWorking()) {
            System.out.println("You are not working a role – take a role first.");
            return;
        }
        if (p.hasActed()) {
            System.out.println("You have already acted this turn.");
            return;
        }
        if (p.hasRehearsed()) {
            System.out.println("You already rehearsed this turn; you cannot also act.");
            return;
        }

        Set    set    = (Set)    p.getLocation();
        Role   role   = p.getCurrentRole();
        int    budget = set.getActiveCard().getBudget();
        int    roll   = rng.nextInt(6) + 1;
        int    total  = roll + p.getRehearsalChips();

        System.out.println(p.getName() + " rolls a " + roll
            + " (+" + p.getRehearsalChips() + " rehearsal) = " + total
            + " vs budget " + budget);

        p.setHasActed(true);

        if (total >= budget) {
            // ── Success ───────────────────────────────────────────────────────
            boolean wrapped = set.removeOneTake();

            if (role.isOnCard()) {
                p.addCredits(2);
                System.out.println("Success! " + p.getName()
                    + " earns 2 credits. Takes remaining: "
                    + set.countActiveTakes());
            } else {
                p.addDollars(1);
                System.out.println("Success! " + p.getName()
                    + " earns $1. Takes remaining: "
                    + set.countActiveTakes());
            }

            if (wrapped) {
                handleSceneWrap(set);
            }
        } else {
            // ── Failure ───────────────────────────────────────────────────────
            System.out.println("Failed. No reward this time.");
        }
    }

    /**
     * Handles end-of-scene wrap:
     *  1. Rolls budget dice and distributes to on-card players (round-robin,
     *     highest die to highest-rank role).
     *  2. Frees all players from their roles.
     *  3. Checks for end-of-day condition.
     */
    private void handleSceneWrap(Set set) {
        System.out.println("\n*** Scene \"" + set.getActiveCard().getName()
            + "\" WRAPS! ***");

        // Collect on-card players sorted by role level descending
        List<Role> onCardRoles = new ArrayList<>(set.getActiveCard().getRoles());
        List<Player> starring  = new ArrayList<>();
        for (Role r : onCardRoles) {
            if (r.getOccupiedBy() != null) starring.add(r.getOccupiedBy());
        }

        // Sort by role level descending so highest-rank role gets first die
        starring.sort((a, b) ->
            b.getCurrentRole().getLevel() - a.getCurrentRole().getLevel());

        if (!starring.isEmpty()) {
            // Roll budget dice
            int budget = set.getActiveCard().getBudget();
            int[] dice = new int[budget];
            System.out.print("Budget payout – rolling " + budget + " dice: ");
            for (int i = 0; i < budget; i++) {
                dice[i] = rng.nextInt(6) + 1;
                System.out.print(dice[i] + " ");
            }
            System.out.println();

            // Sort dice descending
            Arrays.sort(dice);
            int[] sortedDice = new int[budget];
            for (int i = 0; i < budget; i++) sortedDice[i] = dice[budget - 1 - i];

            // Distribute round-robin
            for (int i = 0; i < budget; i++) {
                Player recipient = starring.get(i % starring.size());
                recipient.addDollars(sortedDice[i]);
                System.out.println("  " + recipient.getName()
                    + " receives $" + sortedDice[i]);
            }
        } else {
            System.out.println("(No on-card players to receive payout.)");
        }

        // Free all players from their roles at this set
        for (Role r : set.getAllRoles()) {
            Player occupant = r.getOccupiedBy();
            if (occupant != null) {
                occupant.setCurrentRole(null);
                occupant.resetRehearsalChips();
                r.setOccupiedBy(null);
            }
        }
        set.getActiveCard().resetRoles();

        System.out.println("All players released from roles at " + set.getName() + ".");

        // Check end-of-day: ≤1 active scene remains
        if (board.countActiveSets() <= 1) {
            System.out.println("Only " + board.countActiveSets()
                + " scene(s) remaining – day ends.");
            endDay();
        }
    }

    /**
     * rehearse – adds one rehearsal chip (bonus to next act roll).
     * Chips cannot be accumulated beyond (budget - 1) to prevent guaranteed success.
     * Cannot rehearse the same turn as acting.
     */
    public void cmdRehearse() {
        Player p = activePlayer();

        if (!p.isWorking()) {
            System.out.println("You are not working a role.");
            return;
        }
        if (p.hasRehearsed() || p.hasActed()) {
            System.out.println("You have already acted or rehearsed this turn.");
            return;
        }

        Set set    = (Set) p.getLocation();
        int budget = set.getActiveCard().getBudget();

        // Cap: chips cannot guarantee success (chips < budget)
        if (p.getRehearsalChips() >= budget - 1) {
            System.out.println("You already have the maximum rehearsal chips ("
                + p.getRehearsalChips() + ") for this scene (budget " + budget + ").");
            return;
        }

        p.addRehearsalChip();
        p.setHasRehearsed(true);
        System.out.println(p.getName() + " rehearses. Rehearsal chips: "
            + p.getRehearsalChips());
    }

    /**
     * upgrade – upgrades the active player's rank at the Casting Office.
     * Can upgrade to any rank above the current one (not just +1).
     * Supported currencies: "dollar" and "credit".
     *
     * @param level    Target rank (2–6)
     * @param currency "dollar" or "credit"
     */
    public void cmdUpgrade(int level, String currency) {
        Player p = activePlayer();

        if (!(p.getLocation() instanceof CastingOffice)) {
            System.out.println("You must be at the Casting Office to upgrade.");
            return;
        }
        if (p.isWorking()) {
            System.out.println("Cannot upgrade while working a role.");
            return;
        }
        if (level <= p.getRank()) {
            System.out.println("Target rank " + level
                + " must be higher than your current rank " + p.getRank() + ".");
            return;
        }
        if (level < 2 || level > 6) {
            System.out.println("Rank must be between 2 and 6.");
            return;
        }

        CastingOffice office = (CastingOffice) p.getLocation();
        Upgrade u = office.findUpgrade(level, currency);
        if (u == null) {
            System.out.println("No upgrade found for rank " + level
                + " with " + currency + "s.");
            listUpgrades(office, p);
            return;
        }

        // Check affordability
        if (currency.equalsIgnoreCase("dollar")) {
            if (p.getDollars() < u.getAmount()) {
                System.out.println("Not enough dollars. Need " + u.getAmount()
                    + ", have " + p.getDollars() + ".");
                return;
            }
            p.removeDollars(u.getAmount());
        } else if (currency.equalsIgnoreCase("credit")) {
            if (p.getCredits() < u.getAmount()) {
                System.out.println("Not enough credits. Need " + u.getAmount()
                    + ", have " + p.getCredits() + ".");
                return;
            }
            p.removeCredits(u.getAmount());
        } else {
            System.out.println("Unknown currency \"" + currency
                + "\". Use 'dollar' or 'credit'.");
            return;
        }

        int old = p.getRank();
        p.setRank(level);
        System.out.println(p.getName() + " upgrades from rank " + old
            + " to rank " + level + " (paid " + u.getAmount() + " "
            + currency + "s). Balance: $" + p.getDollars()
            + ", " + p.getCredits() + " credits.");
    }

    /**
     * end – ends the active player's turn and advances to the next player.
     */
    public void cmdEnd() {
        Player p = activePlayer();
        System.out.println(p.getName() + " ends their turn.");

        // Advance to next player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        players.get(currentPlayerIndex).resetTurnState();

        if (!gameOver) {
            announceActivePlayer();
        }
    }

    /**
     * quit – forces the game to end immediately (for testing).
     */
    public void cmdQuit() {
        System.out.println("Game ended by player request.");
        endGame();
    }

    /**
     * roles – lists available roles at the active player's current location.
     */
    public void cmdRoles() {
        Room loc = activePlayer().getLocation();
        if (!(loc instanceof Set)) {
            System.out.println("No roles available here.");
            return;
        }
        Set set = (Set) loc;
        if (set.isWrapped()) {
            System.out.println("Scene at " + set.getName() + " is wrapped.");
            return;
        }
        System.out.println("Roles at " + set.getName() + ":");
        listRoles(set);
    }

    /** Displays the upgrade table for the Casting Office. */
    public void cmdUpgrades() {
        Room loc = activePlayer().getLocation();
        if (!(loc instanceof CastingOffice)) {
            System.out.println("You must be at the Casting Office to see upgrades.");
            return;
        }
        listUpgrades((CastingOffice) loc, activePlayer());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String neighborNames(Room r) {
        StringBuilder sb = new StringBuilder();
        List<Room> nbs = r.getNeighbors();
        for (int i = 0; i < nbs.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(nbs.get(i).getName());
        }
        return sb.toString();
    }

    private void listRoles(Set set) {
        int playerRank = activePlayer().getRank();
        // Off-card extras
        System.out.println("  [Extra roles]");
        for (Role r : set.getExtras()) {
            String avail = !r.isAvailable() ? " (taken by "
                + r.getOccupiedBy().getName() + ")" : "";
            String rankOk = r.getLevel() <= playerRank ? "" : " [rank too low]";
            System.out.println("    " + r + avail + rankOk);
        }
        // On-card (starring) roles
        if (set.getActiveCard() != null) {
            System.out.println("  [Starring roles – " + set.getActiveCard().getName() + "]");
            for (Role r : set.getActiveCard().getRoles()) {
                String avail = !r.isAvailable() ? " (taken by "
                    + r.getOccupiedBy().getName() + ")" : "";
                String rankOk = r.getLevel() <= playerRank ? "" : " [rank too low]";
                System.out.println("    " + r + avail + rankOk);
            }
        }
    }

    private void listUpgrades(CastingOffice office, Player p) {
        System.out.println("Available upgrades (your rank: " + p.getRank() + "):");
        System.out.println("  [Dollar upgrades]");
        for (Upgrade u : office.getUpgrades()) {
            if (u.getCurrency().equals("dollar") && u.getLevel() > p.getRank())
                System.out.println(u);
        }
        System.out.println("  [Credit upgrades]");
        for (Upgrade u : office.getUpgrades()) {
            if (u.getCurrency().equals("credit") && u.getLevel() > p.getRank())
                System.out.println(u);
        }
    }
}

/**
 * Deadwood – Main Program Entry Point
 *
 * Launches the Deadwood console game.  Handles all user input/output
 * and delegates every game-logic decision to GameManager.
 *
 * Usage:
 *   java Deadwood <numPlayers>       (uses default XML paths)
 *   java Deadwood <numPlayers> <boardXML> <cardsXML>
 *
 * Commands (case-insensitive):
 *   who                        – print active player info
 *   where                      – print active player location
 *   board / players            – print all players' locations
 *   move <room>                – move to adjacent room
 *   work <role> / role <role>  – take a role at current set
 *   act                        – act on current role
 *   rehearse                   – rehearse (add chip)
 *   upgrade <level> <currency> – upgrade rank at Casting Office
 *   roles                      – list available roles here
 *   upgrades                   – list available upgrades (at office)
 *   end                        – end current player's turn
 *   quit / end game            – force-end the game
 *   help                       – show this command list
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import java.util.Scanner;

public class Deadwood {

    /** Default XML file paths (relative to working directory). */
    private static final String DEFAULT_BOARD = "board.xml";
    private static final String DEFAULT_CARDS = "cards.xml";

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        // ── Validate arguments ────────────────────────────────────────────────
        if (args.length < 1) {
            System.err.println("Usage: java Deadwood <numPlayers> [boardXML] [cardsXML]");
            System.exit(1);
        }

        int numPlayers;
        try {
            numPlayers = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error: numPlayers must be an integer.");
            System.exit(1);
            return;
        }

        if (numPlayers < 2 || numPlayers > 8) {
            System.err.println("Error: Deadwood supports 2–8 players.");
            System.exit(1);
        }

        String boardFile = args.length >= 2 ? args[1] : DEFAULT_BOARD;
        String cardsFile = args.length >= 3 ? args[2] : DEFAULT_CARDS;

        // ── Initialise game ───────────────────────────────────────────────────
        GameManager gm = new GameManager();
        try {
            gm.setup(numPlayers, boardFile, cardsFile);
        } catch (Exception e) {
            System.err.println("Failed to load game data: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // ── Game loop ─────────────────────────────────────────────────────────
        Scanner scanner = new Scanner(System.in);
        printHelp();

        while (!gm.isGameOver()) {
            System.out.print("> ");
            System.out.flush();

            if (!scanner.hasNextLine()) break; // EOF (e.g. piped input)
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            processInput(input, gm);
        }

        scanner.close();
    }

    // ── Input processing ──────────────────────────────────────────────────────

    /**
     * Parses one line of user input and dispatches to the appropriate
     * GameManager command.
     *
     * @param input Raw input line
     * @param gm    The active GameManager
     */
    private static void processInput(String input, GameManager gm) {
        // Normalise: lower-case, collapse whitespace
        String lower = input.toLowerCase().trim();
        String[] tokens = lower.split("\\s+");
        String cmd   = tokens[0];

        // ── "end game" / "quit" ───────────────────────────────────────────────
        if (lower.equals("end game") || lower.equals("quit")
                || lower.equals("q")) {
            gm.cmdQuit();
            return;
        }

        switch (cmd) {

            // ── Information commands ──────────────────────────────────────────
            case "who":
                gm.cmdWho();
                break;

            case "where":
                gm.cmdWhere();
                break;

            case "board":
            case "players":
                gm.cmdBoard();
                break;

            case "roles":
                gm.cmdRoles();
                break;

            case "upgrades":
                gm.cmdUpgrades();
                break;

            case "help":
                printHelp();
                break;

            // ── Action commands ───────────────────────────────────────────────

            case "move": {
                if (tokens.length < 2) {
                    System.out.println("Usage: move <room name>");
                } else {
                    // Reconstruct the original-cased destination from raw input
                    String dest = extractArgument(input, "move");
                    gm.cmdMove(dest);
                }
                break;
            }

            case "work":
            case "role": {
                if (tokens.length < 2) {
                    System.out.println("Usage: work <role name>");
                } else {
                    String roleName = extractArgument(input, tokens[0]);
                    gm.cmdWork(roleName);
                }
                break;
            }

            case "act":
                gm.cmdAct();
                break;

            case "rehearse":
            case "r":
                gm.cmdRehearse();
                break;

            case "upgrade": {
                // Expected: upgrade <level> <dollar|credit>
                if (tokens.length < 3) {
                    System.out.println("Usage: upgrade <level> <dollar|credit>");
                } else {
                    try {
                        int    level    = Integer.parseInt(tokens[1]);
                        String currency = tokens[2];
                        gm.cmdUpgrade(level, currency);
                    } catch (NumberFormatException e) {
                        System.out.println("Usage: upgrade <level> <dollar|credit>"
                            + "  (level must be a number)");
                    }
                }
                break;
            }

            case "end":
                // Guard against "end game" that wasn't caught above
                if (tokens.length >= 2 && tokens[1].equals("game")) {
                    gm.cmdQuit();
                } else {
                    gm.cmdEnd();
                }
                break;

            default:
                System.out.println("Unknown command: \"" + cmd
                    + "\".  Type 'help' for a list of commands.");
        }
    }

    /**
     * Extracts everything after the first word as the argument string,
     * preserving original case from the raw input.
     *
     * @param raw   Original input line
     * @param cmd   Command word to strip
     * @return      Argument string, trimmed
     */
    private static String extractArgument(String raw, String cmd) {
        int idx = raw.toLowerCase().indexOf(cmd.toLowerCase());
        if (idx < 0) return "";
        String remainder = raw.substring(idx + cmd.length()).trim();
        return remainder;
    }

    // ── Help text ─────────────────────────────────────────────────────────────

    private static void printHelp() {
        System.out.println(
            "\n--- Deadwood Commands ---\n"
          + "  who                         active player info\n"
          + "  where                        active player location\n"
          + "  board / players              all players on the board\n"
          + "  move <room>                  move to adjacent room\n"
          + "  work <role> / role <role>    take a role at current set\n"
          + "  act                          act on your current role\n"
          + "  rehearse                     rehearse (earn a bonus chip)\n"
          + "  upgrade <rank> <dollar|credit>   upgrade rank at Casting Office\n"
          + "  roles                        list roles at current location\n"
          + "  upgrades                     list upgrade costs (at Casting Office)\n"
          + "  end                          end your turn\n"
          + "  end game / quit              force-end the game\n"
          + "  help                         show this list\n"
        );
    }
}

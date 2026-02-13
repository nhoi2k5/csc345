# Deadwood – Console Implementation
## CSCI 345 – Assignment 2

---

GITHUB ACCESSS : https://github.com/nhoi2k5/csc345

## Files

| File | Description |
|------|-------------|
| `Deadwood.java`    | Main entry point; handles all command-line I/O |
| `GameManager.java` | All game-state and rule-enforcement logic |
| `Board.java`       | Aggregates all rooms; provides lookup utilities |
| `Room.java`        | Abstract base class for every board location |
| `Set.java`         | Filming location (extends Room); owns Takes and extra Roles |
| `Trailer.java`     | Permanent starting room (extends Room) |
| `CastingOffice.java` | Upgrade room (extends Room); owns Upgrade list |
| `Player.java`      | Per-player state (rank, dollars, credits, role, etc.) |
| `Role.java`        | A part; on-card (starring) or off-card (extra) |
| `SceneCard.java`   | Scene card with budget, scene number, and on-card roles |
| `Take.java`        | Shot counter; removed on successful acts |
| `Upgrade.java`     | A rank-upgrade option (level, currency, cost) |
| `XMLParser.java`   | Parses board.xml and cards.xml into model objects |

---

## How to Compile

Make sure `board.xml` and `cards.xml` are in the same directory as the `.java` files.

```bash
javac *.java
```

If your environment only has a JRE (no `javac` in PATH), use:

```bash
java -classpath /usr/share/java/tools.jar com.sun.tools.javac.Main *.java
```

---

## How to Run

```bash
java Deadwood <numPlayers>
```

**numPlayers** must be an integer from 2 to 8.

Optional: provide custom XML paths:
```bash
java Deadwood <numPlayers> <path/to/board.xml> <path/to/cards.xml>
```

### Examples

```bash
java Deadwood 3
java Deadwood 4 board.xml cards.xml
```

---

## Commands (case-insensitive)

| Command | Description |
|---------|-------------|
| `who`                           | Print active player's info (rank, $, credits, current role) |
| `where`                         | Print active player's location |
| `board` / `players`             | Print all players and their locations |
| `move <room>`                   | Move to an adjacent room |
| `work <role>` / `role <role>`   | Take a role at the current filming set |
| `act`                           | Act on your current role (roll dice) |
| `rehearse`                      | Rehearse – gain +1 bonus chip for next act roll |
| `upgrade <rank> <dollar\|credit>` | Upgrade your rank at the Casting Office |
| `roles`                         | List available roles at your current location |
| `upgrades`                      | List rank upgrade costs (must be at Casting Office) |
| `end`                           | End your current turn |
| `end game` / `quit`             | Force-end the game (for testing) |
| `help`                          | Show the command list |

---

## Example Interaction

```
> who
player blue ($0, 0cr, rank 1)
> where
blue is in Trailer
> move Main Street
blue moves to Main Street shooting Law and the Old West scene 20
> roles
  [Extra roles]
    Railroad Worker (level 1) [extra]
    Falls off Roof (level 2) [extra] [rank too low]
    ...
  [Starring roles - Law and the Old West]
    Rug Merchant (level 1) [starring]
    ...
> work Crusty Prospector
blue takes the extra role: Crusty Prospector (level 1)
  Line: "Aww, peaches!"
> act
blue rolls a 4 (+0 rehearsal) = 4 vs budget 3
Success! blue earns $1. Takes remaining: 2
> end
```

---

## Game Rules Summary

### Setup
- **2–3 players**: 3 days; **4–8 players**: 4 days.
- Starting dollars: 2-4 players: $0; 5: $1; 6: $2; 7-8: $3.
- All players start at the Trailer.
- 10 scene cards are randomly dealt to the 10 filming sets each day.

### Player Turn
Each turn, a player may:
1. **Move** to an adjacent room (only if not working a role)
2. **Take a role** at their current Set (must meet the role's rank requirement)
3. **Act** on their current role (1d6 + rehearsal chips vs. scene budget)
4. **Rehearse** instead of acting (adds +1 chip; chips < budget)
5. **Upgrade** rank at the Casting Office
6. **End** their turn

### Acting
- **Success** (roll ≥ budget): remove one shot counter.
  - *Starring role*: earn 2 credits.
  - *Extra role*: earn $1.
- **Failure** (roll < budget): no reward.

### Scene Wrap
When the last shot counter is removed:
- Budget dice are rolled, sorted descending, and distributed round-robin to
  on-card players sorted by role-level descending.
- All players at that set are released from their roles.
- If only 1 (or 0) active scenes remain on the board, the day ends.

### Upgrading (Casting Office)
| Target Rank | Dollar Cost | Credit Cost |
|-------------|-------------|-------------|
| 2 | $4 | 5 cr |
| 3 | $10 | 10 cr |
| 4 | $18 | 15 cr |
| 5 | $28 | 20 cr |
| 6 | $40 | 25 cr |

### End of Game
After all days: **score = dollars + credits + rank**. Highest score wins.

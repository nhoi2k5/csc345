/**
 * Parses the two game XML data files (board.xml and cards.xml) and
 * produces the corresponding model objects.
 *
 * Single Responsibility: this class is only responsible for XML I/O.
 * All game-logic decisions are handled by GameManager.
 *
 * CSCI 345 – Deadwood Assignment 2
 */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Builds a DOM Document from the given file path.
     * @param filename Path to the XML file
     * @return Parsed Document
     * @throws Exception on any I/O or parse failure
     */
    private Document getDocument(String filename) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(filename));
    }

    /**
     * Returns the first *direct child* Element with the given tag name,
     * avoiding false matches from deeper descendants.
     */
    private Element getFirstChildElement(Element parent, String tag) {
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && tag.equals(n.getNodeName())) {
                return (Element) n;
            }
        }
        return null;
    }

    /**
     * Returns all *direct child* Elements with the given tag name.
     */
    private List<Element> getDirectChildren(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && tag.equals(n.getNodeName())) {
                result.add((Element) n);
            }
        }
        return result;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Parses board.xml and returns a fully wired Board.
     * After this call every room has its neighbor list populated.
     *
     * @param filename Path to board.xml
     * @return Initialised Board
     */
    public Board parseBoard(String filename) throws Exception {
        Board board = new Board();
        Document doc = getDocument(filename);
        Element root = doc.getDocumentElement();

        // ── Parse <set> elements ─────────────────────────────────────────────
        NodeList setNodes = root.getElementsByTagName("set");
        for (int i = 0; i < setNodes.getLength(); i++) {
            Element setElem = (Element) setNodes.item(i);
            String  setName = setElem.getAttribute("name");
            Set     set     = new Set(setName);

            // Parse takes
            Element takesElem = getFirstChildElement(setElem, "takes");
            if (takesElem != null) {
                for (Element te : getDirectChildren(takesElem, "take")) {
                    int num = Integer.parseInt(te.getAttribute("number"));
                    set.addTake(new Take(num));
                }
            }

            // Parse off-card (extra) parts
            Element partsElem = getFirstChildElement(setElem, "parts");
            if (partsElem != null) {
                for (Element pe : getDirectChildren(partsElem, "part")) {
                    String partName  = pe.getAttribute("name");
                    int    partLevel = Integer.parseInt(pe.getAttribute("level"));
                    String line      = pe.getElementsByTagName("line")
                                         .item(0).getTextContent().trim();
                    set.addExtra(new Role(partName, partLevel, line, false));
                }
            }

            board.addRoom(set);
        }

        // ── Parse <trailer> ──────────────────────────────────────────────────
        Element trailerElem = (Element) root.getElementsByTagName("trailer").item(0);
        Trailer trailer = new Trailer();
        board.addRoom(trailer);

        // ── Parse <office> ───────────────────────────────────────────────────
        Element officeElem = (Element) root.getElementsByTagName("office").item(0);
        CastingOffice office = new CastingOffice();
        for (Element ue : getDirectChildren(officeElem, "upgrades")) {
            // nothing – proceed to upgrades
        }
        Element upgradesElem = getFirstChildElement(officeElem, "upgrades");
        if (upgradesElem != null) {
            for (Element ue : getDirectChildren(upgradesElem, "upgrade")) {
                int    level    = Integer.parseInt(ue.getAttribute("level"));
                String currency = ue.getAttribute("currency");
                int    amt      = Integer.parseInt(ue.getAttribute("amt"));
                office.addUpgrade(new Upgrade(level, currency, amt));
            }
        }
        board.addRoom(office);

        // ── Wire neighbors ────────────────────────────────────────────────────
        // Sets
        for (int i = 0; i < setNodes.getLength(); i++) {
            Element  setElem   = (Element) setNodes.item(i);
            String   setName   = setElem.getAttribute("name");
            Room     room      = board.getRoom(setName.toLowerCase());
            Element  neighborsElem = getFirstChildElement(setElem, "neighbors");
            if (neighborsElem != null) {
                for (Element ne : getDirectChildren(neighborsElem, "neighbor")) {
                    String nbName = ne.getAttribute("name");
                    Room   nb     = board.getRoom(nbName.toLowerCase());
                    if (nb != null) room.addNeighbor(nb);
                }
            }
        }

        // Trailer neighbors
        Element trailerNbs = getFirstChildElement(trailerElem, "neighbors");
        if (trailerNbs != null) {
            for (Element ne : getDirectChildren(trailerNbs, "neighbor")) {
                String nbName = ne.getAttribute("name");
                Room   nb     = board.getRoom(nbName.toLowerCase());
                if (nb != null) trailer.addNeighbor(nb);
            }
        }

        // Office neighbors
        Element officeNbs = getFirstChildElement(officeElem, "neighbors");
        if (officeNbs != null) {
            for (Element ne : getDirectChildren(officeNbs, "neighbor")) {
                String nbName = ne.getAttribute("name");
                Room   nb     = board.getRoom(nbName.toLowerCase());
                if (nb != null) office.addNeighbor(nb);
            }
        }

        return board;
    }

    /**
     * Parses cards.xml and returns all 40 SceneCards.
     *
     * @param filename Path to cards.xml
     * @return List of SceneCard objects (not yet shuffled)
     */
    public List<SceneCard> parseCards(String filename) throws Exception {
        List<SceneCard> cards = new ArrayList<>();
        Document doc = getDocument(filename);
        Element  root = doc.getDocumentElement();

        NodeList cardNodes = root.getElementsByTagName("card");
        for (int i = 0; i < cardNodes.getLength(); i++) {
            Element cardElem   = (Element) cardNodes.item(i);
            String  cardName   = cardElem.getAttribute("name");
            int     budget     = Integer.parseInt(cardElem.getAttribute("budget"));

            Element sceneElem  = (Element)
                    cardElem.getElementsByTagName("scene").item(0);
            int     sceneNum   = Integer.parseInt(sceneElem.getAttribute("number"));
            String  desc       = sceneElem.getTextContent().trim();

            SceneCard card = new SceneCard(cardName, budget, sceneNum, desc);

            // On-card (starring) parts
            NodeList partNodes = cardElem.getElementsByTagName("part");
            for (int j = 0; j < partNodes.getLength(); j++) {
                Element pe       = (Element) partNodes.item(j);
                String  partName = pe.getAttribute("name");
                int     level    = Integer.parseInt(pe.getAttribute("level"));
                String  line     = pe.getElementsByTagName("line")
                                     .item(0).getTextContent().trim();
                card.addRole(new Role(partName, level, line, true));
            }

            cards.add(card);
        }
        return cards;
    }
}

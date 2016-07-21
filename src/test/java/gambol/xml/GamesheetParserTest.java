package gambol.xml;

import java.net.URL;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author osa
 */
public class GamesheetParserTest {

    Schema schema;
    JAXBContext jaxbContext;

    @Before
    public void setUp() throws JAXBException, SAXException {
        jaxbContext = JAXBContext.newInstance(Gamesheet.class);

        assertNotNull(jaxbContext);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = sf.newSchema(getClass().getResource("/gambol.xsd"));
    }

    private Gamesheet parseGame(String path) throws JAXBException {
        URL src = getClass().getResource(path);
        assertNotNull(path, src);
        Unmarshaller um = jaxbContext.createUnmarshaller();
        assertNotNull(um);
        um.setSchema(schema);
        Gamesheet game = (Gamesheet)um.unmarshal(src);
        assertNotNull(game);
        assertEquals(2, game.getRosters().size());
        
        return game;
    }

    @Test
    public void parseGame31597() throws JAXBException {
        Gamesheet game = parseGame("/sample/game-31597.xml");
        
    }

    @Test
    public void parseGame31811() throws JAXBException {
        Gamesheet game = parseGame("/sample/game-31811.xml");
    }

    @Test
    public void parseGame25977() throws JAXBException {
        Gamesheet game = parseGame("/sample/game-25977.xml");
        
        assertEquals(30, game.getSpectators().intValue());
        
        for (Roster r : game.getRosters())
            if (FixtureSideRole.HOME.equals(r.getSide())) {
                assertEquals(18, r.getPlayers().size());
                assertEquals(2, r.getOfficials().size());
            }
            else {
                assertEquals(11, r.getPlayers().size());
                assertEquals(0, r.getOfficials().size());
            }
        
        assertEquals(7, game.getEvents().getGoalsAndPenalties().size());
        
        int eeHome = 0, eeAway = 0;
        for (Event e : game.getEvents().getGoalsAndPenalties())
            if (e instanceof GoalEvent) {
                if (FixtureSideRole.HOME.equals(e.getSide()))
                  eeHome++;
                else
                    eeAway++;
            }
        assertEquals(5, eeHome);
        assertEquals(2, eeAway);
        
        int psHome = 0, psAway = 0;
        for (PeriodSummary ps : game.getGamePeriods()) {
            psHome += ps.getGoals().getHome();
            psAway += ps.getGoals().getAway();
        }
        assertEquals(5, psHome);
        assertEquals(2, psAway);
        
    }
}

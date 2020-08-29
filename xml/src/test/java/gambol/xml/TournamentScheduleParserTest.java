package gambol.xml;

import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 * @author osa
 */
public class TournamentScheduleParserTest {

    JAXBContext jaxbContext;    
    Schema schema;

    @Before
    public void setUp() throws JAXBException, SAXException {
        jaxbContext = JAXBContext.newInstance(Tournament.class);

        assertNotNull(jaxbContext);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = sf.newSchema(getClass().getResource("/gambol.xsd"));
    }

    private Tournament parseTournament(String path) throws JAXBException {
        URL src = getClass().getResource(path);

        Unmarshaller um = jaxbContext.createUnmarshaller();
        Tournament cal = (Tournament)um.unmarshal(src);
        assertNotNull(cal);
        
        int n = 0;
        for (Fixture f : cal.getFixtures()) {
            Side home = f.getSides().get(0);
            Side away = f.getSides().get(1);
            System.out.println(f.getStartTime() + ": " + home.getTeam().value + "-" + away.getTeam().value + " " + home.getScore() + "-" + away.getScore() + " (" + f.getSourceRef()+")");
            ++n;
        }
        
        return cal;
    }

    @Test
    public void parseTournament1156() throws JAXBException {
        Tournament game = parseTournament("/sample/tournament-1156.xml");
        assertNotNull(game);
        
        assertEquals(56, game.fixtures.size());
    }
    

    @Test
    public void parseTournament973() throws JAXBException {
        Tournament game = parseTournament("/sample/tournament-973.xml");
        
        assertEquals(56, game.fixtures.size());
    }
}

package gambol.xml;

import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author osa
 */
public class GambolParserTest {

    JAXBContext jaxbContext;    
    Schema schema;

    @Before
    public void setUp() throws JAXBException, SAXException {
        jaxbContext = JAXBContext.newInstance(Gambol.class);

        assertNotNull(jaxbContext);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = sf.newSchema(getClass().getResource("/gambol.xsd"));
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void parseTournamentList() throws JAXBException {
        URL src = getClass().getResource("/sample/tournaments-2013_14.xml");

        Unmarshaller um = jaxbContext.createUnmarshaller();
        um.setSchema(schema);
        Gambol cal = (Gambol)um.unmarshal(src);
        assertNotNull(cal);

        int n = 0;
        for (Section s : cal.getSections())
            for (TournamentTemp t : s.getTournaments()) 
                ++n;
        
        assertEquals(88, n);
    }

    @Test
    public void parseTournament973() throws JAXBException {
        URL src = getClass().getResource("/sample/tournament-973.xml");

        Unmarshaller um = jaxbContext.createUnmarshaller();
        Gambol cal = (Gambol)um.unmarshal(src);
        assertNotNull(cal);
        
        int n = 0;
        for (Fixture f : cal.getTournament().getFixtures()) {
            Side home = f.getSides().get(0);
            Side away = f.getSides().get(1);
            System.out.println(f.getStartTime() + ": " + home.getTeam() + "-" + away.getTeam() + " " + home.getScore() + "-" + away.getScore() + " (" + f.getSourceRef()+")");
            ++n;
        }
        
        assertEquals(56, n);
    }
}

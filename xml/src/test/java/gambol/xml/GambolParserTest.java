package gambol.xml;

import java.net.URL;
import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
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
        schema = sf.newSchema(getClass().getClassLoader().getResource("META-INF/xsd/gambol.xsd"));
        assertNotNull(schema);
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
            for (Tournament t : s.getTournaments())
                ++n;

        assertEquals(88, n);
    }
}

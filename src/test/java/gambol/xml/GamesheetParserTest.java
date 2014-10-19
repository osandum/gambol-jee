package gambol.xml;

import java.net.URL;
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
        return (Gamesheet)um.unmarshal(src);
    }

    @Test
    public void parseGame31597() throws JAXBException {
        Gamesheet game = parseGame("/sample/game-31597.xml");
        assertNotNull(game);
    }

    @Test
    public void parseGame31811() throws JAXBException {
        Gamesheet game = parseGame("/sample/game-31811.xml");
        assertNotNull(game);
    }

    @Test
    public void parseGame25977() throws JAXBException {
        Gamesheet game = parseGame("/sample/game-25977.xml");
        assertNotNull(game);
    }
}

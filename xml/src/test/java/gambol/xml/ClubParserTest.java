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
public class ClubParserTest {

    Schema schema;
    JAXBContext jaxbContext;

    @Before
    public void setUp() throws JAXBException, SAXException {
        jaxbContext = JAXBContext.newInstance(Club.class);

        assertNotNull(jaxbContext);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = sf.newSchema(getClass().getResource("/gambol.xsd"));

    }

    @After
    public void tearDown() {
    }

    @Test
    public void parseClub() throws JAXBException {
        URL src = getClass().getResource("/sample/club-ksf.xml");
        Unmarshaller um = jaxbContext.createUnmarshaller();
        um.setSchema(schema);
        Club ksf = (Club) um.unmarshal(src);
        assertNotNull(ksf);
    }
}

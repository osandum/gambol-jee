package net.sandum.xml;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author osa
 */
public class GambolHelper {
    private GambolHelper() {}

    public static Schema getSchema() {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            return sf.newSchema(GambolHelper.class.getResource("/gambol.xsd"));
        }
        catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }
}

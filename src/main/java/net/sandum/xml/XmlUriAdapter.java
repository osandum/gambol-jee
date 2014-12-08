package net.sandum.xml;

import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author osa
 */
public class XmlUriAdapter
        extends XmlAdapter<String, URI> {

    @Override
    public URI unmarshal(String s) {
        return URI.create(s);
    }

    @Override
    public String marshal(URI uri) {
        return uri == null ? null : uri.toString();
    }

}

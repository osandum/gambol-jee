package net.sandum.xml;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author osa
 */
public class XmlDateTimeAdapter extends XmlAdapter<String, Date> {

    private DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public Date unmarshal(String s) throws ParseException {
        return DF.parse(s);
    }

    @Override
    public String marshal(Date d) {
        return DF.format(d);
    }
    
}

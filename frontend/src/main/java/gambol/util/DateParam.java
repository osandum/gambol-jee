package gambol.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author osa
 */
public class DateParam {

    private final Date value;

    private DateParam(Date d) {
        this.value = d;
    }
    
    public static DateParam valueOf(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = sdf.parse(s);
            return new DateParam(d);
        }
        catch (ParseException ex) {
            throw new RuntimeException("bad date format", ex);
        }
    }
    
    public Date getValue() {
        return value;
    }
}

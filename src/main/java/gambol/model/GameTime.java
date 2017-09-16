package gambol.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author osa
 */
public class GameTime {

    private final static Pattern MMMM_SS = Pattern.compile("(\\d+):(\\d+)");

    public static Integer parse(String mmmm_ss) {
        Matcher m = MMMM_SS.matcher(mmmm_ss);
        if (!m.matches())
            throw new IllegalArgumentException(mmmm_ss);
        int mm = Integer.parseInt(m.group(1));
        int ss = Integer.parseInt(m.group(2));
        return mm * 60 + ss;
    }

    public static String format(Integer gameSec) {
        return gameSec == null ? null : String.format("%d:%02d", gameSec / 60, gameSec % 60);
    }
    
}

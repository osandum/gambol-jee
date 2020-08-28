package gambol.model;

import java.text.Normalizer;
import java.util.Random;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
public class ModelUtil {

    private final static Logger LOG = LoggerFactory.getLogger(ModelUtil.class);
    
    private final static Pattern DIACRIT = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private final static Pattern LIG_A = Pattern.compile("[æÆ]");
    private final static Pattern LIG_O = Pattern.compile("[øØꟹŒœ]");
    
    public static String unaccent(String s) {
        String decomposed = Normalizer.normalize(s, Normalizer.Form.NFD);
        LOG.info("# decompose('{}') = '{}'", s, decomposed);
        String reduced = DIACRIT.matcher(decomposed).replaceAll("");
        reduced = LIG_A.matcher(reduced).replaceAll("a");
        reduced = LIG_O.matcher(reduced).replaceAll("o");
        LOG.info("# reduce('{}') = '{}'", decomposed, reduced);
        return reduced;
    }    

    public static String asSlug(String name, int maxLength) {
        String slug = unaccent(name)
                .replaceAll("[-_/,. ]+", "-")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "")
                .toLowerCase();
        if (slug.length() > maxLength)
            slug = slug.substring(0, maxLength);
        return slug;
    }

    private final static Random RAND = new Random();
    
    public static String oneOf(String alpha) {
        int pos = RAND.nextInt(alpha.length());
        return alpha.substring(pos, pos+1);
    }
}

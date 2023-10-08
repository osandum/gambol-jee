package gambol.source;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author osa
 */
public abstract class FixtureSource {

  public abstract URI getGamesheetUrl();
  public abstract URI getLiveMinutesUrl();

  private static final Pattern DIU_REF =
          Pattern.compile("^DIU:(?<tournamentNumber>[^:]*):(?<fixtureNumber>[^:]*):(?<homeSide>[^:]*):(?<awaySide>[^:]*)$");

  public static FixtureSource forSourceRef(String sourceRef) {
    Matcher m = DIU_REF.matcher(sourceRef);
    if (m.matches()) {
      final String fixtureNumber = m.group("fixtureNumber");
      if (fixtureNumber.matches("\\d+"))
        return new FixtureSource() {
          public URI getGamesheetUrl() {
            return URI.create("http://www.hockeyligaen.dk/gamesheet2.aspx?GameId=" + fixtureNumber);
          }
          public URI getLiveMinutesUrl() {
            return URI.create("http://www.hockeyligaen.dk/livedetails.aspx?GameId=" + fixtureNumber);
          }
        };

      final String tournamentNumber = m.group("tournamentNumber");
      if (tournamentNumber.matches("\\d+"))
        return new FixtureSource() {
          public URI getGamesheetUrl() {
            return URI.create("http://stats.sportsadmin.dk/schedule.aspx?tournamentID=" + tournamentNumber);
          }
          public URI getLiveMinutesUrl() {
            return URI.create("http://stats.sportsadmin.dk/schedule.aspx?tournamentID=" + tournamentNumber);
          }
        };
    }

    return NULL;
  }

  private static FixtureSource NULL = new FixtureSource() {
    public URI getGamesheetUrl() {
      return null;
    }

    public URI getLiveMinutesUrl() {
      return null;
    }
  };
}

package gambol.ejb;

import java.util.Date;

/**
 * @author osa
 */
public class PlayersQueryParam {
    String name;
    Date yearOfBirth;
    FixturesQueryParam playedGamesQuery;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Date yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public FixturesQueryParam getPlayedGamesQuery() {
        return playedGamesQuery;
    }

    public void setPlayedGamesQuery(FixturesQueryParam playedGamesQuery) {
        this.playedGamesQuery = playedGamesQuery;
    }

    @Override
    public String toString() {
        return "PlayersQueryParam{" + "name=" + name + ", yearOfBirth=" + yearOfBirth + ", playedGamesQuery=" + playedGamesQuery + '}';
    }
}

package gambol.ejb;

import java.util.Date;

/**
 * @author osa
 */
public class PlayersQueryParam {
    String firstName;
    String lastName;
    Date yearOfBirth;
    FixturesQueryParam playedGamesQuery;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
        return "PlayersQueryParam{" + "firstName=" + firstName + ", lastName=" + lastName + ", yearOfBirth=" + yearOfBirth + ", playedGamesQuery=" + playedGamesQuery + '}';
    }
}

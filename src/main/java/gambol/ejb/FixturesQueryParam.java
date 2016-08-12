package gambol.ejb;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author osa
 */
public class FixturesQueryParam implements Serializable {

    Date start; 
    Date end; 
    List<String> seasonId; 
    List<String> seriesId; 
    List<String> tournamentRef;
    List<String> clubRef;
    List<String> homeClubRef;
    List<String> awayClubRef;
    Boolean hasGamesheet;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Boolean hasGamesheet() {
        return hasGamesheet;
    }

    public void setGamesheet(Boolean gamesheet) {
        this.hasGamesheet = gamesheet;
    }

    public List<String> getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(List<String> seasonId) {
        this.seasonId = seasonId;
    }

    public List<String> getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(List<String> seriesId) {
        this.seriesId = seriesId;
    }

    public List<String> getTournamentRef() {
        return tournamentRef;
    }

    public void setTournamentRef(List<String> tournamentRef) {
        this.tournamentRef = tournamentRef;
    }

    public List<String> getClubRef() {
        return clubRef;
    }

    public void setClubRef(List<String> clubRef) {
        this.clubRef = clubRef;
    }

    public List<String> getHomeClubRef() {
        return homeClubRef;
    }

    public void setHomeClubRef(List<String> homeClubRef) {
        this.homeClubRef = homeClubRef;
    }

    public List<String> getAwayClubRef() {
        return awayClubRef;
    }

    public void setAwayClubRef(List<String> awayClubRef) {
        this.awayClubRef = awayClubRef;
    }

    @Override
    public String toString() {
        return "FixturesQueryParam{" + "start=" + start + ", end=" + end + ", seasonId=" + seasonId + ", seriesId=" + seriesId + ", tournamentRef=" + tournamentRef + ", clubRef=" + clubRef + ", homeClubRef=" + homeClubRef + ", awayClubRef=" + awayClubRef + ", sheet=" + hasGamesheet + '}';
    }
}

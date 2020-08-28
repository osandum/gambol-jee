package gambol.ejb;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author osa
 */
public class FixturesQueryParam implements Serializable {

    private final static List<String> _EMPTY = Collections.emptyList();
    
    Date start;
    Date end;
    List<String> seasonId = _EMPTY;
    List<String> seriesId = _EMPTY;
    List<String> tournamentRef = _EMPTY;
    String sourcePrefix;
    List<String> clubRef = _EMPTY;
    List<String> homeClubRef = _EMPTY;
    List<String> awayClubRef = _EMPTY;
    Boolean hasGamesheet;
    String lastFixtureRef;
    Integer maxResults;
    Boolean reverseChrono;

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

    public String getSourcePrefix() {
        return sourcePrefix;
    }

    public void setSourcePrefix(String sourcePrefix) {
        this.sourcePrefix = sourcePrefix;
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

    public String getLastFixtureRef() {
        return lastFixtureRef;
    }

    public void setLastFixtureRef(String ref) {
        this.lastFixtureRef = ref;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Boolean getReverseChrono() {
        return reverseChrono;
    }

    public void setReverseChrono(Boolean reverse) {
        this.reverseChrono = reverse;
    }

    @Override
    public String toString() {
        return "FixturesQueryParam{" + "start=" + start + ", end=" + end + ", seasonId=" + seasonId + ", seriesId=" + seriesId + ", tournamentRef=" + tournamentRef + ", sourcePrefix=" + sourcePrefix + ", clubRef=" + clubRef + ", homeClubRef=" + homeClubRef + ", awayClubRef=" + awayClubRef + ", sheet=" + hasGamesheet + ", after=" + lastFixtureRef + ", max=" + maxResults + '}';
    }
}

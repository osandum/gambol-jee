package gambol.model;

import gambol.xml.FixtureSideRole;
import static gambol.xml.FixtureSideRole.AWAY;
import gambol.xml.ScheduleStatus;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author osa
 */
@Entity(name = "fixture")
public class FixtureEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 64, nullable = false, unique = true)
    private String sourceRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    @ManyToOne(optional = false)
    private TournamentEntity tournament;
    
    @ManyToOne(cascade = CascadeType.ALL)
    private FixtureSideEntity homeSide;

    @ManyToOne(cascade = CascadeType.ALL)
    private FixtureSideEntity awaySide;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    @Column(length = 16)
    private String matchNumber;
    
    public TournamentEntity getTournament() {
        return tournament;
    }

    public void setTournament(TournamentEntity tournament) {
        this.tournament = tournament;
    }

    public FixtureSideEntity getHomeSide() {
        return homeSide;
    }

    public void setHomeSide(FixtureSideEntity homeSide) {
        this.homeSide = homeSide;
    }

    public FixtureSideEntity getAwaySide() {
        return awaySide;
    }

    public void setAwaySide(FixtureSideEntity awaySide) {
        this.awaySide = awaySide;
    }
    
    public FixtureSideEntity getSide(FixtureSideRole role) {
        return role == AWAY ? getAwaySide() : getHomeSide();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date estimateEndTime() {
        if (getEndTime() != null)
            return getEndTime();
        if (getStartTime() == null)
            return null;

        Calendar d = Calendar.getInstance();
        d.setTime(getStartTime());
        int durationMinutes = getTournament().getSeries().getFixtureDuration();
        d.add(Calendar.MINUTE, durationMinutes);
        Date eet = d.getTime();
        
        Date curfew = getHomeSide().getTeam().getClub().curfewAfter(getStartTime());
        return curfew != null && eet.after(curfew) ? curfew : eet;
    }
    
    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        this.matchNumber = matchNumber;
    }
    
    public String getEventDescription() {
        // Create the event
        return tournament.getName() + ", kamp " + matchNumber;
    }

    public String getEventTitle() {
        String eventName = homeSide.getTeam().getName() + " \u2013 " + awaySide.getTeam().getName();
        eventName += " (" + tournament.getSeries().getName() + ")";
        if (homeSide.getScore() != null && awaySide.getScore() != null) {
            eventName += ": " + homeSide.getScore() + "-" + awaySide.getScore();
        }        
        return eventName;
    }

    @Override
    public String toString() {
        return "{[" + sourceRef + "] id=" + id + " home=" + homeSide + " away=" + awaySide + "}";
    }
}

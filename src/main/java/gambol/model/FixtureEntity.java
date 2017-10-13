package gambol.model;

import gambol.xml.FixtureSideRole;
import static gambol.xml.FixtureSideRole.AWAY;
import gambol.xml.GamesheetStatus;
import gambol.xml.ScheduleStatus;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
@Entity(name = "fixture")
public class FixtureEntity implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(FixtureEntity.class);

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Timestamp lastModified;

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
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_tournament"))
    private TournamentEntity tournament;
    
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_home"))
    private FixtureSideEntity homeSide;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_away"))
    private FixtureSideEntity awaySide;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fixture", orphanRemoval = true)
    @OrderBy("gameTimeSecond")
    private List<FixtureEventEntity> events;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private ScheduleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private GamesheetStatus sheet;

    @Column(length = 15, name="match_number")
    private String matchNumber;

    @Column(length = 31, name="title")
    private String titleAnnotation;

    @Column(length = 255, name = "description")
    private String descriptionAnnotation;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_arena"))
    private ClubEntity arena;
    
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

    
    public boolean isGamesheetLoaded() {
        if (!ScheduleStatus.CONFIRMED.equals(status))
            return false; // game not scheduled
        if (endTime.after(new Date()))
            return false; // game not finished
        if (!getHomeSide().isGameDetailsLoaded())
            return false; // no home players listed
        if (!getAwaySide().isGameDetailsLoaded())
            return false; // no away players listed
        if (getEvents().isEmpty())
            return false; // no game events listed -- boooring
        
        return true;
    }

    public ClubEntity getArena() {
        return arena;
    }

    public void setArena(ClubEntity location) {
        this.arena = location;
    }

    public ClubEntity resolveArena() {
        ClubEntity a = getArena();
        if (a == null)
            a = getTournament().getArena();
        if (a == null)
            a = getHomeSide().getTeam().getClub();
        return a;
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


    public List<FixtureEventEntity> getEvents() {
        return events;
    }

    public void setEvents(List<FixtureEventEntity> events) {
        this.events = events;
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
        
        ClubEntity homeClub = getHomeSide().getTeam().getClub();        
        Date curfew = homeClub.curfewAfter(getStartTime());
        if (curfew != null && eet.after(curfew)) {
            LOG.info("{}: culling estimated end-time {} to {} (as per {} rules)", sourceRef, eet, curfew, homeClub.getName());
            eet = curfew;
        }
        
        return eet;
    }
    
    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public GamesheetStatus getSheet() {
        return sheet;
    }

    public void setSheet(GamesheetStatus sheet) {
        this.sheet = sheet;
    }

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        this.matchNumber = matchNumber;
    }

    public String getTitleAnnotation() {
        return titleAnnotation;
    }

    public void setTitleAnnotation(String titleAnnotation) {
        this.titleAnnotation = titleAnnotation;
    }

    public String getDesciptionAnnotation() {
        return descriptionAnnotation;
    }

    public void setDesciptionAnnotation(String desciptionAnnotation) {
        this.descriptionAnnotation = desciptionAnnotation;
    }
    
    public String getEventDescription() {
        String descr = tournament.getName() + ", kamp " + matchNumber;
        if (!StringUtils.isBlank(descriptionAnnotation))
            descr += "\n\n" + descriptionAnnotation;
        return descr;
    }

    public String getEventTitle() {
        String eventName = homeSide.getTeam().getName() + " \u2013 " + awaySide.getTeam().getName();
        eventName += " (" + tournament.getSeries().getName() + ")";
        if (homeSide.getScore() != null && awaySide.getScore() != null) {
            eventName += ": " + homeSide.getScore() + "-" + awaySide.getScore();
        }
        if (!StringUtils.isBlank(titleAnnotation))
            eventName = titleAnnotation + " " + eventName;

        return eventName;
    }

    @Override
    public String toString() {
        return "{[" + sourceRef + "] id=" + id + " home=" + homeSide + " away=" + awaySide + "}";
    }
}

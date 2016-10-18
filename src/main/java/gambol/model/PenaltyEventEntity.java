package gambol.model;

import gambol.ejb.App;
import gambol.xml.Event;
import gambol.xml.GameOffense;
import gambol.xml.PenaltyEvent;
import gambol.xml.PlayerRef;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriInfo;

/**
 * @see  http://en.wikipedia.org/wiki/Penalty_(ice_hockey)
 * @author osa
 */
@Entity(name = "penalty_event")
@DiscriminatorValue("PLTY")
public class PenaltyEventEntity extends FixtureEventEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 7)
    private GameOffense offense;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_player"))
    private FixturePlayerEntity player;

    @NotNull
    @Column(name = "penalty_minutes") //, nullable = false) wont't fly on single-table inheritance
    private Integer penaltyMinutes;

    @NotNull
    @Column(name = "starttime_second")
    private Integer starttimeSecond;

    @Column(name = "endtime_second")
    private Integer endtimeSecond;


    public GameOffense getOffense() {
        return offense;
    }

    public void setOffense(GameOffense offense) {
        this.offense = offense;
    }


    public FixturePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(FixturePlayerEntity player) {
        this.player = player;
    }

    public Integer getStarttimeSecond() {
        return starttimeSecond;
    }

    public void setStarttimeSecond(Integer sec) {
        this.starttimeSecond = sec;
    }


    public Integer getEndtimeSecond() {
        return endtimeSecond;
    }

    public void setEndtimeSecond(Integer sec) {
        this.endtimeSecond = sec;
    }


    public Integer getPenaltyMinutes() {
        return penaltyMinutes;
    }

    public void setPenaltyMinutes(Integer minutes) {
        this.penaltyMinutes = minutes;
    }


    @Override
    public String toString() {
        return "[" + getOffense() + "("+penaltyMinutes+"):"+getId()+" " + getGameTimeSecond()/60 + ":"+getGameTimeSecond()%60+" " + getPlayer() + "]";
    }

    @Override
    public String signature() {
        return super.signature() +
                String.format(":P:%d:%s:%d:%d:%d", player.getId(), offense, penaltyMinutes, starttimeSecond, endtimeSecond);
    }

    @Override
    public boolean usesPlayer(FixturePlayerEntity unused) {
        return player.equals(unused);
    }

    public Event asXml(UriInfo uriInfo) {
        PenaltyEvent pe = new PenaltyEvent();

        PlayerRef pr = new PlayerRef();
        pr.setNumber(getPlayer().getJerseyNumber());
        pe.setPlayer(pr);
        pe.setSide(getSide());
        pe.setTime(GameTime.format(getGameTimeSecond()));

        pe.setOffense(getOffense());
        pe.setMinutes(getPenaltyMinutes());
        pe.setStartTime(GameTime.format(getStarttimeSecond()));
        pe.setEndTime(GameTime.format(getEndtimeSecond()));

        return pe;
    }
}

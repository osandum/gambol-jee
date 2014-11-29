package gambol.model;

import gambol.xml.GameOffense;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

/**
 * @see  http://en.wikipedia.org/wiki/Penalty_(ice_hockey)
 * @author osa
 */
@Entity(name = "penalty_event")
@DiscriminatorValue("PLTY")
public class PenaltyEventEntity extends FixtureEventEntity {

    @Enumerated(EnumType.STRING)
    private GameOffense offense;

    public GameOffense getOffense() {
        return offense;
    }

    public void setOffense(GameOffense offense) {
        this.offense = offense;
    }


    @ManyToOne(optional = false)
    private FixturePlayerEntity player;

    public FixturePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(FixturePlayerEntity player) {
        this.player = player;
    }


    @Column(name = "endtime_second") //, nullable = false) wont't fly on single-table inheritance
    private Integer endtimeSecond;

    public Integer getEndtimeSecond() {
        return endtimeSecond;
    }

    public void setEndtimeSecond(Integer endtime) {
        this.endtimeSecond = endtime;
    }


    @Column(name = "penalty_minutes") //, nullable = false) wont't fly on single-table inheritance
    private Integer penaltyMinutes;

    public Integer getPenaltyMinutes() {
        return penaltyMinutes;
    }

    public void setPenaltyMinutes(Integer minutes) {
        this.penaltyMinutes = minutes;
    }


    @Override
    public String toString() {
        return "["+getId()+" " + getGameTimeSecond()/60 + ":"+getGameTimeSecond()%60+" " + getPlayer() + "]";
    }
    
}

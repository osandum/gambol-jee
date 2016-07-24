package gambol.model;

import gambol.xml.GameSituation;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;

/**
 * @author osa
 */
@Entity(name = "goal_event")
@DiscriminatorValue("GOAL")
public class GoalEventEntity extends FixtureEventEntity {

    @Enumerated(EnumType.STRING)
    private GameSituation gameSituation;

    public GameSituation getGameSituation() {
        return gameSituation;
    }

    public void setGameSituation(GameSituation situation) {
        this.gameSituation = situation;
    }


    @ManyToOne(optional = false)
    private FixturePlayerEntity player;

    public FixturePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(FixturePlayerEntity player) {
        this.player = player;
    }


    @ManyToMany
    @OrderColumn
    @JoinTable(name = "goal_assist")
    private List<FixturePlayerEntity> assists;

    public List<FixturePlayerEntity> getAssists() {
        return assists;
    }

    public void setAssists(List<FixturePlayerEntity> players) {
        this.assists = players;
    }


    @ManyToMany
    @JoinTable(name = "goal_participation_pos")
    private Set<FixturePlayerEntity> positiveParticipants;

    public Set<FixturePlayerEntity> getPositiveParticipants() {
        return positiveParticipants;
    }

    public void setPositiveParticipants(Set<FixturePlayerEntity> players) {
        this.positiveParticipants = players;
    }


    @ManyToMany
    @JoinTable(name = "goal_participation_neg")
    private Set<FixturePlayerEntity> negativeParticipants;

    public Set<FixturePlayerEntity> getNegativeParticipants() {
        return negativeParticipants;
    }

    public void setNegativeParticipants(Set<FixturePlayerEntity> players) {
        this.negativeParticipants = players;
    }

    @Override
    public String toString() {
        return "[GOAL:"+getId()+" " + getGameTimeSecond()/60 + ":"+getGameTimeSecond()%60+" " + getPlayer() + "]";
    }
}

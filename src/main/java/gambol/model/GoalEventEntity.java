package gambol.model;

import gambol.xml.GameSituation;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;

/**
 * @author osa
 */
@Entity(name = "goal_event")
@DiscriminatorValue("GOAL")
public class GoalEventEntity extends FixtureEventEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "game_situation", length = 7)
    private GameSituation gameSituation;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_player"))
    private FixturePlayerEntity player;

    @ManyToMany
    @OrderColumn
    @JoinTable(name = "goal_assist")
    private List<FixturePlayerEntity> assists;

    public GameSituation getGameSituation() {
        return gameSituation;
    }

    public void setGameSituation(GameSituation situation) {
        this.gameSituation = situation;
    }


    public FixturePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(FixturePlayerEntity player) {
        this.player = player;
    }


    public List<FixturePlayerEntity> getAssists() {
        return assists;
    }

    public void setAssists(List<FixturePlayerEntity> players) {
        this.assists = players;
    }

/*    
    @ManyToMany
    @JoinTable(name = "goal_part_positive")
    private Set<FixturePlayerEntity> positiveParticipants;

    @ManyToMany
    @JoinTable(name = "goal_part_negative")
    private Set<FixturePlayerEntity> negativeParticipants;
    
    public Set<FixturePlayerEntity> getPositiveParticipants() {
        return positiveParticipants;
    }

    public void setPositiveParticipants(Set<FixturePlayerEntity> players) {
        this.positiveParticipants = players;
    }


    public Set<FixturePlayerEntity> getNegativeParticipants() {
        return negativeParticipants;
    }

    public void setNegativeParticipants(Set<FixturePlayerEntity> players) {
        this.negativeParticipants = players;
    }
*/

    @Override
    public String toString() {
        return "[GOAL:"+getId()+" " + getGameTimeSecond()/60 + ":"+getGameTimeSecond()%60+" " + getPlayer() + "]";
    }

    @Override
    public String signature() {
        return super.signature() +
                String.format(":G:%d", player.getId());
    }

    @Override
    public boolean usesPlayer(FixturePlayerEntity unused) {
        return player.equals(unused) || assists.contains(unused);
    }    
}

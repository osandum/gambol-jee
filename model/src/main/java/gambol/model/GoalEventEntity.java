package gambol.model;

import gambol.xml.Event;
import gambol.xml.GameSituation;
import gambol.xml.GoalEvent;
import gambol.xml.PlayerRef;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriInfo;

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
        String sig = super.signature();
        sig += String.format(":G:%d", player.getId());
        for (FixturePlayerEntity a : assists)
            sig += String.format(":A:%d", a.getId());
        return sig;
    }

    @Override
    public boolean usesPlayer(FixturePlayerEntity unused) {
        return player.equals(unused) || assists.contains(unused);
    }

    @Override
    public Event asXml(UriInfo uriInfo) {
        GoalEvent ge = new GoalEvent();

        PlayerRef pr = new PlayerRef();
        pr.setNumber(getPlayer().getJerseyNumber());
        ge.setPlayer(pr);
        ge.setSide(getSide());
        ge.setTime(GameTime.format(getGameTimeSecond()));

        for (FixturePlayerEntity as : getAssists()) {
            PlayerRef ar = new PlayerRef();
            ar.setNumber(as.getJerseyNumber());
            ge.getAssists().add(ar);
        }
        ge.setGameSituation(getGameSituation());

//      if (uriInfo != null)
//          ge.setFixture(uriInfo.getBaseUriBuilder().path(FixtureResource.class).build(getFixture().getId()));

        return ge;
    }
}

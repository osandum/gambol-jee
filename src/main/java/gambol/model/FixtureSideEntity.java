package gambol.model;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 *
 * @author osa
 */
@Entity(name = "fixture_side")
public class FixtureSideEntity implements Serializable {

    private static Logger LOG = Logger.getLogger(FixtureSideEntity.class.getName());

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_team"))
    private TournamentTeamEntity team;

    public TournamentTeamEntity getTeam() {
        return team;
    }

    public void setTeam(TournamentTeamEntity team) {
        this.team = team;
    }

    @OneToMany(mappedBy = "side")
    @OrderBy("jerseyNumber")
    private List<FixturePlayerEntity> players;

    public List<FixturePlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<FixturePlayerEntity> players) {
        this.players = players;
    }

    
    private Integer score;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "[" + id + ":" + team + "]";
    }

    public FixturePlayerEntity getPlayerByJerseyNumber(Integer n) {
        for (FixturePlayerEntity fpe : getPlayers()) 
            if (n.equals(fpe.getJerseyNumber()))
                return fpe;

        LOG.info("??? " + n + " not in " + team);
        return null;
    }
    
}

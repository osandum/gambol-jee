package gambol.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author osa
 */
@Entity(name = "fixture_side")
public class FixtureSideEntity implements Serializable {
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
    private TournamentTeamEntity team;

    public TournamentTeamEntity getTeam() {
        return team;
    }

    public void setTeam(TournamentTeamEntity team) {
        this.team = team;
    }

    private Integer score;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
    
}

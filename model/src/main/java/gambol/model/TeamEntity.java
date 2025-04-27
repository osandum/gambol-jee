package gambol.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * @author osa
 */
@Entity(name = "team")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames={"slug", "tournament_id", "name"}) })
public class TeamEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 16, nullable = false)
    private String slug;

    @Column(length = 64, nullable = false)
    private String name;
    
    @ManyToOne(optional = false)
    private ClubEntity club;
    
    @ManyToOne(optional = false)
    private TournamentEntity tournament;

    
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public ClubEntity getClub() {
        return club;
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    
    public TournamentEntity getTournament() {
        return tournament;
    }

    public void setTournament(TournamentEntity tournament) {
        this.tournament = tournament;
    }

    @Override
    public String toString() {
        return "["+id+":\""+name+"\"]";
    }
    
}

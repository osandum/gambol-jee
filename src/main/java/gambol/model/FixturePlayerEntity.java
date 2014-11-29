package gambol.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author osa
 */
@Entity(name = "fixture_player")
public class FixturePlayerEntity implements Serializable {
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
    private FixtureSideEntity side;

    public FixtureSideEntity getSide() {
        return side;
    }

    public void setSide(FixtureSideEntity side) {
        this.side = side;
    }

    
    public String getRef() {
        return getSide().getTeam().getClub().getSlug() + ":" + getJerseyNumber() + ":" + getPerson().getLastName().toLowerCase() + ":" + getPerson().getFirstNames().toLowerCase();
    }
    

    @ManyToOne(optional = false)
    private PersonEntity person;

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }


    private Integer jerseyNumber;

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }


    private String line;

    public String getLineupLine() {
        return line;
    }

    public void setLineupLine(String line) {
        this.line = line;
    }

    
    private String pos;

    public String getLineupPosition() {
        return pos;
    }

    public void setLineupPosition(String pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "{" + id + ":" + side + " " + jerseyNumber + " " + person + " "+line+":"+pos+"}";
    }

}

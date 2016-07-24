package gambol.model;

import java.util.Comparator;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author osa
 */
@Entity(name = "fixture_player")
@DiscriminatorValue("PLAYER")
public class FixturePlayerEntity extends FixturePersonEntity {

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_side"))
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
        return "{" + getId() + ":" + side + " " + jerseyNumber + " " + getPerson() + " "+line+":"+pos+"}";
    }
}

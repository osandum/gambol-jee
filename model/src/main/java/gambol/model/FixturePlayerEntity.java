package gambol.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author osa
 */
@Entity(name = "fixture_person")
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "player_uq",
        columnNames={"fixture_id", "side_id", "person_id", "jerseynumber", "person_role"}) },
  indexes = {
    @Index(name = "player_fk_fixture", columnList = "fixture" ),
    @Index(name = "player_fk_person", columnList = "person" )
  })
public class FixturePlayerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_fixture"))
    private FixtureEntity fixture;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_person"))
    private PersonEntity person;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public FixtureEntity getFixture() {
        return fixture;
    }

    public void setFixture(FixtureEntity fixture) {
        this.fixture = fixture;
    }


    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_side"))
    private FixtureSideEntity side;

    @Column(length = 3)
    private String line;

    @Column(length = 3)
    private String pos;

    private Integer jerseyNumber;

    @Column(name = "person_role")
    private String personRole;

    public String getPersonRole() {
      return personRole;
    }

    public void setPersonRole(String role) {
      this.personRole = role;
    }

    public FixtureSideEntity getSide() {
        return side;
    }

    public void setSide(FixtureSideEntity side) {
        this.side = side;
    }


    public String getRef() {
        return getSide().getTeam().getClub().getSlug() + ":" + getJerseyNumber() + ":" + getPerson().getLastName().toLowerCase() + ":" + getPerson().getFirstNames().toLowerCase();
    }


    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }


    public String getLineupLine() {
        return line;
    }

    public void setLineupLine(String line) {
        this.line = line;
    }


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

    public String signature() {
        return String.format("%d:%d:%s", getFixture().getId(), person.getId(), getPersonRole());
    }
}

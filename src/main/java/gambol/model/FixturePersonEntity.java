package gambol.model;

import java.io.Serializable;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author osa
 */
@Entity(name = "fixture_person")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "person_role")
@Table(uniqueConstraints = { 
    @UniqueConstraint(name = "player_uq", 
            columnNames={"fixture_id", "side_id", "person_id", "person_role", "jerseynumber"}) })
public class FixturePersonEntity implements Serializable {
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

    public String signature() {
        return String.format("%d:%d", getFixture().getId(), person.getId());
    }
}

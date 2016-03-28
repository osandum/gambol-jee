package gambol.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * @author osa
 */
@Entity(name = "fixture_person")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "person_role")
public class FixturePersonEntity implements Serializable {
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
    private FixtureEntity fixture;

    public FixtureEntity getFixture() {
        return fixture;
    }

    public void setFixture(FixtureEntity fixture) {
        this.fixture = fixture;
    }

    
    @ManyToOne(optional = false)
    private PersonEntity person;

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }
    
}

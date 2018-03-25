package gambol.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
@Entity(name = "person")
public class PersonEntity implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(PersonEntity.class);
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

    @Size(min = 1, max = 16)
    @Column(length = 16, nullable = false, unique = true)
    private String slug;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    @PostLoad
    public void fixupSlug() {
        if (slug == null) {
            LOG.info("{}, {}: null slug", lastName, firstNames);
        }
    }

    @Column(length = 64, nullable = false)
    private String firstNames;

    @Column(length = 64, nullable = false)
    private String lastName;

    @Temporal(TemporalType.DATE)
    private Date yearOfBirth;

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Date yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }


  /*private List<? extends FixturePersonEntity> fixturesPlayes;

    @OneToMany(mappedBy = "person")
    public List<? extends FixturePersonEntity> getFixturesPlayed() {
        return fixturesPlayes;
    }

    public void setFixturesPlayed(List<? extends FixturePersonEntity> fps) {
        this.fixturesPlayes = fps;
    }*/


    @Override
    public String toString() {
        return "["+id+":" + String.valueOf(lastName).toUpperCase() + ", " + firstNames + "]";
    }

    public boolean isTUC() {
        return "(ukendt)".equals(firstNames) && "(ukendt)".equals(lastName);
    }
}

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
import org.apache.commons.lang.StringUtils;
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
            slug = generateSlug();
            LOG.info("{}, {}: null slug -> {}", lastName, firstNames, slug);            
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

    private String generateSlug() {
/*  slug = translate(left(lower(unaccent(substr(firstnames, 1, 1) || lastname)), 11) || '_'
      || substr('bcdfghkmnpqrstvwxz', (random() * id)::integer % 18 + 1, 1)
      || substr('aeiouy18', (random() * id)::integer % 8 + 1, 1)
      || substr('bcdfhkmnpqrstwxz2345679', (random() * id)::integer % 23 + 1, 1)
      || substr('bcdfhkmnpqrstwxz2345679', (random() * id)::integer % 23 + 1, 1), ' -/,', '')
*/
        String name;
        if (StringUtils.isEmpty(firstNames))
            name = lastName;
        else
            name = firstNames.substring(0, 1) + lastName;
        name = ModelUtil.asSlug(name, 11);

        String random =
                ModelUtil.oneOf("bcdfghkmnpqrstvwxz") +
                ModelUtil.oneOf("aeiouy18") +
                ModelUtil.oneOf("bcdfhkmnpqrstwxz2345679") +
                ModelUtil.oneOf("bcdfhkmnpqrstwxz2345679");
        
        return name + "_" + random;
    }
}

package gambol.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author osa
 */
@Entity(name = "person")
public class PersonEntity implements Serializable {
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

    @Override
    public String toString() {
        return "["+id+":" + String.valueOf(lastName).toUpperCase() + ", " + firstNames + "]";
    }

    public boolean isTUC() {
        return "(ukendt)".equals(firstNames) && "(ukendt)".equals(lastName);
    }
}

package gambol.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author osa
 */
@Entity(name = "season")
public class SeasonEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(length = 16, nullable = false)
    private String id;
    
    @Column(length = 16, nullable = false)
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}

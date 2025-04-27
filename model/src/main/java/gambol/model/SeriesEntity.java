package gambol.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * @author osa
 */
@Entity(name = "series")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames={"slug"}) })
public class SeriesEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 16, nullable = false)
    private String slug;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(nullable = false)
    private int fixtureDuration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getFixtureDuration() {
        return fixtureDuration;
    }

    public void setFixtureDuration(int fixtureDuration) {
        this.fixtureDuration = fixtureDuration;
    }

    @Override
    public String toString() {
        return "{" + slug + " id=" + id + " name=\"" + name + "\" dur=" + fixtureDuration + "}";
    }
    
}

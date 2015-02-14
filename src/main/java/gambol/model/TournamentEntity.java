package gambol.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author osa
 */
@Entity(name = "tournament")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames={"slug", "season_id"}) })
public class TournamentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 32, nullable = false)
    private String slug;

    @Column(length = 16, nullable = false)
    private String sourceRef;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(insertable = false, updatable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(insertable = false, updatable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastModified;

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

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        lastModified = new Date();
        if (dateCreated == null) {
            dateCreated = new Date();
        }
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @ManyToOne(optional = false)
    private SeasonEntity season;

    public SeasonEntity getSeason() {
        return season;
    }

    public void setSeason(SeasonEntity season) {
        this.season = season;
    }

    @ManyToOne(optional = false)
    private SeriesEntity series;

    public SeriesEntity getSeries() {
        return series;
    }

    public void setSeries(SeriesEntity series) {
        this.series = series;
    }


    @ManyToOne
    private ClubEntity arena;

    public ClubEntity getArena() {
        return arena;
    }

    public void setArena(ClubEntity location) {
        this.arena = location;
    }


    @OneToMany(mappedBy = "tournament")
    private List<FixtureEntity> fixtures;

    public List<FixtureEntity> getFixtures() {
        return fixtures;
    }

    public void setFixtures(List<FixtureEntity> fixtures) {
        this.fixtures = fixtures;
    }

    public static TournamentEntity create(SeasonEntity season, SeriesEntity series, String sourceRef) {
        TournamentEntity t = new TournamentEntity();
        t.setSourceRef(sourceRef);
        t.setSeason(season);
        t.setSeries(series);
        t.setFixtures(Collections.<FixtureEntity>emptyList());
        return t;
    }
}

package gambol.model;

import java.io.Serializable;
import java.sql.Timestamp;
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
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

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

    @Temporal(TemporalType.DATE)
    private Date birthYearMin;
    
    @Temporal(TemporalType.DATE)
    private Date birthYearMax;
    
    private Timestamp dateCreated;

    @Version
    private Timestamp lastModified;

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

    public Date getBirthYearMin() {
        return birthYearMin;
    }

    public void setBirthYearMin(Date birthYearMin) {
        this.birthYearMin = birthYearMin;
    }

    public Date getBirthYearMax() {
        return birthYearMax;
    }

    public void setBirthYearMax(Date birthYearMax) {
        this.birthYearMax = birthYearMax;
    }

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        if (dateCreated == null)
            dateCreated = new Timestamp(System.currentTimeMillis());
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

    public Integer getGwsTimeSecond() {
        return GameTime.parse("65:00");
    }
}

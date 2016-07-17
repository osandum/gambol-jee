package gambol.model;

import gambol.xml.FixtureSideRole;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author osa
 */
@Entity(name = "fixture_event")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type")
public class FixtureEventEntity implements Serializable {
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


    @Column(name = "game_second", nullable = false)
    private Integer gameTimeSecond;

    public Integer getGameTimeSecond() {
        return gameTimeSecond;
    }

    public void setGameTimeSecond(Integer gameTimeSecond) {
        this.gameTimeSecond = gameTimeSecond;
    }


    @Enumerated(EnumType.STRING)
    private FixtureSideRole side;

    public FixtureSideRole getSide() {
        return side;
    }

    public void setSide(FixtureSideRole side) {
        this.side = side;
    }
    
    
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_fixture"))
    private FixtureEntity fixture;

    public FixtureEntity getFixture() {
        return fixture;
    }

    public void setFixture(FixtureEntity fixture) {
        this.fixture = fixture;
    }

    
    // .......

    @Override
    public String toString() {
        FixtureSideEntity fs = getFixture().getSide(side);
        return String.format("[%d] %02d:%02d %s", id, gameTimeSecond / 60, gameTimeSecond % 60, fs.getTeam().getName());
    }

/*  public final static Comparator<FixtureEventEntity> BY_TIME = new Comparator<FixtureEventEntity>() {
        @Override
        public int compare(FixtureEventEntity e1, FixtureEventEntity e2) {
            int t1 = e1.getGameTimeSecond();
            int t2 = e2.getGameTimeSecond();
            if (t1 != t2)
                return t1 - t2;
            
            
            
        }
    }*/
}

package gambol.model;

import gambol.xml.Event;
import gambol.xml.FixtureSideRole;
import static gambol.xml.FixtureSideRole.*;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.ws.rs.core.UriInfo;

/**
 * @author osa
 */
@Entity(name = "fixture_event")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type")
@Table(indexes = {
  @Index(name = "event_fk_player", columnList = "player_id"),
  @Index(name = "event_fk_fixture", columnList = "fixture_id")
})
public abstract class FixtureEventEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @ManyToOne(optional = false)
    protected FixturePlayerEntity player;

    public FixturePlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(FixturePlayerEntity player) {
        this.player = player;
    }


    @Column(name = "game_second", nullable = false)
    protected Integer gameTimeSecond;

    public Integer getGameTimeSecond() {
        return gameTimeSecond;
    }

    public void setGameTimeSecond(Integer gameTimeSecond) {
        this.gameTimeSecond = gameTimeSecond;
    }


    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    protected FixtureSideRole side;

    public FixtureSideRole getSide() {
        return side;
    }

    public void setSide(FixtureSideRole side) {
        this.side = side;
    }


    @ManyToOne(optional = false)
    protected FixtureEntity fixture;

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

    public String signature() {
        return String.format("%02d%02d:%s", gameTimeSecond / 60, gameTimeSecond % 60, side.value());
    }

    public abstract boolean usesPlayer(FixturePlayerEntity unused);
    public abstract Event asXml(UriInfo uriInfo);

    public boolean isHome() { return HOME.equals(getSide()); }
    public boolean isAway() { return AWAY.equals(getSide()); }
}

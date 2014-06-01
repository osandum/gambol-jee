package gambol.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author osa
 */
@Entity
@XmlRootElement
public class Fixture implements Serializable {
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
    private Tournament tournament;
    
    @ManyToOne(optional = false)
    private Lineup homeSide;

    @ManyToOne(optional = false)
    private Lineup awaySide;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @XmlTransient
    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Lineup getHomeSide() {
        return homeSide;
    }

    public void setHomeSide(Lineup homeSide) {
        this.homeSide = homeSide;
    }

    public Lineup getAwaySide() {
        return awaySide;
    }

    public void setAwaySide(Lineup awaySide) {
        this.awaySide = awaySide;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

}

package gambol.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 * @author osa
 */
@Entity(name = "club")
public class ClubEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 16, nullable = false, unique = true)
    private String slug;

    @Column(length = 64, nullable = false)
    private String name;
    
    @ElementCollection
    @Column(name = "alias_name", length = 64)
    @CollectionTable(name = "club_alias")
    private Set<String> aliasNames = new HashSet<String>();

    @Column(length = 128)
    private String address;

    @Column(precision = 10, scale = 6, name = "lat")
    private Double geoLatitude;
    @Column(precision = 10, scale = 6, name = "lon")
    private Double geoLongitude;

    @Column(length = 2)
    private String country;
    
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountryIso2() {
        return country;
    }

    public void setCountryIso2(String iso2) {
        this.country = iso2;
    }

    public Double getLatitude() {
        return geoLatitude;
    }

    public void setLatitude(Double getLatitude) {
        this.geoLatitude = getLatitude;
    }

    public Double getLongitude() {
        return geoLongitude;
    }

    public void setLongitude(Double getLongitude) {
        this.geoLongitude = getLongitude;
    }

    public Set<String> getAliasNames() {
        return aliasNames;
    }

    public void setAliasNames(Set<String> aliases) {
        aliasNames = aliases;
    }
}

package gambol.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

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

    @Size(min = 1, max = 16)
    @Column(length = 16, nullable = false, unique = true)
    private String slug;

    @Column(length = 64, nullable = false)
    private String name;
    
    @ElementCollection
    @Column(name = "alias_name", length = 64)
    @CollectionTable(name = "club_alias")
    private Set<String> aliasNames = new HashSet<String>();

    @Column(length = 5, name = "ice_curfew")
    private String iceCurfewHHMM;
    
    @Column(length = 128)
    private String address;

    @Column(precision = 10, scale = 6, name = "lat")
    private BigDecimal geoLatitude;
    @Column(precision = 10, scale = 6, name = "lon")
    private BigDecimal geoLongitude;

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
    
    public String getCurfew() {
        return iceCurfewHHMM;
    }

    public void setCurfew(String curfew) {
        this.iceCurfewHHMM = curfew;
    }
    
    private final static Pattern HH_MM = Pattern.compile("([01][0-9]|2[0-3]):([0-5][0-9])");
    
    public Date curfewAfter(Date from) {
        if (from == null || iceCurfewHHMM == null)
            return null;
        Matcher m = HH_MM.matcher(iceCurfewHHMM);
        if (!m.matches())             
            throw new RuntimeException(iceCurfewHHMM + ": unrecognized time code");
        
        int hh = Integer.valueOf(m.group(1));
        int mm = Integer.valueOf(m.group(2));
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.set(Calendar.HOUR_OF_DAY, hh);
        cal.set(Calendar.MINUTE, mm);
        Date curfew = cal.getTime();
        if (!curfew.after(from)) {
            cal.add(Calendar.DATE, 1);
            curfew = cal.getTime();
        }
        return curfew;
    }

    public BigDecimal getLatitude() {
        return geoLatitude;
    }

    public void setLatitude(BigDecimal getLatitude) {
        this.geoLatitude = getLatitude;
    }

    public BigDecimal getLongitude() {
        return geoLongitude;
    }

    public void setLongitude(BigDecimal getLongitude) {
        this.geoLongitude = getLongitude;
    }

    public Set<String> getAliasNames() {
        return aliasNames;
    }

    public void setAliasNames(Set<String> aliases) {
        aliasNames = aliases;
    }
}

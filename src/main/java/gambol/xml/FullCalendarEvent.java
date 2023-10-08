package gambol.xml;

import java.net.URI;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Specialized value object used to deserialize events in a JSON-format
 * directly compatible with http://arshaw.com/fullcalendar/
 *
 * @author osa
 */
@XmlRootElement(name = "calendar-event")
@XmlAccessorType(XmlAccessType.FIELD)
public class FullCalendarEvent {
    @XmlElement
    private String id;
    private String title;

//  @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
//  @XmlJavaTypeAdapter(XmlDateTimeAdapter.class)
//  @XmlSchemaType(name = "dateTime")
    private LocalDateTime start;

//  @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
//  @XmlJavaTypeAdapter(XmlDateTimeAdapter.class)
//  @XmlSchemaType(name = "dateTime")
    private LocalDateTime end;

    private URI url;

    /*
    Boolean allDay;
    URI url;
    String className;

    Boolean editable;
    Boolean startEditable;
    Boolean durationEditable;

    String color;
    String backgroundColor;
    String borderColor;
    String textColor;
*/

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public void setEnd(LocalDateTime end) {
    this.end = end;
  }

  public URI getUrl() {
    return url;
  }

  public void setUrl(URI url) {
    this.url = url;
  }

  public String toString() {
    return id + "[" + start + " \"" + title + "\"]";
  }
}

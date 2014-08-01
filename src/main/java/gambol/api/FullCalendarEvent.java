package gambol.api;

import java.net.URI;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sandum.xml.XmlDateTimeAdapter;

/**
 * @author osa
 */

@XmlRootElement(name = "calendar-event")
@XmlAccessorType(XmlAccessType.FIELD)
public class FullCalendarEvent {
    String id;
    String title;
    
    @XmlJavaTypeAdapter(XmlDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    Date start;
    
    @XmlJavaTypeAdapter(XmlDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    Date end;
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
}

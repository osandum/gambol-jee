/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gambol.xml;

import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author osa
 */
public class JaxbParserTest {

    JAXBContext jaxbContext;
    
    public JaxbParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(Gambol.class);

        assertNotNull(jaxbContext);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void parseTournamentList() throws JAXBException {
        URL src = getClass().getResource("/sample/tournaments-2013_14.xml");

        Unmarshaller um = jaxbContext.createUnmarshaller();
        Gambol cal = (Gambol)um.unmarshal(src);
        assertNotNull(cal);

        int n = 0;
        for (Section s : cal.getSection())
            for (Tournament t : s.getTournament()) 
                ++n;
        
        assertEquals(88, n);
    }

    @Test
    public void parseTournament973() throws JAXBException {
        URL src = getClass().getResource("/sample/tournament-973.xml");

        Unmarshaller um = jaxbContext.createUnmarshaller();
        Gambol cal = (Gambol)um.unmarshal(src);
        assertNotNull(cal);
        
        int n = 0;
        for (Fixture f : cal.getTournament().getFixture()) {
            ++n;
            for (Side s : f.getSide()) {
                System.out.println(s);
            }
        }
        
        assertEquals(56, n);
    }
}

package oktion.telnet;

import net.wimpi.telnetd.TelnetD;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;

import java.io.File;

public class TelnetServerTest {

    static {
        DOMConfigurator.configure("log4j.xml");
    }

    private static Logger logger = Logger.getLogger(TelnetServerTest.class);

    @Test
    public void testTelnetServerSklad() throws Exception {
        TelnetD myTD;

        try {
            String propertyFile;
            propertyFile = "file:///" + System.getProperty("user.dir") + File.separator + "telnetd.properties";
            logger.info("read property from file: " + propertyFile);
            myTD = TelnetD.createTelnetD(propertyFile);
            logger.info("start telnet server");
            myTD.start();
            logger.info("server started");
            Thread.sleep(5000);
            myTD.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
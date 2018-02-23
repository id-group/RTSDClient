package oktion.telnet;

import net.wimpi.telnetd.TelnetD;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * User: Andrey Kazantsev
 * Date: 09.05.14
 * Time: 8:43
 */
public class TelnetServer {
    static {
        DOMConfigurator.configure("log4j.xml");
    }

    private static Logger logger = Logger.getLogger(TelnetServer.class);

    public static void main(String[] args) throws IOException, JAXBException, TransformerConfigurationException, SAXException {

        TelnetD myTD;

        try {

            String propertyFile;
            if (args.length == 0) {
                propertyFile = "file:///" + System.getProperty("user.dir") + File.separator + "telnetd.properties";
            } else {
                propertyFile = args[0];
            }
            logger.info("read property from file: " + propertyFile);
            myTD = TelnetD.createTelnetD(propertyFile);
            logger.info("start oktion.telnet server");
            myTD.start();
            logger.info("server started");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

package oktion.telnet.shell;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.toolkit.Editfield;
import net.wimpi.telnetd.io.toolkit.Label;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;
import oktion.service.BaseSystem;
import oktion.telnet.Terminal;
import oktion.telnet.TerminalController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.IOException;

/**
 * User: Andrey Kazantsev
 * Date: 10.05.14
 * Time: 13:19
 */
public class LoginShell implements Shell {

    private static Log log = LogFactory.getLog(LoginShell.class);
    private Connection m_Connection;
    private BasicTerminalIO m_IO;
    private Editfield m_EF;

    GenericXmlApplicationContext m_ctx;

    /**
     * Method that runs a shell
     *
     * @param con Connection that runs the shell.
     */
    public void run(Connection con) {
        try {

            m_Connection = con;
            m_Connection.setNextShell("stock");
            m_IO = m_Connection.getTerminalIO();
            //dont forget to register listener
            m_Connection.addConnectionListener(this);

            //clear the screen and start from zero
            m_IO.eraseScreen();
            m_IO.homeCursor();

            Label loginLabel = new Label(m_IO, "login", "имя: ");
            Editfield loginField = new Editfield(m_IO, "login", 50);
            loginLabel.draw();
            loginField.setJustBackspace(true);
            loginField.run();
            m_IO.write(BasicTerminalIO.CRLF);

            Label passwordLabel = new Label(m_IO, "login", "пароль: ");
            Editfield passwdField = new Editfield(m_IO, "passwdfield", 8);
            passwdField.setPasswordField(true);
            passwordLabel.draw();
            passwdField.run();
            m_IO.write(BasicTerminalIO.CRLF);

/*
            ApplicationContext m_ctx = new AnnotationConfigApplicationContext(RTSDServiceConfiguration.class);
            BaseSystemImpl baseSystem = m_ctx.getBean(BaseSystemImpl.class);
*/
            m_ctx = new GenericXmlApplicationContext();
            m_ctx.load("classpath:config-app.xml");
            m_ctx.refresh();
            BaseSystem baseSystem = m_ctx.getBean("RTSDClient", BaseSystem.class);

            if (!baseSystem.login(loginField.getValue(), passwdField.getValue())) {
                m_IO.homeCursor();
                m_IO.eraseScreen();
                m_IO.write("Incorrect login or password. Goodbye!.\r\n\r\n");
                m_IO.flush();
                m_IO.read();
                m_Connection.close();
            }
            m_Connection.setName(loginField.getValue());

            TerminalController tc = TerminalController.getInstance();
            Terminal terminal = tc.getTerminalByIp(con.getConnectionData().getHostAddress());
            if(terminal ==null)
                terminal = tc.addTerminal(con);
            else if (terminal != null && !terminal.getLogin().equals(m_Connection.getName())) {
                terminal.clear();
            }
            terminal.setLogin(m_Connection.getName());

        } catch (Exception ex) {
            log.error("run()", ex);
            try {
                m_IO.homeCursor();
                m_IO.eraseScreen();
                m_IO.write("Incorrect login or password. Goodbye!.\r\n\r\n");
                m_IO.flush();
                m_IO.read();
                m_Connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//run

    //this implements the ConnectionListener!
    public void connectionTimedOut(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_TIMEDOUT");
            m_IO.flush();
            //close connection
            m_Connection.close();
        } catch (Exception ex) {
            log.error("connectionTimedOut()", ex);
        }
    }//connectionTimedOut

    public void connectionIdle(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_IDLE");
            m_IO.flush();
        } catch (IOException e) {
            log.error("connectionIdle()", e);
        }

    }//connectionIdle

    public void connectionLogoutRequest(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_LOGOUTREQUEST");
            m_IO.flush();
        } catch (Exception ex) {
            log.error("connectionLogoutRequest()", ex);
        }
    }//connectionLogout

    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_BREAK");
            m_IO.flush();
        } catch (Exception ex) {
            log.error("connectionSentBreak()", ex);
        }
    }//connectionSentBreak


    public static Shell createShell() {
        return new LoginShell();
    }//createShell

}

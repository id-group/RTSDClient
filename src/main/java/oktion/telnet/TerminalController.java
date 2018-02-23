package oktion.telnet;

import net.wimpi.telnetd.net.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 01.04.2015.
 */
public class TerminalController {
    private static final TerminalController instance  = new TerminalController();

    private List<Terminal> terminals = new ArrayList<Terminal>();

    private TerminalController() { }

    public static TerminalController getInstance() {
        return instance;
    }

    public Terminal getTerminalByIp( String ip ) {
        for ( Terminal terminal : this.terminals ) {
            if(terminal.getIp().equals(ip))
                return terminal;
        }
        return null;
    }

    public Terminal addTerminal( Connection connection) {

        for ( Terminal terminal : this.terminals ) {
            if(terminal.getIp().equals(connection.getConnectionData().getHostAddress())) {
                terminal.setConnection(connection);
                return terminal;
            }
        }

        Terminal terminal = new Terminal(connection);
        terminals.add(terminal);
        return terminal;
    }
}

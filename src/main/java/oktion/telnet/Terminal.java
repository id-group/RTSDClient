package oktion.telnet;

import net.wimpi.telnetd.net.Connection;
import oktion.telnet.shell.StockOperation;
//import oktion.telnet.shell.TerminalError;
import rtsd.wsdl.Movement;
import rtsd.wsdl.Order;
import rtsd.wsdl.Stock;

/**
 * Created by Andrey Kazantsev on 01.04.2015.
 */
public class Terminal {
    private String ip;

    private Connection connection;

    private Movement movement = null;
    private Stock stock = null;
    private Order order = null;

    private String login = null;
    private StockOperation operation = null;

//    private TerminalError lastError = null;
    public Terminal(Connection con) {
        connection = con;
    }

    public String getIp() {
        return connection.getConnectionData().getHostAddress();
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.operation = StockOperation.PACKAGE;
        this.movement = movement;
    }

    public Order getOrder() { return order; }

    public void setOrder(Order order) {
        this.operation = StockOperation.STOCK_OUT;
        this.order = order;
    }

    public Stock getStock() { return stock; }

    public void setStock(Stock stock) {
        this.operation = StockOperation.STOCK_IN_FROM_FACTORY;
        this.stock = stock;
    }


    public void clear() {
        this.order = null;
        this.operation = null;
        this.movement = null;
        this.stock = null;
        //this.lastError = null;
    }

    public void setLastError(String msg) {
        this.clear();
        //this.lastError = new TerminalError(msg);
    }


    public void setConnection(Connection connection) {
        if (this.connection != null && !this.connection.equals(connection)) {
            //connection.setName(this.connection.getName());
            this.connection.close();
        }
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setOperation(StockOperation operation) {
        this.operation = operation;
    }

    public StockOperation getOperation() {
        return operation;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isLogin() {
        if(login == null)
            return false;
        else
            return true;
    }

/*
    public TerminalError getLastError() {
        return lastError;
    }
*/
}

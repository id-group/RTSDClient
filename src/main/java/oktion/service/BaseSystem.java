package oktion.service;

import rtsd.wsdl.Movement;
import rtsd.wsdl.Order;
import rtsd.wsdl.Stock;

/**
 * Created by IntelliJ IDEA.
 * User: Andrey Kazantsev
 * Date: 10.05.14
 * Time: 16:32
 */
public interface BaseSystem {
    Order getOrder(String orderNumber);
    Integer setOrder(Order order);
    Boolean login(String user, String password);
    Integer setMovement(Movement movement);
    Movement getMovement(String docNumber);
    String getLoggedUser();
    public void setLoggerUser(String user);
    public Integer setStock(Stock stock);
    public Stock getStock();
}

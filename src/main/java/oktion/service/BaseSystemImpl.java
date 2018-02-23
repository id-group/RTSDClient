package oktion.service;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import rtsd.wsdl.*;

import java.net.MalformedURLException;

public class BaseSystemImpl extends WebServiceGatewaySupport implements BaseSystem {

    private String loggedUser = "";

    public String getLoggedUser() {
        return loggedUser;
    }

    public void setLoggerUser(String user) {
        this.loggedUser = user;
    }

    public BaseSystemImpl() throws MalformedURLException {
        //String wsdlFile = System.getProperty("oktion.service.service.address.url", "http://localhost:9090/Server1CServicePort");

        // Add a port to the Service
        //oktion.service.service.addPort(WSRTSDServerSoap, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
/*
        URL wsdlURL;
        File wsdlFile = new File("WSRTSDServer.wsdl");
        if (wsdlFile.exists()) {
            wsdlURL = wsdlFile.toURI().toURL();
        } else {
            throw new RuntimeException("can not find file WSRTSDServer.wsdl. put this file into application directory");
        }
*/

//        LogUtils.setLoggerClass(org.apache.cxf.common.logging.Log4jLogger.class);
/*
        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        loggingInInterceptor.setPrettyLogging(true);
        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        loggingOutInterceptor.setPrettyLogging(true);
*/

    }

    public Order getOrder(String orderNumber) {
        GetOrder request = new GetOrder();
        request.setArg0(orderNumber);

        GetOrderResponse response = (GetOrderResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:getOrder"));

        Order order = response.getReturn();
        for (OrderItem oi : order.getOrderItems()) {
            if (oi.getAcceptedQty() == null) {
                oi.setAcceptedQty(0);
            }
        }
        return order;
    }

    public Integer setOrder(Order order) {
        SetOrder request = new SetOrder();
        request.setArg0(order);

        SetOrderResponse response = (SetOrderResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:setOrder"));

        return response.getReturn();
    }

    public Boolean login(String user, String password) {
        Login request = new Login();
        request.setUser(user);
        request.setPassword(password);

        LoginResponse response = (LoginResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:login"));

        if (response.isReturn())
            this.loggedUser = user;
        return response.isReturn();
    }

    public Integer setMovement(Movement movement) {
        SetMovement request = new SetMovement();
        request.setMovement(movement);
        request.setUser(this.getLoggedUser());

        SetMovementResponse response = (SetMovementResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:setMovement"));
        return response.getReturn();
    }

    public Movement getMovement(String docNumber) {
        GetMovement request = new GetMovement();
        request.setUser(this.getLoggedUser());
        request.setNumber(docNumber);

        GetMovementResponse response = (GetMovementResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:getMovement"));
        return response.getReturn();
    }

    public Integer setStock(Stock stock) {
        SetStock request = new SetStock();
        request.setStock(stock);

        SetStockResponse response = (SetStockResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:setStock"));
        return response.getReturn();    }

    public Stock getStock() {
        GetStock request = new GetStock();
        request.setUser(this.getLoggedUser());

        GetStockResponse response = (GetStockResponse) getWebServiceTemplate().marshalSendAndReceive(
                request,
                new SoapActionCallback( "http://server.ws.gdi.ru/#WSRTSDServer:getStock"));
        return response.getReturn();
    }

}

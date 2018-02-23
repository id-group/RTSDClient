package oktion.telnet.shell;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.terminal.ColorHelper;
import net.wimpi.telnetd.io.toolkit.Editfield;
import net.wimpi.telnetd.io.toolkit.Label;
import net.wimpi.telnetd.io.toolkit.Statusbar;
import net.wimpi.telnetd.io.toolkit.Titlebar;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;
import oktion.service.BaseSystem;
import oktion.telnet.Terminal;
import oktion.telnet.TerminalController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericXmlApplicationContext;
import rtsd.wsdl.*;

import java.io.IOException;

/**
 * User: Andrey Kazantsev
 * Date: 10.05.14
 * Time: 13:19
 */
public class StockShell implements Shell {

    private static final int SPACE = 32;
    private static final int FIXED_ROWS = 3;
    private static final int DEFAULT_ROWS = 25;
    private static final int DEFAULT_COLUMNS = 30;
    private static final String INVERSE_ON = "\033[7m";
    private static final String INVERSE_OFF = "\033[m";


    private static Log log = LogFactory.getLog(StockShell.class);
    private Connection m_Connection;
    private BasicTerminalIO m_IO;
    private Editfield m_EF;
    private Integer topPosition = 0;
    private Integer visiblePositions = 0;

    private Integer customRows = 0;
    private Integer customColumns = 0;
    Titlebar titleBar;
    CustomStatusBar statusBar;

    private Integer internalGetRows() {
        if (!customRows.equals(0)) {
            return customRows;
        } else if (m_IO != null) {
            return m_IO.getRows();
        } else {
            return DEFAULT_ROWS;
        }
    }

    private Integer internalGetColumns() {
        if (!customColumns.equals(0)) {
            return customColumns;
        } else if (m_IO != null) {
            return m_IO.getColumns();
        } else {
            return DEFAULT_COLUMNS;
        }
    }

    private void drawTitleBar(String msg) throws IOException {
        titleBar.setTitleText(INVERSE_ON + msg + INVERSE_OFF);
        titleBar.draw();
    }

    private void drawStatusBar(String msg) throws IOException {
        statusBar.setStatusText(INVERSE_ON + msg + INVERSE_OFF);
        statusBar.draw();
    }

    private String restrictMessage(String msg) {
        StringBuilder sb = new StringBuilder();
        int colWidth = internalGetColumns();
        int curLineLength = 0;
        for (int i = 0; i < msg.length(); i++) {
            sb.append(msg.charAt(i));
            curLineLength++;
            if (curLineLength == colWidth
                    && msg.charAt(i) != '\n' && msg.charAt(i) != '\r'
                    && (i + 1 < msg.length())
                    && msg.charAt(i + 1) != '\n' && msg.charAt(i + 1) != '\r') {
                sb.append("\n");
                curLineLength = 0;
            }
            if (msg.charAt(i) == '\n' || msg.charAt(i) == '\r') {
                curLineLength = 0;
            }
        }
        return sb.toString();
    }

    private void showOrder(Order order) throws IOException {
        m_IO.setCursor(2, 1);
        m_IO.eraseToEndOfScreen();
        String topMesg = (order.getAcceptedQty().equals(order.getQty()) ? INVERSE_ON : "") + restrictMessage(order.getRecipient() + ", " +
                order.getRoute() +
                ". позиций: " + order.getOrderItems().size() +
                ", " + order.getAcceptedQty() + "/" + order.getQty() + "\n") + INVERSE_OFF;
        m_IO.write(topMesg);

        Integer pos;
        Integer totalLength = 0;
        visiblePositions = 0;
        Integer topMsgLen = topMesg.replace("\n", "").length();
        Integer termTotalLength = (internalGetRows() - FIXED_ROWS - (int) Math.ceil((double) topMsgLen / internalGetColumns())) * internalGetColumns();
        for (pos = 0; pos < order.getOrderItems().size(); pos++) {
            if (pos + topPosition >= order.getOrderItems().size()) {
                break;
            }
            OrderItem oi = order.getOrderItems().get(pos + topPosition);
            String buf = restrictMessage(String.format("%03d %s %s(%d/%d)\n",
                    pos + topPosition + 1,
                    //lastOrder.getOrderItems().size(),
                    //oi.getLine(),
                    oi.getBarcode(),
                    //oi.getItemId(),
                    oi.getItemName(),
                    oi.getAcceptedQty(),
                    oi.getQty()
            ));

            Integer bufLen = buf.replace("\n", "").length();
            totalLength += internalGetColumns() * (int) Math.ceil((double) bufLen / internalGetColumns());
            if (totalLength > termTotalLength) {
                break;
            }
            visiblePositions++;
            m_IO.write((oi.getQty().equals(oi.getAcceptedQty()) ? INVERSE_ON : "") + buf + INVERSE_OFF);
        }
        statusBar.draw();
    }

    private void showMovement(Movement movement) throws IOException {
        m_IO.setCursor(2, 1);
        m_IO.eraseToEndOfScreen();
        Integer acceptedQty = 0;
        Integer totalQty = 0;
        for (MovementItem mi : movement.getMovementItems()) {
            acceptedQty += mi.getAcceptedQty() / mi.getAmnBox();
            totalQty += mi.getQty() / mi.getAmnBox();
        }

        String topMesg = (movement.getAcceptedQty() == movement.getQty() ? INVERSE_ON : "") + restrictMessage(movement.getStockName() + ", " +
                movement.getNumber() +
                ". коробов: " + movement.getMovementItems().size() +
                ", " + acceptedQty + "/" + totalQty + "\n") + INVERSE_OFF;
        m_IO.write(topMesg);

        Integer pos;
        Integer totalLength = 0;
        visiblePositions = 0;
        Integer topMsgLen = topMesg.replace("\n", "").length();
        Integer termTotalLength = (internalGetRows() - FIXED_ROWS - (int) Math.ceil((double) topMsgLen / internalGetColumns())) * internalGetColumns();
        for (pos = 0; pos < movement.getMovementItems().size(); pos++) {
            if (pos + topPosition >= movement.getMovementItems().size()) {
                break;
            }
            MovementItem movementItem = movement.getMovementItems().get(pos + topPosition);
            String buf = restrictMessage(String.format("%03d %s %s(%d/%d)[%d]\n",
                    pos + topPosition + 1,
                    //lastOrder.getOrderItems().size(),
                    //oi.getLine(),
                    movementItem.getBarcode(),
                    //oi.getItemId(),
                    movementItem.getItemName(),
                    movementItem.getAcceptedQty() / movementItem.getAmnBox(),
                    movementItem.getQty() / movementItem.getAmnBox(),
                    movementItem.getAmnBox()
            ));

            Integer bufLen = buf.replace("\n", "").length();
            totalLength += internalGetColumns() * (int) Math.ceil((double) bufLen / internalGetColumns());
            if (totalLength > termTotalLength) {
                break;
            }
            visiblePositions++;
            m_IO.write((movementItem.getQty() == movementItem.getAcceptedQty() ? INVERSE_ON : "") + buf + INVERSE_OFF);
        }
        statusBar.draw();
    }

    private void showStock(Stock stock) throws IOException {
        m_IO.setCursor(2, 1);
        m_IO.eraseToEndOfScreen();
        String topMesg = (stock.getAcceptedQty() == stock.getQty() ? INVERSE_ON : "") + restrictMessage(stock.getStockName() + ", " +
                stock.getStockId() +
                ". позиций: " + stock.getStockItems().size() +
                ", " + stock.getAcceptedQty() + "/" + stock.getQty() + "\n") + INVERSE_OFF;
        m_IO.write(topMesg);

        Integer pos;
        Integer totalLength = 0;
        visiblePositions = 0;
        Integer topMsgLen = topMesg.replace("\n", "").length();
        Integer termTotalLength = (internalGetRows() - FIXED_ROWS - (int) Math.ceil((double) topMsgLen / internalGetColumns())) * internalGetColumns();
        for (pos = 0; pos < stock.getStockItems().size(); pos++) {
            if (pos + topPosition >= stock.getStockItems().size()) {
                break;
            }
            StockItem stockItem = stock.getStockItems().get(pos + topPosition);
            String buf = restrictMessage(String.format("%03d %s %s(%d/%d)\n",
                    pos + topPosition + 1,
                    //lastStock.getOrderItems().size(),
                    //stockItem.getLine(),
                    stockItem.getBarcode(),
                    //stockItem.getItemId(),
                    stockItem.getItemName(),
                    stockItem.getAcceptedQty(),
                    stockItem.getQty()
            ));

            Integer bufLen = buf.replace("\n", "").length();
            totalLength += internalGetColumns() * (int) Math.ceil((double) bufLen / internalGetColumns());
            if (totalLength > termTotalLength) {
                break;
            }
            visiblePositions++;
            m_IO.write((stockItem.getQty() == stockItem.getAcceptedQty() ? INVERSE_ON : "") + buf + INVERSE_OFF);
        }
        statusBar.draw();
    }

    private void showMessage(String msg) throws IOException {
        m_IO.setCursor(2, 1);
        m_IO.eraseToEndOfScreen();
        m_IO.write(restrictMessage(msg));
        statusBar.draw();
    }

    private Integer checkOrder(Order order) {
        Integer pos;
        for (pos = 0; pos < order.getOrderItems().size(); pos++) {
            OrderItem oi = order.getOrderItems().get(pos);
            if (!oi.getAcceptedQty().equals(oi.getQty())) {
                return pos;
            }
        }
        return null;
    }

    private Integer checkStock(Stock stock) {
        Integer pos;
        for (pos = 0; pos < stock.getStockItems().size(); pos++) {
            StockItem oi = stock.getStockItems().get(pos);
            if (oi.getAcceptedQty() != oi.getQty()) {
                return pos;
            }
        }
        return null;
    }

    private Integer checkMovement(Movement movement) {
        Integer pos;
        for (pos = 0; pos < movement.getMovementItems().size(); pos++) {
            MovementItem movementItem = movement.getMovementItems().get(pos);
            if (movementItem.getAcceptedQty() != movementItem.getQty()) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Method that runs a shell
     *
     * @param con Connection that runs the shell.
     */
    public void run(Connection con) {
        try {
            Terminal terminal = TerminalController.getInstance().addTerminal(con);
            if (!terminal.isLogin())
                return;

            GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
            ctx.load("classpath:config-app.xml");
            ctx.refresh();
            BaseSystem baseSystem = ctx.getBean("RTSDClient", BaseSystem.class);
            baseSystem.setLoggerUser(terminal.getLogin());

/*
            Connection[] cons = con.getConnectionData().getManager().getConnectionsByAdddress(con.getConnectionData().getInetAddress());
            if (cons.length > 0) {
                for (Connection connection : cons) {
                    if (connection.getName().equalsIgnoreCase(con.getName())) {
                        for (Object customData : connection.getConnectionData().getCustomData()) {
                            if (customData instanceof Order) {
                                lastOrder = (Order) customData;
                            } else if (customData instanceof Movement) {
                                lastMovement = (Movement) customData;
                            } else if (customData instanceof Stock) {
                                lastStock = (Stock) customData;
                            }
                        }
                    }
                }
            }
*/
            m_Connection = con;
            con.setNextShell("nothing");
            m_IO = m_Connection.getTerminalIO();
            //dont forget to register listener
            m_Connection.addConnectionListener(this);
            customColumns = Integer.parseInt(System.getProperty("customColumns"));
            customRows = Integer.parseInt(System.getProperty("customRows"));
            log.info("connect from IP: " + m_Connection.getConnectionData().getInetAddress().getHostAddress());
            boolean done = false;
            while (!done) {
                //clear the screen and start from zero
                m_IO.eraseScreen();
                m_IO.homeCursor();

                titleBar = new Titlebar(m_IO, "title 1");
                titleBar.setAlignment(Titlebar.ALIGN_LEFT);
                titleBar.setBackgroundColor(ColorHelper.BLUE);
                titleBar.setForegroundColor(ColorHelper.YELLOW);

                statusBar = new CustomStatusBar(m_IO, "status 1");
                statusBar.setAlignment(Statusbar.ALIGN_LEFT);
                statusBar.setBackgroundColor(ColorHelper.BLUE);
                statusBar.setForegroundColor(ColorHelper.YELLOW);
                statusBar.setCustomColumns(customColumns);
                statusBar.setCustomRows(customRows);

                for (StockOperation so : StockOperation.values()) {
                    m_IO.write(restrictMessage(so.getCode() + ") " + so.getDesc() + "\n"));
                }

/*
                if (terminal.getLastError() != null)
                    this.drawStatusBar(terminal.getLastError().messageError);
*/

                StockOperation stockOperation;

                if (terminal.getOperation() == null) {
                    int ch = m_IO.read();
                    if (ch == -1 || ch == -2) {
                        log.debug("Input(Code):" + ch);
                        done = true;
                    }

                    stockOperation = terminal.getOperation();
                    if (ch == StockOperation.STOCK_OUT.getScanCode()) {
                        //    done = true;
                        stockOperation = StockOperation.STOCK_OUT;
                    } else if (ch == StockOperation.STOCK_IN_FROM_FACTORY.getScanCode()) {
                        //    done = true;
                        stockOperation = StockOperation.STOCK_IN_FROM_FACTORY;
                    } else if (ch == StockOperation.PACKAGE.getScanCode()) {
                        //    done = true;
                        stockOperation = StockOperation.PACKAGE;
                    }
                }
                else
                    stockOperation = terminal.getOperation();

                if (stockOperation != null) {
                    //clear the screen and start from zero
                    m_IO.eraseScreen();
                    m_IO.homeCursor();

                    drawTitleBar(stockOperation.getDesc());
                    drawStatusBar("");

                    switch (stockOperation) {
                        case STOCK_OUT: {
                            Order order;
                            String orderNumber = "";
                            try {
                                if (terminal.getOrder() == null) {
                                    //run editfield test
                                    m_IO.setCursor(2, 1);
                                    Label l = new Label(m_IO, "testedit", "заказ: ");
                                    l.draw();
                                    Editfield efOrderNumber = new Editfield(m_IO, "orderNumber", 50);
                                    efOrderNumber.run();
                                    m_IO.write(BasicTerminalIO.CRLF);
                                    orderNumber = efOrderNumber.getValue();
                                    order = baseSystem.getOrder(orderNumber);
                                    orderNumber = efOrderNumber.getValue();
                                    log.info("get lastOrder " + orderNumber);
                                    log.info("finish get lastOrder " + orderNumber);
                                } else {
                                    order = terminal.getOrder();
                                }

                                if (order != null) {
                                    terminal.setOrder(order);
                                    drawTitleBar("зак: " + order.getNumber());

                                    showOrder(order);
                                    m_IO.setCursor(internalGetRows() - 1, 1);
                                    Editfield itemId = new Editfield(m_IO, "sbar", 50);
                                    boolean orderDone = false;
                                    boolean orderExit = false;// выход без подтверждения
                                    while (!orderDone) {
                                        int in = m_IO.read();
                                        switch (in) {
                                            case BasicTerminalIO.UP:
                                                if (topPosition > 0)
                                                    topPosition--;
                                                break;
                                            case BasicTerminalIO.DOWN:
                                                if (topPosition < order.getOrderItems().size() - 1)
                                                    topPosition++;
                                                break;
                                            case SPACE:
                                            case '\n':
                                            case '\r':
                                                if (topPosition < order.getOrderItems().size() - visiblePositions - 1) {
                                                    topPosition += visiblePositions;
                                                } else {
                                                    topPosition = 0;
                                                }
                                                break;
                                            case 'z':
                                            case 'Z':
                                                Integer checkIndex = checkOrder(order);
                                                if (checkIndex == null) {
                                                    orderDone = true;
                                                } else {
                                                    m_IO.bell();
                                                    //Toolkit.getDefaultToolkit().beep();
                                                    OrderItem checkOi = order.getOrderItems().get(checkIndex);
                                                    String buf = "Заказ проверен неполностью.\n" +
                                                            String.format("%03d %s.\nОсталось набрать %d.\nЗавершить контроль (y/n)?",
                                                                    checkIndex,
                                                                    checkOi.getItemName(),
                                                                    checkOi.getQty() - checkOi.getAcceptedQty()) + "\n" +
                                                            "Выход без подтверждения (b)";

                                                    showMessage(buf);
                                                    Boolean isAnswer = false;
                                                    while (!isAnswer) {
                                                        int ans = m_IO.read();
                                                        switch (ans) {
                                                            case 'b':
                                                            case 'B':
                                                                orderDone = true;
                                                                isAnswer = true;
                                                                orderExit = true;
                                                                break;
                                                            case 'y':
                                                            case 'Y':
                                                                orderDone = true;
                                                                isAnswer = true;
                                                                break;
                                                            case 'n':
                                                            case 'N':
                                                                topPosition = checkIndex;
                                                                showOrder(order);
                                                                isAnswer = true;
                                                                break;
                                                        }
                                                    }
                                                }
                                                break;

                                            default:
                                                m_IO.setCursor(internalGetRows() - 1, 1);
                                                itemId.clear();
                                                itemId.setValue(Character.toString((char) in));
                                                itemId.run();

                                                if (!itemId.getValue().equals("\n")) {
                                                    Boolean found = false;
                                                    Integer foundPos = 0;
                                                    String statusMsg = "";
                                                    for (OrderItem oi : order.getOrderItems()) {
                                                        if (oi.getBarcode().equals(itemId.getValue())) {
                                                            if (oi.getAcceptedQty().equals(oi.getQty())) {
                                                                m_IO.bell();
                                                                m_IO.bell();
                                                                //Toolkit.getDefaultToolkit().beep();
                                                                //Toolkit.getDefaultToolkit().beep();
                                                                statusMsg = "Ошибка: излишек";
                                                            } else {
                                                                oi.setAcceptedQty(oi.getAcceptedQty() + 1);
                                                                order.setAcceptedQty(order.getAcceptedQty() + 1);
                                                            }
                                                            found = true;
                                                            break;
                                                        }
                                                        foundPos++;
                                                    }
                                                    if (!found) {
                                                        m_IO.bell();
                                                        m_IO.bell();
                                                        m_IO.bell();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        statusMsg = "Ошибка: не найден.";
                                                    } else {
                                                        topPosition = foundPos;
                                                        showOrder(order);
                                                    }
                                                    drawStatusBar(statusMsg);
                                                }
                                        }
                                        if (!orderDone)
                                            showOrder(order);
                                    }
                                    log.info("set lastOrder " + order.toString());
                                    if(!orderExit) {
                                        Integer result = baseSystem.setOrder(order);
                                        log.info("finish Order " + order.toString() + "Result: " + result);
                                    }
                                    else
                                        log.info("exit from Order " + order.toString());
                                    terminal.clear();
                                    //showOrder(lastOrder);
                                } else {
                                    m_IO.bell();
                                    //Toolkit.getDefaultToolkit().beep();
                                    drawStatusBar("заказ " + orderNumber + " не найден");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                m_IO.bell();
                                //Toolkit.getDefaultToolkit().beep();
                                drawStatusBar("ошибка связи с учетной системой");
                                terminal.setLastError("Ошибка");
                            }
                            break;
                        }

                        // приход на склад
                        case STOCK_IN_FROM_FACTORY: {
                            Stock stock;
                            try {
                                if (terminal.getStock() == null) {
                                    //run editfield test
                                    m_IO.setCursor(2, 1);
                                    log.info("get lastStock");
                                    stock = baseSystem.getStock();
                                    log.info("finish get lastStock ");
                                } else {
                                    stock = terminal.getStock();
                                }
                                if (stock != null) {
                                    terminal.setStock(stock);
                                    showStock(stock);
                                    m_IO.setCursor(internalGetRows() - 1, 1);
                                    Editfield itemId = new Editfield(m_IO, "sbar", 50);
                                    boolean orderDone = false;
                                    while (!orderDone) {
                                        int in = m_IO.read();
                                        switch (in) {
                                            case BasicTerminalIO.UP:
                                                if (topPosition > 0)
                                                    topPosition--;
                                                break;
                                            case BasicTerminalIO.DOWN:
                                                if (topPosition < stock.getStockItems().size() - 1)
                                                    topPosition++;
                                                break;
                                            case SPACE:
                                            case '\n':
                                            case '\r':
                                                if (topPosition < stock.getStockItems().size() - visiblePositions - 1) {
                                                    topPosition += visiblePositions;
                                                } else {
                                                    topPosition = 0;
                                                }
                                                break;
                                            case 'z':
                                            case 'Z':
                                                Integer checkIndex = checkStock(stock);
                                                if (checkIndex == null) {
                                                    orderDone = true;
                                                } else {
                                                    m_IO.bell();
                                                    //Toolkit.getDefaultToolkit().beep();
                                                    StockItem checkOi = stock.getStockItems().get(checkIndex);
                                                    String buf = "Приход проверен неполностью.\n" +
                                                            String.format("%03d %s.\nОсталось принять %d.\nЗавершить контроль (y/n)?",
                                                                    checkIndex,
                                                                    checkOi.getItemName(),
                                                                    checkOi.getQty() - checkOi.getAcceptedQty());

                                                    showMessage(buf);
                                                    Boolean isAnswer = false;
                                                    while (!isAnswer) {
                                                        int ans = m_IO.read();
                                                        switch (ans) {
                                                            case 'y':
                                                            case 'Y':
                                                                orderDone = true;
                                                                isAnswer = true;
                                                                break;
                                                            case 'n':
                                                            case 'N':
                                                                topPosition = checkIndex;
                                                                showStock(stock);
                                                                isAnswer = true;
                                                                break;
                                                        }
                                                    }
                                                }
                                                break;

                                            default:
                                                m_IO.setCursor(internalGetRows() - 1, 1);
                                                itemId.clear();
                                                itemId.setValue(Character.toString((char) in));
                                                itemId.run();

                                                if (!itemId.getValue().equals("\n")) {
                                                    Boolean found = false;
                                                    Integer foundPos = 0;
                                                    String statusMsg = "";
                                                    for (StockItem oi : stock.getStockItems()) {
                                                        if (oi.getBarcode().equals(itemId.getValue())) {
                                                            if (oi.getAcceptedQty() == oi.getQty()) {
                                                                m_IO.bell();
                                                                m_IO.bell();
                                                                //Toolkit.getDefaultToolkit().beep();
                                                                //Toolkit.getDefaultToolkit().beep();
                                                                statusMsg = "Ошибка: излишек";
                                                            } else {
                                                                oi.setAcceptedQty(oi.getAcceptedQty() + 1);
                                                                stock.setAcceptedQty(stock.getAcceptedQty() + 1);
                                                            }
                                                            found = true;
                                                            break;
                                                        }
                                                        foundPos++;
                                                    }
                                                    if (!found) {
                                                        m_IO.bell();
                                                        m_IO.bell();
                                                        m_IO.bell();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        statusMsg = "Ошибка: не найден.";
                                                    } else {
                                                        topPosition = foundPos;
                                                        showStock(stock);
                                                    }
                                                    drawStatusBar(statusMsg);
                                                }
                                        }
                                        if (!orderDone)
                                            showStock(stock);
                                    }
                                    log.info("set lastOrder " + stock.toString());
                                    Integer result = baseSystem.setStock(stock);
                                    log.info("finish set lastOrder " + stock.toString() + "Result: " + result);
                                    terminal.clear();
                                    //showOrder(lastOrder);
                                } else {
                                    m_IO.bell();
                                    //Toolkit.getDefaultToolkit().beep();
                                    //drawStatusBar("заказ " + orderNumber + " не найден");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                m_IO.bell();
                                //Toolkit.getDefaultToolkit().beep();
                                drawStatusBar("ошибка связи с учетной системой");
                                terminal.setLastError("Ошибка");
                            }
                            break;
                        }

                        // упаковка
                        case PACKAGE: {
                            Movement movement = null;
                            String docNumber = null;
                            try {
                                if (terminal.getMovement() == null) {
                                    //run editfield test
                                    m_IO.setCursor(2, 1);
                                    Label l = new Label(m_IO, "testedit", "номер док.: ");
                                    l.draw();
                                    Editfield efDocNumber = new Editfield(m_IO, "docNumber", 50);
                                    efDocNumber.run();
                                    m_IO.write(BasicTerminalIO.CRLF);
                                    docNumber = efDocNumber.getValue();
                                    drawTitleBar("номер док.: " + docNumber);
                                    log.info("get movement " + docNumber);
                                    movement = baseSystem.getMovement(docNumber);
                                    if (movement != null) {
                                        terminal.setMovement(movement);
                                    }
                                    log.info("finish get movement " + docNumber);
                                }
                                else {
                                    movement = terminal.getMovement();
                                }

                                if (movement != null) {
                                    showMovement(movement);
                                    m_IO.setCursor(internalGetRows() - 1, 1);
                                    Editfield itemId = new Editfield(m_IO, "sbar", 50);
                                    boolean orderDone = false;
                                    while (!orderDone) {
                                        int in = m_IO.read();
                                        switch (in) {
                                            case BasicTerminalIO.UP:
                                                if (topPosition > 0)
                                                    topPosition--;
                                                break;
                                            case BasicTerminalIO.DOWN:
                                                if (topPosition < movement.getMovementItems().size() - 1)
                                                    topPosition++;
                                                break;
                                            case SPACE:
                                            case '\n':
                                            case '\r':
                                                if (topPosition < movement.getMovementItems().size() - visiblePositions - 1) {
                                                    topPosition += visiblePositions;
                                                } else {
                                                    topPosition = 0;
                                                }
                                                break;
                                            case 'z':
                                            case 'Z':
                                                Integer checkIndex = checkMovement(movement);
                                                if (checkIndex == null) {
                                                    orderDone = true;
                                                } else {
                                                    m_IO.bell();
                                                    //Toolkit.getDefaultToolkit().beep();
                                                    MovementItem checkOi = movement.getMovementItems().get(checkIndex);
                                                    String buf = "Документ проверен неполностью.\n" +
                                                            String.format("%03d %s.\nОсталось проверить %d.\nЗавершить контроль (y/n)?",
                                                                    checkIndex,
                                                                    checkOi.getItemName(),
                                                                    (checkOi.getQty() - checkOi.getAcceptedQty()) / checkOi.getAmnBox());

                                                    showMessage(buf);
                                                    Boolean isAnswer = false;
                                                    while (!isAnswer) {
                                                        int ans = m_IO.read();
                                                        switch (ans) {
                                                            case 'y':
                                                            case 'Y':
                                                                orderDone = true;
                                                                isAnswer = true;
                                                                break;
                                                            case 'n':
                                                            case 'N':
                                                                topPosition = checkIndex;
                                                                showMovement(movement);
                                                                isAnswer = true;
                                                                break;
                                                        }
                                                    }
                                                }
                                                break;

                                            default:
                                                m_IO.setCursor(internalGetRows() - 1, 1);
                                                itemId.clear();
                                                itemId.setValue(Character.toString((char) in));
                                                itemId.run();

                                                if (!itemId.getValue().equals("\n")) {
                                                    Boolean found = false;
                                                    Integer foundPos = 0;
                                                    String statusMsg = "";
                                                    for (MovementItem movementItem : movement.getMovementItems()) {
                                                        if (movementItem.getBarcode().equals(itemId.getValue())) {
                                                            if (movementItem.getAcceptedQty() == movementItem.getQty()) {
                                                                m_IO.bell();
                                                                m_IO.bell();
                                                                //Toolkit.getDefaultToolkit().beep();
                                                                //Toolkit.getDefaultToolkit().beep();
                                                                statusMsg = "Ошибка: излишек";
                                                            } else {
                                                                movementItem.setAcceptedQty(movementItem.getAcceptedQty() + movementItem.getAmnBox());
                                                                movement.setAcceptedQty(movement.getAcceptedQty() + movementItem.getAmnBox());
                                                            }
                                                            found = true;
                                                            break;
                                                        }
                                                        foundPos++;
                                                    }
                                                    if (!found) {
                                                        m_IO.bell();
                                                        m_IO.bell();
                                                        m_IO.bell();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        //Toolkit.getDefaultToolkit().beep();
                                                        statusMsg = "Ошибка: не найден.";
                                                    } else {
                                                        topPosition = foundPos;
                                                        showMovement(movement);
                                                    }
                                                    drawStatusBar(statusMsg);
                                                }
                                        }
                                        if (!orderDone)
                                            showMovement(movement);
                                    }
                                    log.info("set movement " + movement.toString());
                                    Integer result = baseSystem.setMovement(movement);
                                    log.info("finish set movement " + movement.toString() + "Result: " + result);
                                    terminal.clear();
                                    //showOrder(movement);
                                } else {
                                    m_IO.bell();
                                    //Toolkit.getDefaultToolkit().beep();
                                    drawStatusBar("документ " + docNumber + " не найден");
                                }
                            } catch (Exception e) {
                                log.error("ошибка:", e);
                                e.printStackTrace();
                                m_IO.bell();
                                //Toolkit.getDefaultToolkit().beep();
                                drawStatusBar("ошибка связи с учетной системой");
                                terminal.setLastError("Ошибка");
                            }
                        }
                        break;

                    }
                }
            }
            m_IO.read();
        } catch (Exception ex) {
            log.error("run()", ex);
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
            log.debug("CONNECTION_LOGOUTREQUEST");
            m_IO.flush();
            m_Connection.close();
        } catch (Exception ex) {
            log.error("connectionLogoutRequest()", ex);
        }
    }//connectionLogout

    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_BREAK");
            m_IO.flush();
            m_Connection.close();
        } catch (Exception ex) {
            log.error("connectionSentBreak()", ex);
        }
    }//connectionSentBreak


    public static Shell createShell() {
        return new StockShell();
    }//createShell

}

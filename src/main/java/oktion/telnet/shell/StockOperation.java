package oktion.telnet.shell;

/**
 * User: Andrey Kazantsev
 * Date: 10.05.14
 * Time: 15:46
 */
public enum StockOperation {

    PACKAGE(1, 49, "упаковка"),
    STOCK_IN_FROM_FACTORY(2, 50, "приход на склад"),
    STOCK_OUT(3, 51, "отгрузка");

    StockOperation(Integer code, Integer scanCode, String desc) {
        this.code = code;
        this.scanCode = scanCode;
        this.desc = desc;
    }

    private Integer code;
    private String desc;
    private Integer scanCode;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getScanCode() {
        return scanCode;
    }

    public void setScanCode(Integer scanCode) {
        this.scanCode = scanCode;
    }
}

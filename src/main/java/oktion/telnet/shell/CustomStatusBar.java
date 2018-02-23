package oktion.telnet.shell;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.terminal.ColorHelper;
import net.wimpi.telnetd.io.toolkit.InertComponent;

import java.io.IOException;

/**
 */
public class CustomStatusBar extends InertComponent {
    //Members
    private String m_Status;
    private int m_Align;
    private String m_BgColor;
    private String m_FgColor;
    private Integer customColumns = 0;
    private Integer customRows = 0;

    public Integer getCustomColumns() {
        return customColumns;
    }

    public void setCustomColumns(Integer customColumns) {
        this.customColumns = customColumns;
    }

    public Integer getCustomRows() {
        return customRows;
    }

    public void setCustomRows(Integer customRows) {
        this.customRows = customRows;
    }

    /**
     * Constructor for a simple statusbar instance.
     */
    public CustomStatusBar(BasicTerminalIO io, String name) {
        super(io, name);

    }//constructor


    /**
     * Mutator method for the statustext property of the statusbar component.
     *
     * @param text status String displayed in the titlebar.
     */
    public void setStatusText(String text) {
        m_Status = text;
    }//setStatusText

    /**
     * Accessor method for the statustext property of the statusbar component.
     *
     * @return String that is displayed when the bar is drawn.
     */
    public String getStatusText() {
        return m_Status;
    }//getStatusText

    /**
     * Mutator method for the alignment property.
     *
     * @param alignment integer, valid if one of  the ALIGN_* constants.
     */
    public void setAlignment(int alignment) {
        if (alignment < 1 || alignment > 3) {
            alignment = 2;    //left default
        } else {
            m_Align = alignment;
        }
    }//setAlignment

    /**
     * Mutator method for the SoregroundColor property.
     *
     * @param color String, valid if it is a ColorHelper color constant.
     */
    public void setForegroundColor(String color) {
        m_FgColor = color;
    }//setForegroundColor

    /**
     * Mutator method for the BackgroundColor property.
     *
     * @param color String, valid if it is a ColorHelper color constant.
     */
    public void setBackgroundColor(String color) {
        m_BgColor = color;
    }//setBackgroundColor


    /**
     * Method that draws the statusbar on the screen.
     */
    public void draw() throws IOException {
        m_IO.storeCursor();
        m_IO.setCursor(!customRows.equals(0) ? customRows : m_IO.getRows(), 1);
        m_IO.write(getBar());
        m_IO.restoreCursor();
    }//draw


    /**
     * Internal method that creates the true titlebarstring displayed
     * on the terminal.
     */
    private String getBar() {
        String tstatus = m_Status;
        //get actual screen width
        int width = (!customColumns.equals(0) ? customColumns : m_IO.getColumns()) - 1;
        //get actual statustext width
        int textwidth = (int) ColorHelper.getVisibleLength(m_Status);

        if (textwidth > width) tstatus = m_Status.substring(0, width);
        textwidth = (int) ColorHelper.getVisibleLength(tstatus);

        //prepare a buffer with enough space
        StringBuffer bar = new StringBuffer(width + textwidth);
        switch (m_Align) {
            case ALIGN_LEFT:
                bar.append(tstatus);
                appendSpaceString(bar, width - textwidth);
                break;
            case ALIGN_RIGHT:
                appendSpaceString(bar, width - textwidth);
                bar.append(tstatus);
                break;
            case ALIGN_CENTER:
                int left = ((width - textwidth != 0) ? ((width - textwidth) / 2) : (0));
                int right = width - textwidth - left;
                appendSpaceString(bar, left);
                bar.append(tstatus);
                appendSpaceString(bar, right);
        }
        if (m_FgColor != null && m_BgColor != null) {
            return ColorHelper.boldcolorizeText(bar.toString(), m_FgColor, m_BgColor);
        } else if (m_FgColor != null && m_BgColor == null) {
            return ColorHelper.boldcolorizeText(bar.toString(), m_FgColor);
        } else if (m_FgColor == null && m_BgColor != null) {
            return ColorHelper.colorizeBackground(bar.toString(), m_BgColor);
        } else {
            return bar.toString();
        }
    }//getBar


    private void appendSpaceString(StringBuffer sbuf, int length) {
        for (int i = 0; i < length; i++) {
            sbuf.append(" ");
        }
    }//appendSpaceString


    // Constant definitions
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_LEFT = 2;
    public static final int ALIGN_CENTER = 3;
}

package jackielisummer17;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by JacquelineLi on 6/1/17.
 */
public class ParsedCommand {
    private int command = 0;
    private double x = -33333, y = -33333, i = -33333, j = -33333;

    public ParsedCommand() {
        command = -1;
        x = -5555.00;
        y = -5555.00;
    }

    public ParsedCommand(int command) {
        this.command = command;
    }

    public ParsedCommand(int command, double x, double y) {
        Double truncatedx = BigDecimal.valueOf(x)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedy = BigDecimal.valueOf(y)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        this.command = command;
        this.x = truncatedx;
        this.y = truncatedy;
    }

    public ParsedCommand(int command, double x, double y, double i, double j) {
        Double truncatedx = BigDecimal.valueOf(x)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedy = BigDecimal.valueOf(y)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedi = BigDecimal.valueOf(i)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedj = BigDecimal.valueOf(j)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        this.command = command;
        this.x = truncatedx;
        this.y = truncatedy;
        this.i = truncatedi;
        this.j = truncatedj;
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public double getJ() {
        return j;
    }

    public int getCommand() {
        return command;
    }
    public boolean isMoveTo() {
        return (command == 0);
    }
    public boolean isLineTo() {
        return (command  == 1);
    }
    public boolean isSystem() { return (!(isMoveTo() || isLineTo())); }

    public boolean equals(ParsedCommand b) {
        return ((Math.abs(getX() - b.getX()) < 0.00001) && (Math.abs(getY() - b.getY()) < 0.00001));
    }

    public boolean isArc() {
        return (command == 2) || (command == 3);
    }
    public String toString() {
        return getCommand() + ":" + getX() + "," + getY();
    }

}

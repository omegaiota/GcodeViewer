package jackielisummer17;

/**
 * Created by JacquelineLi on 6/1/17.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcesser {
    private final Path fFilePath;
    private String fileName;
    private boolean linearizedMode = false;
    private boolean viewMode = false;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private List<ParsedCommand> commandList = new ArrayList<>();
    int absX = 0, absY = 0, absV = 0;

    public FileProcesser(String filePath, String fileName) {
        fFilePath = Paths.get(filePath);
        this.fileName = fileName;
    }

    /**
     * Template method that calls {@link #processLine(String)}.
     */

    public final List<ParsedCommand> processLineByLine(boolean toLinearize, boolean toView) throws IOException {
        ParsedCommand parsedThis;
        this.linearizedMode = toLinearize;
        this.viewMode = toView;
        if (isViewMode())
            System.out.println("processing .pat file");
        else
            System.out.println("processing .gcode file");

        try (Scanner scanner = new Scanner(fFilePath, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                    if (isViewMode()) {
                        parsedThis = parsePatLine(scanner.nextLine());
                        System.out.println(parsedThis.toString());
                    } else {
                        parsedThis = convertLine(scanner.nextLine());
                    }
                    if (parsedThis.isMoveTo() || parsedThis.isLineTo()){
                        if (Math.abs(parsedThis.getX()) > absX) {
                            absX = (int) (Math.abs(parsedThis.getX())) + 1;
                            System.out.println("absX is now " + absX);
                        }
                        if (Math.abs(parsedThis.getY()) > absY) {
                            absY = (int) (Math.abs(parsedThis.getY())) + 1;
                            System.out.println("absY is now " + absY);
                        }
                        if (absX > absV) {
                            System.out.println(parsedThis.toString());
                            absV = absX;
                            System.out.println("absV is now " + absV);

                        }
                        if (absY > absV) {
                            System.out.println(parsedThis.toString());
                            absV = absY;
                            System.out.println("absV is now " + absV);

                        }
                    }
                if ((commandList.size() == 0) || (!commandList.get(commandList.size() - 1).equals(parsedThis)))
                    commandList.add(parsedThis);
            }
        }
        log("total processed: " + commandList.size());
        return commandList;
    }

    public void outputCommands() {
        try{
            PrintWriter writer = new PrintWriter(fileName + ".pat", "UTF-8");
            int count = 0;
            for (ParsedCommand command : commandList) {
                count++;
                    if (command.isMoveTo() || command.isLineTo())
                        writer.println( "N" + count + "G0" + command.getCommand() + "X" + command.getX() + "Y" + command.getY());
                    else if (command.isArc())
                        writer.println( "N" + count + "G0" + command.getCommand() + "X" + command.getX()
                                + "Y" + command.getY() + "I" + command.getI() + "J" + command.getJ());
            }
            count++;
            writer.println("N" + count + "M02");
            writer.close();


        } catch (IOException e) {
            // do something
        }
    }

    protected ParsedCommand convertLine(String inputLine) {
        //use a second Scanner to parse the content of each line
        //System.out.println(inputLine);
        String linePatternString = "G(.*) X(.*) Y(.*)";
        String linePatternStringF = "G(.*) X(.*) Y(.*) F.*";
        String arcPatternString = "G(.*) X(.*) Y(.*) I(.*) J(.*)";
        String arcPatternStringF = "G(.*) X(.*) Y(.*) I(.*) J(.*) F.*";
        Pattern linePattern = Pattern.compile(linePatternString);
        Pattern arcPattern = Pattern.compile(arcPatternString);
        Pattern linePatternF = Pattern.compile(linePatternStringF);
        Pattern arcPatternF = Pattern.compile(arcPatternStringF);
        Matcher lineMatcher = linePattern.matcher(inputLine);
        Matcher arcMatcher = arcPattern.matcher(inputLine);
        Matcher lineMatcherF = linePatternF.matcher(inputLine);
        Matcher arcMatcherF = arcPatternF.matcher(inputLine);
        ParsedCommand parsed = new ParsedCommand();

        if (arcMatcher.find() || arcMatcherF.find()) {
            //log("this is an arc");
            Matcher matched = arcMatcher;
            if (arcMatcherF.find()) matched = arcMatcherF;
            int command = Integer.parseInt(matched.group(1));
            if (isLinearizedMode())
                command = 1;
            double x = Double.parseDouble(matched.group(2));
            double y = Double.parseDouble(matched.group(3));
            double i = Double.parseDouble(matched.group(4));
            double j = Double.parseDouble(matched.group(5));
            parsed = new ParsedCommand(command, x, y, i, j);
        } else if (lineMatcher.find() || lineMatcherF.find()) {
            //log("this is a straight line" );
            Matcher matched = lineMatcher;
            if (lineMatcherF.find()) matched = lineMatcherF;
            int command = Integer.parseInt(matched.group(1));
            double x = Double.parseDouble(matched.group(2));
            double y = Double.parseDouble(matched.group(3));
            parsed = new ParsedCommand(command, x, y);
        } else {
            //log("this is a system command, ignored");
        }
        return parsed;
    }

    protected ParsedCommand parsePatLine(String inputLine) {
        //use a second Scanner to parse the content of each line
        //System.out.println(inputLine);
        String linePatternString = "N.*G(.*)X(.*)Y(.*)";
        String arcPatternString = "N.*G(.*)X(.*)Y(.*)I(.*)J(.*)";
        Pattern linePattern = Pattern.compile(linePatternString);
        Pattern arcPattern = Pattern.compile(arcPatternString);
        Matcher lineMatcher = linePattern.matcher(inputLine);
        Matcher arcMatcher = arcPattern.matcher(inputLine);
        ParsedCommand parsed = new ParsedCommand();

        if (arcMatcher.find()) {
            log("this is an arc");
            Matcher matched = arcMatcher;
            int command = Integer.parseInt(matched.group(1));
            double x = Double.parseDouble(matched.group(2));
            double y = Double.parseDouble(matched.group(3));
            double i = Double.parseDouble(matched.group(4));
            double j = Double.parseDouble(matched.group(5));
            parsed = new ParsedCommand(command, x, y, i, j);
            if (Math.abs(x)  > 200)
                System.out.println(inputLine);
        } else if (lineMatcher.find()) {
            log("this is a straight line" );
            Matcher matched = lineMatcher;
            int command = Integer.parseInt(matched.group(1));
            double x = Double.parseDouble(matched.group(2));
            double y = Double.parseDouble(matched.group(3));
            parsed = new ParsedCommand(command, x, y);
            if (Math.abs(x) > 200)
                System.out.println(inputLine);
        } else {
            //log("this is a system command, ignored");
        }
        return parsed;
    }

    public int getAbsV() {
        return absV;
    }

    public boolean isLinearizedMode() {
        return linearizedMode;
    }


    public boolean isViewMode() {
        return viewMode;
    }

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private String quote(String aText) {
        String QUOTE = "'";
        return QUOTE + aText + QUOTE;
    }
}
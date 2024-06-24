import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrintNthQuery {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java PrintNthQuery <filename> <query number>");
            return;
        }

        String filename = args[0];
        int lineNumber;
        
        try {
            lineNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("The second argument must be an integer representing the line number.");
            return;
        }

        lineNumber = lineNumber * 3 - 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int currentLine = 0;
            boolean lineFound = false;
            while ((line = reader.readLine()) != null) {
                if (currentLine == lineNumber - 1) {
                    System.out.println(line);
                    lineFound = true;
                    break;
                }
                currentLine++;
            }
            if (!lineFound) {
                System.out.println("Line number " + lineNumber + " not found in the file.");
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }
}


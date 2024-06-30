import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrokerLogBinderImproved {
    
    private static final String QUESTION_MARK = " ?.?.? ";
    private static final String QUESTION_MARK_FINDER = "\\s\\?\\.\\?\\.\\?\\s";

    /**
     * Parses a broker log to convert parameterized SQL statements with placeholders
     * into executable SQL statements by replacing placeholders with actual parameter values.
     *
     * @param brokerLog       The broker log content
     * @param removeComments  Flag to indicate if comments should be removed
     * @return The parsed SQL statement
     */
    public static String parseBrokerLogToSQL(String brokerLog, boolean removeComments) {
        StringBuilder result = new StringBuilder();

        Pattern pstmtSQLPattern = Pattern.compile("(execute|execute_all)\\ssrv_h_id\\s\\d+\\s.+");
        Matcher pstmtSQLMatcher = pstmtSQLPattern.matcher(brokerLog);

        Pattern pstmtSQLPattern2 = Pattern.compile("(execute|execute_all)\\ssrv_h_id\\s\\d+\\s");

        Pattern paramPattern = Pattern.compile("bind\\s\\d+\\s:\\s(INT\\s.+|DATETIME\\s.+|TIME\\s.+|DATE\\s.+|BIGINT\\s.+|DOUBLE\\s.+|FLOAT\\s.+|SHORT\\s.+|(VARCHAR\\s\\(\\d+\\).+)|VARCHAR\\s\\(\\d+\\)|NULL)");
        Pattern valuePattern = Pattern.compile("bind\\s\\d+\\s:\\s(INT\\s|DATETIME\\s|DATE\\s|TIME\\s|BIGINT\\s|DOUBLE\\s|FLOAT\\s|SHORT\\s|(VARCHAR\\s\\(\\d+\\)|.+)|NULL)");
        Pattern typePattern = Pattern.compile("bind\\s\\d+\\s:\\s");

        extractPstmtSQL(brokerLog, result, pstmtSQLMatcher, pstmtSQLPattern2, removeComments);

        if (result.length() == 0) {
            return "";
        }

        List<Map<String, String>> paramTypeValueList = extractParameters(brokerLog, paramPattern, valuePattern, typePattern);

        String pstmtSQL = result.toString();
        pstmtSQL = replacePlaceholders(pstmtSQL, paramTypeValueList);

        return pstmtSQL.replaceAll(QUESTION_MARK_FINDER, "?");
    }

    private static void extractPstmtSQL(String brokerLog, StringBuilder result, Matcher pstmtSQLMatcher, Pattern pstmtSQLPattern2, boolean removeComments) {
        int index = 0;

        while (pstmtSQLMatcher.find()) {
            String onePstmtSQLString = pstmtSQLMatcher.group(0);
            Matcher pstmtSQLMatcher2 = pstmtSQLPattern2.matcher(onePstmtSQLString);

            if (pstmtSQLMatcher2.find()) {
                if (index != 0) {
                    result.append(System.lineSeparator());
                }

                String pstmtSQL = onePstmtSQLString.substring(pstmtSQLMatcher2.end())
                        .replaceAll("\\s{2,10}", " ")
                        .replaceAll("\\?", QUESTION_MARK);

                if (removeComments) {
                    pstmtSQL = removeComments(pstmtSQL);
                }

                result.append(pstmtSQL).append(" ;").append(System.lineSeparator());
                index++;
            }
        }
    }

    private static List<Map<String, String>> extractParameters(String brokerLog, Pattern paramPattern, Pattern valuePattern, Pattern typePattern) {
        List<Map<String, String>> paramTypeValueList = new ArrayList<>();
        Matcher paramMatcher = paramPattern.matcher(brokerLog);

        while (paramMatcher.find()) {
            String paramString = paramMatcher.group(0);
            Matcher valueMatcher = valuePattern.matcher(paramString);
            Matcher typeMatcher = typePattern.matcher(paramString);

            if (valueMatcher.find()) {
                String parameter = paramString.substring(valueMatcher.end());

                if (typeMatcher.find()) {
                    String type = paramString.substring(typeMatcher.end(), valueMatcher.end());
                    Map<String, String> map = new HashMap<>();
                    map.put(parameter, type);
                    paramTypeValueList.add(map);
                }
            }
        }

        return paramTypeValueList;
    }

    private static String replacePlaceholders(String pstmtSQL, List<Map<String, String>> paramTypeValueList) {
        for (Map<String, String> map : paramTypeValueList) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getKey();
                String type = entry.getValue();

                if (isNumber(type)) {
                    pstmtSQL = pstmtSQL.replaceFirst(QUESTION_MARK_FINDER, value);
                } else if ("NULL".equals(type)) {
                    pstmtSQL = pstmtSQL.replaceFirst(QUESTION_MARK_FINDER, "NULL");
                } else {
                    pstmtSQL = pstmtSQL.replaceFirst(QUESTION_MARK_FINDER, "'" + value + "'");
                }
            }
        }

        return pstmtSQL;
    }

    private static boolean isNumber(String type) {
        return type.contains("INT") || type.contains("BIGINT") || type.contains("DOUBLE") || type.contains("FLOAT") || type.contains("SHORT");
    }

    private static String removeComments(String sql) {
        sql = sql.replaceAll("/\\*.*?\\*/", " ");
        return sql.trim();
    }

    public static void main(String[] args) {
        boolean removeComments = false;
        String filename = null;

        for (String arg : args) {
            if ("--rm-comments".equals(arg)) {
                removeComments = true;
            } else if (filename == null) {
                filename = arg;
            } else {
                System.out.println("Usage: java BrokerLogBinder [--rm-comments] <filename>");
                return;
            }
        }

        if (filename == null) {
            System.out.println("Usage: java BrokerLogBinder [--rm-comments] <filename>");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String firstLine = br.readLine();
            if (firstLine != null && firstLine.startsWith("[Q")) {
                processQueryBlocks(br, firstLine, removeComments);
            } else {
                processEntireFile(br, firstLine, removeComments);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processQueryBlocks(BufferedReader br, String firstLine, boolean removeComments) throws IOException {
        StringBuilder queryBlock = new StringBuilder();
        boolean inQueryBlock = true;
        int queryBlockNumber = 1;

        queryBlock.append(firstLine).append(System.lineSeparator());

        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("[Q")) {
                if (queryBlock.length() > 0) {
                    System.out.println(queryBlockNumber++);
                    System.out.println(parseBrokerLogToSQL(queryBlock.toString(), removeComments));
                    queryBlock.setLength(0);
                }
                inQueryBlock = true;
            }

            if (inQueryBlock) {
                queryBlock.append(line).append(System.lineSeparator());
            }
        }

        if (queryBlock.length() > 0) {
            System.out.println(queryBlockNumber++);
            System.out.println(parseBrokerLogToSQL(queryBlock.toString(), removeComments));
        }
    }

    private static void processEntireFile(BufferedReader br, String firstLine, boolean removeComments) throws IOException {
        StringBuilder entireFileContent = new StringBuilder();
        entireFileContent.append(firstLine).append(System.lineSeparator());

        String line;
        while ((line = br.readLine()) != null) {
            entireFileContent.append(line).append(System.lineSeparator());
        }

        System.out.println(parseBrokerLogToSQL(entireFileContent.toString(), removeComments));
    }
}

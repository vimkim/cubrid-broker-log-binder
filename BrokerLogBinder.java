/**
 * Author: vimkim
 * Date: 2024-05-29
 *
 * This implementation is adapted from the CUBRID Manager project.
 * 
 * Original source code can be found at:
 * https://github.com/CUBRID/cubrid-manager/blob/develop/com.cubrid.common.ui/src/com/cubrid/common/ui/spi/util/CommonUITool.java#L1247
 *
 * The method provided parses a broker log to convert parameterized SQL statements
 * with placeholders into executable SQL statements by replacing placeholders with
 * actual parameter values.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrokerLogBinder {
    /**
     * get normal SQL from pstmtSQL and the parameter bind log
     *
     * @param pstmtSQL
     * @param parameterLog
     * @return
    */
    public static String parseBrokerLogToSQL(String brokerLog, boolean removeComments) {

        String questionMark = " ?.?.? ";
        String questionMarkFinder = "\\s\\?\\.\\?\\.\\?\\s";
        StringBuilder result = new StringBuilder();

        String pstmtSQLRegex = "(execute|execute_all)\\ssrv_h_id\\s\\d+\\s.+";
        Pattern pstmtSQLPattern = Pattern.compile(pstmtSQLRegex);
        Matcher pstmtSQLMatcher = pstmtSQLPattern.matcher(brokerLog);

        String pstmtSQLRegex2 = "(execute|execute_all)\\ssrv_h_id\\s\\d+\\s";
        Pattern pstmtSQLPattern2 = Pattern.compile(pstmtSQLRegex2);

        String paramRegex = "bind\\s\\d+\\s:\\s(INT\\s.+|DATETIME\\s.+|TIME\\s.+|DATE\\s.+|BIGINT\\s.+|DOUBLE\\s.+|FLOAT\\s.+|SHORT\\s.+|(VARCHAR\\s\\(\\d+\\).+)|VARCHAR\\s\\(\\d+\\)|NULL)";
        String valueRegex = "bind\\s\\d+\\s:\\s(INT\\s|DATETIME\\s|DATE\\s|TIME\\s|BIGINT\\s|DOUBLE\\s|FLOAT\\s|SHORT\\s|(VARCHAR\\s\\(\\d+\\)|.+)|NULL)";
        String typeRegex = "bind\\s\\d+\\s:\\s";
        Pattern paramPattern = Pattern.compile(paramRegex);
        Pattern valuePattern = Pattern.compile(valueRegex);
        Pattern typePattern = Pattern.compile(typeRegex);

        int index = 0;
        while (pstmtSQLMatcher.find()) {
            String onePstmtSQLString = pstmtSQLMatcher.group(0);
            Matcher pstmtSQLMatcher2 = pstmtSQLPattern2.matcher(onePstmtSQLString);
            if (pstmtSQLMatcher2.find()) {
                if (index != 0) {
                    result.append(System.getProperty("line.separator"));
                }
                String pstmtSQL = onePstmtSQLString.substring(pstmtSQLMatcher2.end());
                pstmtSQL = pstmtSQL.replaceAll("\\s{2,10}", " ");
                pstmtSQL = pstmtSQL.replaceAll("\\?", questionMark);

                if (removeComments) {
                    pstmtSQL = removeComments(pstmtSQL);
                }

                result.append(pstmtSQL).append(';').append(System.getProperty("line.separator"));
            }
            index++;
        }

    if (index == 0) {
            return "";
        }

        Matcher paramMatcher = paramPattern.matcher(brokerLog);
        List<HashMap<String, String>> paramTypeValueList = new ArrayList<HashMap<String, String>>();
        while (paramMatcher.find()) {
            String paramString = paramMatcher.group(0);
            Matcher valueMatcher = valuePattern.matcher(paramString);
            Matcher typeMatcher = typePattern.matcher(paramString);
            String parameter = "";
        if (valueMatcher.find()) {
                parameter = paramString.substring(valueMatcher.end());
                String type = "";
                if (typeMatcher.find()) {
                    type = paramString.substring(typeMatcher.end(), valueMatcher.end());
                }
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(parameter, type);
                paramTypeValueList.add(map);
            }
        }

        String pstmtSQL = result.toString();
        for (Map<String, String> map : paramTypeValueList) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
            if (isNumber(entry.getValue())) {
                    pstmtSQL = pstmtSQL.replaceFirst(questionMarkFinder, entry.getKey());
                } else if ("NULL".equals(entry.getValue())) {
                    pstmtSQL = pstmtSQL.replaceFirst(questionMarkFinder, "NULL");
                } else {
                    pstmtSQL = pstmtSQL.replaceFirst(questionMarkFinder, "'" + entry.getKey() + "'");
                }
            }
        }

        pstmtSQL = pstmtSQL.replaceAll(questionMarkFinder, "?");
        return pstmtSQL;
    }

    public static boolean isNumber(String type) {
        if (type.indexOf("INT") > -1 || type.indexOf("BIGINT") > -1 || type.indexOf("DOUBLE") > -1
        || type.indexOf("FLOAT") > -1 || type.indexOf("SHORT") > -1) {
            return true;
        }
        return false;
    }

    public static String removeComments(String sql) {
        // Remove single-line comments (starting with --)
        sql = sql.replaceAll("--.*?(\r?\n|$)", " ");
        // Remove multi-line comments (starting with /* and ending with */)
        sql = sql.replaceAll("/\\*.*?\\*/", " ");
        return sql.trim();
    }

       public static void main(String[] args) {
        boolean removeComments = false;
        String filename = null;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("--rm-comments".equals(args[i])) {
                removeComments = true;
            } else if (filename == null) {
                filename = args[i];
            } else {
                System.out.println("Usage: java BrokerLogBinder [--rm-comments] <filename>");
                return;
            }
        }

        if (filename == null) {
            System.out.println("Usage: java BrokerLogBinder [--rm-comments] <filename>");
            return;
        }

        StringBuilder brokerLog = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                brokerLog.append(line).append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sql = parseBrokerLogToSQL(brokerLog.toString(), removeComments);
        System.out.println(sql);
    }

}

package ag.com.dbo.services.queue.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ag.com.dbo.utils.Utils.getObjectMapper;

public class LogParser {
    /**
     *
     * @param table
     * table like
     * [ {
     *   "cnt" : "349965",
     *   "a" : "555",
     *   "xxxxx---" : "fgdfhsgfhdfscs"
     * }, {
     *   "cnt" : "1111115",
     *   "a" : "333",
     *   "xxxxx---" : "frereeeeeee"
     * } ]
     * @param row 0 - first  row
     * @param colName name of column
     * @return data
     */
    public static String parseTableAndSearchData(String table, int row, List<String> colName) throws Exception {
        String jsonTable =parseTable(table);
        Map result =  searchData(jsonTable,row, colName );
        return getObjectMapper().writeValueAsString(result);
    }

    private static String parseTable(String tableString) throws JsonProcessingException {
        String[] lines = tableString.split("\n");
        List<String> headers = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        // Match table rows: | value1 | value2 |
        Pattern rowPattern = Pattern.compile("\\|\\s*([^|]+)\\s*");

        for (String line : lines) {
            // Skip the borders like "+---------+" and "+------------+"
            if (line.trim().startsWith("+-")) continue;

            Matcher matcher = rowPattern.matcher(line);
            List<String> cells = new ArrayList<>();
            while (matcher.find()) {
                cells.add(matcher.group(1).trim());
            }

            if (!cells.isEmpty()) {
                if (headers.isEmpty()) {
                    headers.addAll(cells); // First data row with '|' is the header
                } else {
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int i = 0; i < headers.size() && i < cells.size(); i++) {
                        row.put(headers.get(i), cells.get(i));
                    }
                    rows.add(row);
                }
            }
        }

        return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rows);

    }


    public static String test() throws JsonProcessingException {
        String asciiData = "+---------+\n" +
                "| cnt | a |  xxxxx---|\n" +
                "+------------+\n" +
                "| 349965 |555|fgdfhsgfhdfscs|\n" +
                "+------------+\n"+
                "| 1111115 |333|frereeeeeee|\n" +
                "+------------+";
        ;

        String parsed = parseTable(asciiData);

        System.out.println("Row " + parsed);
        return parsed;
    }

    private static Map<String, String> searchData(String jsonString, int row, List<String> names) throws Exception{

        Map<String,String> result = new HashMap<>();
        ObjectMapper mapper = getObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);
        JsonNode rowNode =rootNode.get(row);

        for(String name: names){
            JsonNode node= rowNode.get(name);
            if (node== null){
                result.put(name, null);
            }else{
                result.put(name, node.textValue());
            }
        }

        return  result;


    }

}

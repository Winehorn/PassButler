package edu.hm.cs.ig.passbutler.util;

/**
 * Created by dennis on 07.12.17.
 */

public class SqlUtil {

    public static final String SELECTION_PLACEHOLDER_SUFFIX = "=?";
    public static final String AND = "AND";
    public static final String SPACE = " ";

    public static String createSelectionString(String... columnNames) {
        if(columnNames.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        String columnName;
        for (int i = 0; i < columnNames.length; i++) {
            columnName = columnNames[i];
            builder.append(columnName).append(SELECTION_PLACEHOLDER_SUFFIX);

            if((i + 1) < columnNames.length) {
                builder.append(SPACE)
                        .append(AND)
                        .append(SPACE);
            }
        }
        return builder.toString();
    }
}

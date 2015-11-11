package com.excelsiorjet;

import java.text.*;
import java.util.*;

/**
 * Utility class for retrieving strings from Excelsior JET {@code Strings.properties} files.
 * Those files consist of lines of the form
 * {@code 
 * <id> = <value>
 * }
 * Values are either plain strings or format strings eligible for {@code pattern} argument
 * of {@link MessageFormat#format(String, Object...)}
 *
 */
public class Messages {

    private final ResourceBundle messageRB;

    /**
     * Constructs an instance for a {@code Strings.properties} resource with the given name.
     * @param resourceName Strings.properties resource
     */
    public Messages(String resourceName) {
        messageRB = ResourceBundle.getBundle(resourceName);
        if (messageRB.getLocale().getLanguage().equals("")
        && ! Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage()))
        {
            Locale.setDefault(Locale.ENGLISH);
        }
    }

    /**
     * Obtains a string with the given {@code id} and formats it using the given {@code params}.
     * @param id key value in Strings.properties
     * @param params optional parameters to be expanded
     *
     * @return formatted resources string or {@code null} if there is no such string.
     */
    public String format(String id, Object ... params) {
        String str = getStringIfExists(id);
        if (str != null && params.length > 0) {
            return MessageFormat.format(str, params);
        } else {
            return str;
        }
    }

    /**
     * @return string with the given {@code id}, or {@code null} if it does not exist
     */
    private String getStringIfExists(String id) {
        String s;
        try {
            s = messageRB.getString(id);
        } catch (MissingResourceException e) {
            return null;
        }
        s = s.trim();
        if (s.length() >= 2 && s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\'') {
            s = s.substring(1, s.length()-1);
        }
        return s;
    }
}

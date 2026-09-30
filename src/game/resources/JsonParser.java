package game.resources;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small dependency-free JSON parser used for the game's data files.
 *
 * <p>The project is intentionally built with the JDK alone, so the content
 * files use this focused parser instead of requiring a third-party library.</p>
 */
public final class JsonParser {

    private final String source;
    private int index;

    private JsonParser(String source) {
        this.source = source;
    }

    public static Object parse(String source) {
        if (source == null) {
            throw new IllegalArgumentException("JSON source cannot be null.");
        }

        JsonParser parser = new JsonParser(source);
        Object value = parser.readValue();
        parser.skipWhitespace();

        if (!parser.isAtEnd()) {
            throw parser.error("Unexpected characters after the JSON value");
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected a JSON object.");
        }

        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> array(Object value) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected a JSON array.");
        }

        return (List<Object>) list;
    }

    public static String string(Map<String, Object> object, String key, String fallback) {
        Object value = object.get(key);
        return value instanceof String string ? string : fallback;
    }

    public static boolean bool(Map<String, Object> object, String key, boolean fallback) {
        Object value = object.get(key);
        return value instanceof Boolean bool ? bool : fallback;
    }

    public static Integer integer(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    private Object readValue() {
        skipWhitespace();

        if (isAtEnd()) {
            throw error("Expected a JSON value");
        }

        return switch (source.charAt(index)) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();

        if (consume('}')) {
            return object;
        }

        while (true) {
            skipWhitespace();
            if (isAtEnd() || source.charAt(index) != '"') {
                throw error("Expected a string object key");
            }

            String key = readString();
            skipWhitespace();
            expect(':');
            object.put(key, readValue());
            skipWhitespace();

            if (consume('}')) {
                return object;
            }

            expect(',');
        }
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> array = new ArrayList<>();
        skipWhitespace();

        if (consume(']')) {
            return array;
        }

        while (true) {
            array.add(readValue());
            skipWhitespace();

            if (consume(']')) {
                return array;
            }

            expect(',');
        }
    }

    private String readString() {
        expect('"');
        StringBuilder value = new StringBuilder();

        while (!isAtEnd()) {
            char character = source.charAt(index++);

            if (character == '"') {
                return value.toString();
            }

            if (character != '\\') {
                if (character < 0x20) {
                    throw error("Unescaped control character in string");
                }
                value.append(character);
                continue;
            }

            if (isAtEnd()) {
                throw error("Unterminated escape sequence");
            }

            char escaped = source.charAt(index++);
            switch (escaped) {
                case '"' -> value.append('"');
                case '\\' -> value.append('\\');
                case '/' -> value.append('/');
                case 'b' -> value.append('\b');
                case 'f' -> value.append('\f');
                case 'n' -> value.append('\n');
                case 'r' -> value.append('\r');
                case 't' -> value.append('\t');
                case 'u' -> value.append(readUnicodeEscape());
                default -> throw error("Invalid escape sequence");
            }
        }

        throw error("Unterminated string");
    }

    private char readUnicodeEscape() {
        if (index + 4 > source.length()) {
            throw error("Incomplete Unicode escape sequence");
        }

        String hex = source.substring(index, index + 4);
        index += 4;

        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException exception) {
            throw error("Invalid Unicode escape sequence");
        }
    }

    private Object readNumber() {
        int start = index;

        if (consume('-')) {
            // The rest of the number is validated below.
        }

        if (consume('0')) {
            // A leading zero is only valid by itself.
            if (!isAtEnd() && Character.isDigit(source.charAt(index))) {
                throw error("Invalid leading zero in number");
            }
        } else {
            if (isAtEnd() || !isDigitOneToNine(source.charAt(index))) {
                throw error("Invalid number");
            }

            while (!isAtEnd() && Character.isDigit(source.charAt(index))) {
                index++;
            }
        }

        if (consume('.')) {
            if (isAtEnd() || !Character.isDigit(source.charAt(index))) {
                throw error("Invalid number fraction");
            }

            while (!isAtEnd() && Character.isDigit(source.charAt(index))) {
                index++;
            }
        }

        if (!isAtEnd() && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
            index++;
            if (!isAtEnd() && (source.charAt(index) == '+' || source.charAt(index) == '-')) {
                index++;
            }
            if (isAtEnd() || !Character.isDigit(source.charAt(index))) {
                throw error("Invalid number exponent");
            }

            while (!isAtEnd() && Character.isDigit(source.charAt(index))) {
                index++;
            }
        }

        String number = source.substring(start, index);
        try {
            if (number.indexOf('.') < 0 && number.indexOf('e') < 0 && number.indexOf('E') < 0) {
                return Long.parseLong(number);
            }
            return Double.parseDouble(number);
        } catch (NumberFormatException exception) {
            throw error("Invalid number");
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!source.startsWith(literal, index)) {
            throw error("Invalid JSON literal");
        }

        index += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }

    private void expect(char expected) {
        if (!consume(expected)) {
            throw error("Expected '" + expected + "'");
        }
    }

    private boolean consume(char expected) {
        if (!isAtEnd() && source.charAt(index) == expected) {
            index++;
            return true;
        }

        return false;
    }

    private boolean isAtEnd() {
        return index >= source.length();
    }

    private static boolean isDigitOneToNine(char character) {
        return character >= '1' && character <= '9';
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at character " + index + ".");
    }
}

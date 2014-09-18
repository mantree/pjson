package pjson;

import java.nio.charset.Charset;

/**
 * Fast simple json parser.<br/>
 * The code is ugly but the aim is performance.
 *
 * @TODO Test parse messages with escape characters
 * @TODO test assoc then asString on maps and vectors
 * @TODO test LazyMap and LazyVector
 * @TODO Test JSONAssociative structures
 */
public final class PJSON {


    private static final char OBJECT_START = '{';
    private static final char OBJECT_END = '}';
    private static final char ARR_START = '[';
    private static final char ARR_END = ']';
    private static final int STR_MARK = '\"';
    private static final char STR_COMMA = ',';
    private static final char STR_ESCAPE = '\\';
    private static final char STR_SPACE = ' ';

    private static final char STR_DOT = '.';
    private static final char STR_T = 'T';
    private static final char STR_F = 'F';
    private static final char STR_t = 't';
    private static final char STR_f = 'f';

    private static final char STR_COL = ':';

    public static final Object parse(final Charset charset, final byte[] bts, final int start, final int len, final JSONListener events){
        final char[] chars = StringUtil.toCharArrayFromBytes(bts, charset, start, len);
        return parse(chars, 0, chars.length, events);
    }

    public static final Object lazyParse(final Charset charset, final byte[] bts, final int start, final int len, final JSONListener events){
        final char[] chars = StringUtil.toCharArrayFromBytes(bts, charset, start, len);
        return lazyParse(chars, 0, chars.length, events);
    }

    public static final Object parse(final char[] bts, final int start, final int end, final JSONListener events){
        performParse(bts, start, end, events);
        return events.getValue();
    }

    public static final Object lazyParse(final char[] bts, final int start, final int end, final JSONListener events){
        performLazyParse(bts, start, end, events);
        return events.getValue();
    }

    /**
     * Parses the byte array for json data.
     * @param bts byte array
     * @param start start offset
     * @param end the end index
     * @param events JSONListener, all events will be sent to this instance.
     */
    private static final int performParse(final char[] bts, final int start, final int end, final JSONListener events){
        final int btsLen = end;
        boolean inString = false;
        char bt;
        int i;

        for(i = start; i < btsLen; i++){

            bt = CharArrayTool.getChar(bts, i);
            if(bt == '{'){
                events.objectStart();

                int strStart = CharArrayTool.indexOf(bts, i+1, end, '"') + 1;
                int idx = CharArrayTool.endOfString(bts, strStart, end);
                //create string here
                events.key(StringUtil.fastToString(bts, strStart, idx - strStart));
                idx = CharArrayTool.indexOf(bts, idx+1, end, ':');
                idx++;

                for(; idx < btsLen; idx++){
                    idx = CharArrayTool.skipWhiteSpace(bts, idx, btsLen);
                    bt = CharArrayTool.getChar(bts, idx);

                    if(bt == '}') {
                        events.objectEnd();
                        return idx++;
                    }else if(bt == '"'){
                        final int strStart2 = idx + 1;
                        idx = CharArrayTool.endOfString(bts, strStart2, end);
                        events.string(StringUtil.fastToString(bts, strStart2, idx - strStart2));

                        //add short cut to the next item

                    }else if(Character.isDigit(bt)) {
                        final int endIndex = CharArrayTool.indexFirstNonNumeric(bts, idx, end);

                        if (bts[endIndex] == '.') {
                            int idx2 = CharArrayTool.indexFirstNonNumeric(bts, endIndex + 1, end);
                            parseDouble(bts, idx, idx2, events);
                            idx = idx2-1;
                        }else {
                            parseInt(bts, idx, endIndex, events);
                            idx = endIndex-1;
                        }
                    }
                    else if(bt == 't' || bt == 'T'){
                        events.number(Boolean.TRUE);
                        idx += 3;
                    }else if(bt == 'f' || bt == 'F'){
                        events.number(Boolean.FALSE);
                        idx += 4;
                    }else if(bt == '{' || bt == '[') {
                        idx = performParse(bts, idx, btsLen, events);
                    }else if(bt == ','){
                        int keyStart = CharArrayTool.indexOf(bts, idx+1, end, '"') + 1;
                        int keyEnd = CharArrayTool.endOfString(bts, keyStart, end);
                        events.key(StringUtil.fastToString(bts, keyStart, keyEnd-keyStart));
                        idx = CharArrayTool.indexOf(bts, idx+1, end, ':');
                    }

                }

                return idx;

            }else if(bt == '['){
                events.arrStart();

                int idx = CharArrayTool.skipWhiteSpace(bts, i+1, btsLen);

                for(; idx < btsLen; idx++){
                    idx = CharArrayTool.skipWhiteSpace(bts, idx, btsLen);
                    bt = CharArrayTool.getChar(bts, idx);

                    if(bt == ']') {
                        events.arrEnd();
                        return idx++;
                    }else if(bt == '"'){
                        final int strStart2 = idx + 1;
                        idx = CharArrayTool.endOfString(bts, strStart2, end);
                        events.string(StringUtil.fastToString(bts, strStart2, idx - strStart2));

                        //add short cut to the next item

                    }else if(Character.isDigit(bt)) {
                        final int endIndex = CharArrayTool.indexFirstNonNumeric(bts, idx, end);

                        if (bts[endIndex] == '.') {
                            int idx2 = CharArrayTool.indexFirstNonNumeric(bts, endIndex + 1, end);
                            parseDouble(bts, idx, idx2, events);
                            idx = idx2-1;
                        }else {
                            parseInt(bts, idx, endIndex, events);
                            idx = endIndex-1;
                        }
                    }
                    else if(bt == 't' || bt == 'T'){
                        events.number(Boolean.TRUE);
                        idx += 3;
                    }else if(bt == 'f' || bt == 'F'){
                        events.number(Boolean.FALSE);
                        idx += 4;
                    }else if(bt == '{' || bt == '[') {
                        idx = performParse(bts, idx, btsLen, events);
                    }
                }

                return idx;
            }
        }

        return i;
    }

    /**
     * Parses the byte array for json data and create lazy objects
     * @param bts byte array
     * @param start start offset
     * @param end the end index
     * @param events JSONListener, all events will be sent to this instance.
     */
    protected static final int performLazyParse(final char[] bts, final int start, final int end, final JSONListener events){
        final int btsLen = end;
        boolean inString = false;
        char bt;
        int i, endIndex;

        for(i = start; i < btsLen; i++){

            bt = CharArrayTool.getChar(bts, i);
            if(bt == '{'){
                events.objectStart();

                int strStart = CharArrayTool.indexOf(bts, i+1, end, '"') + 1;
                int idx = CharArrayTool.endOfString(bts, strStart, end);
                //create string here
                if(idx >= btsLen){
                    events.objectEnd();
                    return btsLen;
                }
                events.key(StringUtil.fastToString(bts, strStart, idx - strStart));
                idx = CharArrayTool.indexOf(bts, idx+1, end, ':');
                idx++;

                for(; idx < btsLen; idx++){
                    idx = CharArrayTool.skipWhiteSpace(bts, idx, btsLen);
                    bt = CharArrayTool.getChar(bts, idx);

                    switch (bt) {
                        case '}': events.objectEnd();
                                  break;
                        case '"': final int strStart2 = idx + 1;
                                  idx = CharArrayTool.endOfString(bts, strStart2, end);
                                  events.string(StringUtil.fastToString(bts, strStart2, idx - strStart2));
                                  break;
                        case 'n': events.string(null);
                                  idx += 3;
                                  break;
                        case 't':
                        case 'T': events.number(Boolean.TRUE);
                                  idx += 3;
                                  break;
                        case 'f':
                        case 'F': events.number(Boolean.FALSE);
                                  idx += 4;
                                  break;
                        case '{': endIndex = CharArrayTool.indexOfEndOfObject(bts, idx+1, btsLen, '{', '}');
                                  events.lazyObject(bts, idx, endIndex);
                                  idx = endIndex-1;
                                  break;
                        case '[': endIndex = CharArrayTool.indexOfEndOfObject(bts, idx+1, btsLen, '[', ']');
                                  events.lazyArr(bts, idx, endIndex);
                                  idx = endIndex-1;
                                  break;
                        case ',': int keyStart = CharArrayTool.indexOf(bts, idx+1, end, '"') + 1;
                                  int keyEnd = CharArrayTool.endOfString(bts, keyStart, end);
                                  events.key(StringUtil.fastToString(bts, keyStart, keyEnd-keyStart));
                                  idx = CharArrayTool.indexOf(bts, idx+1, end, ':');
                                  break;
                        default:  if(Character.isDigit(bt)) {
                                      endIndex = CharArrayTool.indexFirstNonNumeric(bts, idx, end);

                                      if (bts[endIndex] == '.') {
                                          int idx2 = CharArrayTool.indexFirstNonNumeric(bts, endIndex + 1, end);
                                          parseDouble(bts, idx, idx2, events);
                                          idx = idx2-1;
                                      }else {
                                          parseInt(bts, idx, endIndex, events);
                                          idx = endIndex-1;
                                      }
                                  }
                        }
                }

                return idx;

            }else if(bt == '['){
                events.arrStart();

                int idx = CharArrayTool.skipWhiteSpace(bts, i+1, btsLen);

                for(; idx < btsLen; idx++){
                    idx = CharArrayTool.skipWhiteSpace(bts, idx, btsLen);
                    bt = CharArrayTool.getChar(bts, idx);

                    switch (bt) {
                        case ']': events.arrEnd();
                                  break;
                        case '"': final int strStart2 = idx + 1;
                                  idx = CharArrayTool.endOfString(bts, strStart2, end);
                                  events.string(StringUtil.fastToString(bts, strStart2, idx - strStart2));
                                  break;
                        case 't': events.number(Boolean.TRUE);
                                  idx += 3;
                                  break;
                        case 'T': events.number(Boolean.TRUE);
                                  idx += 3;
                                  break;
                        case 'f': events.number(Boolean.FALSE);
                                  idx += 3;
                                  break;
                        case 'F': events.number(Boolean.FALSE);
                                  idx += 3;
                                  break;
                        case '{': endIndex = CharArrayTool.indexOfEndOfObject(bts, idx+1, btsLen, '{', '}');
                                  events.lazyObject(bts, idx, endIndex);
                                  idx = endIndex;
                                  break;
                        case '[': endIndex = CharArrayTool.indexOfEndOfObject(bts, idx+1, btsLen, '[', ']');
                                  events.lazyArr(bts, idx, endIndex);
                                  idx = endIndex;
                                  break;
                        default: if(Character.isDigit(bt)) {
                                      endIndex = CharArrayTool.indexFirstNonNumeric(bts, idx, end);

                                      if (bts[endIndex] == '.') {
                                          int idx2 = CharArrayTool.indexFirstNonNumeric(bts, endIndex + 1, end);
                                          parseDouble(bts, idx, idx2, events);
                                          idx = idx2-1;
                                      }else {
                                          parseInt(bts, idx, endIndex, events);
                                          idx = endIndex-1;
                                      }
                                  }
                    }
                }
                return idx;
            }
        }

        return i;
    }

    private static final void parseInt(char[] bts, int offset, int end, JSONListener events){
        final String str = StringUtil.fastToString(bts, offset, end-offset);
        events.number(Long.valueOf(str));
    }

    private static final void parseDouble(char[] bts, int offset, int end, JSONListener events){
        final String str = StringUtil.fastToString(bts, offset, end-offset);
        events.number(Double.valueOf(str));
    }

    /**
     * Parse the bts byte array from offset start and to < len.
     * @param bts
     * @param start
     * @param len
     * @return Object Map or Collection
     */
    public static final Object defaultParse(final Charset charset, final byte[] bts, final int start, final int len){
        final DefaultListener list = new DefaultListener();
        return parse(charset, bts, start, len, list);
    }

    /**
     * Parse the whole byte array bts.
     * @param bts
     * @return Object Map or Collection.
     */
    public static final Object defaultParse(final Charset charset, final byte[] bts){
        return defaultParse(charset, bts, 0, bts.length);
    }

    /**
     * Parse the bts byte array from offset start and to < len.
     * @param bts
     * @param start
     * @param len
     * @return Object Map or Collection
     */
    public static final Object defaultLazyParse(final Charset charset, final byte[] bts, final int start, final int len){
        final DefaultListener list = new DefaultListener();
        //lazyParse(final char[] bts, final int start, final int end, final JSONListener events
        return lazyParse(charset, bts, start, len, list);
    }

    public static final Object defaultLazyParse(final char[] bts, final int start, final int len){
        final DefaultListener list = new DefaultListener();
        //lazyParse(final char[] bts, final int start, final int end, final JSONListener events
        return lazyParse(bts, start, len, list);
    }

    /**
     * Parse the whole byte array bts.
     * @param bts
     * @return Object Map or Collection.
     */
    public static final Object defaultLazyParse(final Charset charset, final char[] bts){
        return defaultLazyParse(bts, 0, bts.length);
    }

    /**
     * Parse the whole byte array bts.
     * @param bts
     * @return Object Map or Collection.
     */
    public static final Object defaultLazyParse(final Charset charset, final byte[] bts){
        return defaultLazyParse(charset, bts, 0, bts.length);
    }
}

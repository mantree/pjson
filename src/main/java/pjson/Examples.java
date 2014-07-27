package pjson;

import java.util.Map;

/**
 */
public class Examples {

    public static void testReadMessage(){

        byte[] msg_bts = "{\"id\" \"0.0.1.71.105.212.33.205.21.86.123.210.94.161.24.1.3\", \"uuid\" 5090315992110618240, \"id2\" 11, \"ts\" 1406229815757, \"type\" 1, \"anothertype\" \"null\", \"events\" {\"info\" {\"id\" 770128}, \"id2\" 3, \"events\" {\"id\" 930415460}, \"timestamp\" 1406229815757}, \"size\" 5, \"slots\" 1, \"geo\" {\"city\" 9065607, \"country\" 59, \"state\" 162, \"zip\" 121397}}".getBytes();

        Map<Object, Object> obj = (Map<Object, Object>) PJSON.defaultParse(StringUtil.DEFAULT_CHAR_SET, msg_bts);
        System.out.println(obj);
    }

    public static void main(String args[]){
        testReadMessage();

    }
}

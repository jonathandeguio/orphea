package io.movetodata.sharedUtils.Response;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class OkResponse {

    public Map<String, Object> okResponse (String message) {

        Map<String, Object> map = new TreeMap<>(((Comparator<String>) String::compareTo).reversed());
        map.put("status", 200);
        map.put("message", message);

        return map;
    }
}

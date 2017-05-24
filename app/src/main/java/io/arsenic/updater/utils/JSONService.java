package io.arsenic.updater.utils;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;

public class JSONService {


    /**
     * Making web service call
     *
     * @param urlAddress - url to make request
     */
    public static String request(String urlAddress) {
        StringBuilder response = new StringBuilder();
        try {
            response.append( Bridge
                    .get(urlAddress)
                    .request()
                    .response()
                    .asString());
            } catch (BridgeException e1) {
            e1.printStackTrace();
        }
        return response.toString();
    }

}
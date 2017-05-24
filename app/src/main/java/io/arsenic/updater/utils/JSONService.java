package io.arsenic.updater.utils;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;

import org.json.JSONObject;

public class JSONService {


    /**
     * Making web service call
     *
     * @param urlAddress - url to make request
     */
    public static JSONObject request(String urlAddress) {
        JSONObject response =  new JSONObject();
        try {
            response = Bridge
                    .get(urlAddress)
                    .request()
                    .response()
                    .asJsonObject();
            } catch (BridgeException e1) {
            e1.printStackTrace();
        }
        return response;
    }

}
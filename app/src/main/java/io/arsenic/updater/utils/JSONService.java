package io.arsenic.updater.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class JSONService {

    public final static int GET = 1;
    private final static int POST = 2;

    /**
     * Making web service call
     *
     * @param urlAddress - url to make request
     * @param method - http request method
     */
    public static String request(String urlAddress, int method) {
        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL(urlAddress);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);

            if (method == POST)
                conn.setRequestMethod("POST");
            else if (method == GET)
                conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null)
                    response.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

}
/*
 * Heavily based on https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java
 */

package game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

class HttpURLConnection {

    private static final String USER_AGENT = "Eist-TheGame";

    // HTTP GET request
    static String sendGet(String url) throws Exception {

        URL obj = new URL(url);
        java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();

        System.out.println("Sending 'GET' request to " + url);
        System.out.println("Response Code: " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();

    }
}

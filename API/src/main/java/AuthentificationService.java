package main.java;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class AuthentificationService {
    private static final String CAS_URL = "https://identites.ensea.fr/cas/v1/tickets";
    private final String username;
    private final String password;

    public AuthentificationService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getTicketGrantingTicket() throws Exception {
        URL url = new URL(CAS_URL);
        String params = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            String locationHeader = connection.getHeaderField("Location");
            if (locationHeader != null && locationHeader.contains("TGT-")) {
                return locationHeader; // Return the TGT URL directly
            } else {
                throw new RuntimeException("Failed to get TGT");
            }
        } else {
            throw new IOException("Server returned HTTP response code: " + responseCode + " for URL: " + url);
        }
    }

    public String getServiceTicket(String tgtUrl, String serviceUrl) throws Exception {
        URL url = new URL(tgtUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String input = "service=" + URLEncoder.encode(serviceUrl, StandardCharsets.UTF_8);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(input);
        writer.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();
        } else {
            throw new IOException("Server returned HTTP response code: " + responseCode + " for URL: " + url);
        }
    }

    public Future<UserAttributes> getUserAttributes(String authToken, String serviceUrl) throws IOException {
        URL url = new URL("https://identites.ensea.fr/cas/serviceValidate?ticket=" + URLEncoder.encode(authToken, StandardCharsets.UTF_8) + "&service=" + URLEncoder.encode(serviceUrl, StandardCharsets.UTF_8));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Traitement de la réponse pour extraire les attributs utilisateur
            String responseBody = response.toString();
            UserAttributes userAttributes = extractUserAttributes(responseBody); // Une méthode à implémenter pour extraire les attributs de la réponse

            return CompletableFuture.completedFuture(userAttributes);
        } else {
            throw new IOException("Server returned HTTP response code: " + responseCode + " for URL: " + url);
        }
    }

    private UserAttributes extractUserAttributes(String responseBody) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));

            NodeList attributes = document.getElementsByTagName("cas:attributes");
            if (attributes.getLength() > 0) {
                Element attributesElement = (Element) attributes.item(0);

                String clientIpAddress = attributesElement.getElementsByTagName("cas:clientIpAddress").item(0).getTextContent();
                String isFromNewLogin = attributesElement.getElementsByTagName("cas:isFromNewLogin").item(0).getTextContent();
                String mail = attributesElement.getElementsByTagName("cas:mail").item(0).getTextContent();
                String authenticationDate = attributesElement.getElementsByTagName("cas:authenticationDate").item(0).getTextContent();

                return new UserAttributes(clientIpAddress, isFromNewLogin, mail, authenticationDate);
            } else {
                throw new RuntimeException("No attributes found in the response");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the response", e);
        }
    }

    public void shutdown() {
    }
}
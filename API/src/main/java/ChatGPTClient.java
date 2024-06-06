package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

public class ChatGPTClient {
    private String APIKey;
    private final String https_url = "https://api.openai.com/v1/chat/completions";
    private int promptTokens = 0;
    private int completionTokens = 0;
    private int totalTokens = 0;

    public ChatGPTClient() {
        try {
            FileReader fileReader = new FileReader(new File("lib/API_Key.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            APIKey = bufferedReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String queryChatGPT() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Authorization", "Bearer " + APIKey, "Content-Type", "application/json", "Accept", "application/json")
                .uri(URI.create(https_url))
                .POST(HttpRequest.BodyPublishers.ofString("{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\", \"content\":\"Say i'm ChatGPT\"}]}"))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());

            if (jsonResponse.has("usage")) {
                this.promptTokens = jsonResponse.getJSONObject("usage").getInt("prompt_tokens");
                this.completionTokens = jsonResponse.getJSONObject("usage").getInt("completion_tokens");
                this.totalTokens = jsonResponse.getJSONObject("usage").getInt("total_tokens");
            }

            JSONArray choices = jsonResponse.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OOK";
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }
}

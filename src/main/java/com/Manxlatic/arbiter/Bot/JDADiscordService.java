package com.Manxlatic.arbiter.Bot;

import okhttp3.*;
import org.json.simple.JSONObject;

import java.io.IOException;


public class JDADiscordService {

    private final OkHttpClient client = new OkHttpClient();
    private final String webhookUrl;

    public JDADiscordService(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendMessage(String content) {
        // Escape double quotes in the content
        JSONObject json = new JSONObject();
        json.put("content", content);

        // Convert JSON object to string
        String jsonBody = json.toString();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonBody
        );

        // Create the request
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace(); // Handle the failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.err.println("Failed to send message: " + response.message());
                    System.err.println("Response body: " + response.body().string());
                }
            }
        });
    }
}



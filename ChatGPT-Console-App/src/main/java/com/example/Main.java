package com.example;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    // The OpenAI Chat Completions API endpoint
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    // The OpenAI API Key should be set as an environment variable
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    // Media type for JSON requests
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void main(String[] args) {
        // Check if API key is available
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            System.err.println("Error: OPENAI_API_KEY environment variable is not set.");
            System.err.println("Please set your OpenAI API key before running the application.");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // For pretty printing JSON responses

        Scanner scanner = new Scanner(System.in);
        System.out.println("ChatGPT Console Application 🤖");
        System.out.println("Type your message and press Enter. Type 'exit' to quit.");

        while (true) {
            System.out.print("\n🧑 You: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Exiting application. Goodbye! 👋");
                break;
            }

            try {
                // Construct the JSON request body
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", userInput);

                JsonArray messagesArray = new JsonArray();
                messagesArray.add(message);

                JsonObject requestBodyJson = new JsonObject();
                requestBodyJson.addProperty("model", "gpt-3.5-turbo"); // Or "gpt-4o" for a more advanced model
                requestBodyJson.add("messages", messagesArray);
                requestBodyJson.addProperty("max_tokens", 150); // Limit the response length
                requestBodyJson.addProperty("temperature", 0.7); // Creativity of the response (0.0-1.0)

                RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);

                // Build the HTTP request
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Execute the request and get the response
                Response response = client.newCall(request).execute();

                // Check if the request was successful
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    // Extract and print the assistant's message
                    String assistantMessage = jsonResponse.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();
                    System.out.println("🤖 ChatGPT: " + assistantMessage.trim());
                } else {
                    System.err.println("Error: " + response.code() + " " + response.message());
                    System.err.println("Response body: " + response.body().string());
                }
            } catch (IOException e) {
                System.err.println("An error occurred while communicating with the API: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}
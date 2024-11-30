package com.example.CareAi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText symptomsInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private GenerativeModel model;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Initialize Gemini AI with API key
        model = new GenerativeModel("gemini-pro", "AIzaSyADF-j-FhEVlPgeokePfkN3Sz0RcCeyLbc");

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        symptomsInput = findViewById(R.id.symptomsInput);
        sendButton = findViewById(R.id.sendButton);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);  // Find the RecyclerView in the layout

// Set the layout manager for vertical scrolling
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

// Set the adapter for the RecyclerView
        chatAdapter = new ChatAdapter();  // Initialize the adapter
        chatRecyclerView.setAdapter(chatAdapter);

        // Add initial bot message
        chatAdapter.addMessage(new Message(
                "Hello! I'm Care AI. Please describe your symptoms.",
                false // false for AI message
        ));

        // Setup send button action
        sendButton.setOnClickListener(v -> handleUserInput());
    }

    private void handleUserInput() {
        String userInput = symptomsInput.getText().toString().trim();
        if (userInput.isEmpty()) return;

        // Add user message to chat
        chatAdapter.addMessage(new Message(userInput, true));  // 'true' indicates it's a user message
        symptomsInput.setText("");  // Clear input field

        // Process with Gemini AI
        analyzeSymptoms(userInput);
    }

    private void analyzeSymptoms(String symptoms) {
        String prompt = "Based on these symptoms: " + symptoms +
                "\nProvide a possible diagnosis, suggested over-the-counter medications " +
                "(with standard dosage), and home remedies.";

        executorService.execute(() -> {
            try {
                // AI response handling
                Continuation<GenerateContentResponse> continuation = new Continuation<GenerateContentResponse>() {
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(Object o) {
                        if (o instanceof GenerateContentResponse) {
                            GenerateContentResponse response = (GenerateContentResponse) o;
                            String result = response.getText();
                            runOnUiThread(() -> {
                                // Add AI message
                                chatAdapter.addMessage(new Message(result, false));  // 'false' indicates it's an AI response
                                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                            });
                        } else if (o instanceof Throwable) {
                            runOnUiThread(() -> {
                                chatAdapter.addMessage(new Message(
                                        "Sorry, I couldn't process your symptoms. Please try again.",
                                        false
                                ));
                            });
                        }
                    }
                };

                // Call generateContent with the continuation
                model.generateContent(prompt, continuation);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    chatAdapter.addMessage(new Message(
                            "Sorry, I couldn't process your symptoms. Please try again.",
                            false
                    ));
                });
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // Clean up the executor service
    }
}
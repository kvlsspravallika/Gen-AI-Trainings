package com.epam.trainings.gen_ai_task_3.util;

import com.azure.ai.openai.models.ImageGenerations;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ResponseGenerator {

    public String getChatResponseAsString(ChatHistory chatHistory) {
        return chatHistory.getMessages().stream()
                .map(chatMessageContent -> chatMessageContent.getAuthorRole().name()
                        + " : " + chatMessageContent.getContent())
                .collect(Collectors.joining("\n\n"));
    }

//    public List<String> getImageUrlsList(Mono<ImageGenerations> images) throws ExecutionException, InterruptedException {
//        CompletableFuture<List<String>> result = images.map(response -> {
//            List<String> imageUrls = new ArrayList<>();
//            for (var image : response.getData()) {
//                imageUrls.add(image.getUrl());
//            }
//            return imageUrls;
//        }).toFuture(); // Converts Mono<List<String>> to CompletableFuture<List<String>> and then retrieves result using blockingGet
//
//        List<String> output = new ArrayList<>();
//        result.thenAccept(imageUrls -> {
//            System.out.println("Generated Image URLs: " + imageUrls);
//            output.add(imageUrls);
//        });
//    }
}

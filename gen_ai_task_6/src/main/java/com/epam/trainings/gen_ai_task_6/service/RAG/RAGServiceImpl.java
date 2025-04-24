package com.epam.trainings.gen_ai_task_6.service.RAG;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.trainings.gen_ai_task_6.model.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_6.service.GenAI.GenAIService;
import com.epam.trainings.gen_ai_task_6.service.textEmbedding.TextEmbeddingService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Component
public class RAGServiceImpl implements RAGService{

    @Value("${deployment.embedding.models}")
    private String embeddingDeploymentName;

    private static final String COLLECTION_NAME = "rag-knowledge";

    private final OpenAIAsyncClient openAIAsyncClient;
    private final QdrantClient qdrantClient;
    private final TextEmbeddingService textEmbeddingService;
    private final GenAIService genAIService;


    RAGServiceImpl(@Qualifier("getGenericOpenAIAsyncClientBean") OpenAIAsyncClient openAIAsyncClient,
                QdrantClient qdrantClient, TextEmbeddingService textEmbeddingService,
                   GenAIService genAIService) {
        this.openAIAsyncClient = openAIAsyncClient;
        this.qdrantClient = qdrantClient;
        this.textEmbeddingService = textEmbeddingService;
        this.genAIService = genAIService;
    }

    @Override
    public String uploadAndStoreKnowledge(String url) throws IOException, ExecutionException, InterruptedException {
        String textFromUrl = readFromUrl(url);
        return storeKnowledge(textFromUrl, url);
    }

    private String storeKnowledgeInChunks(List<String> chunks, String contextPath) throws Exception {
        for(String chunk : chunks) {
            var points = new ArrayList<List<Float>>();
            openAIAsyncClient
                    .getEmbeddings(embeddingDeploymentName,
                            new EmbeddingsOptions(List.of(chunk)))
                    .block().getData().forEach(
                            embeddingItem ->
                                    points.add(new ArrayList<>(embeddingItem.getEmbedding())));
            var pointStructs = new ArrayList<Points.PointStruct>();
            Map<String, JsonWithInt.Value> payloadMap = new HashMap<>();
            payloadMap.put("context", JsonWithInt.Value.newBuilder().setStringValue(contextPath).build());
            payloadMap.put("Data", JsonWithInt.Value.newBuilder().setStringValue(chunk).build());
            points.forEach(point -> {
                pointStructs.add(Points.PointStruct.newBuilder()
                        .setId(id(UUID.randomUUID()))
                        .setVectors(vectors(point))
                        .putAllPayload(payloadMap)
                        .build());
            });
            Points.UpdateResult response = qdrantClient.upsertAsync(COLLECTION_NAME, pointStructs).get();
            if (response != null && response.getStatus() == Points.UpdateStatus.Completed) {
                log.info("Upsert successful! Operation ID: {} and pointId: {} ", response.getOperationId(), pointStructs.getFirst().getId());
            } else {
                log.error("Upsert failed with status:  {} ", (response != null ? response.getStatus() : "null"));
                throw new Exception("failed to store knowledge in chunks");
            }
        }
        return "Knowledge stored in chunks successfully";
    }

    private String storeKnowledge(String input, String contextPath) throws ExecutionException, InterruptedException {
        var points = new ArrayList<List<Float>>();
        openAIAsyncClient
                .getEmbeddings(embeddingDeploymentName,
                        new EmbeddingsOptions(List.of(input)))
                .block().getData().forEach(
                        embeddingItem ->
                                points.add(new ArrayList<>(embeddingItem.getEmbedding())));
        var pointStructs = new ArrayList<Points.PointStruct>();
        Map<String, JsonWithInt.Value> payloadMap = new HashMap<>();
        payloadMap.put("context", JsonWithInt.Value.newBuilder().setStringValue(contextPath).build());
        payloadMap.put("Data", JsonWithInt.Value.newBuilder().setStringValue(input).build());
        points.forEach(point -> {
            pointStructs.add(Points.PointStruct.newBuilder()
                    .setId(id(UUID.randomUUID()))
                    .setVectors(vectors(point))
                    .putAllPayload(payloadMap)
                    .build());
        });
        Points.UpdateResult response = qdrantClient.upsertAsync(COLLECTION_NAME, pointStructs).get();
        if (response!=null && response.getStatus() == Points.UpdateStatus.Completed) {
            log.info("Upsert successful! Operation ID: {}  ", response.getOperationId());
            return "Knowledge successfully stored successfully with Point id : " + pointStructs.getFirst().getId();
        } else {
            log.error("Upsert failed with status:  {} ", (response != null ? response.getStatus() : "null"));
            return "Failed to store vector";
        }
    }

    private String readFromUrl(String url) throws IOException {
        try {
            Document document = Jsoup.connect(url).get();
            return document.text();
        } catch(Exception e) {
            log.error("Exception occurred while reading from url");
            throw e;
        }
    }

    @Override
    public String getResponseFromRAG(ChatBotRequest request) throws ExecutionException, InterruptedException {
        List<Points.ScoredPoint> response = textEmbeddingService.search(request.getUserPrompt(), Optional.of(COLLECTION_NAME));
        String promptWithRag = "Use the following context(from vector Collection): " +
                "\n\n" +
                response.getFirst().getPayloadMap().get("Data") +
                "\n" +
                " to get the answer for the question: " +
                request.getUserPrompt() +
                "\n\n";
        return genAIService.getChatResult(ChatBotRequest.builder()
                .userPrompt(promptWithRag)
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .build(), Optional.of("gpt-4"));
    }

    @Override
    public String uploadAndStoreKnowledgeFromFile(MultipartFile file) throws Exception {
        String extractedText = extractText(file);
        // dividing extracted input into chunks
        List<String> chunks = chunkText(extractedText, 500, 100);
        // Normalize line breaks
        String cleanedText = extractedText.replaceAll("\\r\\n", "\n").replaceAll("\\\\n", "\n");

        return storeKnowledge(cleanedText, file.getName());
    }

    private String extractText(MultipartFile file) throws Exception {
        Tika tika = new Tika();
        return tika.parseToString(file.getInputStream());
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }

}

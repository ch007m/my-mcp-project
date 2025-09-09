package dev.snowdrop;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.vertexai.VertexAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;

import java.time.Duration;
import java.util.List;

public class McpAIClient {

    private static String projectId = "itpc-gcp-cp-pe-eng-claude";
    private static String location = "us-east5";
    private static String modelName = "claude-sonnet-4-20250514"; //"gemini-2.5-flash";
    private static String publisher = "anthropic";
    private static String endpoint = "-aiplatform.googleapis.com:443";

    public static void main(String[] args) {
        StreamableHttpMcpTransport transport = new StreamableHttpMcpTransport.Builder()
            .url("http://0.0.0.0:8080/mcp/sse")
            .logRequests(true) // if you want to see the traffic in the log
            .logResponses(true)
            .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
            .transport(transport)
            .toolExecutionTimeout(Duration.ofSeconds(4))
            .build();
        mcpClient.listTools().stream().forEach(System.out::println);

        ToolProvider toolProvider = McpToolProvider.builder()
            .mcpClients(List.of(mcpClient))
            .build();

        VertexAiChatModel model = VertexAiChatModel.builder()
        //VertexAiGeminiChatModel model = VertexAiGeminiChatModel.builder()
            .endpoint(location + "-" + endpoint)
            .project(projectId)
            .location(location)
            .publisher(publisher)
            .modelName(modelName)
            .maxOutputTokens(500)
            .build();

        WeatherAssistant meteo = AiServices.builder(WeatherAssistant.class)
            .chatModel(model)
            .toolProvider(toolProvider)
            .build();

        List.of(
            "Hello!",
            "What's the weather like in Paris today?"
        ).forEach((String q) -> {
            System.out.println(q);
            System.out.println(meteo.request(q));
        });
    }
}

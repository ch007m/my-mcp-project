package dev.snowdrop;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;

import java.time.Duration;

public class McpClient {

    public static void main(String[] args) {
        StreamableHttpMcpTransport transport = new StreamableHttpMcpTransport.Builder()
            .url("http://0.0.0.0:8080/mcp/sse")
            .logRequests(true) // if you want to see the traffic in the log
            .logResponses(true)
            .build();

        dev.langchain4j.mcp.client.McpClient mcpClient = new DefaultMcpClient.Builder()
            .transport(transport)
            .toolExecutionTimeout(Duration.ofSeconds(4))
            .build();
        mcpClient.listTools().stream().forEach(System.out::println);

        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
            .name("weather-forecast")
            .arguments("{\"location\":\"Brussels\"}").build();
        String result = mcpClient.executeTool(toolExecutionRequest);
        System.out.println(result);

    }
}

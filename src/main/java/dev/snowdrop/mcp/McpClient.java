package dev.snowdrop.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import org.jboss.logging.Logger;

import java.time.Duration;

public class McpClient {
    private static Logger logger = Logger.getLogger(McpClient.class);
    public static void main(String[] args) {
        StreamableHttpMcpTransport transport = new StreamableHttpMcpTransport.Builder()
            // Use the streamable endpoint - See quarkus server log
            .url("http://0.0.0.0:8080/mcp")
            .logRequests(true) // if you want to see the traffic in the log
            .logResponses(true)
            .build();

        dev.langchain4j.mcp.client.McpClient mcpClient = new DefaultMcpClient.Builder()
            .transport(transport)
            .toolExecutionTimeout(Duration.ofSeconds(4))
            .build();

        logger.infof("Listing tools of the MCP server ...");
        mcpClient.listTools().stream().forEach(t -> logger.info(t.toString()));

        logger.infof("Call the getAlerts tool ...");
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
            .name("getAlerts")
            .arguments("{\"state\":\"CA\"}").build();

        try {
            String result = mcpClient.executeTool(toolExecutionRequest);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

package dev.snowdrop;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.List;

public class JettyMCPServer {
    public static void main(String[] args) throws Exception {
        HttpServletSseServerTransportProvider transportProvider =
            new HttpServletSseServerTransportProvider(
                new ObjectMapper(), "/sse", "/sse");

        McpSyncServer syncServer = McpServer.sync(transportProvider)
            .serverInfo("custom-server", "0.0.1")
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .resources(false, false)
                .prompts(false)
                .build())
            .build();

        McpServerFeatures.SyncToolSpecification syncToolSpecification =
            new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("weather-forecast",
                    "gives today's weather forecast for a given location",
                    """
                        {
                          "type": "object",
                          "properties": {
                            "location": {
                              "type": "string"
                            }
                          },
                          "required": ["location"]
                        }
                        """
                ),
                (mcpSyncServerExchange, stringObjectMap) -> {
                    return new McpSchema.CallToolResult(
                        List.of(new McpSchema.TextContent("""
                            {
                                "location": "Paris",
                                "forecast": "Nice and sunny weather, with clear blue sky, and temperature of 17°C."
                            }
                            """
                        )), false);
                }
            );
        syncServer.addTool(syncToolSpecification);

        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(45450);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(transportProvider), "/*");

        server.setHandler(context);
        server.start();
        System.out.println("Server started on port: " + connector.getLocalPort());
    }
}

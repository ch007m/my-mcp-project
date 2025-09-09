package dev.snowdrop.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import org.jboss.logging.Logger;

import java.time.Duration;

import static dev.snowdrop.weather.tools.WeatherMcpTools.roundingCoordinate;

public class StreamableHttpClient {
    private static Logger logger = Logger.getLogger(StreamableHttpClient.class);

    public static void main(String[] args) throws Exception {

        StreamableHttpMcpTransport transport = new StreamableHttpMcpTransport.Builder()
            // Use the streamable endpoint - See quarkus server log
            .url("http://0.0.0.0:8080/mcp")
            .logRequests(true)
            .logResponses(true)
            .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
            .transport(transport)
            .toolExecutionTimeout(Duration.ofSeconds(4))
            .build();

        logger.infof("Listing the tools discovered from the MCP server ...");
        mcpClient.listTools().stream().forEach(t -> logger.info(t.toString()));

        try {
            ToolExecutionRequest toolExecutionRequest;

/*            logger.infof("Call the getAlerts tool ...");
            toolExecutionRequest = ToolExecutionRequest.builder()
                .name("getAlerts")
                .arguments("{\"state\":\"CA\"}")
                .build();
            logger.info(mcpClient.executeTool(toolExecutionRequest));*/

            /*
             !!!! To avoid the following error returned by the API: "Adjusting Precision Of Point Coordinate" with an HTTP 301
             it is then needed to round the coordinate: latitude or longitude

            double latitude = roundingCoordinate(40.71427);
            double longitude = roundingCoordinate(-74.00597);
            logger.infof("Latitude: %s",latitude);
            logger.infof("Longitude: %s",longitude);
            */

            String location = """
                {
                  "latitude": 40.7143,
                  "longitude": -74.006
                }
                """;


            logger.infof("Call the getForecast tool ...");
            toolExecutionRequest = ToolExecutionRequest.builder()
                .name("getForecast")
                .arguments(location)
                .build();
            logger.info(mcpClient.executeTool(toolExecutionRequest));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mcpClient.close();
        }


    }
}

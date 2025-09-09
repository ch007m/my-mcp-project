package dev.snowdrop.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import org.jboss.logging.Logger;
import org.wildfly.common.Assert;

import java.text.DecimalFormat;
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

            /*
             !!!! To avoid the following error returned by the API: "Adjusting Precision Of Point Coordinate" with an HTTP 301
             it is then needed to round the coordinate: latitude or longitude

             Example: 40.71427 -> 40.7143; -74.00597 -> -74.006
            */
            double latitude = roundingCoordinate(40.71427);
            Assert.assertTrue(latitude == 40.7143);
            double longitude = roundingCoordinate(-74.00597);
            Assert.assertTrue(longitude == -74.006);

            DecimalFormat df = new DecimalFormat("#.####");
            String tmplLocation = """
                {
                  "latitude": %s,
                  "longitude": %s
                }
                """;

            String location = String.format(tmplLocation, df.format(latitude), df.format(longitude));
            logger.info(location);

            logger.infof("Call the getForecast tool ...");
            toolExecutionRequest = ToolExecutionRequest.builder()
                .name("getForecast")
                .arguments(location)
                .build();
            logger.info(mcpClient.executeTool(toolExecutionRequest));

            logger.infof("Call the getAlerts tool ...");
            toolExecutionRequest = ToolExecutionRequest.builder()
                .name("getAlerts")
                .arguments("{\"state\":\"CA\"}")
                .build();
            logger.info(mcpClient.executeTool(toolExecutionRequest));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mcpClient.close();
        }


    }
}

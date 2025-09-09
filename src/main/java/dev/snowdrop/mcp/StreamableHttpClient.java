package dev.snowdrop.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import io.quarkus.qute.Qute;
import org.jboss.logging.Logger;
import org.wildfly.common.Assert;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Map;

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
            .toolExecutionTimeout(Duration.ofSeconds(1))
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
                  "latitude": {latitude},
                  "longitude": {longitude}
                }
                """;

            String location = Qute.fmt(tmplLocation, Map.of("latitude",df.format(latitude),"longitude", df.format(longitude)));
            logger.debugf("Coordinates: %s", location);

            logger.infof("\n=================================================\n Calling the getForecast tool ... \n=================================================");
            toolExecutionRequest = ToolExecutionRequest.builder()
                .name("getForecast")
                .arguments(location)
                .build();
            logger.info(mcpClient.executeTool(toolExecutionRequest));

            Thread.sleep(5000);

            logger.infof("\n=================================================\n Calling the getAlerts tool ... \n=================================================");
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

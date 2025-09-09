# MCP Quarkus Server and client

## Instructions

Build and launch the Quarkus MCP server exposing 2 Weather API services: GetAlerts and GetLocation

```shell
mvn clean install quarkus:dev
```

In a separate terminal, launch the client
```shell
mvn exec:java -Dexec.mainClass="dev.snowdrop.mcp.McpClient"
```
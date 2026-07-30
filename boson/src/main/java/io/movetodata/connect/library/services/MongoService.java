package io.movetodata.connect.library.services;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.sharedutils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * MongoService — handles MongoDB connectivity (non-JDBC).
 *
 * Field mapping from DatabaseSourceConfig:
 *   server   → host
 *   port     → port (default 27017)
 *   database → database name
 *   username → MongoDB username
 *   password → MongoDB password (base64-encoded by the frontend)
 *   schema   → authSource (default: admin)
 *   userRole → SSL enabled ("true" / "false")
 */
@Slf4j
@Service
public class MongoService {

    /**
     * Tests the MongoDB connection by attempting to list database names.
     * Throws an exception if the connection cannot be established.
     */
    public void testConnection(DatabaseSourceConfig config) throws Exception {
        String connectionString = buildConnectionString(config);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();
        try (MongoClient client = MongoClients.create(settings)) {
            // listDatabaseNames().first() forces an actual network round-trip to the server.
            client.listDatabaseNames().first();
        }
    }

    /**
     * Builds a MongoDB connection URI from DatabaseSourceConfig fields.
     * Format: mongodb://[user:password@]host:port/database?authSource=<authSource>[&tls=true]
     */
    String buildConnectionString(DatabaseSourceConfig config) {
        String host = config.getServer();
        int port = (config.getPort() != null) ? config.getPort() : 27017;
        String database = config.getDatabase();

        String authSource = (config.getSchema() != null && !config.getSchema().isBlank())
                ? config.getSchema()
                : "admin";
        boolean ssl = "true".equalsIgnoreCase(config.getUserRole());

        StringBuilder sb = new StringBuilder("mongodb://");

        String username = config.getUsername();
        String password = config.getPassword();
        if (username != null && !username.isBlank()) {
            if (password != null && !password.isBlank()) {
                if (Utils.isBase64(password)) {
                    password = Utils.decodeBase64(password);
                }
            }
            sb.append(encodeComponent(username))
              .append(":")
              .append(encodeComponent(password != null ? password : ""))
              .append("@");
        }

        sb.append(host).append(":").append(port).append("/").append(database);
        sb.append("?authSource=").append(authSource);
        if (ssl) {
            sb.append("&tls=true");
        }

        return sb.toString();
    }

    private String encodeComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

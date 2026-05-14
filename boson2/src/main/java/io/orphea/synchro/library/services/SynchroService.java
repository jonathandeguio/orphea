package io.orphea.synchro.library.services;

import io.orphea.bob.library.models.SocketMessage;
import io.orphea.passport.library.repository.UserRepository;
import io.orphea.passport.library.service.JwtService;
import io.orphea.sharedUtils.KubernetesUtils;
import io.orphea.sharedUtils.Utils;
import io.orphea.synchro.library.models.PostgresSyncProperties;
import io.orphea.synchro.library.models.PostgresSyncSpecification;
import io.orphea.synchro.library.repository.PostgresSyncRepository;
import io.orphea.zoro.library.services.ZoroService;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SynchroService {

    private final PostgresSyncRepository postgresSyncRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ZoroService zoroService;

    @Autowired
    SimpMessagingTemplate template;

    @Async
    public void performSync(UUID userId, UUID datasetId, String branch) throws Exception {

        if (postgresSyncRepository.existsByDatasetIdAndBranch(datasetId, branch)) {

            PostgresSyncSpecification postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(datasetId, branch);

            if (postgresSyncRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                if (Utils.isSparkInKubernetes()) {
                    HashMap<String, String > envVars = new HashMap<>();
                    envVars.put("DATASET_ID", String.valueOf(datasetId));
                    envVars.put("BRANCH", branch);
                    envVars.put("TABLE", postgresSyncSpecification.tableName);
                    envVars.put("BUILD_ID", String.valueOf(datasetId));

                    long currentTimeMillis = System.currentTimeMillis(); // Get current time in milliseconds
                    long millisInOneDay = 12 * 60 * 60 * 1000L; // Calculate milliseconds in 12 hours
                    Date expiresIn = new Date(currentTimeMillis + millisInOneDay); // Add one day to the current time

                    var user = userRepository.findById(userId)
                            .orElseThrow();
                    var access_token = jwtService.generateToken(user);

                    envVars.put("ACCESS_TOKEN", access_token);

                    new KubernetesUtils().sparkWithKubernetes(envVars, "io.orphea.Synchro", "synchro",1, "512M", 1, 0);
                } else {
                    Dataset<Row> sparkDF = zoroService.getSparkDF(datasetId, branch, -1);

                    Properties connectionProperties = new Properties();
                    connectionProperties.put("user", System.getenv("DB_USERNAME"));
                    connectionProperties.put("password", System.getenv("DB_PASSWORD"));

                    // Saving data to a JDBC source
                    sparkDF.write()
                            .mode("overwrite")
                            .jdbc("jdbc:postgresql://" + System.getenv("DB_HOST") + "/kepler",
                                    "public." + postgresSyncSpecification.getTableName(),
                                    connectionProperties);

                    postgresSyncSpecification.setSyncedBy(userId);

                    // Sending to socket
                    SocketMessage textMessage = new SocketMessage();
                    textMessage.setMessage("success");

                    template.convertAndSend("/topic/postgresSync/" + datasetId + "/" + branch, textMessage);

                    postgresSyncSpecification.setFinishedAt(new Date());
                    postgresSyncSpecification.setSyncStatus("completed");

                    postgresSyncRepository.save(postgresSyncSpecification);
                }
            }

        }
    }

    public void createSync(PostgresSyncProperties postgresSyncProperties, UUID userId) {

        PostgresSyncSpecification postgresSyncSpecification = new PostgresSyncSpecification();
        if (postgresSyncRepository.existsByDatasetIdAndBranch(postgresSyncProperties.getDatasetId(), postgresSyncProperties.getBranch())) {

            postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(postgresSyncProperties.getDatasetId(), postgresSyncProperties.getBranch());

            postgresSyncSpecification.setEnabled(postgresSyncProperties.isEnabled());
            postgresSyncSpecification.setTableName(postgresSyncProperties.getTableName());
            postgresSyncSpecification.setIndexNames(postgresSyncProperties.getIndexNames());
            postgresSyncSpecification.setUpdatedBy(userId);
            postgresSyncSpecification.setUpdatedAt(new Date());

            // TODO : delete old table, if it is an update
//            throw new AlreadyExistsException("Postgres sync already exists for the datasetId and branch.");
        } else {
            postgresSyncSpecification.setDatasetId(postgresSyncProperties.getDatasetId());
            postgresSyncSpecification.setBranch(postgresSyncProperties.getBranch());
            postgresSyncSpecification.setEnabled(postgresSyncProperties.isEnabled());
            postgresSyncSpecification.setTableName(postgresSyncProperties.getTableName());
            postgresSyncSpecification.setIndexNames(postgresSyncProperties.getIndexNames());
            postgresSyncSpecification.setCreatedBy(userId);
            postgresSyncSpecification.setCreatedAt(new Date());
        }

        postgresSyncRepository.saveAndFlush(postgresSyncSpecification);
    }
}

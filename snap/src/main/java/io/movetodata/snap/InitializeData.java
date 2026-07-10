package io.movetodata.snap;

import io.movetodata.snap.build.library.enums.BuildType;
import io.movetodata.snap.build.library.models.TriggerManagerModel;
import io.movetodata.snap.build.library.repository.TriggerRepository;
import io.movetodata.snap.passport.library.models.AuthProvider;
import io.movetodata.snap.passport.library.models.User;
import io.movetodata.snap.passport.library.models.UserPreferences;
import io.movetodata.snap.passport.library.repository.UserRepository;
import io.movetodata.snap.passport.library.service.UserService;
import io.movetodata.snap.platform.library.models.PlatformConfig;
import io.movetodata.snap.platform.library.models.SMTPConfigModel;
import io.movetodata.snap.platform.library.repository.PlatformConfigRepository;
import io.movetodata.snap.platform.library.repository.SMTPConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

@SpringBootApplication
@RequiredArgsConstructor
public class InitializeData {

    // Inject the UserService and UserRepository dependencies
    private final UserService userService;
    private final UserRepository userRepository;
    private final PlatformConfigRepository platformConfigRepository;
    private final SMTPConfigRepository smtpConfigRepository;
    private final TriggerRepository triggerRepository;

    // Define a CommandLineRunner bean method
    @Bean
    CommandLineRunner initializeDataRunner() {
        return args -> {
            // Your initialization logic goes here
            if (!userRepository.existsByUsername("platform-administrator")) {
                userService.saveUser(new User(null, "Platform Administrator", "platform-administrator", "snap2024", "Platform", "Administrator", "None",
                        "",
                        "platform-administrator@movetodata.io", AuthProvider.local, null, null, false, null, new UserPreferences(), null, null, null, null, null, null));
            }

            if (!userRepository.existsByUsername("platform-internal")) {
                User platformInternal = userService.saveUser(new User(null, "Platform Internal", "platform-internal", "b071181ada05d564a60d", "Platform", "Internal", "None",
                        "",
                        "platform-internal@movetodata.io", AuthProvider.local, null, null, false, null, new UserPreferences(), null, null, null, null, null, null));
            }

            User platformAdministrator = userRepository.findByUsername("platform-administrator").orElseThrow(() ->
                    new UsernameNotFoundException("User not found with username : platform-administrator")
            );

            if (!platformConfigRepository.existsByName("platformConfig")) {
                platformConfigRepository.save(new PlatformConfig(null, new Date(), new Date(), null, null, "Snap", "platformConfig", null, "https://pypi.python.org/simple/", "http://username:pssword@proxy.example.com", false));
            }

            if (!smtpConfigRepository.existsByConfig("platform")) {
                smtpConfigRepository.save(new SMTPConfigModel("platform", "movetodata.mailer@gmail.com", "lvrbzwlbkivpnpkn", "smtp.gmail.com", 587, "true", "true"));
            }
            Optional<User> platformInternal = userRepository.findByUsername("platform-internal");
            String repoUrl = "https://github.com/MoveToData-io/";
            if (!triggerRepository.existsByName("boson-main")) {
                TriggerManagerModel bosonMain = new TriggerManagerModel(null, "boson-main", "boson build for main branch with Dockerfile", "main", "boson", repoUrl + "boson.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(bosonMain);
            }
            if (!triggerRepository.existsByName("frontend-main")) {
                TriggerManagerModel frontendMain = new TriggerManagerModel(null, "frontend-main", "frontend build for main branch with Dockerfile", "main", "frontend", repoUrl + "frontend.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(frontendMain);
            }
            if (!triggerRepository.existsByName("parler-main")) {
                TriggerManagerModel parlerMain = new TriggerManagerModel(null, "parler-main", "parler build for main branch with Dockerfile", "main", "parler", repoUrl + "parler.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(parlerMain);
            }
            if (!triggerRepository.existsByName("callisto-main")) {
                TriggerManagerModel callistoMain = new TriggerManagerModel(null, "callisto-main", "callisto build for main branch with Dockerfile", "main", "callisto", repoUrl + "callisto.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(callistoMain);
            }
            if (!triggerRepository.existsByName("capture-main")) {
                TriggerManagerModel captureMain = new TriggerManagerModel(null, "capture-main", "capture build for main branch with Dockerfile", "main", "capture", repoUrl + "capture.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(captureMain);
            }
            if (!triggerRepository.existsByName("julia-main")) {
                TriggerManagerModel juliaMain = new TriggerManagerModel(null, "julia-main", "julia build for main branch with Dockerfile", "main", "julia", repoUrl + "julia.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(juliaMain);
            }
            if (!triggerRepository.existsByName("movetodata-docs-main")) {
                TriggerManagerModel movetodataDocsMain = new TriggerManagerModel(null, "movetodata-docs-main", "movetodata-docs build for main branch with Dockerfile", "main", "movetodata-docs", repoUrl + "movetodata-docs.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(movetodataDocsMain);
            }
            if (!triggerRepository.existsByName("spark-history-server-main")) {
                TriggerManagerModel sparkHistoryServerMain = new TriggerManagerModel(null, "spark-history-server-main", "spark-history-server build for main branch with Dockerfile", "main", "spark-history-server", repoUrl + "spark-history-server.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(sparkHistoryServerMain);
            }

            if (!triggerRepository.existsByName("boson-lite")) {
                TriggerManagerModel bosonLite = new TriggerManagerModel(null, "boson-lite", "boson build for main branch with Dockerfile.liteViz", "main", "boson", repoUrl + "boson.git", null, null, BuildType.MANUAL, "Dockerfile.liteViz", "lite-viz", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(bosonLite);
            }
            if (!triggerRepository.existsByName("frontend-lite")) {
                TriggerManagerModel frontendLite = new TriggerManagerModel(null, "frontend-lite", "frontend build for main branch with Dockerfile", "main", "frontend", repoUrl + "frontend.git", null, null, BuildType.MANUAL, "Dockerfile", "lite-viz", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(frontendLite);
            }
            if (!triggerRepository.existsByName("movetodata-docs-lite")) {
                TriggerManagerModel movetodataDocsLite = new TriggerManagerModel(null, "movetodata-docs-lite", "MoveToData-Docs build for main branch with Dockerfile", "main", "movetodata-docs", repoUrl + "movetodata-docs.git", null, null, BuildType.MANUAL, "Dockerfile", "lite-viz", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(movetodataDocsLite);
            }

            if (!triggerRepository.existsByName("boson-feature")) {
                TriggerManagerModel bosonFeature = new TriggerManagerModel(null, "boson-feature", "boson build for feature branch with Dockerfile", "feature", "boson", repoUrl + "boson.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(bosonFeature);
            }
            if (!triggerRepository.existsByName("frontend-feature")) {
                TriggerManagerModel frontendFeature = new TriggerManagerModel(null, "frontend-feature", "frontend build for feature branch with Dockerfile", "feature", "frontend", repoUrl + "frontend.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(frontendFeature);
            }
            if (!triggerRepository.existsByName("parler-feature")) {
                TriggerManagerModel parlerFeature = new TriggerManagerModel(null, "parler-feature", "parler build for feature branch with Dockerfile", "feature", "parler", repoUrl + "parler.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(parlerFeature);
            }
            if (!triggerRepository.existsByName("callisto-feature")) {
                TriggerManagerModel callistoFeature = new TriggerManagerModel(null, "callisto-feature", "callisto build for feature branch with Dockerfile", "feature", "callisto", repoUrl + "callisto.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(callistoFeature);
            }
            if (!triggerRepository.existsByName("capture-feature")) {
                TriggerManagerModel captureFeature = new TriggerManagerModel(null, "capture-feature", "capture build for feature branch with Dockerfile", "feature", "capture", repoUrl + "capture.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(captureFeature);
            }
            if (!triggerRepository.existsByName("julia-feature")) {
                TriggerManagerModel juliaFeature = new TriggerManagerModel(null, "julia-feature", "julia build for feature branch with Dockerfile", "feature", "julia", repoUrl + "julia.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(juliaFeature);
            }
            if (!triggerRepository.existsByName("movetodata-docs-feature")) {
                TriggerManagerModel movetodataDocsFeature = new TriggerManagerModel(null, "movetodata-docs-feature", "movetodata-docs build for feature branch with Dockerfile", "feature", "movetodata-docs", repoUrl + "movetodata-docs.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(movetodataDocsFeature);
            }
            if (!triggerRepository.existsByName("spark-history-server-feature")) {
                TriggerManagerModel sparkHistoryServerFeature = new TriggerManagerModel(null, "spark-history-server-feature", "spark-history-server build for feature branch with Dockerfile", "feature", "spark-history-server", repoUrl + "spark-history-server.git", null, null, BuildType.MANUAL, "Dockerfile", "movetodata-feature", null, null, null, null, new Date(), null, platformInternal.get().getId(), null);
                triggerRepository.save(sparkHistoryServerFeature);
            }
        };
    }
}




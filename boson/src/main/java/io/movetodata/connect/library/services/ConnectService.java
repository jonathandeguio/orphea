package io.movetodata.connect.library.services;

import io.movetodata.build.library.models.SocketMessage;
import io.movetodata.connect.library.models.*;
import io.movetodata.connect.library.repository.AgentRepository;
import io.movetodata.connect.library.repository.AgentStatsRepository;
import io.movetodata.connect.library.repository.ConnectConfigRepository;
import io.movetodata.connect.library.repository.SourcesRepository;
import io.movetodata.connect.library.responses.ConnectResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConnectService {
    private final SourcesRepository sourcesRepository;
    private final ConnectConfigRepository connectConfigRepository;
    private final AgentRepository agentRepository;
    private final AgentStatsRepository agentStatsRepository;
    private final LinkService linkService;
    private final SimpMessagingTemplate template;

    @Autowired
    public ConnectService(SourcesRepository sourcesRepository, ConnectConfigRepository connectConfigRepository, AgentRepository agentRepository, AgentStatsRepository agentStatsRepository, LinkService linkService, SimpMessagingTemplate template) {
        this.sourcesRepository = sourcesRepository;
        this.connectConfigRepository = connectConfigRepository;
        this.agentRepository = agentRepository;
        this.agentStatsRepository = agentStatsRepository;
        this.linkService = linkService;
        this.template = template;
    }

    public ConnectResponse getConnectResponse(UUID agentId) {
        // first check if it is valid Agent
        if (!agentRepository.existsById(agentId)) {
            throw new NoSuchElementException("agent with Id " + agentId + " does not exist");
        }

        List<Source> sourceList = sourcesRepository.findAllByAgentId(agentId);
        List<Link> linksList = new ArrayList<>();
        List<Link> buildNowList = new ArrayList<>();

        sourceList.parallelStream().forEach(source -> {
            List<Link> links = linkService.findBySourceId(source.getId());
            linksList.addAll(links);

            links.parallelStream().forEach(link -> {
                if (link.getBuild() != null) {
                    Date timeNow = new Date();
                    final long millis = timeNow.getTime() - link.getBuild().getTime();  // TODO : need to think of a better logic here... its not the best using time

                    // 45 seconds or less otherwise don't put in the buildNow
                    if (millis < 45000) {
                        buildNowList.add(link);
                    }
                }
            });
        });

        ConnectResponse connectResponse = ConnectResponse.builder().sources(sourceList).links(linksList).buildNow(buildNowList).build();
        return connectResponse;
    }

    @NotNull
    public ConnectConfig getConnectConfig(UUID agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new NoSuchElementException("agent with Id " + agentId + " does not exist");
        }

        // Update lastStatus
        Agents agents = agentRepository.getById(agentId);
        agents.setLastStatus(new Date());
        agentRepository.save(agents);

        ConnectConfig connectConfig = connectConfigRepository.findFirstByAgentIdOrderByUpdatedAtDesc(agentId);

        // if not config found then create an initial one.
        if (connectConfig == null) {
            connectConfig = new ConnectConfig();
            connectConfig.setAgentId(agentId);
            connectConfig.setVersion(UUID.randomUUID());
        }
        return connectConfig;
    }

    @NotNull
    public AgentStats getAgentStats(UUID agentId, AgentStats agentStats) {
        if (!agentRepository.existsById(agentId)) {
            throw new NoSuchElementException("agent with Id " + agentId + " does not exist");
        }

        agentStats.setCreatedAt(new Date());

        AgentStats agentStats1 = agentStatsRepository.save(agentStats);

        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("agentStats");
        template.convertAndSend("/topic/connect/" + agentId, textMessage);
        return agentStats1;
    }
}

package io.movetodata.connect.library.services;

import io.movetodata.connect.library.models.AgentOneTimeCode;
import io.movetodata.connect.library.repository.AgentOneTimeCodeRepository;
import io.movetodata.sharedutils.RandomString;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AgentOneTimeCodeService {
    private final AgentOneTimeCodeRepository agentOneTimeCodeRepository;

    public AgentOneTimeCodeService(AgentOneTimeCodeRepository agentOneTimeCodeRepository) {
        this.agentOneTimeCodeRepository = agentOneTimeCodeRepository;
    }

    public AgentOneTimeCode newAgentOneTimeCode(UUID agentId, UUID userId) {
        AgentOneTimeCode agentOneTimeCode = new AgentOneTimeCode();

        RandomString randomString = new RandomString(24, ThreadLocalRandom.current());

        agentOneTimeCode.setCode(randomString.nextString());
        agentOneTimeCode.setAgentId(agentId);
        agentOneTimeCode.setExpiryTime(new Date());
        agentOneTimeCode.setDownloadUsed(false);
        agentOneTimeCode.setInstallUsed(false);
        agentOneTimeCode.setCreatedBy(userId);

        return agentOneTimeCodeRepository.save(agentOneTimeCode);
    }
    public AgentOneTimeCode findByCode(String code) {
        return agentOneTimeCodeRepository.findByCode(code).orElseThrow();
    }
    public boolean existsByCode(String code) {
        return agentOneTimeCodeRepository.existsByCode(code);
    }
    public void setDownloadUsed(AgentOneTimeCode agentOneTimeCode) {
        agentOneTimeCode.setDownloadUsed(true);
        agentOneTimeCodeRepository.save(agentOneTimeCode);
    }
}

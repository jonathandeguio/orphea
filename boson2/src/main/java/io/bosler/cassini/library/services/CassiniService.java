package io.bosler.cassini.library.services;

import io.bosler.bob.library.models.SocketMessage;
import io.bosler.kitab.library.repository.BranchRepository;
import io.bosler.synchro.library.models.PostgresSyncSpecification;
import io.bosler.synchro.library.repository.PostgresSyncRepository;
import io.bosler.zoro.library.services.ZoroService;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CassiniService {

}

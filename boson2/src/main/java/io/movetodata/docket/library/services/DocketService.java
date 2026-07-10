package io.movetodata.docket.library.services;

import io.movetodata.bob.library.models.SocketMessage;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.synchro.library.models.PostgresSyncSpecification;
import io.movetodata.synchro.library.repository.PostgresSyncRepository;
import io.movetodata.zoro.library.services.ZoroService;
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
public class DocketService {

}

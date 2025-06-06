package io.bosler.kitab.library.services;

import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KitabService {

    private final FolderRepository folderRepository;

    public List<FolderModel> getPathById(UUID id, List<FolderModel> path) {

        FolderModel pathId = folderRepository.getById(id);
        path.add(pathId);

        if (!Objects.equals(pathId.getParent(), new UUID(0, 0))) {
            getPathById(pathId.getParent(), path);
        }
        return path;
    }

}

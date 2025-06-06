package io.bosler.comments.library.repository;

import io.bosler.comments.library.models.CommentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentModel, UUID> {

    List<CommentModel> getByResourceIdAndStatusAndParentOrderByCreatedAtDesc(UUID resourceId,String status,UUID parent);

    CommentModel getById(UUID id);


}

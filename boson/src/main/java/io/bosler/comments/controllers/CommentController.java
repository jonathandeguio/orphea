package io.bosler.comments.controllers;

import io.bosler.build.library.models.SocketMessage;
import io.bosler.comments.library.models.CommentModel;
import io.bosler.comments.library.models.CommentRequestModel;
import io.bosler.comments.library.repository.CommentRepository;
import io.bosler.comments.library.services.CommentService;
import io.bosler.passport.library.models.User;
import io.bosler.passport.library.repository.UserRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.scheduler.library.services.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.Date;
import java.security.Principal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/comments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class CommentController {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final CommentService commentService;


    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Get All Comments with the id and type")
    @GetMapping("/{resourceId}/{status}")
    public ResponseEntity<Object> getAllComments(Principal principal, @PathVariable("resourceId") UUID resourceId, @PathVariable String status) {
        if (status.equals("open") || status.equals("resolved")) {
            List<CommentModel> allComments = commentRepository.getByResourceIdAndStatusAndParentOrderByCreatedAtDesc(resourceId, status, new UUID(0, 0));
            return new ResponseEntity<>(allComments, HttpStatus.OK);
        } else
            return new ResponseEntity<>("Incorrect Type of Comments", HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Add a comment or reply to a comment")
    @PostMapping("/newComment")
    public ResponseEntity<Object> createComment(Principal principal, @RequestBody CommentRequestModel comment) throws MessagingException {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (comment.getParent() != null && !commentRepository.existsById(comment.getParent()))
            return new ResponseEntity<>("No Such Comment present to reply", HttpStatus.BAD_REQUEST);

        CommentModel newComment = new CommentModel();
        newComment.setMessage(comment.getMessage());
        newComment.setResourceId(comment.getResourceId());
        newComment.setStatus("open");
        newComment.setCreatedAt(new Date());
        newComment.setCreatedBy(userId);

        if (comment.getParent() != null) {
            newComment.setParent(comment.getParent());
            CommentModel parentComment = commentRepository.getReferenceById(comment.getParent());
            newComment.setStatus("reply");
            parentComment.getReplies().add(newComment);
        } else {
            newComment.setParent(new UUID(0, 0));
        }

        commentRepository.save(newComment);
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage("commentsUpdated");
        template.convertAndSend("/topic/comment/" + comment.getResourceId(), socketMessage);

        Thread backgroundThread = new Thread(() -> {
            // Function to be run in the background
            try {
                commentService.checkMessageAndDoNotificationStuff(newComment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        backgroundThread.start();

        return new ResponseEntity<>("Comment Added successfully", HttpStatus.OK);

    }

    @Operation(summary = "Delete the comment with reference of id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteComment(Principal principal, @PathVariable("id") UUID id) {

        if (!commentRepository.existsById(id))
            return new ResponseEntity<>("No Comment with the id exist", HttpStatus.FORBIDDEN);
        CommentModel comment = commentRepository.getReferenceById(id);
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!userId.equals(comment.getCreatedBy()) && !authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied to Delete the comment", HttpStatus.FORBIDDEN);
        if (!comment.getParent().equals(new UUID(0, 0))) {
            CommentModel parentComment = commentRepository.getReferenceById(comment.getParent());
            if (parentComment.getReplies().contains(comment))
                parentComment.getReplies().remove(comment);
        } else {
            List<CommentModel> replies = comment.getReplies();

            //trying to delete the replies of the comment which was deleted
            for (CommentModel reply : replies) {

                commentRepository.delete(reply);
            }
            //comment.getReplies().clear();

        }
        commentRepository.delete(comment);

        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage("commentsUpdated");
        template.convertAndSend("/topic/comment/" + comment.getResourceId(), socketMessage);
        return new ResponseEntity<>("Comment Deleted Successfully", HttpStatus.OK);

    }

    @Operation(summary = "Edit the comment with reference of id")
    @PutMapping("/editComment/{id}")
    public ResponseEntity<Object> editComment(Principal principal, @PathVariable("id") UUID id, @RequestBody CommentRequestModel cmt) {

        if (!commentRepository.existsById(id))
            return new ResponseEntity<>("No Comment with the id exist", HttpStatus.FORBIDDEN);
        CommentModel comment = commentRepository.getReferenceById(id);
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!userId.equals(comment.getCreatedBy()) && !authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied to Edit the comment", HttpStatus.FORBIDDEN);

        if (cmt.getStatus() != null)
            comment.setStatus(cmt.getStatus());
        else {
            comment.setMessage(cmt.getMessage());
            comment.setUpdatedBy(userId);
            comment.setUpdatedAt(new Date());
        }

        commentRepository.save(comment);

        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage("commentsUpdated");
        template.convertAndSend("/topic/comment/" + comment.getResourceId(), socketMessage);

        return new ResponseEntity<>("Comment Edited Successfully", HttpStatus.OK);
    }
}

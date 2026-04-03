package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.publicview.PublicCommentCreateRequest;
import com.example.onlinetalaba.dto.publicview.PublicCommentResponse;
import com.example.onlinetalaba.dto.publicview.PublicCommentUpdateRequest;
import com.example.onlinetalaba.entity.PublicCommentMessage;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.PublicCommentMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicCommentService {

    private final PublicCommentMessageRepository publicCommentMessageRepository;

    @Transactional
    public PublicCommentResponse create(PublicCommentCreateRequest request, User currentUser) {
        String content = request == null ? null : request.getContent();
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Comment content is required");
        }

        PublicCommentMessage parent = null;
        Long parentCommentId = request == null ? null : request.getParentCommentId();
        Boolean questionParam = request == null ? null : request.getQuestion();
        boolean question = questionParam == null || questionParam;

        if (parentCommentId != null) {
            parent = publicCommentMessageRepository.findByIdAndDeletedFalse(parentCommentId)
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
            question = false;
        }

        PublicCommentMessage message = PublicCommentMessage.builder()
                .sender(currentUser)
                .parent(parent)
                .content(content.trim())
                .question(question)
                .resolved(false)
                .edited(false)
                .deleted(false)
                .build();

        publicCommentMessageRepository.save(message);

        if (parent != null && parent.isQuestion() && !parent.isResolved()) {
            parent.setResolved(true);
            publicCommentMessageRepository.save(parent);
        }

        return mapToResponse(message, currentUser);
    }

    @Transactional(readOnly = true)
    public List<PublicCommentResponse> getThreads(User currentUser) {
        List<PublicCommentMessage> messages = publicCommentMessageRepository.findAllByDeletedFalseOrderByIdAsc();
        Map<Long, PublicCommentResponse> mapped = new LinkedHashMap<>();
        List<PublicCommentResponse> roots = new ArrayList<>();

        for (PublicCommentMessage message : messages) {
            mapped.put(message.getId(), mapToResponse(message, currentUser));
        }

        for (PublicCommentMessage message : messages) {
            PublicCommentResponse response = mapped.get(message.getId());
            PublicCommentMessage parent = message.getParent();

            if (parent != null && mapped.containsKey(parent.getId())) {
                mapped.get(parent.getId()).getReplies().add(response);
            } else {
                roots.add(response);
            }
        }

        return roots;
    }

    @Transactional
    public PublicCommentResponse update(Long commentId, PublicCommentUpdateRequest request, User currentUser) {
        PublicCommentMessage message = publicCommentMessageRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only comment owner can edit");
        }

        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Comment content is required");
        }

        message.setContent(request.getContent().trim());
        message.setEdited(true);
        publicCommentMessageRepository.save(message);

        return mapToResponse(message, currentUser);
    }

    @Transactional
    public void delete(Long commentId, User currentUser) {
        PublicCommentMessage message = publicCommentMessageRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!canModerate(message, currentUser)) {
            throw new ForbiddenException("You do not have permission to delete this comment");
        }

        message.setDeleted(true);
        publicCommentMessageRepository.save(message);
    }

    @Transactional
    public PublicCommentResponse markResolved(Long commentId, boolean resolved, User currentUser) {
        PublicCommentMessage message = publicCommentMessageRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!message.isQuestion()) {
            throw new ForbiddenException("Only question comments can be resolved");
        }

        boolean isOwner = message.getSender().getId().equals(currentUser.getId());
        boolean isAdmin = isSuperScope(currentUser);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Only question owner or admin can resolve");
        }

        message.setResolved(resolved);
        publicCommentMessageRepository.save(message);

        return mapToResponse(message, currentUser);
    }

    private PublicCommentResponse mapToResponse(PublicCommentMessage message, User currentUser) {
        return PublicCommentResponse.builder()
                .id(message.getId())
                .parentCommentId(message.getParent() == null ? null : message.getParent().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderRole(message.getSender().getRoles().getAppRoleName())
                .content(message.getContent())
                .question(message.isQuestion())
                .resolved(message.isResolved())
                .edited(message.isEdited())
                .replyCount(publicCommentMessageRepository.countByParentIdAndDeletedFalse(message.getId()))
                .canEdit(message.getSender().getId().equals(currentUser.getId()))
                .canDelete(canModerate(message, currentUser))
                .createdAt(message.getDatetimeCreated())
                .updatedAt(message.getDatetimeUpdated())
                .replies(new ArrayList<>())
                .build();
    }

    private boolean canModerate(PublicCommentMessage message, User currentUser) {
        return message.getSender().getId().equals(currentUser.getId()) || isSuperScope(currentUser);
    }

    private boolean isSuperScope(User currentUser) {
        AppRoleName role = currentUser.getRoles().getAppRoleName();
        return role == AppRoleName.SUPER_ADMIN || role == AppRoleName.ADMIN;
    }
}

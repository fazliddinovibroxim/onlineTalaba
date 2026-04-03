package com.example.onlinetalaba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "public_comment_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCommentMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PublicCommentMessage parent;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private boolean question;

    @Column(nullable = false)
    private boolean resolved;

    @Column(nullable = false)
    private boolean edited;

    @Column(nullable = false)
    private boolean deleted;
}

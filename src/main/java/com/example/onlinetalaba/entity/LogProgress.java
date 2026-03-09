package com.example.onlinetalaba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity(name = "log_progress")
public class LogProgress extends BaseEntity {
    private String action;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String exception;
}

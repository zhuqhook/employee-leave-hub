package com.draxlmaier.leavehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AttachmentDto {
    private Long attachmentId;
    private String fileName;
    private LocalDateTime uploadedAt;
}

package com.cuea.rmp.request.application.dto;

import java.util.UUID;

public record ApproveRequestCommand(UUID requestId, UUID approverId) {}

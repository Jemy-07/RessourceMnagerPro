package com.cuea.rmp.request.application.dto;

import java.util.UUID;

public record RejectRequestCommand(UUID requestId, UUID approverId, String comments) {}

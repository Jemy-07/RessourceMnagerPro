package com.cuea.rmp.shared.application;

import java.util.List;

/**
 * Framework-agnostic pagination wrapper, kept free of Spring Data types so the
 * application layer has no persistence/web dependencies.
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(List.copyOf(content), page, size, totalElements, totalPages);
    }
}

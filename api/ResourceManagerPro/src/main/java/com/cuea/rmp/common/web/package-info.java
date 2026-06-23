/**
 * Shared WEB layer — cross-cutting REST concerns.
 * <p>
 * Holds {@code ApiResponse<T>} envelope and the single GlobalExceptionHandler.
 * Never exposes domain or JPA objects over HTTP.
 */
package com.cuea.rmp.common.web;

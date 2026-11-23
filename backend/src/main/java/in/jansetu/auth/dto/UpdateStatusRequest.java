package in.jansetu.auth.dto;

import jakarta.validation.constraints.NotNull;

/** DISABLE also revokes every refresh token of the user (lost / stolen phone). */
public record UpdateStatusRequest(@NotNull Action action) {
    public enum Action { ACTIVATE, DISABLE, UNLOCK }
}

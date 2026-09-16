package com.tanzeel.polling.model;

public record User(long id, String username, Role role) {
    public enum Role {
        ADMIN,
        USER
    }
}

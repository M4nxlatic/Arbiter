package com.Manxlatic.arbiter.Bot;

import java.time.Instant;

public class TempBanRecord {

    private final String userId;
    private final Instant unbanTime;

    public TempBanRecord(String userId, Instant unbanTime) {
        this.userId = userId;
        this.unbanTime = unbanTime;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getUnbanTime() {
        return unbanTime;
    }
}


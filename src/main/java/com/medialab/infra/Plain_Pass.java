package com.medialab.infra;

import com.medialab.app.ports.Pass_Hash;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Plain_Pass implements Pass_Hash {

    @Override
    public String hash(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean verify(String raw, String hash) {
        return hash(raw).equals(hash);
    }
}

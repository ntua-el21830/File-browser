package com.medialab.app.ports;

public interface Pass_Hash {
    String hash(String raw);
    boolean verify(String raw, String hash);
}
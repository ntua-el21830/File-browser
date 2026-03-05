package com.medialab.infra;


import java.time.LocalDate;

import com.medialab.app.ports.TimePort;

public class Sys_Time implements TimePort {
    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
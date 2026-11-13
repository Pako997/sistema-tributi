package it.pa.tributi.tributiservice.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneratoreIuv {

    private final SecureRandom random = new SecureRandom();

    public String genera() {
        return System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
    }
}

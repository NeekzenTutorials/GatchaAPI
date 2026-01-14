package com.imt.api.AuthAPI.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class Passwords {
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  private Passwords() {}

  public static String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  public static boolean matches(String rawPassword, String hash) {
    return encoder.matches(rawPassword, hash);
  }
}

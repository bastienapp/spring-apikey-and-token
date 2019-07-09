package com.example.authentication;

import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

public class Util {

    static public String hash(String str) {
        return Hashing.sha256().hashString(str, Charset.defaultCharset()).toString();
    }
}

package edu.hm.cs.ig.passbutler.security;

/**
 * Created by dennis on 27.12.17.
 */

public class MissingKeyException extends Exception {

    public MissingKeyException(String message) {
        super(message);
    }
}

package com.sofka.insurancequoter.core.zipcode.domain.exception;

/**
 * Domain exception thrown when a requested zip code does not exist in the catalog.
 */
public class ZipCodeNotFoundException extends RuntimeException {

    public ZipCodeNotFoundException(String zipCode) {
        super("Zip code not found: " + zipCode);
    }
}

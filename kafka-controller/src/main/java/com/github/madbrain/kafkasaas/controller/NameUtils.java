package com.github.madbrain.kafkasaas.controller;

import scala.util.Random;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NameUtils {
    private static final int MAX_NAME_LENGTH = 63;
    private static final int RANDOM_LENGTH = 5;
    private static final int MAX_GENERATED_NAME_LENGTH = MAX_NAME_LENGTH - RANDOM_LENGTH;

    private static final String ALPHA_NUMS = "bcdfghjklmnpqrstvwxz2456789";

    public static String generateName(String base) {
        if (base.length() > MAX_GENERATED_NAME_LENGTH) {
            base = base.substring(0, MAX_GENERATED_NAME_LENGTH);
        }
        return base + randomString(RANDOM_LENGTH);
    }

    private static String randomString(int randomLength) {
        Random rand = new Random();
        return IntStream.range(0, randomLength)
                .map(x -> ALPHA_NUMS.charAt(rand.nextInt(ALPHA_NUMS.length())))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}

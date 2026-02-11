package com.example.RestaurantApplication.config.tracing;

public class LogHelper {

    private static final StackWalker WALKER = StackWalker.getInstance();

    public static String loc() {
        return WALKER.walk(s -> s
            .skip(1)
            .findFirst()
            .map(f -> f.getFileName() + ":" + f.getLineNumber())
            .orElse("unknown"));
    }
}

package it.chrccl.umb.model;

import lombok.Getter;

@Getter
public enum Issue {
    STRETCHMARKS("SMAGLIATURE"),
    LONGEVITY("LONGEVITY"),
    MICROLIPOSUCTION("MICROLIPOSUZIONE");

    public static final String STRETCHMARKS_SERVICE = "STRETCHMARKS";
    public static final String LONGEVITY_SERVICE = "LONGEVITY";
    public static final String MICROLIPOSUCTION_SERVICE = "MICROLIPOSUCTION";
    public static final String ROUTER_SERVICE = "ROUTER";

    private final String name;

    Issue(String name) {
        this.name = name;
    }
}
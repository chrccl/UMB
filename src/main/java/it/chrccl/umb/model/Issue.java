package it.chrccl.umb.model;

import lombok.Getter;

@Getter
public enum Issue {
    STRETCHMARKS("SMAGLIATURE");
    public static final String STRETCHMARKS_SERVICE = "STRETCHMARKS";


    private final String name;

    Issue(String name) {
        this.name = name;
    }
}

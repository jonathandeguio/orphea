package io.orphea.news.library.enums;

import lombok.Getter;

@Getter
public enum NewsPriority {
    HIGHEST(1),
    HIGH(2),
    MEDIUM(3),
    LOW(4),
    LOWEST(5);

    private final int value;

    NewsPriority(int value) {
        this.value = value;
    }

}

package io.movetodata.kepler.library.DTOs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabCustomizeDTO {
    private String chartHeadingTextColor;
    private String chartHeadingBg;
    private String chartBodyBg;
    private String canvasBg;
    private String pageBg;

    private int topPadding;
    private int rightPadding;
    private int bottomPadding;
    private int leftPadding;

    private boolean preventCollision;
    private boolean allowOverlap;
}

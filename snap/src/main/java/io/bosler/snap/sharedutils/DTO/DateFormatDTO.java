package io.bosler.snap.sharedutils.DTO;

import lombok.*;

import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DateFormatDTO {
    private String format;
    private Locale locale;

    @Override
    public String toString() {
        return "StringFormatDTO {" +
                "format='" + format + '\'' +
                ", locale=" + locale +
                '}';
    }
}

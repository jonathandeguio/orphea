package io.movetodata.snap.sharedutils.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
    public List<T> content;
    public Number totalElements;
}

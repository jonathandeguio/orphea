package io.bosler.snap.platform.library.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Versions {
    String snapDocs = System.getenv("VERSION-snapDocs");
    String snap = System.getenv("VERSION-snap");
    String frontend = System.getenv("VERSION-frontend");
}

package io.bosler.platform.library.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class Versions {
    private String platform = System.getenv("VERSION-platform");
    private String boslerDocs = System.getenv("VERSION-boslerDocs");
    private String boson = System.getenv("VERSION-boson");
    private String callisto = System.getenv("VERSION-callisto");
    private String frontend = System.getenv("VERSION-frontend");
    private String connect = System.getenv("VERSION-connect");
    private String julia = System.getenv("VERSION-julia");
    private String parler = System.getenv("VERSION-parler");
    private String sparkHistoryServer = System.getenv("VERSION-sparkHistoryServer");

    public Map<String, String> getNonNullVersions() {
        Map<String, String> nonNullVersions = new HashMap<>();

        if (platform != null) nonNullVersions.put("platform", platform);
        if (boslerDocs != null) nonNullVersions.put("boslerDocs", boslerDocs);
        if (boson != null) nonNullVersions.put("boson", boson);
        if (callisto != null) nonNullVersions.put("callisto", callisto);
        if (frontend != null) nonNullVersions.put("frontend", frontend);
        if (connect != null) nonNullVersions.put("connect", connect);
        if (julia != null) nonNullVersions.put("julia", julia);
        if (parler != null) nonNullVersions.put("parler", parler);
        if (sparkHistoryServer != null) nonNullVersions.put("sparkHistoryServer", sparkHistoryServer);

        return nonNullVersions;
    }
}

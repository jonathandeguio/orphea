package io.movetodata.build.BobEnums;

public enum BuildLaunchedBy {
    /*
    Manual 	- Dataset, Repository, Ignite
    Cron 	- Build Schedule, Ignite Schedule
    Source 	- Build Schedule
     */
    MANUAL,
    CRON,
    SOURCE,

    TRANSFORM,
    UNKNOWN,
    UPLOAD
}

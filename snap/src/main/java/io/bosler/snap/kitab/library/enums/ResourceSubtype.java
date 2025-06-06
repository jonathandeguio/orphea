package io.bosler.snap.kitab.library.enums;

public enum ResourceSubtype {
    FILE("FILE"),
    FOLDER("FOLDER"),
    DASHBOARD("DASHBOARD"),

    JDBC("JDBC"),

    // Connect
    LIVE("LIVE"),
    STORE("STORE"),

    // CHART TYPES
    PIECHART("Pie Chart"),

    BARCHART("Bar Chart"),

    LINECHART("Line Chart"),

    SCATTERCHART("Scatter Chart"),
    LINEAREACHART("Line Area Chart"),

    HORIZONTALBARCHART("Horizontal Bar Chart"),
    BIGNUMBER("Big Number"),
    MAPCHART("Map Chart"),
    GAUGECHART("Gauge Chart"),
    RADARCHART("Radar Chart"),
    SUNBURSTCHART("Sunburst Chart"),
    TABLE("Table"),
    VERTICALAXISCHART("Vertical Axis Chart"),
    HORIZONTALCHART("Horizontal Chart"),
    PYTHON("python"),

    NONE("None"),

    // Files
    TXT("Text"),
    RAW("Raw dataset file"),
    PARQUET("Parquet dataset file"),
    CSV("Comma-Separated Values"),
    MD("Markdown"),
    PY("python"),
    IPYNB("Interactive Python Notebook"),
    GITIGNORE("GITIGNORE"),
    SQL("SQL"),
    XLS("Microsoft Excel Spreadsheet"),
    XLSX("Microsoft Excel Open XML Spreadsheet"),
    DOC("Microsoft Word Document"),
    DOCX("Microsoft Word Open XML Document"),
    PPT("Microsoft Word Document"),
    PPTX("Microsoft Word Open XML Document"),
    PDF("Portable Document Format"),
    JSON("JavaScript Object Notation"),
    XML("eXtensible Markup Language"),
    PNG("Portable Network Graphics"),
    JPEG("Joint Photographic Experts Group"),
    GIF("Graphics Interchange Format"),
    MP3("MPEG Audio Layer III"),
    MP4("MPEG-4 Part 14"),
    AVI("Audio Video Interleave"),
    ZIP("Zipped File"),
    RAR("WinRAR Compressed Archive"),
    TAR("Tape Archive"),
    GZ("Gzip Compressed Archive"),
    EXE("Executable File"),
    DLL("Dynamic Link Library"),
    HTML("Hypertext Markup Language"),
    CSS("Cascading Style Sheets"),
    JS("JavaScript"),
    JAVA("Java Source Code"),
    BAT("Batch File"),
    JPG("Joint Photographic Group"),
    BMP("Bitmap Image"),
    TIFF("Tagged Image File Format"),
    ODT("Open Document Text"),
    ODS("Open Document Spreadsheet"),
    ODP("Open Document Presentation"),
    WAV("Waveform Audio File Format"),
    FLV("Flash Video"),
    MPG("Moving Picture Experts Group"),
    SVG("Scalable Vector Graphics"),
    INI("Initialization File"),
    LOG("Log File"),
    RTF("Rich Text Format"),
    HTM("Hypertext Markup Language"),
    ICS("iCalendar File"),
    M4A("MPEG-4 Audio Layer"),
    ODF("Open Document Format"),
    WMV("Windows Media Video"),
    FLAC("Free Lossless Audio Codec"),
    MKV("Matroska Video"),
    PGP("Pretty Good Privacy"),
    TEX("LaTeX Source Document"),
    WMA("Windows Media Audio"),
    EPS("Encapsulated PostScript"),
    ODG("Open Document Graphics"),
    OGG("Ogg Vorbis Compressed Audio"),
    ICO("Icon File"),
    SWF("Shockwave Flash Movie");


    private final String displayName;

    ResourceSubtype(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}

CREATE TABLE folder_link_config
(
    id         UUID NOT NULL,
    sub_folder VARCHAR(255),
    CONSTRAINT pk_folder_link_config PRIMARY KEY (id)
);

CREATE TABLE sharepoint_link_config
(
    id         UUID NOT NULL,
    file_id    VARCHAR(255),
    sheet_name VARCHAR(255),
    file_type  VARCHAR(255),
    CONSTRAINT pk_sharepoint_link_config PRIMARY KEY (id)
);

ALTER TABLE connect_links
    ADD link_config_id UUID;
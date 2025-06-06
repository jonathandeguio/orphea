CREATE TABLE platform_config_datamart (
                                          config VARCHAR(255) PRIMARY KEY,
                                          host VARCHAR(255) ,
                                          port INTEGER,
                                          username VARCHAR(255) ,
                                          password VARCHAR(255) ,
                                          database_driver VARCHAR(255) ,
                                          ignore_datamart_users BOOLEAN ,
                                          limit_rows INTEGER ,
                                          limit_size INTEGER ,
                                          database VARCHAR(255) ,
                                          data_source_config_Id VARCHAR(255)
);
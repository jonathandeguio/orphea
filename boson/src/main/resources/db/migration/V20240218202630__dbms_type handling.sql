UPDATE database_source_config
SET dbms_type = 'POSTGRES'
WHERE dbms_type = 'postgres';

UPDATE database_source_config
SET dbms_type = 'MARIADB'
WHERE dbms_type = 'mariadb';

UPDATE database_source_config
SET dbms_type = 'JDBC'
WHERE dbms_type = 'jdbc';

UPDATE database_source_config
SET dbms_type = 'MSSQLSERVER'
WHERE dbms_type = 'mssql-server';
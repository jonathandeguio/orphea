ALTER TABLE logging_model
    DROP CONSTRAINT fk_logging_model_on_tag;

DROP TABLE logging_model CASCADE;

DROP TABLE logging_tags CASCADE;
CREATE TABLE comment_model_mentions
(
    comment_model_id UUID NOT NULL,
    mentions         UUID
);

ALTER TABLE comment_model_mentions
    ADD CONSTRAINT fk_commentmodel_mentions_on_comment_model FOREIGN KEY (comment_model_id) REFERENCES kitab_comments (id);
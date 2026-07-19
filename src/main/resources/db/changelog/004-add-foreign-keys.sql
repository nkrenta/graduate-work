--liquibase formatted sql

--changeset graduate-work:4
ALTER TABLE ads ADD CONSTRAINT fk_ads_author FOREIGN KEY (author_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT fk_comments_ad FOREIGN KEY (ad_id) REFERENCES ads(id);
ALTER TABLE comments ADD CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id);

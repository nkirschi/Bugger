/*
 * Sets up the database for use by Bugger.
 *
 * This SQL script is intended to be run on a clean database. If any of the
 * artifacts defined below already exist, the setup will be aborted, resulting
 * in an incomplete installation. To erase all of the artifacts defined below
 * that might exist in the database, run the attached SQL script 'erase.sql'.
 */

CREATE TABLE version (
    version VARCHAR NOT NULL PRIMARY KEY DEFAULT '1.0',
    CONSTRAINT version_only_one_row CHECK (version = '1.0')
);

INSERT INTO version DEFAULT VALUES;


CREATE TABLE system_settings (
    id INTEGER NOT NULL PRIMARY KEY DEFAULT 0,

    organization_name VARCHAR NOT NULL DEFAULT 'Bugger',
    organization_logo BYTEA DEFAULT '',
    organization_theme VARCHAR NOT NULL DEFAULT 'light.css',
    privacy_policy VARCHAR NOT NULL DEFAULT '',
    imprint VARCHAR NOT NULL DEFAULT '',

    guest_reading_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    closed_report_posting BOOLEAN NOT NULL DEFAULT FALSE,
    user_email_regex VARCHAR NOT NULL DEFAULT '.*',
    allowed_file_extensions VARCHAR NOT NULL DEFAULT '',
    max_attachments_per_post INTEGER NOT NULL DEFAULT 5,
    voting_weight_definition VARCHAR NOT NULL DEFAULT '0,10,25,50,100,200,400,600,800,1000',

    CONSTRAINT system_settings_only_one_row CHECK (id = 0)
);

INSERT INTO system_settings DEFAULT VALUES;


CREATE TYPE profile_visibility_degree AS ENUM (
    'FULL',
    'MINIMAL'
);

CREATE TABLE "user" (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,

    username VARCHAR UNIQUE NOT NULL,
    password_hash VARCHAR NOT NULL,
    password_salt VARCHAR NOT NULL,
    hashing_algorithm VARCHAR NOT NULL,

    email_address VARCHAR NOT NULL,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    avatar BYTEA,
    avatar_thumbnail BYTEA,
    biography VARCHAR,
    preferred_language VARCHAR,
    profile_visibility profile_visibility_degree,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_admin BOOLEAN,
    forced_voting_weight INTEGER,

    CONSTRAINT user_first_name_non_empty CHECK (length(first_name) >= 1),
    CONSTRAINT user_last_name_non_empty CHECK (length(last_name) >= 1),
    CONSTRAINT user_email_address_valid CHECK (email_address LIKE '_%@_%._%')
);

CREATE UNIQUE INDEX user_email_unique_up_to_case_index ON "user" (
    LOWER(email_address)
);


CREATE TYPE token_type AS ENUM (
    'CHANGE_EMAIL',
    'FORGOT_PASSWORD',
    'REGISTER'
);

CREATE TABLE token (
    value VARCHAR PRIMARY KEY,
    type token_type NOT NULL,
    "timestamp" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    verifies INTEGER REFERENCES "user" (id) ON DELETE CASCADE
);


CREATE TABLE topic (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title VARCHAR UNIQUE NOT NULL,
    description VARCHAR,
    CONSTRAINT topic_title_non_empty CHECK (length(title) >= 1)
);


CREATE TYPE report_type AS ENUM (
    'BUG',
    'FEATURE',
    'HINT'
);

CREATE TYPE report_severity AS ENUM (
    'MINOR',
    'RELEVANT',
    'SEVERE'
);

CREATE TABLE report (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 10000),

    title VARCHAR NOT NULL,
    type report_type NOT NULL,
    severity report_severity NOT NULL,
    version VARCHAR,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    forced_relevance INTEGER,
    duplicate_of INTEGER REFERENCES report (id) ON DELETE SET NULL,

    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,

    CONSTRAINT report_title_non_empty CHECK (length(title) >= 1),
    CONSTRAINT report_no_self_duplicate CHECK (id <> duplicate_of)
);


CREATE TABLE post (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 10000),
    report INTEGER NOT NULL REFERENCES report (id) ON DELETE CASCADE,
    content VARCHAR NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,

    CONSTRAINT post_content_non_empty CHECK (length(content) >= 1)
);


CREATE TABLE attachment (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR NOT NULL,
    content BYTEA NOT NULL,
    mimetype VARCHAR NOT NULL,
    post INTEGER NOT NULL REFERENCES post (id) ON DELETE CASCADE,

    CONSTRAINT attachment_name_non_empty CHECK (length(name) >= 1)
);


CREATE TYPE notification_type AS ENUM (
    'EDITED_POST',
    'EDITED_REPORT',
    'MOVED_REPORT',
    'NEW_POST',
    'NEW_REPORT'
);

CREATE TABLE notification (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    sent BOOLEAN NOT NULL,
    "read" BOOLEAN NOT NULL,
    type notification_type NOT NULL,

    recipient INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    actuator INTEGER REFERENCES "user" (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    report INTEGER NOT NULL REFERENCES report (id) ON DELETE CASCADE,
    post INTEGER NOT NULL REFERENCES post (id) ON DELETE CASCADE
);


CREATE TABLE user_subscription (
    subscriber INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    subscribee INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT user_subscription_pkey PRIMARY KEY (subscriber, subscribee),
    CONSTRAINT user_subscription_no_self_subscription CHECK (
        subscriber <> subscribee)
);

CREATE TABLE topic_subscription (
    subscriber INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    CONSTRAINT topic_subscription_pkey PRIMARY KEY (subscriber, topic)
);

CREATE TABLE report_subscription (
    subscriber INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    report INTEGER NOT NULL REFERENCES report (id) ON DELETE CASCADE,
    CONSTRAINT report_subscription_pkey PRIMARY KEY (subscriber, report)
);

CREATE TABLE topic_moderation (
    subscriber INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    CONSTRAINT topic_moderation_pkey PRIMARY KEY (subscriber, topic)
);

CREATE TABLE topic_ban (
    subscriber INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    CONSTRAINT topic_ban_pkey PRIMARY KEY (subscriber, topic)
);

CREATE TABLE relevance_vote (
    voter INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    report INTEGER NOT NULL REFERENCES report (id) ON DELETE CASCADE,
    voted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    voting_weight INTEGER NOT NULL,
    CONSTRAINT relevance_vote_pkey PRIMARY KEY (voter, report)
);


CREATE FUNCTION validate_new_attachment() RETURNS TRIGGER AS
$$
DECLARE max_attachments_per_post INTEGER;
BEGIN
    SELECT system_settings.max_attachments_per_post
    INTO max_attachments_per_post FROM system_settings
    WHERE id = 0;

    IF (SELECT COUNT(*)
        FROM attachment
        WHERE attachment.post = NEW.post) >= max_attachments_per_post
        THEN
        RAISE EXCEPTION 'No more than % attachments allowed per post', max_attachments_per_post;
    END IF;

    IF (SELECT COUNT(*)
        FROM attachment, post
        WHERE attachment.post = post.id
        AND attachment.name = NEW.name
        ) > 0
        THEN
        RAISE EXCEPTION 'Attachment must be unique within post';
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER new_attachment
BEFORE INSERT
ON attachment
FOR EACH ROW
    EXECUTE PROCEDURE validate_new_attachment();


CREATE VIEW report_last_activity (report, last_activity) AS
    SELECT r.id, GREATEST(MAX(p.last_modified_at), r.last_modified_at)
    FROM report AS r
    LEFT OUTER JOIN post AS p
    ON r.id = p.report
    GROUP BY r.id;

CREATE VIEW topic_last_activity (topic, last_activity) AS
    SELECT t.id, MAX(l.last_activity)
    FROM topic AS t
    LEFT OUTER JOIN report AS r
    ON t.id = r.topic
    JOIN report_last_activity AS l
    ON l.report = r.id
    GROUP BY t.id;

CREATE VIEW report_relevance (report, relevance) AS
    SELECT r.id, SUM(v.voting_weight)
    FROM report AS r
    LEFT OUTER JOIN relevance_vote AS v
    ON r.id = v.report
    GROUP BY r.id;

CREATE VIEW user_num_reports (author, num_reports) AS
    SELECT u.id, COUNT(r.created_by)
    FROM "user" AS u
    LEFT OUTER JOIN report AS r
    ON u.id = r.created_by
    GROUP BY u.id;

CREATE VIEW top_ten_reports (report, recent_relevance_gain) AS
    SELECT r.id, SUM(v.voting_weight)
    FROM report AS r
    LEFT OUTER JOIN relevance_vote AS v
    ON r.id = v.report
    WHERE v.voted_at > NOW() - '24 hours'::interval
    GROUP BY r.id
    ORDER BY SUM(v.voting_weight) DESC, r.id ASC
    LIMIT 10;

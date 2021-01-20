/*
 * Sets up the database for use by Bugger.
 *
 * This SQL script is intended to be run on a clean database. If any of the
 * artifacts defined below already exist, the setup will be aborted, resulting
 * in an incomplete installation. To erase all of the artifacts defined below
 * that might exist in the database, run the attached SQL script 'erase.sql'.
 */


/**************************************************************************
 *  Definition of tables for entities and associated enumerations types.  *
 **************************************************************************/

CREATE TABLE metadata (
    id INTEGER NOT NULL PRIMARY KEY DEFAULT 0,
    version VARCHAR DEFAULT '1.0',
    CONSTRAINT metadata_only_one_row CHECK (id = 0)
);

INSERT INTO metadata DEFAULT VALUES;


CREATE TABLE system_settings (
    id INTEGER NOT NULL PRIMARY KEY DEFAULT 0,

    organization_name VARCHAR NOT NULL DEFAULT 'Bugger',
    organization_logo BYTEA DEFAULT '',
    organization_theme VARCHAR NOT NULL DEFAULT 'light.css',
    organization_privacy_policy VARCHAR NOT NULL DEFAULT '',
    organization_imprint VARCHAR NOT NULL DEFAULT '',
    organization_support_info VARCHAR NOT NULL DEFAULT '',

    guest_reading BOOLEAN NOT NULL DEFAULT TRUE,
    closed_report_posting BOOLEAN NOT NULL DEFAULT FALSE,
    user_email_format VARCHAR NOT NULL DEFAULT '.+@.+',
    voting_weight_definition VARCHAR NOT NULL DEFAULT '0,10,25,50,100,200,400,600,800,1000',
    allowed_file_extensions VARCHAR NOT NULL DEFAULT '.txt,.log,.pdf,.jpg,.png,.gif,.tif,.bmp,.svg,.webp,.wav,.m4a,.flac,.mp3,.mp4,.ogg',
    max_attachments_per_post INTEGER NOT NULL DEFAULT 5,

    CONSTRAINT system_settings_only_one_row CHECK (id = 0)
);

INSERT INTO system_settings DEFAULT VALUES;


CREATE TYPE user_profile_visibility AS ENUM (
    'FULL',
    'MINIMAL'
);

CREATE TABLE "user" (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,

    username VARCHAR UNIQUE NOT NULL,
    password_hash VARCHAR,
    password_salt VARCHAR,
    hashing_algorithm VARCHAR,

    email_address VARCHAR UNIQUE NOT NULL,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    avatar BYTEA NOT NULL DEFAULT '',
    avatar_thumbnail BYTEA NOT NULL DEFAULT '',
    biography VARCHAR,
    preferred_language VARCHAR NOT NULL DEFAULT 'en',
    profile_visibility user_profile_visibility NOT NULL DEFAULT 'FULL',
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    forced_voting_weight INTEGER,
    is_admin BOOLEAN,

    CONSTRAINT user_first_name_non_empty CHECK (length(first_name) >= 1),
    CONSTRAINT user_last_name_non_empty CHECK (length(last_name) >= 1)
);

-- Insert the first admin with the password 'BuggerFahrenMachtSpass42'
INSERT INTO "user" (username, password_hash, password_salt, hashing_algorithm,
    email_address, first_name, last_name, avatar, avatar_thumbnail, biography,
    profile_visibility, is_admin, forced_voting_weight)
VALUES ('admin',
    'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393',
    'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25',
    'SHA3-512', '', 'Admin', 'Is Traitor', '', '', '', 'MINIMAL', TRUE, NULL
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
    meta VARCHAR,
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
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 100),

    title VARCHAR NOT NULL,
    type report_type NOT NULL,
    severity report_severity NOT NULL,
    version VARCHAR,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    duplicate_of INTEGER REFERENCES report (id) ON DELETE SET NULL,
    forced_relevance INTEGER,

    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,

    CONSTRAINT report_title_non_empty CHECK (length(title) >= 1),
    CONSTRAINT report_no_self_duplicate CHECK (id <> duplicate_of)
);


CREATE TABLE post (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 100),
    content VARCHAR NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by INTEGER REFERENCES "user" (id) ON DELETE SET NULL,

    report INTEGER NOT NULL REFERENCES report (id) ON DELETE CASCADE,

    CONSTRAINT post_content_non_empty CHECK (length(content) >= 1)
);


CREATE TABLE attachment (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR NOT NULL,
    content BYTEA NOT NULL,
    mimetype VARCHAR NOT NULL,

    post INTEGER NOT NULL REFERENCES post (id) ON DELETE CASCADE,

    CONSTRAINT attachment_name_non_empty CHECK (length(name) >= 1),
    CONSTRAINT attachment_name_unique_in_post UNIQUE (name, post)
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
    causer INTEGER REFERENCES "user" (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    topic INTEGER REFERENCES topic (id) ON DELETE CASCADE,
    report INTEGER REFERENCES report (id) ON DELETE CASCADE,
    post INTEGER REFERENCES post (id) ON DELETE CASCADE
);


/**********************************************************
 *  Definition of tables for many-to-many relationships.  *
 **********************************************************/

CREATE TABLE user_subscription (
    subscriber INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    subscribee INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT user_subscription_pkey PRIMARY KEY (subscriber, subscribee),
    CONSTRAINT user_subscription_no_self_subscription CHECK (subscriber <> subscribee)
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
    moderator INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    CONSTRAINT topic_moderation_pkey PRIMARY KEY (moderator, topic)
);

CREATE TABLE topic_ban (
    outcast INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    topic INTEGER NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    CONSTRAINT topic_ban_pkey PRIMARY KEY (outcast, topic)
);

CREATE TABLE relevance_vote (
    voter INTEGER NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    report INTEGER NOT NULL REFERENCES report (id) ON DELETE CASCADE,
    voted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    weight INTEGER NOT NULL,
    CONSTRAINT relevance_vote_pkey PRIMARY KEY (voter, report)
);



/**************************
 *  Definition of views.  *
 **************************/

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
    LEFT OUTER JOIN report_last_activity AS l
    ON l.report = r.id
    GROUP BY t.id;

CREATE VIEW report_relevance (report, relevance) AS
    SELECT r.id, COALESCE(SUM(v.weight), 0)
    FROM report AS r
    LEFT OUTER JOIN relevance_vote AS v
    ON r.id = v.report
    GROUP BY r.id;

CREATE VIEW user_num_posts (author, num_posts) AS
    SELECT u.id, COUNT(p.created_by)
    FROM "user" AS u
    LEFT OUTER JOIN post AS p
    ON u.id = p.created_by
    GROUP BY u.id;

CREATE VIEW last_day_votes (report, weight) AS
    SELECT report, weight
    FROM relevance_vote
    WHERE voted_at > NOW() - '24 hours'::interval;

CREATE VIEW top_reports (report, relevance_gain) AS
    SELECT r.id, COALESCE(SUM(v.weight), 0)
    FROM report AS r
    LEFT OUTER JOIN last_day_votes AS v
    ON r.id = v.report
    GROUP BY r.id
    ORDER BY COALESCE(SUM(v.weight), 0) DESC, r.id ASC;

CREATE VIEW top_users ("user", earned_relevance) AS
    SELECT u.id, COALESCE(SUM(s.relevance), 0)
    FROM "user" AS u
    LEFT OUTER JOIN report AS r
    ON r.created_by = u.id
    LEFT OUTER JOIN report_relevance AS s
    ON r.id = s.report
    GROUP BY u.id
    ORDER BY COALESCE(SUM(s.relevance), 0) DESC, u.id ASC;
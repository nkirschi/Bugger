/*
 * Erases all existing artifacts from the database that belong to Bugger.
 *
 * Use with caution! All data concerning Bugger will be irreversibly deleted.
 */

DROP TRIGGER IF EXISTS new_attachment ON attachment;
DROP FUNCTION IF EXISTS validate_new_attachment;

DROP VIEW IF EXISTS top_ten_reports;
DROP VIEW IF EXISTS user_num_reports;
DROP VIEW IF EXISTS report_relevance;
DROP VIEW IF EXISTS topic_last_activity;
DROP VIEW IF EXISTS report_last_activity;

DROP TABLE IF EXISTS user_subscription;
DROP TABLE IF EXISTS topic_subscription;
DROP TABLE IF EXISTS report_subscription;
DROP TABLE IF EXISTS topic_moderation;
DROP TABLE IF EXISTS topic_ban;
DROP TABLE IF EXISTS relevance_vote;

DROP TABLE IF EXISTS notification;
DROP TYPE IF EXISTS notification_type;
DROP TABLE IF EXISTS attachment;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS report;
DROP TYPE IF EXISTS report_severity;
DROP TYPE IF EXISTS report_type;
DROP TABLE IF EXISTS topic;
DROP TABLE IF EXISTS token;
DROP TYPE IF EXISTS token_type;
DROP TABLE IF EXISTS "user";
DROP TYPE IF EXISTS profile_visibility_degree;

DROP TABLE IF EXISTS version;
DROP TABLE IF EXISTS system_settings;

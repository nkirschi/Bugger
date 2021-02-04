DELETE FROM user_subscription;
DELETE FROM topic_subscription;
DELETE FROM report_subscription;
DELETE FROM topic_moderation;
DELETE FROM topic_ban;
DELETE FROM relevance_vote;

DELETE FROM notification;
DELETE FROM attachment;
DELETE FROM post;
DELETE FROM report;
DELETE FROM topic;
DELETE FROM token;
DELETE FROM "user" WHERE id != 1;

DELETE FROM system_settings WHERE id != 0;
DELETE FROM metadata WHERE id != 0;
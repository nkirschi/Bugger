DELETE FROM "user" where id <> 1;
DELETE FROM topic;

INSERT INTO "user" (username, email_address, first_name, last_name,
    password_hash, password_salt, hashing_algorithm, is_admin) VALUES
    ('testuser', 'testuser@example.org', 'Test', 'User', 'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393', 'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25', 'SHA3-512', false);

INSERT INTO topic (title, description) VALUES
    ('testtopic', 'Description of testtopic');

INSERT INTO report (topic, title, type, severity, version, created_by, created_at, closed_at) VALUES
    (1, 'testreport', 'BUG', 'MINOR', 'testversion', 1, NOW(), null),
    (1, 'bestreport', 'BUG', 'MINOR', 'testversion', 2, '2021-01-01 00:00:00', '2021-01-01 8:00:00'),
    (1, 'westreport', 'BUG', 'MINOR', 'testversion', 1, '2021-01-01 00:00:00', '2021-01-01 16:00:00');

INSERT INTO post (report, content) VALUES
    (100, 'testpost'),
    (101, 'testpost');

INSERT INTO attachment (post, name, mimetype, content) VALUES
    (100, 'testattachment.txt', 'text/plain', 'testcontent');

INSERT INTO relevance_vote (voter, report, voted_at, weight) VALUES
    (1, 100, NOW(), 2),
    (1, 101, NOW(), 5),
    (1, 102, '1900-01-01 00:00:00', 8);
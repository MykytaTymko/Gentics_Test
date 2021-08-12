In order to conduct correct tests, please follow these steps:

1) create 2 databases:
create table domains
(
    id INT NOT NULL PRIMARY KEY,
    text VARCHAR(255) NOT NULL UNIQUE
);
create table links
(
    id INT NOT NULL PRIMARY KEY,
    domain_id INT NOT NULL,
    text VARCHAR(2048) NOT NULL UNIQUE,
    domain_text VARCHAR(255) NOT NULL,
    FOREIGN KEY (domain_id) REFERENCES domains(id)
);

2) change the file database.properties (src/main/resources/database.properties).

3) take the getConnection() test in class AnalyzerTest to make sure there is a connection to the database.

After that, you can start.

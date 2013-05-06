-- Local repository
CREATE TABLE modules (id IDENTITY PRIMARY KEY, name VARCHAR UNIQUE NOT NULL);
CREATE TABLE releases (id IDENTITY PRIMARY KEY, module_id BIGINT NOT NULL,
	version VARCHAR NOT NULL, path VARCHAR NOT NULL, last_modified TIMESTAMP NOT NULL,
	content BLOB NOT NULL, UNIQUE (module_id, version),
	FOREIGN KEY (module_id) REFERENCES modules(id));
CREATE TABLE dependency_types (id TINYINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR NOT NULL);
INSERT INTO dependency_types (name) VALUES ('BUILD');
INSERT INTO dependency_types (name) VALUES ('RUNTIME');

CREATE TABLE release_dependencies (release_id BIGINT NOT NULL,
	module VARCHAR NOT NULL, version VARCHAR NOT NULL, type TINYINT NOT NULL, uri VARCHAR NOT NULL,
	FOREIGN KEY (release_id) REFERENCES releases(id),
	FOREIGN KEY (type) REFERENCES dependency_types(id));
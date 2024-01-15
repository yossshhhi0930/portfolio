DROP TABLE IF EXISTS diary_image CASCADE;
DROP TABLE IF EXISTS diary CASCADE;
DROP TABLE IF EXISTS plan CASCADE;
DROP TABLE IF EXISTS section CASCADE;
DROP TABLE IF EXISTS crop_image CASCADE;
DROP TABLE IF EXISTS crop CASCADE;
DROP TABLE IF EXISTS user_email_changes CASCADE;
DROP TABLE IF EXISTS failed_password CASCADE;
DROP TABLE IF EXISTS password_reissue CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS users (
  user_id SERIAL NOT NULL,
  username VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  authority VARCHAR(255) NOT NULL,
  token VARCHAR(255),
  enabled BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS password_reissue (
  id SERIAL NOT NULL,
  username VARCHAR(255) NOT NULL,
  token VARCHAR(255) NOT NULL,
  secret VARCHAR(255) NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS failed_password (
  id SERIAL NOT NULL,
  email VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_email_changes (
  id SERIAL NOT NULL,
  user_id INT NOT NULL,
  token VARCHAR(255) NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  new_email VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS crop (
  id SERIAL NOT NULL,
  user_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  sowing_start DATE NOT NULL,
  sowing_end DATE NOT NULL,
  cultivationp_period INT NOT NULL,
  manual VARCHAR(1000),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE crop ADD CONSTRAINT FK_users_crop FOREIGN KEY (user_id) REFERENCES users;

CREATE TABLE IF NOT EXISTS crop_image (
  id SERIAL NOT NULL,
  crop_id INT NOT NULL,
  path VARCHAR(255) NOT NULL,
  top_image BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE crop_image ADD CONSTRAINT FK_crop_crop_image FOREIGN KEY (crop_id) REFERENCES crop;

CREATE TABLE IF NOT EXISTS section (
  id SERIAL NOT NULL,
  user_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE section ADD CONSTRAINT FK_users_section FOREIGN KEY (user_id) REFERENCES users;

CREATE TABLE IF NOT EXISTS plan (
  id SERIAL NOT NULL,
  user_id INT NOT NULL,
  crop_id INT NOT NULL,
  section_id INT NOT NULL,
  sowing_date DATE NOT NULL,
  harvest_completion_date DATE NOT NULL,
  completion BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE plan ADD CONSTRAINT FK_users_plan FOREIGN KEY (user_id) REFERENCES users;
ALTER TABLE plan ADD CONSTRAINT FK_crop_plan FOREIGN KEY (crop_id) REFERENCES crop;
ALTER TABLE plan ADD CONSTRAINT FK_section_plan FOREIGN KEY (section_id) REFERENCES section;

CREATE TABLE IF NOT EXISTS diary (
  id SERIAL NOT NULL,
  user_id INT NOT NULL,
  plan_id INT NOT NULL,
  record_date DATE NOT NULL,
  description VARCHAR(1000),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE diary ADD CONSTRAINT FK_users_diary FOREIGN KEY (user_id) REFERENCES users;
ALTER TABLE diary ADD CONSTRAINT FK_plan_diary FOREIGN KEY (plan_id) REFERENCES plan;

CREATE TABLE IF NOT EXISTS diary_image (
  id SERIAL NOT NULL,
  diary_id INT NOT NULL,
  path VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE diary_image ADD CONSTRAINT FK_diary_diary_image FOREIGN KEY (diary_id) REFERENCES diary;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO postgres;
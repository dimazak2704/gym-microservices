CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE training_types (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                training_type_name VARCHAR(255) NOT NULL
);

CREATE TABLE trainees (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          date_of_birth DATE,
                          address VARCHAR(255),
                          user_id BIGINT NOT NULL UNIQUE,
                          FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE trainers (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          specialization_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL UNIQUE,
                          FOREIGN KEY (specialization_id) REFERENCES training_types(id),
                          FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE trainings (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           trainee_id BIGINT NOT NULL,
                           trainer_id BIGINT NOT NULL,
                           training_name VARCHAR(255) NOT NULL,
                           training_type_id BIGINT NOT NULL,
                           training_date DATE NOT NULL,
                           training_duration INT NOT NULL,
                           FOREIGN KEY (trainee_id) REFERENCES trainees(id) ON DELETE CASCADE,
                           FOREIGN KEY (trainer_id) REFERENCES trainers(id),
                           FOREIGN KEY (training_type_id) REFERENCES training_types(id)
);

CREATE TABLE trainee_trainer (
                                 trainee_id BIGINT NOT NULL,
                                 trainer_id BIGINT NOT NULL,
                                 PRIMARY KEY (trainee_id, trainer_id),
                                 FOREIGN KEY (trainee_id) REFERENCES trainees(id) ON DELETE CASCADE,
                                 FOREIGN KEY (trainer_id) REFERENCES trainers(id)
);
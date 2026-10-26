-- =====================================================================
-- Employee Task Management System: Database Schema (MySQL 8)
-- Run this file ONCE as the MySQL root user (or another admin account).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS employee_task_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE employee_task_db;

-- ---------------------------------------------------------------------
-- 1. users: login accounts (one row per person who can log in)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
                                     user_id       INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                     username      VARCHAR(50)   NOT NULL,
    password_hash VARCHAR(100)  NOT NULL,          -- BCrypt hash, never plaintext
    role          ENUM('MANAGER','EMPLOYEE') NOT NULL,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_username (username)
    ) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. employees: profile details, linked 1-to-1 with a user account
--    (managers also have a profile row so every account has a name/email)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
                                         employee_id   INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                         user_id       INT UNSIGNED  NOT NULL,
                                         employee_name VARCHAR(100)  NOT NULL,
    email         VARCHAR(120)  NOT NULL,
    department    VARCHAR(80)   NOT NULL,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id),
    UNIQUE KEY uq_employees_user  (user_id),
    UNIQUE KEY uq_employees_email (email),
    KEY idx_employees_name       (employee_name),
    KEY idx_employees_department (department),
    CONSTRAINT fk_employees_user
    FOREIGN KEY (user_id) REFERENCES users (user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
    ) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. tasks: work assigned by a manager to an employee
--    "Overdue" is NOT stored; it is calculated:
--    due_date < CURDATE() AND status IN ('PENDING','IN_PROGRESS')
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tasks (
                                     task_id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                     title               VARCHAR(150) NOT NULL,
    description         TEXT         NOT NULL,
    assigned_to         INT UNSIGNED NOT NULL,      -- employees.employee_id
    assigned_by         INT UNSIGNED NOT NULL,      -- users.user_id (the manager)
    priority            ENUM('LOW','MEDIUM','HIGH','URGENT') NOT NULL DEFAULT 'MEDIUM',
    status              ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    progress_percentage TINYINT UNSIGNED NOT NULL DEFAULT 0,
    start_date          DATE         NOT NULL,
    due_date            DATE         NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id),
    KEY idx_tasks_assigned_to (assigned_to),
    KEY idx_tasks_assigned_by (assigned_by),
    KEY idx_tasks_status      (status),
    KEY idx_tasks_priority    (priority),
    KEY idx_tasks_due_date    (due_date),
    KEY idx_tasks_title       (title),
    CONSTRAINT fk_tasks_assigned_to
    FOREIGN KEY (assigned_to) REFERENCES employees (employee_id)
                                                                        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tasks_assigned_by
    FOREIGN KEY (assigned_by) REFERENCES users (user_id)
                                                                        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_tasks_progress CHECK (progress_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_tasks_dates    CHECK (due_date >= start_date)
    ) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. task_updates: history log; one row every time a task changes
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS task_updates (
                                            update_id           INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                            task_id             INT UNSIGNED NOT NULL,
                                            updated_by          INT UNSIGNED NOT NULL,      -- users.user_id
                                            old_status          ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NULL,
    new_status          ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL,
    progress_percentage TINYINT UNSIGNED NOT NULL,
    comment             VARCHAR(1000) NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (update_id),
    KEY idx_updates_task    (task_id),
    KEY idx_updates_created (created_at),
    CONSTRAINT fk_updates_task
    FOREIGN KEY (task_id) REFERENCES tasks (task_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_updates_user
    FOREIGN KEY (updated_by) REFERENCES users (user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_updates_progress CHECK (progress_percentage BETWEEN 0 AND 100)
    ) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Dedicated application account (the app should NOT use root).
-- !!! Replace CHANGE_ME with your own password before running !!!
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'etm_user'@'localhost' IDENTIFIED BY 'Etm@2026secure';
GRANT SELECT, INSERT, UPDATE ON employee_task_db.* TO 'etm_user'@'localhost';
FLUSH PRIVILEGES;
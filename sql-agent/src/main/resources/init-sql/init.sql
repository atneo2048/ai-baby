-- =========================================================
-- SQL Agent Demo Database
-- MySQL / OceanBase MySQL Mode
-- =========================================================


-- =========================================================
-- 2. 删除旧表
-- =========================================================

DROP TABLE IF EXISTS employee_project;
DROP TABLE IF EXISTS employee_salary;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;


-- =========================================================
-- 3. 部门表
-- =========================================================

CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_code VARCHAR(50) NOT NULL UNIQUE,
    department_name VARCHAR(100) NOT NULL,
    manager_id BIGINT NULL,
    location VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='公司部门信息';


-- =========================================================
-- 4. 员工表
-- =========================================================

CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    age INT,
    department_id BIGINT NOT NULL,
    position VARCHAR(100),
    hire_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES department(id)
) COMMENT='公司员工信息';


-- =========================================================
-- 5. 员工薪资表
-- =========================================================

CREATE TABLE employee_salary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    salary DECIMAL(12,2) NOT NULL,
    bonus DECIMAL(12,2) DEFAULT 0,
    effective_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_salary_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee(id)
) COMMENT='员工薪资信息';


-- =========================================================
-- 6. 项目表
-- =========================================================

CREATE TABLE project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    project_name VARCHAR(200) NOT NULL,
    project_type VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE,
    end_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='公司项目';


-- =========================================================
-- 7. 员工-项目关系表
-- =========================================================

CREATE TABLE employee_project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    role_name VARCHAR(100),
    join_date DATE,
    leave_date DATE,

    CONSTRAINT fk_ep_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee(id),

    CONSTRAINT fk_ep_project
        FOREIGN KEY (project_id)
        REFERENCES project(id),

    UNIQUE KEY uk_employee_project (
        employee_id,
        project_id
    )
) COMMENT='员工参与项目关系';


-- =========================================================
-- 8. 初始化部门
-- =========================================================

INSERT INTO department
    (department_code, department_name, manager_id, location)
VALUES
    ('TECH', '研发部', NULL, '上海'),
    ('PRODUCT', '产品部', NULL, '上海'),
    ('HR', '人力资源部', NULL, '上海'),
    ('FINANCE', '财务部', NULL, '上海'),
    ('SALES', '销售部', NULL, '北京'),
    ('MARKETING', '市场部', NULL, '上海');


-- =========================================================
-- 9. 初始化员工
-- =========================================================

INSERT INTO employee
    (
        employee_no,
        name,
        gender,
        age,
        department_id,
        position,
        hire_date,
        status
    )
VALUES

-- 研发部
(
    'E001',
    '张伟',
    '男',
    29,
    1,
    'Java高级工程师',
    '2020-03-15',
    'ACTIVE'
),

(
    'E002',
    '李娜',
    '女',
    27,
    1,
    'Java工程师',
    '2021-06-01',
    'ACTIVE'
),

(
    'E003',
    '王强',
    '男',
    32,
    1,
    '技术负责人',
    '2018-05-20',
    'ACTIVE'
),

(
    'E004',
    '赵敏',
    '女',
    26,
    1,
    '测试工程师',
    '2022-02-15',
    'ACTIVE'
),

(
    'E005',
    '刘洋',
    '男',
    30,
    1,
    '架构师',
    '2019-08-10',
    'ACTIVE'
),

-- 产品部
(
    'E006',
    '陈静',
    '女',
    30,
    2,
    '产品经理',
    '2019-04-01',
    'ACTIVE'
),

(
    'E007',
    '杨帆',
    '男',
    28,
    2,
    '产品经理',
    '2021-09-10',
    'ACTIVE'
),

-- 人力资源部
(
    'E008',
    '黄芳',
    '女',
    35,
    3,
    'HR经理',
    '2017-03-01',
    'ACTIVE'
),

(
    'E009',
    '周杰',
    '男',
    29,
    3,
    'HR专员',
    '2022-05-12',
    'ACTIVE'
),

-- 财务部
(
    'E010',
    '吴婷',
    '女',
    31,
    4,
    '财务经理',
    '2018-07-01',
    'ACTIVE'
),

(
    'E011',
    '徐磊',
    '男',
    28,
    4,
    '财务专员',
    '2021-11-01',
    'ACTIVE'
),

-- 销售部
(
    'E012',
    '孙伟',
    '男',
    34,
    5,
    '销售经理',
    '2016-08-15',
    'ACTIVE'
),

(
    'E013',
    '何丽',
    '女',
    27,
    5,
    '销售专员',
    '2022-03-01',
    'ACTIVE'
),

-- 市场部
(
    'E014',
    '高峰',
    '男',
    31,
    6,
    '市场经理',
    '2019-10-01',
    'ACTIVE'
),

(
    'E015',
    '林晓',
    '女',
    26,
    6,
    '市场专员',
    '2023-01-10',
    'ACTIVE'
);


-- =========================================================
-- 10. 初始化薪资
-- =========================================================

INSERT INTO employee_salary
    (
        employee_id,
        salary,
        bonus,
        effective_date
    )
VALUES
    (1, 22000, 30000, '2026-01-01'),
    (2, 18000, 20000, '2026-01-01'),
    (3, 30000, 50000, '2026-01-01'),
    (4, 16000, 15000, '2026-01-01'),
    (5, 35000, 60000, '2026-01-01'),

    (6, 20000, 25000, '2026-01-01'),
    (7, 18000, 20000, '2026-01-01'),

    (8, 22000, 30000, '2026-01-01'),
    (9, 14000, 12000, '2026-01-01'),

    (10, 24000, 30000, '2026-01-01'),
    (11, 15000, 12000, '2026-01-01'),

    (12, 26000, 40000, '2026-01-01'),
    (13, 15000, 15000, '2026-01-01'),

    (14, 23000, 30000, '2026-01-01'),
    (15, 14000, 12000, '2026-01-01');


-- =========================================================
-- 11. 初始化项目
-- =========================================================

INSERT INTO project
    (
        project_code,
        project_name,
        project_type,
        status,
        start_date,
        end_date
    )
VALUES
    (
        'P001',
        'SQL Agent智能查询系统',
        'AI',
        'ACTIVE',
        '2026-01-01',
        NULL
    ),

    (
        'P002',
        '企业数据平台',
        'Backend',
        'ACTIVE',
        '2025-06-01',
        NULL
    ),

    (
        'P003',
        '用户中心系统',
        'Backend',
        'ACTIVE',
        '2025-03-01',
        NULL
    ),

    (
        'P004',
        '移动端APP',
        'APP',
        'COMPLETED',
        '2025-01-01',
        '2025-12-31'
    ),

    (
        'P005',
        '数据可视化平台',
        'BI',
        'ACTIVE',
        '2026-02-01',
        NULL
    );


-- =========================================================
-- 12. 初始化员工项目关系
-- =========================================================

INSERT INTO employee_project
    (
        employee_id,
        project_id,
        role_name,
        join_date
    )
VALUES

-- SQL Agent
(1, 1, '后端开发', '2026-01-01'),
(3, 1, '技术负责人', '2026-01-01'),
(4, 1, '测试工程师', '2026-01-15'),
(5, 1, '架构师', '2026-01-01'),

-- 企业数据平台
(1, 2, '后端开发', '2025-06-01'),
(2, 2, '后端开发', '2025-06-01'),
(5, 2, '架构师', '2025-06-01'),

-- 用户中心
(2, 3, '后端开发', '2025-03-01'),
(3, 3, '技术负责人', '2025-03-01'),

-- APP
(4, 4, '测试工程师', '2025-01-01'),

-- 数据可视化
(1, 5, '后端开发', '2026-02-01'),
(2, 5, '后端开发', '2026-02-01'),
(6, 5, '产品经理', '2026-02-01');


-- =========================================================
-- 13. 更新部门负责人
-- =========================================================

UPDATE department
SET manager_id = 3
WHERE department_code = 'TECH';

UPDATE department
SET manager_id = 6
WHERE department_code = 'PRODUCT';

UPDATE department
SET manager_id = 8
WHERE department_code = 'HR';

UPDATE department
SET manager_id = 10
WHERE department_code = 'FINANCE';

UPDATE department
SET manager_id = 12
WHERE department_code = 'SALES';

UPDATE department
SET manager_id = 14
WHERE department_code = 'MARKETING';
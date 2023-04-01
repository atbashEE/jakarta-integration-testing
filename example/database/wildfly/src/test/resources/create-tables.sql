--
-- Copyright 2022-2023 Rudy De Busscher (https://www.atbash.be)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE Company (id BIGINT NOT NULL, name VARCHAR(255), PRIMARY KEY (id))
CREATE TABLE Employee (EMPLOYEE_ID BIGINT NOT NULL, FAVORITE_COLOR VARCHAR(255), FIRST_NAME VARCHAR(255), GENDER VARCHAR(255), HIRE_DATE DATE, LAST_NAME VARCHAR(255), COMPANY_ID BIGINT, PRIMARY KEY (EMPLOYEE_ID))
ALTER TABLE Employee ADD CONSTRAINT FK_Employee_COMPANY_ID FOREIGN KEY (COMPANY_ID) REFERENCES Company (id)
CREATE TABLE ID_GEN (GEN_NAME VARCHAR(50) NOT NULL, GEN_VAL DECIMAL(38), PRIMARY KEY (GEN_NAME))
INSERT INTO ID_GEN(GEN_NAME, GEN_VAL) values ('CompanyGen', 9)
ALTER TABLE Employee DROP FOREIGN KEY FK_Employee_COMPANY_ID
DROP TABLE company
DROP TABLE Employee
DELETE FROM ID_GEN WHERE GEN_NAME = 'CompanyGen'

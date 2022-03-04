DROP TABLE IF EXISTS users;

CREATE TABLE users (
  username VARCHAR(50)  PRIMARY KEY,
  password VARCHAR(50) NOT NULL,
  enabled BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS authorities;

CREATE TABLE authorities (
  username VARCHAR(50)  PRIMARY KEY,
  authority VARCHAR(50) NOT NULL
);

INSERT INTO users (username, password, enabled) VALUES ('admin', 'admin123', TRUE);
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO users (username, password, enabled) VALUES ('a1', 'a123', TRUE);  
INSERT INTO authorities (username, authority) VALUES ('a1', 'ROLE_ANNOTATOR');
INSERT INTO users (username, password, enabled) VALUES ('a2', 'a2123', TRUE);  
INSERT INTO authorities (username, authority) VALUES ('a2', 'ROLE_ANNOTATOR');
INSERT INTO users (username, password, enabled) VALUES ('a3', 'a3123', TRUE);  
INSERT INTO authorities (username, authority) VALUES ('a3', 'ROLE_ANNOTATOR');

DROP TABLE IF EXISTS system_Setting;

CREATE TABLE system_Setting (
  settings_Id BIGINT identity primary key,
  key VARCHAR(100) NOT NULL,
  value VARCHAR(300) NOT NULL,
  description VARCHAR(300) NOT NULL
);

INSERT INTO system_Setting (settings_Id, key, value, description) VALUES (1, 'slanno.legalref.section.abbr.list', 
'under section,under sections,u.s.,u/s,read with,r/w,r.w.,read with section,read with sections,r/w. s.,r.w.s.,under sec/s.,U/ss.,section,sections', 
'Different ways in which a section may be abbreviated in legal reference');

INSERT INTO system_Setting (settings_Id, key, value, description) VALUES (2, 'slanno.abbrs.in.sentence.list', 
'Ld.','Abbreviations used in sentences, which does not mark end of sentences');

DROP TABLE IF EXISTS legal_Act;

CREATE TABLE legal_Act (
  act_Id BIGINT identity primary key,
  act_Name VARCHAR(250) NOT NULL,
  act_Short_Name_List VARCHAR(500),
  act_Year VARCHAR(10) NOT NULL,
  min_Section_Number VARCHAR(10) NOT NULL,
  max_Section_Number VARCHAR(10) NOT NULL
);

INSERT INTO legal_Act (act_Id, act_Name, act_Short_Name_List, act_Year, min_Section_Number, max_Section_Number) VALUES
(1, 'Code of Criminal Procedure', 'CrPC,Cr.PC,Cr. P. C.', '1973', '1', '484');
INSERT INTO legal_Act (act_Id, act_Name, act_Short_Name_List, act_Year, min_Section_Number, max_Section_Number) VALUES
(2, 'Indian Penal Code', 'IPC,I.P.C.,I.P.C', '1860', '1', '511');
INSERT INTO legal_Act (act_Id, act_Name, act_Short_Name_List, act_Year, min_Section_Number, max_Section_Number) VALUES
(3, 'Maharashtra Money Lending Act', 'MMLA', '2014', '1', '57');

DROP TABLE IF EXISTS project;

CREATE TABLE project (
  project_Id int identity primary key,
  project_Name VARCHAR(50) NOT NULL,
  project_Directory_Name VARCHAR(20) NOT NULL,
  annotator_User_List_String VARCHAR(250)
);

DROP TABLE IF EXISTS legal_Document;

CREATE TABLE legal_Document (
  document_Id BIGINT identity primary key,
  pdf_File_Path VARCHAR(250),
  orig_Text_File_Path VARCHAR(250),
  processed_Text_File_Path VARCHAR(250),
  json_File_Path VARCHAR(250),
  annotation_Processing_Stage int,
  project_Id int NOT NULL,
  constraint legalDocument_project_fk
    foreign key (project_Id)
    references project (project_Id)
);
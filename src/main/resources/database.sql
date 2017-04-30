DROP TABLE GRADE;
DROP TABLE CIRCUIT;
DROP TABLE EVALUATOR;
DROP TABLE TRAINING_SET;
DROP TABLE PROBLEM;

CREATE TABLE PROBLEM (
	PROBLEM_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	NAME varchar(255) NOT NULL,
	USE_MEMORY INTEGER NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CONSTRAINT PROBLEM_PK PRIMARY KEY (PROBLEM_ID),
	CONSTRAINT PROBLEM_UK UNIQUE (NAME)
);

CREATE TABLE TRAINING_SET (
	TRAINING_SET_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	PROBLEM_ID INT NOT NULL,
	OBJECT CLOB NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CONSTRAINT TRAINING_SET_PK PRIMARY KEY (TRAINING_SET_ID), 
	CONSTRAINT TRAINING_SET_PROBLEM_FK FOREIGN KEY (PROBLEM_ID)	REFERENCES PROBLEM (PROBLEM_ID)
);

CREATE TABLE EVALUATOR (
	EVALUATOR_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	PROBLEM_ID INT NOT NULL,
	OBJECT CLOB NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CONSTRAINT EVALUATOR_PK PRIMARY KEY (EVALUATOR_ID), 
	CONSTRAINT EVALUATOR_PROBLEM_FK FOREIGN KEY (PROBLEM_ID) REFERENCES PROBLEM (PROBLEM_ID)
);

CREATE TABLE CIRCUIT (
	CIRCUIT_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	PROBLEM_ID INT NOT NULL,
	OBJECT CLOB NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CONSTRAINT CIRCUIT_PK PRIMARY KEY (CIRCUIT_ID),
	CONSTRAINT CIRCUIT_PROBLEM_FK FOREIGN KEY (PROBLEM_ID)	REFERENCES PROBLEM (PROBLEM_ID)
);


CREATE TABLE GRADE (
	GRADE_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	CIRCUIT_ID INT NOT NULL,
	TRAINING_SET_ID INT NOT NULL,
	EVALUATOR_ID INT NOT NULL,
	NAME VARCHAR(255) NOT NULL,
	VALUE INT NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CONSTRAINT GRADE_PK PRIMARY KEY (GRADE_ID),
--	CONSTRAINT GRADE_UK UNIQUE (GRADE_ID, CIRCUIT_ID, TRAINING_SET_ID, EVALUATOR_ID, NAME),
	CONSTRAINT GRADE_TRAINING_SET_FK FOREIGN KEY (TRAINING_SET_ID) REFERENCES TRAINING_SET (TRAINING_SET_ID),
	CONSTRAINT GRADE_EVALUATOR_FK FOREIGN KEY (EVALUATOR_ID) REFERENCES EVALUATOR (EVALUATOR_ID)
);

insert into problem (name, use_memory , created) values ('CHAR_TYPE', 1, current_timestamp);
insert into problem (name, use_memory , created) values ('SPORTING_BET', 0, current_timestamp);

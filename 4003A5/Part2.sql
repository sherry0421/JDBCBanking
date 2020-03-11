CREATE OR REPLACE PACKAGE bank AS
	FUNCTION branches(addr Branch.address%type) RETURN Branch.b#%type;
    FUNCTION open_branch(addr Branch.address%type) RETURN Branch.address%type;
    FUNCTION close_branch(addr Branch.address%type) RETURN Branch.address%type;
    FUNCTION create_customer(cname Customer.name%type) RETURN Customer.name%type;
    FUNCTION remove_customer(cname Customer.name%type) RETURN Customer.name%type;
    FUNCTION open_account(cname Customer.name%type, addr Branch.address%type, amount account.balance%type) RETURN account.a#%type;
    FUNCTION close_account(acct CHAR) RETURN CHAR;
    FUNCTION withdraw(acct CHAR, amount Account.balance%type) RETURN CHAR;
    FUNCTION deposit(acct CHAR, amount Account.balance%type) RETURN CHAR;
    FUNCTION transfer(acct1 CHAR, acct2 CHAR, amount Account.balance%type) RETURN CHAR;
    FUNCTION show_branch(addr Branch.address%type) RETURN DBMSOUTPUT_LINESARRAY;
    FUNCTION show_all_branches RETURN DBMSOUTPUT_LINESARRAY;
    FUNCTION show_customer(cname Customer.name%type) RETURN DBMSOUTPUT_LINESARRAY;
END bank;
/

CREATE OR REPLACE PACKAGE BODY bank AS
	FUNCTION branches(addr Branch.address%type) RETURN Branch.b#%type IS
	bno Branch.b#%type;
    InvalidAddressException Exception;
	BEGIN
		SELECT b# INTO bno
		FROM Branch
		WHERE Branch.address = addr;
		IF bno IS NULL THEN
            RAISE InvalidAddressException;
        ELSE
			RETURN bno;
		END IF;
        EXCEPTION
		WHEN InvalidAddressException THEN
			dbms_output.put_line('Invalid Branch');
            RETURN NULL;
	END;
    
    FUNCTION open_branch(addr Branch.address%type) RETURN Branch.address%type IS
	BranchAlreadyExistException Exception;
	counter integer;
    counter1 integer;
	bno number;
    temp number;
	BEGIN
		SELECT COUNT(*) INTO counter
		FROM Branch;
		IF counter<1 THEN
			INSERT INTO Branch(b#, address) VALUES ('000',addr);
		ELSE
        	SELECT COUNT(*) INTO counter1
        	FROM Branch
        	WHERE Branch.address = addr;
            IF counter1>0 THEN
                RAISE BranchAlreadyExistException;
    		ELSE
                select TO_NUMBER(min(b.b#)) into temp
                from Branch b;
                IF temp>1 Then
                    INSERT INTO Branch(b#, address) VALUES ('000',addr);
                END IF;
                select TO_NUMBER(min(b.b#))+1 into bno
                from Branch b
                where not exists (select 1 from Branch b2 where To_Number(b2.b#) = To_Number(b.b#) + 1);
                INSERT INTO Branch(b#, address) VALUES (LPAD(TO_CHAR(bno),3,'0'),addr);
            END IF;
        END IF;
        RETURN addr;
	EXCEPTION
		WHEN BranchAlreadyExistException THEN
			dbms_output.put_line('Branch already exist');
			RETURN NULL;
	END;
    
    FUNCTION close_branch(addr Branch.address%type) RETURN Branch.address%type IS
    InvalidAddressException Exception;
	counter integer;
	BEGIN
		SELECT COUNT(*) INTO counter
		FROM Branch
		WHERE Branch.address = addr;
		IF counter>0 THEN
            DELETE FROM Branch WHERE Branch.address = addr;
            RETURN addr;
		ELSE
			RAISE InvalidAddressException; 
		END IF;
	EXCEPTION
		WHEN InvalidAddressException THEN
			dbms_output.put_line('Invalid Branch');
            RETURN NULL;
	END;
    
    FUNCTION create_customer(cname Customer.name%type) RETURN Customer.name%type IS
    CustomerAlreadyExistException EXCEPTION;
	counter integer;
	counter1 integer;
	cno number;
    temp number;
	BEGIN
        SELECT COUNT(*) INTO counter
        FROM Customer;
        IF counter<1 THEN
            INSERT INTO Customer(c#,name) VALUES('00000',cname);
        ELSE
            SELECT COUNT(*) INTO counter1
            FROM Customer
            WHERE Customer.name = cname;
            IF counter1>0 THEN
                RAISE CustomerAlreadyExistException;
            ELSE
                select TO_NUMBER(min(c.c#)) into temp
                from Customer c;
                IF temp>1 Then
                    INSERT INTO Customer(C#, NAME) VALUES ('00000',cname);
                END IF;
                select TO_NUMBER(min(c.c#))+1 into cno
                from Customer c
                where not exists (select 1 from Customer c2 where To_Number(c2.c#) = To_Number(c.c#) + 1);
                INSERT INTO Customer(C#, NAME) VALUES (LPAD(TO_CHAR(cno),5,'0'),cname);
            END IF;
    	END IF;
        RETURN cname;
	EXCEPTION
		WHEN CustomerAlreadyExistException THEN
			dbms_output.put_line('Customer already exists.');
            RETURN NULL;
	END;
    
    FUNCTION remove_customer(cname Customer.name%type) RETURN Customer.name%type IS
	InvalidNameException Exception;
    AccountExistException Exception;
	counter integer;
    counter1 integer;
	BEGIN
		SELECT COUNT(*) INTO counter
		FROM Customer
		WHERE Customer.name = cname;
		IF counter<1 THEN
			RAISE InvalidNameException; 
		ELSE
            SELECT COUNT(*) INTO counter1
            FROM ACCOUNT, CUSTOMER
            WHERE ACCOUNT.C#=CUSTOMER.C# AND CUSTOMER.NAME = cname;
            IF counter1>0 THEN
                RAISE AccountExistException;
            ELSE
                DELETE FROM Customer WHERE Customer.name=cname;
            END IF;
		END IF;
        RETURN cname;
    EXCEPTION
        WHEN AccountExistException THEN
            dbms_output.put_line('Customer still have account');
            RETURN NULL;
        WHEN InvalidNameException THEN
            dbms_output.put_line('Invalid Customer');
            RETURN NULL;
    END;
    
    FUNCTION open_account(cname Customer.name%type, addr Branch.address%type, amount account.balance%type) RETURN account.a#%type IS
    InvalidAmountException EXCEPTION;
	InvalidCustomerException EXCEPTION;
	InvalidBranchException EXCEPTION;
	bno Branch.b#%type;
	cno Customer.c#%type;
	counter integer;
	counter1 integer;
	counter2 integer;
    ano number;
    temp number;
	BEGIN
		IF amount<0 THEN
			RAISE InvalidAmountException;
		END IF;
		SELECT COUNT(*) INTO counter
		FROM Customer
		WHERE Customer.name = cname;
		IF counter<1 THEN
			RAISE InvalidCustomerException;
		END IF;
		SELECT COUNT(*) INTO counter1
		FROM Branch
		WHERE Branch.address = addr;
		IF counter1<1 THEN
			RAISE InvalidBranchException;
		END IF;
		SELECT b# INTO bno
		FROM Branch
		WHERE Branch.address = addr;
		SELECT c# INTO cno
		FROM Customer
		WHERE Customer.name = cname;
		SELECT COUNT(*) INTO counter2
		FROM Account
		WHERE Account.b# = bno;
		IF counter2<1 THEN
			INSERT INTO Account(b#,a#,c#,balance) VALUES(bno,'0000',cno,amount);
            RETURN '0000';
		ELSE
            select TO_NUMBER(min(a.a#)) into temp
            from Account a
            where a.b#=bno;
            IF temp>1 Then
                INSERT INTO Account(b#,a#,c#,balance) VALUES(bno,'0000',cno,amount);
                RETURN '0000';
            END IF;
            select TO_NUMBER(min(a.a#))+1 into ano
            from Account a
            where a.b#=bno AND not exists (select 1 from Account a2 where To_Number(a2.b#||a2.a#) = To_Number(a.b#||a.a#)+1);
			INSERT INTO Account(b#,a#,c#,balance) VALUES(bno,LPAD(TO_CHAR(ano),4,'0'),cno,amount);
		END IF;
        RETURN LPAD(TO_CHAR(ano),4,'0');
		EXCEPTION
			WHEN InvalidAmountException THEN
				dbms_output.put_line('Invalid amount');
                RETURN NULL;
			WHEN InvalidCustomerException THEN
				dbms_output.put_line('Invalid customer');
                RETURN NULL;
			WHEN InvalidBranchException THEN
				dbms_output.put_line('Invalid branch');
                RETURN NULL;
    END;
    
    FUNCTION close_account(acct CHAR) RETURN CHAR IS
    BalanceNotZeroException EXCEPTION;
	InvalidAccountException EXCEPTION;
	amount Account.balance%type;
	counter integer;
	BEGIN
		SELECT COUNT(*) INTO counter
		FROM Account
		WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		IF counter<1 THEN
			RAISE InvalidAccountException;
		END IF;
		SELECT balance INTO amount
		FROM Account
		WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		IF amount>0 THEN
			RAISE BalanceNotZeroException;
		ELSE
			DELETE FROM Account 
            WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		END IF;
        RETURN acct;
		EXCEPTION
			WHEN BalanceNotZeroException THEN
				dbms_output.put_line('Balance greater than 0');
                RETURN NULL;
			WHEN InvalidAccountException THEN
				dbms_output.put_line('Invalid account');
                RETURN NULL;
    END;
    
    FUNCTION withdraw(acct CHAR, amount Account.balance%type) RETURN CHAR IS
    InsufficientFundException EXCEPTION;
	InvalidAccountException EXCEPTION;
	curr Account.balance%type;
	counter integer;
    BEGIN
		SELECT COUNT(*) INTO counter
		FROM Account
		WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		IF counter<1 THEN
			RAISE InvalidAccountException;
		END IF;
		SELECT balance INTO curr
		FROM Account
		WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		IF amount>curr THEN
			RAISE InsufficientFundException;
		ELSE
			UPDATE Account
            SET balance = curr-amount
            WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		END IF;
        RETURN acct;
		EXCEPTION
			WHEN InsufficientFundException THEN
				dbms_output.put_line('Insufficient fund');
                RETURN NULL;
			WHEN InvalidAccountException THEN
				dbms_output.put_line('Invalid account');
                RETURN NULL;
    END;
    
    FUNCTION deposit(acct CHAR, amount Account.balance%type) RETURN CHAR IS
    InvalidAmountException EXCEPTION;
	InvalidAccountException EXCEPTION;
	curr Account.balance%type;
	counter integer;
    BEGIN
		SELECT COUNT(*) INTO counter
		FROM Account
		WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		IF counter<1 THEN
			RAISE InvalidAccountException;
		END IF;
		SELECT balance INTO curr
		FROM Account
		WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		IF curr<0 THEN
			RAISE InvalidAmountException;
		ELSE
			UPDATE Account
            SET balance = curr+amount
            WHERE Account.b# = SUBSTR(acct,1,3) and Account.a# = SUBSTR(acct,4);
		END IF;
        RETURN acct;
		EXCEPTION
			WHEN InvalidAmountException THEN
				dbms_output.put_line('Invalid amount');
                RETURN NULL;
			WHEN InvalidAccountException THEN
				dbms_output.put_line('Invalid account');
                RETURN NULL;
    END;
    
    FUNCTION transfer(acct1 CHAR, acct2 CHAR, amount Account.balance%type) RETURN CHAR IS
    InvalidAmountException EXCEPTION;
	InvalidAccountException EXCEPTION;
    InsufficientFundException EXCEPTION;
	curr1 Account.balance%type;
    curr2 Account.balance%type;
	counter integer;
    counter1 integer;
    BEGIN
		SELECT COUNT(*) INTO counter
		FROM Account
		WHERE Account.b# = SUBSTR(acct1,1,3) and Account.a# = SUBSTR(acct1,4);
		IF counter<1 THEN
			RAISE InvalidAccountException;
		END IF;
        SELECT COUNT(*) INTO counter1
		FROM Account
		WHERE Account.b# = SUBSTR(acct2,1,3) and Account.a# = SUBSTR(acct2,4);
		IF counter1<1 THEN
			RAISE InvalidAccountException;
		END IF;
		SELECT balance INTO curr1
		FROM Account
		WHERE Account.b# = SUBSTR(acct1,1,3) and Account.a# = SUBSTR(acct1,4);
		IF curr1<amount THEN
			RAISE InsufficientFundException;
		END IF;
        SELECT balance INTO curr2
		FROM Account
		WHERE Account.b# = SUBSTR(acct2,1,3) and Account.a# = SUBSTR(acct2,4);
		IF curr2<0 THEN
			RAISE InvalidAmountException;
		END IF;
        UPDATE Account
        SET balance = curr1-amount
        WHERE Account.b# = SUBSTR(acct1,1,3) and Account.a# = SUBSTR(acct1,4);
        UPDATE Account
        SET balance = curr2+amount
        WHERE Account.b# = SUBSTR(acct2,1,3) and Account.a# = SUBSTR(acct2,4);
        RETURN acct1;
		EXCEPTION
			WHEN InvalidAmountException THEN
				dbms_output.put_line('Invalid amount');
                RETURN NULL;
			WHEN InvalidAccountException THEN
				dbms_output.put_line('Invalid account');
                RETURN NULL;
            WHEN InsufficientFundException THEN
				dbms_output.put_line('Insufficient fund');
                RETURN NULL;
    END;
    
    FUNCTION show_branch(addr Branch.address%type) RETURN DBMSOUTPUT_LINESARRAY IS 
    counter integer := 0;
    counter1 integer;
    data DBMSOUTPUT_LINESARRAY;
    total number;
    InvalidBranchException EXCEPTION;
    BEGIN
        SELECT COUNT(*) INTO counter1
		FROM Branch
		WHERE Branch.address = addr;
		IF counter1<1 THEN
			RAISE InvalidBranchException;
		END IF;
        DBMS_OUTPUT.ENABLE(1000000);
        dbms_output.put_line('+==========+===========+=========+');
        dbms_output.put_line('| Account# | Customer# | Balance |');
        dbms_output.put_line('+----------+-----------+---------+');
        counter := counter + 3;
        SELECT SUM(Account.Balance) INTO total
        FROM Branch, Account
        WHERE Branch.B# = Account.B# AND Branch.Address = addr;
        FOR o IN (SELECT Account.B#||Account.A# AS Account#, Account.C#, Account.Balance
                  FROM Branch, Account 
                  WHERE Branch.B# = Account.B# AND Branch.Address = addr
                  ORDER BY Account.C#)
        LOOP
            dbms_output.put_line('|'||LPAD(o.Account#,10,' ')||'|'||LPAD(o.c#,11,' ')||'|'||LPAD(TO_CHAR(o.Balance),9,' ')||'|');
            dbms_output.put_line('+----------+-----------+---------+');
            counter := counter + 2;
        END LOOP;
        dbms_output.put_line('|        Total Balance ='||LPAD(TO_CHAR(total),9,' ')||'|');
        dbms_output.put_line('+==========+===========+=========+');
        counter := counter + 2;
        DBMS_OUTPUT.GET_LINES(data, counter);
        DBMS_OUTPUT.DISABLE();
        RETURN data;
        EXCEPTION
			WHEN InvalidBranchException THEN
				dbms_output.put_line('Invalid branch');
                RETURN NULL;
    END;
    
    FUNCTION show_all_branches RETURN DBMSOUTPUT_LINESARRAY IS
    counter integer := 0;
    data DBMSOUTPUT_LINESARRAY;
    temp DBMSOUTPUT_LINESARRAY;
    total number := 0;
    BEGIN
        DBMS_OUTPUT.ENABLE(1000000);
        FOR o IN (SELECT B#, Address 
                  FROM Branch 
                  ORDER BY B#)
        LOOP
            dbms_output.put_line('================+=================');
            dbms_output.put_line('|    Branch#    |     Address    |');
            dbms_output.put_line('----------------+-----------------');
            dbms_output.put_line('|'||LPAD(o.b#,15,' ')||'|'||LPAD(o.Address,16,' ')||'|');
            dbms_output.put_line('+----------+-----------+---------+');
            dbms_output.put_line('| Account# | Customer# | Balance |');
            dbms_output.put_line('+----------+-----------+---------+');
            counter := counter + 7;
            SELECT SUM(Account.Balance) INTO total
            FROM Branch, Account
            WHERE Branch.B# = Account.B# AND Branch.Address = o.Address;
            FOR o1 IN (SELECT Account.B#||Account.A# AS Account#, Account.C#, Account.Balance
                      FROM Branch, Account 
                      WHERE Branch.B# = Account.B# AND Branch.Address = o.Address
                      ORDER BY Account.C#)
            LOOP
                dbms_output.put_line('|'||LPAD(o1.Account#,10,' ')||'|'||LPAD(o1.c#,11,' ')||'|'||LPAD(TO_CHAR(o1.Balance),9,' ')||'|');
                dbms_output.put_line('+----------+-----------+---------+');
                counter := counter + 2;
            END LOOP;
            dbms_output.put_line('|        Total Balance ='||LPAD(TO_CHAR(total),9,' ')||'|');
            dbms_output.put_line('+==========+===========+=========+');
            counter := counter + 2;
        END LOOP;
        DBMS_OUTPUT.GET_LINES(data, counter);
        DBMS_OUTPUT.DISABLE();
        RETURN data;
    END;
    
    FUNCTION show_customer(cname Customer.name%type) RETURN DBMSOUTPUT_LINESARRAY IS
    counter integer := 0;
    counter1 integer;
    data DBMSOUTPUT_LINESARRAY;
    total number := 0;
    InvalidCustomerException EXCEPTION;
    BEGIN
        SELECT COUNT(*) INTO counter1
		FROM Customer
		WHERE Customer.name = cname;
		IF counter1<1 THEN
			RAISE InvalidCustomerException;
		END IF;
        DBMS_OUTPUT.ENABLE(1000000);
        dbms_output.put_line('+===========+==========+==========+=========+');
        dbms_output.put_line('| Customer# |   Name   | Account# | Balance |');
        dbms_output.put_line('+-----------+----------+----------+---------+');
        counter := counter + 3;
        SELECT SUM(Account.Balance) INTO total
        FROM Customer, Account
        WHERE Customer.C# = Account.C# AND Customer.Name = cname;
        FOR o IN (SELECT Customer.C#, Customer.Name, Account.B#||Account.A# AS Account#, Account.Balance
                  FROM Customer, Account 
                  WHERE Customer.C# = Account.C# AND Customer.Name = cname)
        LOOP
            dbms_output.put_line('|'||LPAD(o.C#,11,' ')||'|'||LPAD(o.Name,10,' ')||'|'||LPAD(o.Account#,10,' ')||'|'||LPAD(TO_CHAR(o.Balance),9,' ')||'|');
            dbms_output.put_line('+-----------+----------+----------+---------+');
            counter := counter + 2;
        END LOOP;
        dbms_output.put_line('|                   Total Balance ='||LPAD(TO_CHAR(total),9,' ')||'|');
        dbms_output.put_line('+===========+==========+==========+=========+');
        counter := counter + 2;
        DBMS_OUTPUT.GET_LINES(data, counter);
        DBMS_OUTPUT.DISABLE();
        RETURN data;
        EXCEPTION
			WHEN InvalidCustomerException THEN
				dbms_output.put_line('Invalid customer');
                RETURN NULL;
    END;
    
END bank;
/
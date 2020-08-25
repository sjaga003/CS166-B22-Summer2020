/*Customer ID Trigger*/
DROP SEQUENCE IF EXISTS customer_id;
DROP TRIGGER IF EXISTS set_customer_id on customer;


CREATE SEQUENCE customer_id;
SELECT setval('customer_id', (SELECT MAX(id) FROM customer));

CREATE OR REPLACE FUNCTION set_customer_id()
RETURNS "trigger" as
$cust_id$
BEGIN
	NEW.id:=nextval('customer_id');
	RETURN NEW;
END
$cust_id$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER CustomerIdTrigger
BEFORE INSERT
ON customer FOR EACH ROW
EXECUTE PROCEDURE set_customer_id();

/*Mechanic ID Trigger*/
DROP SEQUENCE IF EXISTS mechanic_id;
DROP TRIGGER IF EXISTS set_mech_id on mechanic;

CREATE SEQUENCE mechanic_id;
SELECT setval('mechanic_id', (SELECT MAX(id) FROM mechanic));

CREATE OR REPLACE FUNCTION set_mech_id()
RETURNS "trigger" as
$mech_id$
BEGIN
	NEW.id:=nextval('mechanic_id');
	RETURN NEW;
END
$mech_id$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER MechanicIdTrigger
BEFORE INSERT
ON mechanic FOR EACH ROW
EXECUTE PROCEDURE set_mech_id();


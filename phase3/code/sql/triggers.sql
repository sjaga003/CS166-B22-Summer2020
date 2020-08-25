DROP SEQUENCE IF EXISTS cust_id_seq;
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

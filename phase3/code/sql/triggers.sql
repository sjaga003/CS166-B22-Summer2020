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

/*OWNS table trigger*/
DROP TRIGGER IF EXISTS set_owns_id on owns;
DROP SEQUENCE IF EXISTS owns_id;

CREATE SEQUENCE owns_id;
SELECT setval('owns_id', (SELECT MAX(ownership_id) FROM owns));

CREATE OR REPLACE FUNCTION set_owns_id()
RETURNS "trigger" as
$own_id$
BEGIN
	NEW.ownership_id:=nextval('owns_id');
	RETURN NEW;
END
$own_id$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER OwnsIdTrigger
BEFORE INSERT
ON owns FOR EACH ROW
EXECUTE PROCEDURE set_owns_id();

/* Service Request rid trigger */
DROP TRIGGER IF EXISTS set_serv_rid on service_request;
DROP SEQUENCE IF EXISTS serv_rid;

CREATE SEQUENCE serv_rid;
SELECT setval('serv_rid', (SELECT MAX(rid) FROM service_request));

CREATE OR REPLACE FUNCTION set_serv_rid()
RETURNS "trigger" as
$serv_id$
BEGIN
	NEW.rid := nextval('serv_rid');
	RETURN NEW;
END
$serv_id$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER set_serv_rid
BEFORE INSERT
ON service_request FOR EACH ROW
EXECUTE PROCEDURE set_serv_rid();

/* Closed Request rid trigger */
DROP TRIGGER IF EXISTS set_close_wid on closed_request;
DROP SEQUENCE IF EXISTS close_wid;

CREATE SEQUENCE close_wid;
SELECT setval('close_wid', (SELECT MAX(wid) FROM closed_request));

CREATE OR REPLACE FUNCTION set_close_wid()
RETURNS "trigger" as
$clos_wid$
BEGIN
	NEW.wid := nextval('close_wid');
	RETURN NEW;
END
$clos_wid$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER set_close_wid
BEFORE INSERT
ON closed_request FOR EACH ROW
EXECUTE PROCEDURE set_close_wid();

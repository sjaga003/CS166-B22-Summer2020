CREATE INDEX customer_index
ON Customer
USING BTREE
(id);

CREATE INDEX mechanic_index
ON Mechanic
USING BTREE
(id);

CREATE INDEX car_index
ON Car
USING BTREE
(vin);

CREATE INDEX owns_index
ON Owns
USING BTREE
(ownership_id);

CREATE INDEX service_request_index
ON Service_Request
USING BTREE
(rid);

CREATE INDEX closed_request_index
ON Closed_Request
USING BTREE
(wid); 

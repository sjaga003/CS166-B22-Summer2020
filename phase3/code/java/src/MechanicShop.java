/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql, -1); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1
		try{
			String fname = "";
			String lname = "";
			String phone = "";
			String address = "";
			boolean isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter First Name: ");
         			fname = in.readLine();
				if(fname.length() > 0 && fname.length() <= 32) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Last Name: ");
         			lname = in.readLine();
				if(lname.length() > 0 && lname.length() <= 32) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Phone Number: ");
         			phone = in.readLine();
				if(phone.length() > 0 && phone.length() <= 13) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Address: ");
         			address = in.readLine();
				if(address.length() > 0 && address.length() <= 256) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			
         		String query = "INSERT INTO customer(fname, lname, phone, address) VALUES ";
			String input = "(\'" + fname + "\', \'" + lname + "\', \'" + phone + "\', \'" + address + "\')";
         		query += input;

         		esql.executeUpdate(query);
			esql.executeQueryAndPrintResult("SELECT * FROM customer");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		try{
			String fname = "";
			String lname = "";
			String experience = "";
			boolean isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter First Name: ");
         			fname = in.readLine();
				if(fname.length() > 0 && fname.length() <= 32) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Last Name: ");
         			lname = in.readLine();
				if(lname.length() > 0 && lname.length() <= 32) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Years of Experience: ");
         			experience = in.readLine();
				if(experience.length() > 0) {
					try {
						int value = Integer.parseInt(experience);
						if(value >= 0 && value < 100) {
							isValid = true;
						}
						else {
							System.out.println("Invalid input, please try again");
						}
					} catch (NumberFormatException e) {
						System.out.println("Invalid input, please try again");
					}
					
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}	
			isValid = false;

         		String query = "INSERT INTO mechanic(fname, lname, experience) VALUES ";
			String input = "(\'" + fname + "\', \'" + lname + "\', " + experience + ")";
         		query += input;

         		esql.executeUpdate(query);
			esql.executeQueryAndPrintResult("SELECT * FROM mechanic");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}
	
	public static void AddCar(MechanicShop esql, int recentId){//3
		try{
			String vin = "";
			String make = "";
			String model = "";
			String year = "";
			int inDatabase = 0;
			boolean isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Vehicle Identification Number: ");
         			vin = in.readLine();
				if(vin.length() > 0 && vin.length() <= 16) {
					String check = "SELECT * FROM car WHERE vin=\'" + vin + "\'";
					inDatabase = esql.executeQuery(check);
					if(inDatabase == 0) {
						isValid = true;
					}
					else {
						System.out.println("This ID is already in the database, please try again");
					}
				}
				else {
 					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Make: ");
         			make = in.readLine();
				if(make.length() > 0 && make.length() <= 32) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Model: ");
         			model = in.readLine();
				if(model.length() > 0 && model.length() <= 32) {
					isValid = true;
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			while(!isValid) {
         			System.out.print("\tEnter Year: ");
         			year = in.readLine();
				if(year.length() > 0) {
					try {
						int value = Integer.parseInt(year);
						if(value >= 1970) {
							isValid = true;
						}
						else {
							System.out.println("Invalid input, please try again");
						}
					} catch (NumberFormatException e) {
						System.out.println("Invalid input, please try again");
					}
					
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}	
			isValid = false;
         		String query = "INSERT INTO car(vin, make, model, year) VALUES ";
			String input = "(\'" + vin + "\', \'" + make + "\', \'" + model + "\', \'" + year + "\')";
         		query += input;

         		esql.executeUpdate(query);
			
			if(recentId > -1) {//Coming from ServiceRequest
				String ownsQuery = "INSERT INTO owns (customer_id, car_vin) VALUES (\'" + recentId + "\', \'" + vin + "\')";
				esql.executeUpdate(ownsQuery);
			}
			else {//Coming from menu, need to get customer information only to existing customer
				List<List<String>> checkResult = esql.executeQueryAndReturnResult("SELECT * FROM customer WHERE lname = \'alskdjfklfjafkldjadf\'");

				System.out.println("Please link this car to a customer");

				while(checkResult.size() == 0) {
					System.out.print("\tEnter customer's last name: ");
					String lname = in.readLine();
					String queryLname = "SELECT * FROM customer WHERE lname = \'" + lname + "\'";
					checkResult = esql.executeQueryAndReturnResult(queryLname);
					
					if(checkResult.size() == 0) {
						System.out.println("Last name not found, try again");
					}
				}
				for(int i = 0; i < checkResult.size(); i++) {
					String listString = i + ". " + checkResult.get(i).get(1).replaceAll("\\s", "") + " " + checkResult.get(i).get(2).replaceAll("\\s", "") + ", Phone#:" + checkResult.get(i).get(3).replaceAll("\\s", "") + ", Address:" + checkResult.get(i).get(4).replaceAll("\\s++$", "");
				System.out.println(listString);
				}
			
				int listChoice = -1;	
				boolean listValid = false; 
				while(!listValid) {
					try {
						System.out.print("\tSelect the customer number: ");
						listChoice = Integer.parseInt(in.readLine());
						if(listChoice >= 0 && listChoice < checkResult.size()) {
							listValid = true;
						}
						else {
							System.out.println("Invalid option selected, please try again");
						}
						} catch(Exception e) {
							System.out.println("Invalid option selected, please try again");
						}
				}
				System.out.println(listChoice);
				int cid = listChoice;
				String ownsQuery = "INSERT INTO owns (customer_id, car_vin) VALUES (\'" + checkResult.get(cid).get(0) + "\', \'" + vin + "\')";
				
				esql.executeUpdate(ownsQuery);	

			}
					
			
			//esql.executeQueryAndPrintResult("SELECT * FROM car");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		try {
			boolean isValid = false;
			String lname = "";
			int listChoice = -1;
			while(!isValid) {
         			System.out.print("\tEnter Customer's Last Name: ");
				lname = in.readLine();
				if(lname.length() > 0 && lname.length() <= 32) {
					isValid = true;			
				}
				else {
					System.out.println("Invalid input, please try again");
				}
			}
			isValid = false;
			String checkLname = "SELECT * FROM customer WHERE customer.lname=\'" + lname + "\'";
			List<List<String>> checkResult = esql.executeQueryAndReturnResult(checkLname);
			if(checkResult.size() > 0) { //Multiple returns for lastname
				for(int i = 0; i < checkResult.size(); i++) {
					String listString = i + ". " + checkResult.get(i).get(1).replaceAll("\\s", "") + " " + checkResult.get(i).get(2).replaceAll("\\s", "") + ", Phone#:" + checkResult.get(i).get(3).replaceAll("\\s", "") + ", Address:" + checkResult.get(i).get(4).replaceAll("\\s++$", "");			
					System.out.println(listString);
				}
				boolean listValid = false; 
				while(!listValid) {
					try {
						System.out.print("\tSelect the customer number: ");
						listChoice = Integer.parseInt(in.readLine());
						if(listChoice >= 0 && listChoice < checkResult.size()) {
							listValid = true;
						}
						else {
							System.out.println("Invalid option selected, please try again");
						}
					} catch(Exception e) {
						System.out.println("Invalid option selected, please try again");
					}
				}
				System.out.println(listChoice);
					
			}
			else {//No lname found, offer to make a new customer
				while(!isValid) {
					System.out.println("Did not find any customers with that last name");
					System.out.println("Add a new customer? (Y/N)");
					String cont = in.readLine();
					if(cont.equals("Y")) {
						AddCustomer(esql);
						int mostRecent = esql.getCurrSeqVal("customer_id");
						String newCustomerQuery = "SELECT * FROM customer WHERE Customer.id=" + mostRecent;
						List<List<String>> newResult = esql.executeQueryAndReturnResult(newCustomerQuery);
						checkResult.add(newResult.get(0));
						isValid = true;
						listChoice = 0;	
					}
					else if (cont.equals("N")) {
						System.out.println("No new customer added, cancelling service request");
						return;
					}	
				}
				isValid = false;
			}
			
			
			String vin = "";
			
			String carsOwnedQuery = "SELECT C.vin, C.make, C.model, C.year FROM Car C, Owns O WHERE O.customer_id = " + checkResult.get(listChoice).get(0) + " AND O.car_vin = C.vin";
			List<List<String>> carsOwnedResult = esql.executeQueryAndReturnResult(carsOwnedQuery);

			boolean createNewCar = false;
			if(carsOwnedResult.size() > 0) {
				for(int i = 0; i < carsOwnedResult.size(); i++) {
					String printString = i + ". " + carsOwnedResult.get(i).get(0) + ", "  +carsOwnedResult.get(i).get(1) +  ", "  + carsOwnedResult.get(i).get(2) +  ", "  + carsOwnedResult.get(i).get(3);
					System.out.println(printString);
						
				}
			
				System.out.println("Select a car from this list? [Yes/No] ");
				String cont = in.readLine();
				if(cont.equals("Yes")) {
					boolean listValid = false;
					while(!listValid) {
						try {
							System.out.print("\tSelect the car number: ");
							
							int carChoice = -1;
							carChoice = Integer.parseInt(in.readLine());
							if(carChoice >= 0 && carChoice < carsOwnedResult.size()) {
								listValid = true;
								vin = carsOwnedResult.get(carChoice).get(0);
							}
							else {
								System.out.println("Invalid option selected, please try again");
							}
						 } catch(Exception e) {
							System.out.println("Invalid option selected, please try again");
						}
					}
				//	System.out.println(carChoice);

				
				}
				else if(cont.equals("No")) {
					createNewCar = true;	
				}
			
			}
			else {
				System.out.println("\t" + checkResult.get(listChoice).get(1).split("\\s+")[0] + " is not registered to a car currently");
				createNewCar = true;
			}
			
			if(createNewCar == true) {
				System.out.println("\tAdding car for " + checkResult.get(listChoice).get(1).split("\\s+")[0]);
				AddCar(esql, Integer.parseInt(checkResult.get(listChoice).get(0)));
			}
			

			int odometer = -1;
			isValid = false;
			while(!isValid) {
				System.out.print("\tEnter the odometer reading of the car: ");
				try {
					odometer = Integer.parseInt(in.readLine());
                                	if(odometer >= 0) {
                                		isValid = true;
                                	}
                                	else {
                                		System.out.println("Invalid option selected, please try again");
                                	}
                                } catch(Exception e) {
                                	System.out.println("Invalid option selected, please try again");
                        	}
	
			}
			isValid = false;
			
			String complaint = "";
			while(!isValid) {
				System.out.print("\tEnter customer complaint: ");
				complaint = in.readLine();
				if(complaint.length() != 0) {
					isValid = true;
				}
				else {
					System.out.println("Invalid customer complaint");
				}
			}
			
			LocalDateTime dtNow = LocalDateTime.now();
			DateTimeFormatter dt = DateTimeFormatter.ofPattern("MM/dd/yyy HH:mm");
			
			List<List<String>> idTab = esql.executeQueryAndReturnResult("SELECT MAX(ownership_id) FROM owns");
			int ownsId = Integer.parseInt(idTab.get(0).get(0));
			String insertOwnsQuery = "SELECT O.car_vin FROM owns O WHERE O.ownership_id = " + ownsId;
			vin = esql.executeQueryAndReturnResult(insertOwnsQuery).get(0).get(0);
			System.out.println(checkResult.get(listChoice));
				
			String insertSrQuery = "INSERT INTO Service_Request(customer_id, car_vin, date, odometer, complain) VALUES (" + checkResult.get(listChoice).get(0) + ", \'" + vin + "\', \'" + dt.format(dtNow) + "\', " + odometer + ", \'" + complaint + "\')";
			esql.executeUpdate(insertSrQuery);
			esql.executeQueryAndPrintResult("SELECT * FROM Service_Request");	
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}	
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		try {
			boolean isValid = false;
			int mechId = -1;
			int srId = -1;
			int bill = -1;
		
			String getSrQuery = "SELECT * FROM Service_Request S WHERE S.rid NOT IN (SELECT C.rid FROM Closed_Request C)";
			List<List<String>> srResult = esql.executeQueryAndReturnResult(getSrQuery);
			if(srResult.isEmpty()) {
				System.out.println("All Service Requests are closed");
				return;
			}
		
			while(!isValid) {
				try {
				System.out.print("\tEnter mechanic id: ");
				mechId = Integer.parseInt(in.readLine());
				if(mechId <= 0) {
					System.out.println("Invalid mechanic id");
				}
				else {
					String checkMechIdQuery = "SELECT M.id FROM Mechanic M WHERE M.id = " + mechId;
					List<List<String>> checkMechIdResult = esql.executeQueryAndReturnResult(checkMechIdQuery);
					Integer.parseInt(checkMechIdResult.get(0).get(0));
					isValid = true;	
				}
				} catch(Exception e) {
					System.out.println("Invalid mechanic Id");
				}
			}
			isValid = false;
			int srChoice = -1;
			for(int i = 0; i < srResult.size(); i++) {
				System.out.println(i + ". WID:" + srResult.get(i).get(0) + ", RID:" + srResult.get(i).get(1) + ", MID: " + srResult.get(i).get(2) + ", " + srResult.get(i).get(3) + ", " + srResult.get(i).get(4) + ", " + srResult.get(i).get(5));
			}
			while(!isValid) {
                                try {
                                	System.out.print("\tSelect service request option: ");
                                	srChoice = Integer.parseInt(in.readLine());
                                	if(srChoice < 0 || srChoice >= srResult.size()) {
                                        	System.out.println("Invalid service request option");
                                	}
                                	else {
                                        	isValid = true;
                                	}
                                } catch(Exception e) {
                                        System.out.println("Invalid service request option");
                                }
			}
			isValid = false;
						
			srId = srChoice;
			DateTimeFormatter dt = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
			LocalDateTime dtNow = LocalDateTime.now();
			
			System.out.print("\tAny additional comments: ");
			String comment = in.readLine();
			
			while(!isValid) {
				try {
                                        System.out.print("\tEnter total payment due: ");
                                        bill = Integer.parseInt(in.readLine());
                                        if(bill <= 0) {
                                                System.out.println("Invalid service request option");
                                        }
                                        else {
                                                isValid = true;
                                        }
                                } catch(Exception e) {
                                        System.out.println("Invalid service request option");
                                }

			}
			
			String crQuery = "INSERT INTO Closed_Request(rid, mid, date, comment, bill) VALUES (" + srResult.get(srId).get(0) + ", " + mechId + ", \'" + dt.format(dtNow) + "\', \'" + comment + "\', " + bill + ")";
			esql.executeUpdate(crQuery);
			esql.executeQueryAndPrintResult("SELECT * FROM Closed_Request");
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try {
			String query = "SELECT CR.date, CR.comment, CR.bill, CU.fname, CU.lname FROM Closed_Request CR, Customer CU, Service_Request SR WHERE CR.bill < 100 AND SR.rid = CR.rid AND SR.customer_id = CU.id";
			esql.executeQueryAndPrintResult(query);	
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try {
			String query = "SELECT C.fname, C.lname FROM Customer C, Owns O  WHERE C.id = O.customer_id GROUP BY C.id HAVING COUNT(*) > 20";
			esql.executeQueryAndPrintResult(query);	
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try {
			String query = "SELECT C.make, C.model, C.year, SR.odometer FROM Car C, Service_Request SR WHERE C.vin = SR.car_vin AND C.year < 1995 AND SR.odometer < 50000";
			esql.executeQueryAndPrintResult(query);	
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		try {
			System.out.print("\tHow many cars do you want to see?: ");
			int lim = Integer.parseInt(in.readLine());
			String query = "SELECT C.make, C.model, C.year, SR.car_vin, COUNT(SR.car_vin) as requestsNum FROM Car C, Service_Request SR WHERE C.vin = SR.car_vin GROUP BY C.make, C.model, C.year, SR.car_vin ORDER BY requestsNum DESC LIMIT " + lim;
			esql.executeQueryAndPrintResult(query);	
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		try {
			String query = "SELECT C.fname, C.lname, C.id, SUM(CR.bill) total FROM Customer C, Service_Request SR, Closed_Request CR WHERE C.id = SR.customer_id AND CR.rid = SR.rid GROUP BY C.fname, C.lname, C.id ORDER BY total DESC limit 5";
			esql.executeQueryAndPrintResult(query);
			//esql.executeQueryAndPrintResult("EXPLAIN ANALYZE SELECT * FROM CUSTOMER");	
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
}

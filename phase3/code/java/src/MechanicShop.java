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
					case 3: AddCar(esql); break;
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
         		String query = "INSERT INTO customer(fname, lname, phone, address) VALUES ";
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
         		String query = "INSERT INTO mechanic(fname, lname, experience) VALUES ";
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
         		String query = "INSERT INTO car(vin, make, model, year) VALUES ";
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
			String input = "(\'" + vin + "\', \'" + make + "\', \'" + model + "\', \'" + year + "\')";
         		query += input;

         		esql.executeUpdate(query);
			
			if(recentId >= 0) {
				String ownsQuery = "INSERT INTO owns (customer_id, car_vin) VALUES (\'" + recentId + "\', \'" + vin + "\')";
				esql.executeUpdate(ownsQuery);
			}
			else {
				System.out.print("\tIs this an existing customer's car? [Y/N] ";
				String cont = in.readLine();
                                if(cont.equals("Y")) {
					System.out.print("\tEnter customer's last name: ");
					String lname = in.readLine();
					String queryLname = "SELECT * FROM customer WHERE lname = \'" + lname + "\'";
				}
				else {
				
				}
					

			}
					
			
			esql.executeQueryAndPrintResult("SELECT * FROM car");
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
			}
			

			System.out.println(listChoice);		
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}	
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		
	}
	
}

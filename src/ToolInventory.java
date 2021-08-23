import java.sql.Connection;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.cj.jdbc.MysqlDataSource;

/* This class provides methods to connect to and access the inventory table on the tool rental MySQL
 * database I configured for this project. In this table, tool code is unique, therefore a given tool
 * code should either return no results or a single row.
 * The SQL table has 5 columns: Tool_Code, Tool_Type, Brand, Rented, and Contract.
 * The Contract field does not print unless you request it for a specific tool code in the
 * getContractByCode method. This is because contracts are typically reviewed one at a time and an
 * at-a-glance view of multiple contracts at once is more likely to be distracting than helpful.
 */

public class ToolInventory {

	private final String user = "u566381527_demo";
	private final String password = "Testingdemo1";
	private final String host = "sql531.main-hosting.eu";
	private final String dbName = "u566381527_toolrental";
	private final String tableName = "toolcodes";
	MysqlDataSource dbSource;
	Connection conn;
	
	ToolInventory() //initialize the MysqlDataSource with login info
	{
		dbSource = new MysqlDataSource();
		dbSource.setUser(user);
		dbSource.setPassword(password);
		dbSource.setServerName(host);
		dbSource.setDatabaseName(dbName);
	}
	
	int initConnection(){ //initiate the DB connection and return a status indicating the success
		int status = -1; //the return value. -1 if connection failed, 0 if succeeded
		try {
			conn = dbSource.getConnection();
			status = 0;
		}
		catch(Exception e){
			System.out.println("Error: Could not connect to the tool rental database.");
		}
		return status;
		
	}
	
	int closeConnection() { //close the DB connection and return a status indicating the success
		int status = -1; //the return value. -1 if connection failed, 0 if succeeded
		try {
			conn.close();
			status = 0;
		}
		catch(Exception e) {
			System.out.println("Error: Could not close the connection to the tool rental database.");
		}
		return status;
	} 
	
	void printAll() { //print all items in database
		try {
			int initStatus = initConnection();
			if (initStatus == 0) {
				Statement stmt = conn.createStatement(); //send query to DB to get all rows
				ResultSet rs = stmt.executeQuery("select * from " + tableName);
				//check to see if we got back any results
				if (!rs.isBeforeFirst() ) {    
				    System.out.println("No results found in the database."); 
				}
				else {//print table header
					System.out.format("%20s%20s%20s%20s", "TOOL_CODE", "TOOL_TYPE", "BRAND", "RENTED");
					System.out.println();
					while (rs.next()) 
					{   //print result table
						System.out.format("%20s%20s%20s%20s", rs.getString("Tool_Code"), rs.getString("Tool_Type"), rs.getString("Brand"), rs.getString("Rented"));
						System.out.println();
					}
				}
				rs.close();
				stmt.close();
				closeConnection();
				}
			}
		catch(Exception e) {
			System.out.println("Error: Query to retrieve all rows from database failed.");
			closeConnection();
		}
	}
	
	Vector<String> getFilteredResults(String field, String value, boolean doPrint) { //print results of filtered field-value query if print true, else return results as list
		Vector<String> resultList = new Vector<String>();
		
		//for tool_code and tool_type, we will also accept code/tool code and type/tool type
		if (field.equalsIgnoreCase("type") || field.equalsIgnoreCase("tooltype") || field.equalsIgnoreCase("tool type"))
			field = "TOOL_TYPE";
		else if (field.equalsIgnoreCase("code") || field.equalsIgnoreCase("toolcode") || field.equalsIgnoreCase("tool code"))
			field = "TOOL_CODE";
		
		try {
			int initStatus = initConnection();
			if (initStatus == 0) {
				Statement stmt = conn.createStatement(); //send filtered query to DB
				ResultSet rs = stmt.executeQuery("select * from " + tableName + " where " + field + "= '" + value + "'");
				//check to see if we got back any results
				if (!rs.isBeforeFirst() && doPrint) {    
				    System.out.println("No results found in the database."); 
				} 
				else {
					if (doPrint)
					{	
						System.out.format("%20s%20s%20s%20s", "TOOL_CODE", "TOOL_TYPE", "BRAND", "RENTED"); //print table header
						System.out.println();
						while (rs.next()) 
							{   //print result table
								System.out.format("%20s%20s%20s%20s", rs.getString("Tool_Code"), rs.getString("Tool_Type"), rs.getString("Brand"), rs.getString("Rented"));
								System.out.println();
							}
					}
					else {
						while (rs.next()) 
						{ 
							//add results to vector
							resultList.add(rs.getString("Tool_Code"));
							resultList.add(rs.getString("Tool_Type"));
							resultList.add(rs.getString("Brand"));
							resultList.add(rs.getString("Rented"));
						}
					}
				rs.close();
				stmt.close();
				closeConnection();
				}
			}
		}
		catch(Exception e) {
			System.out.println("Error: Filtered query to database failed. Please make sure the entered field is TOOL CODE, TOOL TYPE, BRAND, or RENTED.");
			closeConnection();
		}
		return resultList;
	}
	
	void checkOutTool(String code, String contract) { //updates DB with completed rental contract and marks tool checked out
		/* We do not check to see whether the tool has already been rented out here, as we do this inside
		 * Contract's updateToolDetails method, before the contract string is ever generated.
		 */
		try {
			int initStatus = initConnection();
			if (initStatus == 0) {
				Statement stmt = conn.createStatement(); //send update statement to DB
				stmt.executeUpdate("update " + tableName + " set Rented = 'Y', Contract = '" + contract + "' where Tool_Code = '" + code + "'");
				stmt.close();
				closeConnection();
				}
			}
		catch(Exception e) {
			System.out.println("Error: Database update failed. Tool not checked out. Please make sure the tool code is correct.");
			closeConnection();
		}	
	}
	
	void returnTool(String code) { //updates DB to indicate the contract has been fulfilled and tool has been returned
		/* The user is responsible for collecting the actual tool and money due, and checking to see if the contract was
		 * adhered to.
		 * We could add a check to see if the tool is already in the non-rental status and alert the user, but I opted
		 * not to b/c that would require an additional SQL query, and the update statement would leave the row with the
		 * same values as before in that case, anyway.
		 */
		boolean isValidCode = isToolCodeValid(code);
		try {
				int initStatus = initConnection();
				if (initStatus == 0 && isValidCode) {
					Statement stmt = conn.createStatement(); //send update query to DB, blanking out contract as there is no longer an open contract
					stmt.executeUpdate("update " + tableName + " set Rented = 'N', Contract = '' where tool_code = '" + code + "'");
					stmt.close();
					closeConnection();
					System.out.println("Tool successfully marked as returned.");
				}
				else if (!isToolCodeValid(code)) {
					System.out.println("Tool code is invalid. Could not return the tool.");
				}
			}
		catch(Exception e) {
			System.out.println("Error: Database update failed. Tool not returned. Please make sure the tool code is correct.");
			closeConnection();
		}	
	}
	
	void getContractByCode(String code) { //return the active contract for this tool code, if one exists
		try {
			int initStatus = initConnection();
			if (initStatus == 0) {
				Statement stmt = conn.createStatement(); //send query to DB to get contract for this tool code
				ResultSet rs = stmt.executeQuery("select CONTRACT from " + tableName + " where TOOL_CODE = '" + code + "'");
				//check to see if we got back any results
				if (!rs.isBeforeFirst() ) {    
				    System.out.println("No contract found in the database for this code."); 
				} 
				else {//print out the contract if it's not blank
					rs.next();
					String contract = rs.getString("Contract");
					if (!contract.isEmpty())
						System.out.println(contract + "\n");
					else
						System.out.println("No contract found in the database for this code.");
				}
				rs.close();
				stmt.close();
				closeConnection();
				}
			}
		catch(Exception e) {
			System.out.println("Error: Query to database failed. Could not retrieve the contract.");
			closeConnection();
		}
	}
	
	boolean isToolCodeValid(String toolCode) { //checks to see if toolCode exists in the table
		boolean isValid = false;
		try {
			int initStatus = initConnection();
			if (initStatus == 0) {
				Statement stmt = conn.createStatement(); //send filtered query to DB
				ResultSet rs = stmt.executeQuery("select * from " + tableName + " where tool_code = '" + toolCode + "'");
				//check to see if we got back any results
				if (rs.isBeforeFirst() ) {    
				    isValid = true; 
				} 
				rs.close();
				stmt.close();
				closeConnection();
				}
			}
		catch(Exception e) {
			System.out.println("Error: Query to database to check tool code validity failed.");
			closeConnection();
		}
		return isValid;
	}
	
}

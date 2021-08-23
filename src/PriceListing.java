import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.cj.jdbc.MysqlDataSource;

/* This class provides methods to connect to and access the pricing table on the tool rental MySQL
 * database I configured for this project. In this table, tool type is unique, therefore a given tool
 * type should either return no results or a single row.
 * The SQL table has 5 columns: Tool_Type, Daily_Rate, Weekday, Weekend, and Holiday.
 */

public class PriceListing {

	private final String user = "u566381527_demo";
	private final String password = "Testingdemo1";
	private final String host = "sql531.main-hosting.eu";
	private final String dbName = "u566381527_toolrental";
	private final String tableName = "pricing";
	MysqlDataSource dbSource;
	Connection conn;
	
	PriceListing() //initialize the MysqlDataSource with login info
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
	
	double[] getRowForToolType(String toolType) { //returns the daily rate and weekday/weekend/holiday values for the specified toolType
		double [] resultArr = {-1, -1, -1, -1}; //initialize all elements to sentinel of negative one
		try {
			int initStatus = initConnection();
			if (initStatus == 0) {
				Statement stmt = conn.createStatement(); //send query to DB
				ResultSet rs = stmt.executeQuery("select * from " + tableName + " where Tool_Type = '" + toolType + "'");
				//check to see if we got back any results
				if (!rs.isBeforeFirst()) {    
				    System.out.println("No results found in the database."); 
				}
				else {
						while (rs.next()) { //add results to array
							resultArr[0] = rs.getDouble("Daily_Rate");
							String weekday = rs.getString("Weekday");
							//convert Y/N to 1/0 for use in Contract's method calculateCosts()
							resultArr[1] = weekday.equals("Y") ? 1 : 0;
							String weekend = rs.getString("Weekend");
							resultArr[2] = weekend.equals("Y") ? 1 : 0;
							String holiday = rs.getString("Holiday");
							resultArr[3] = holiday.equals("Y") ? 1 : 0;
						}
					}
				rs.close();
				stmt.close();
				closeConnection();
				}
			}
		catch (Exception e) {
			System.out.println("Error: Query to database for pricing information failed. Please make sure the tool type exists in the database.");
			closeConnection();
		}
		return resultArr;
	}
		
}

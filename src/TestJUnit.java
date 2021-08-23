import org.junit.Test;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.contrib.java.lang.system.TextFromStandardInputStream.*;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.junit.Rule;

public class TestJUnit {

	@Rule
	  public final TextFromStandardInputStream simulatedIn = emptyStandardInputStream();
	 
	@Test
	public void checkInvalidDiscount() {
		 //if this test case is correct, we shouldn't get far enough to input the date
		 //3 is needed to get into the Rent a Tool option, 6 is needed to exit main
		 simulatedIn.provideLines("3", "JAKR", "5", "101", "6");
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 PrintStream printStream = new PrintStream(outStream);
		 System.setOut(printStream);
		 demo.main(null);
		 String outputLines = outStream.toString();
		 String[] splitLines = outputLines.split(System.lineSeparator());
		 String lineAfterDiscountInput = splitLines[splitLines.length-8]; //end of output, minus the 7 extra lines from main menu print
		 String discountMessage = "Discount must be no less 0 than 0 and no greater than 100. Exiting rental process.";
		 Assert.assertEquals(discountMessage, lineAfterDiscountInput);
	 }
	
	@Test
	public void testLadw() {
		 //I use no since we don't need to update the DB, we just need to check generated contract
		 simulatedIn.provideLines("3", "LADW", "3", "10", "07/02/20", "no" , "6");
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 PrintStream printStream = new PrintStream(outStream);
		 System.setOut(printStream);
		 demo.main(null);
		 String outputLines = outStream.toString();
		 //the contract will print between these two lines
		 String myContract = StringUtils.substringBetween(outputLines, "Please enter the tool checkout date in mm/dd/yy format:" + System.lineSeparator(), System.lineSeparator() + "\nRental agreement generated above. Do you want to proceed with the rental? Please enter yes/no:");
		 String correctContract = 
				 "Tool Code: LADW\n"
				 + "Tool Type: Ladder\n"
				 + "Tool Brand: Werner\n" 
				 + "Rental Days: 3\n" 
				 + "Check Out Date: 07/02/20\n"
				 + "Due Date: 07/05/20\n" 
				 + "Daily Rental Charge: $1.99\n" 
				 + "Charge Days: 2\n" 
				 + "Pre-Discount Charge: $3.98\n" 
				 + "Discount Percent: 10%\n" 
				 + "Discount Amount: $0.40\n"
				 + "Final Charge: $3.58";
		 Assert.assertEquals(correctContract, myContract);
	 }
	
	@Test
	public void testChns() {
		 //I use no since we don't need to update the DB, we just need to check generated contract
		 simulatedIn.provideLines("3", "CHNS", "5", "25", "07/02/15", "no" , "6");
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 PrintStream printStream = new PrintStream(outStream);
		 System.setOut(printStream);
		 demo.main(null);
		 String outputLines = outStream.toString();
		 //the contract will print between these two lines
		 String myContract = StringUtils.substringBetween(outputLines, "Please enter the tool checkout date in mm/dd/yy format:" + System.lineSeparator(), System.lineSeparator() + "\nRental agreement generated above. Do you want to proceed with the rental? Please enter yes/no:");
		 String correctContract = 
				 "Tool Code: CHNS\n"
				 + "Tool Type: Chainsaw\n"
				 + "Tool Brand: Stihl\n" 
				 + "Rental Days: 5\n" 
				 + "Check Out Date: 07/02/15\n"
				 + "Due Date: 07/07/15\n" 
				 + "Daily Rental Charge: $1.49\n" 
				 + "Charge Days: 3\n" 
				 + "Pre-Discount Charge: $4.47\n" 
				 + "Discount Percent: 25%\n" 
				 + "Discount Amount: $1.12\n"
				 + "Final Charge: $3.35";
		 Assert.assertEquals(correctContract, myContract);
	 }
	
	@Test
	public void testJakd() {
		 //I use no since we don't need to update the DB, we just need to check generated contract
		 simulatedIn.provideLines("3", "JAKD", "6", "0", "09/03/15", "no" , "6");
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 PrintStream printStream = new PrintStream(outStream);
		 System.setOut(printStream);
		 demo.main(null);
		 String outputLines = outStream.toString();
		 //the contract will print between these two lines
		 String myContract = StringUtils.substringBetween(outputLines, "Please enter the tool checkout date in mm/dd/yy format:" + System.lineSeparator(), System.lineSeparator() + "\nRental agreement generated above. Do you want to proceed with the rental? Please enter yes/no:");
		 String correctContract = 
				 "Tool Code: JAKD\n"
				 + "Tool Type: Jackhammer\n"
				 + "Tool Brand: DeWalt\n" 
				 + "Rental Days: 6\n" 
				 + "Check Out Date: 09/03/15\n"
				 + "Due Date: 09/09/15\n" 
				 + "Daily Rental Charge: $2.99\n" 
				 + "Charge Days: 3\n" 
				 + "Pre-Discount Charge: $8.97\n" 
				 + "Discount Percent: 0%\n" 
				 + "Discount Amount: $0.00\n"
				 + "Final Charge: $8.97";
		 Assert.assertEquals(correctContract, myContract);
	 }
	
	@Test
	public void testJakr1() {
		 //I use no since if we update the DB with the rental, the next test case will not let us rent out the same tool
		 simulatedIn.provideLines("3", "JAKR", "9", "0", "07/02/15", "no" , "6");
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 PrintStream printStream = new PrintStream(outStream);
		 System.setOut(printStream);
		 demo.main(null);
		 String outputLines = outStream.toString();
		 //the contract will print between these two lines
		 String myContract = StringUtils.substringBetween(outputLines, "Please enter the tool checkout date in mm/dd/yy format:" + System.lineSeparator(), System.lineSeparator() + "\nRental agreement generated above. Do you want to proceed with the rental? Please enter yes/no:");
		 String correctContract = 
				 "Tool Code: JAKR\n"
				 + "Tool Type: Jackhammer\n"
				 + "Tool Brand: Ridgid\n" 
				 + "Rental Days: 9\n" 
				 + "Check Out Date: 07/02/15\n"
				 + "Due Date: 07/11/15\n" 
				 + "Daily Rental Charge: $2.99\n" 
				 + "Charge Days: 6\n" 
				 + "Pre-Discount Charge: $17.94\n" 
				 + "Discount Percent: 0%\n" 
				 + "Discount Amount: $0.00\n"
				 + "Final Charge: $17.94";
		 Assert.assertEquals(correctContract, myContract);
	 }
	
	@Test
	public void testJakr2() {
		 simulatedIn.provideLines("3", "JAKR", "4", "50", "07/02/20", "no" , "6");
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 PrintStream printStream = new PrintStream(outStream);
		 System.setOut(printStream);
		 demo.main(null);
		 String outputLines = outStream.toString();
		 //the contract will print between these two lines
		 String myContract = StringUtils.substringBetween(outputLines, "Please enter the tool checkout date in mm/dd/yy format:" + System.lineSeparator(), System.lineSeparator() + "\nRental agreement generated above. Do you want to proceed with the rental? Please enter yes/no:");
		 String correctContract = 
				 "Tool Code: JAKR\n"
				 + "Tool Type: Jackhammer\n"
				 + "Tool Brand: Ridgid\n" 
				 + "Rental Days: 4\n" 
				 + "Check Out Date: 07/02/20\n"
				 + "Due Date: 07/06/20\n" 
				 + "Daily Rental Charge: $2.99\n" 
				 + "Charge Days: 1\n" 
				 + "Pre-Discount Charge: $2.99\n" 
				 + "Discount Percent: 50%\n" 
				 + "Discount Amount: $1.50\n"
				 + "Final Charge: $1.49";
		 Assert.assertEquals(correctContract, myContract);
	 }

	@AfterClass
	public static void resetOutput() {
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}
}

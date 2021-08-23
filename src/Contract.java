import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.TemporalAdjusters.firstInMonth;
import java.text.NumberFormat;

/* This class stores the attributes of, collects information for, and builds a contract. Note that the daily charge, if
 * applicable, is assessed on the checkout date, but not on the due date. You can think of the due date as saying "the
 * tool must be returned by 00:00 this date". Alternatively, the tool rental business may choose not to charge customers
 * for the due date as long as the customer returns the tool in a timely fashion.
 * All checkout dates must be 01/01/2000 or later. However, presumably you are not retroactively renting out tools for use in the 1900s.
 */

public class Contract {

	private String toolCode;
	private String toolType;
	private String brand;
	private LocalDate checkOutDate;
	private LocalDate dueDate;
	private int rentalDays;
	private int chargeableDays;
	private double dailyCharge;
	private double baseCharge;
	private int discountPct;
	private double discountAmt;
	private double finalCharge;
	
	Contract(){ //initialize with default values
		toolCode = "";
		toolType = "";
		brand = "";
		checkOutDate = null;
		dueDate = null;
		rentalDays = 0;
		chargeableDays = 0;
		dailyCharge = 0;
		baseCharge = 0;
		discountPct = 0;
		discountAmt = 0;
		finalCharge = 0;
	}
	
	int updateToolDetails(String code, ToolInventory inv) { //verify tool code, and pull tool details from inventory
		int status = 0; //status of 0 means update was successful
		boolean isCodeValid = inv.isToolCodeValid(code);
		if (!isCodeValid) 
		{   //status of -1 means tool code was invalid, or in rare cases the inventory query failed
			//If the inventory query failed, you will know b/c a message will print from isToolCodeValid indicating that.
			status = -1; 
			System.out.println("The tool code entered was invalid. Exiting rental process.");
		}
		else {
			//check to see if the tool is not already rented
			Vector<String> toolData = inv.getFilteredResults("tool_code", code, false);
			String rented = toolData.get(3);
			if (rented.equals("Y")) 
			{   //status of -2 means tool is currently checked out
				status = -2; 
				System.out.println("This tool is already being rented. Exiting rental process.");
			}
			else { //set values for this contract from toolData
				toolCode = code.toUpperCase();
				toolType = toolData.get(1);
				brand = toolData.get(2);
			}
		}
		return status;
	}
	
	boolean isValidRentalLength(String days) //Checks validity of rental length and updates rentalDays if valid
	{
		boolean isValid = false;
		try {
			int numDays = Integer.parseInt(days);
			if (numDays <= 0)
				throw new RuntimeException("Number of days must be greater than 0. Exiting rental process.");
			else {
				isValid = true;
				rentalDays = numDays;
			}
		}
		catch (NumberFormatException n) //parseInt failed
		{
			System.out.println("Input is not a valid whole number (no words). Exiting rental process.");
		}
		catch (Exception e) //number of days less than 0, or other exception
		{
			if (e.getMessage().equals("Number of days must be greater than 0. Exiting rental process."))
				System.out.println(e.getMessage());
			else
				System.out.println("An unknown exception occurred. Exiting rental process.");
		}
		return isValid;
	}
	
	boolean isValidDiscount(String discount) //Checks validity of discount and updates discountPct if valid
	{
		boolean isValid = false;
		try {
			int discountPercent = Integer.parseInt(discount);
			if (discountPercent < 0 || discountPercent > 100)
				throw new RuntimeException("Discount must be no less 0 than 0 and no greater than 100. Exiting rental process.");
			else {
				isValid = true;
				discountPct = discountPercent;
			}
		}
		catch (NumberFormatException e) //parseInt failed
		{
			System.out.println("Input must be a valid whole number only (no percent sign or words). Exiting rental process.");
		}
		catch (Exception e) //discount not between 0 and 100 inclusive, or other exception
		{
			if(e.getMessage().equals("Discount must be no less 0 than 0 and no greater than 100. Exiting rental process."))
				System.out.println(e.getMessage());
			else
				System.out.println("An unknown exception occurred. Exiting rental process.");
		}
		return isValid;
	}
	
	boolean isValidDate(String date) { //checks to see if entered date is in valid format and updates checkout date if valid
		boolean isValid = false;
		try {
			DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("MM/dd/yy");
			LocalDate myDate = LocalDate.parse(date, datePattern);
			isValid = true;
			checkOutDate = myDate;
		}
		catch (Exception e) {
			System.out.println("Input not a valid date. Date must be in mm/dd/yy format. Exiting rental process.");
		}
		return isValid;
	}
	
	void calculateCosts() { //make sure all of the above methods have been called and return true before running this
		//first, calculate the due date based on checkout date and rental length
		dueDate = checkOutDate.plusDays(rentalDays);
		
		//then, fetch pricing info from the database
		PriceListing pList = new PriceListing();
		try {
			double[] priceInfo = pList.getRowForToolType(toolType);
			//make sure we got valid info for all columns
			boolean isInvalidRow = Arrays.asList(priceInfo).contains(-1);
			if (isInvalidRow)
				throw new RuntimeException("Did not get valid results back from pricing DB query. Failed to calculate costs.");
			dailyCharge = priceInfo[0];
			
			//now for each rental day, we need to determine if it is a weekday, weekend, or holiday
			int weekdayCount = 0;
			int weekendCount = 0;
			int holidayCount = 0;
			for (int i = 0; i < rentalDays; i++)
			{
				LocalDate today = checkOutDate.plusDays(i);
				int dayOfWeek = today.getDayOfWeek().getValue(); //numbering starts with Monday = 1
				int month = today.getMonth().getValue();
				int dayOfMonth = today.getDayOfMonth();
				
				//if dayOfWeek is 7(Sunday) or 6(Saturday), it's the weekend.
				if (dayOfWeek == 6 || dayOfWeek == 7)
					weekendCount++;
				//We need to check for any weekday July 4th, Friday July 3rd and Monday July 5th as those would be considered holidays.
				else if (month == 7 && (dayOfMonth == 4 || (dayOfMonth == 3 && dayOfWeek == 5) || (dayOfMonth == 5 && dayOfWeek == 1)))
					holidayCount++;
				//Now we need to check for Labor Day, which is always the first Monday in September
				else if (month == 9 && dayOfWeek == 1) {
					//get first Monday of September, accounting for year
					LocalDate laborDayWithYear = today.with(firstInMonth(DayOfWeek.MONDAY));
					int laborDay = laborDayWithYear.getDayOfMonth();
					if (laborDay == dayOfMonth)
						holidayCount++;
					else
						weekdayCount++;
					}
				else
					weekdayCount++;
			}
			//since the values of priceInfo[1] through priceInfo[3] are 0 if no charges apply or 1 if they do, we can multiply with them
			chargeableDays = (weekdayCount * (int)priceInfo[1]) + (weekendCount * (int)priceInfo[2]) + (holidayCount * (int)priceInfo[3]);
			baseCharge = Math.round(chargeableDays * dailyCharge * 100.0) / 100.0;
			discountAmt = baseCharge * (discountPct / 100.0);
			discountAmt = Math.round(discountAmt * 100.0) / 100.0;
			finalCharge = baseCharge - discountAmt;
		}
		catch (Exception e){
			if (e.getMessage().equals("Did not get valid results back from pricing DB query. Failed to calculate costs."))
				System.out.println(e.getMessage());
			else
				System.out.println("An unknown exception occurred. Failed to calculate costs.");
		}
	}
	
	public String toString() { //formats Contract as a human-readable String
		//format dates for printing
		DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("MM/dd/yy");
		String checkOut = checkOutDate.format(datePattern);
		String due = dueDate.format(datePattern);
		//format prices for printing
		NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.US);
		String dailyChgStr = moneyFormat.format(dailyCharge);
		String baseChgStr = moneyFormat.format(baseCharge);
		String discountAmtStr = moneyFormat.format(discountAmt);
		String finalChgStr = moneyFormat.format(finalCharge);
		//put together the String
		String contractString = "Tool Code: " + toolCode + "\nTool Type: " + toolType + "\nTool Brand: " + brand +
				"\nRental Days: " + rentalDays + "\nCheck Out Date: " + checkOut +"\nDue Date: " + due + "\nDaily Rental Charge: " +
				 dailyChgStr + "\nCharge Days: " + chargeableDays + "\nPre-Discount Charge: " + baseChgStr +"\nDiscount Percent: " + 
				discountPct + "%\nDiscount Amount: " + discountAmtStr + "\nFinal Charge: " + finalChgStr;
		return contractString;
	}
}

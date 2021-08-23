import java.util.Scanner;

/* Main drives the main menu interface for the tool rental system. From the main menu, you may
 * view all inventory, search the inventory for a field with a specified value, rent a tool,
 * return a tool, look up a rental contract by tool code, or exit the application.
 * You can perform as many actions as you would like before exiting.
 */
public class demo {

	static void printMenuOptions() //helper function for printing main menu prompt
	{
		System.out.println("1 - View All Inventory");
		System.out.println("2 - Search Inventory by Field");
		System.out.println("3 - Rent a Tool");
		System.out.println("4 - Return a Tool");
		System.out.println("5 - Look Up Contract by Tool Code");
		System.out.println("6 - Exit");
		System.out.println("Please type in one of the above option numbers: ");
	}
	public static void main(String[] args) {
		ToolInventory inventory = new ToolInventory();
		Scanner scan = new Scanner(System.in);
		System.out.println("WELCOME TO THE TOOL RENTAL SYSTEM");
		printMenuOptions();
		String keyIn = scan.nextLine();
		
		while (!keyIn.equals("6") && !keyIn.equalsIgnoreCase("exit"))
		{
			if (keyIn.equals("1") || keyIn.equalsIgnoreCase("View All Inventory")) {
				inventory.printAll();
			}
			else if (keyIn.equals("2") || keyIn.equalsIgnoreCase("Search Inventory by Field"))
			{
				String searchField;
				String searchValue;
				System.out.println("Please enter the field to search on - brand, tool code, or tool type:");
				searchField = scan.nextLine();
				int goodInput = -1; //track whether we got a valid response for searchField
				while (goodInput != 0) {
					if (searchField.equalsIgnoreCase("brand")) {
							System.out.println("Please enter the brand name to search for:");
							goodInput = 0;
						}
					else if (searchField.equalsIgnoreCase("type") || searchField.equalsIgnoreCase("tooltype") || searchField.equalsIgnoreCase("tool type") || searchField.equalsIgnoreCase("tool_type")) {
							System.out.println("Please enter the type of tool to search for:");
							goodInput = 0;
						}
					else if (searchField.equalsIgnoreCase("code") || searchField.equalsIgnoreCase("toolcode") || searchField.equalsIgnoreCase("tool code") || searchField.equalsIgnoreCase("tool_code")) {
							System.out.println("Please enter the tool code to search for:");
							goodInput = 0;
						}
					else {
							System.out.println("Invalid input. Please enter the word brand, the words tool code, or the words tool type:");
							searchField = scan.nextLine();
						}
					}
				searchValue = scan.nextLine();
				inventory.getFilteredResults(searchField, searchValue, true);
			}
			else if (keyIn.equals("3") || keyIn.equalsIgnoreCase("Rent a Tool")) {
				System.out.println("Please enter the tool code of the tool to rent:");
				String toolCode = scan.nextLine();
				Contract rentAgreement = new Contract();
				int toolDetailStatus = rentAgreement.updateToolDetails(toolCode, inventory);
				if (toolDetailStatus == 0) { //only continue if updateToolDetails succeeded
					System.out.println("Please enter the number of days to rent the tool for:");
					String numDays = scan.nextLine();
					if (rentAgreement.isValidRentalLength(numDays)) //only continue if valid rental length
					{
						System.out.println("Please enter the % discounted, or 0 if no discount:");
						String discount = scan.nextLine();
						if (rentAgreement.isValidDiscount(discount)) //only continue if discount is valid
						{
							System.out.println("Please enter the tool checkout date in mm/dd/yy format:");
							String dateString = scan.nextLine();
							if(rentAgreement.isValidDate(dateString)) //only continue if date is valid
							{
								rentAgreement.calculateCosts();
								System.out.println(rentAgreement);
								System.out.println("\nRental agreement generated above. Do you want to proceed with the rental? Please enter yes/no:");
								String finishRental = scan.nextLine();
								
								boolean isRentalComplete = false; //controls agreement acknowledgement loop
								while (!isRentalComplete) {
									if (finishRental.equalsIgnoreCase("yes") || finishRental.equalsIgnoreCase("y")) 
									{
										inventory.checkOutTool(toolCode, rentAgreement.toString());
										System.out.println("Tool has successfully been checked out.");
										isRentalComplete = true;
									}
									else if (finishRental.equalsIgnoreCase("no") || finishRental.equalsIgnoreCase("n")) 
									{
										System.out.println("Rental canceled. Exiting the rental process.");
										isRentalComplete = true;
									}
									else 
									{
										System.out.println("Invalid input. Please enter yes, no, y or n: ");
										finishRental = scan.nextLine();
									}
								}
								
							}
						}
					}
				}
			}
			else if (keyIn.equals("4") || keyIn.equalsIgnoreCase("Return a Tool"))
			{
				System.out.println("Please enter the tool code of the tool to mark as returned:");
				String toolCode = scan.nextLine();
				inventory.returnTool(toolCode);
			}
			else if (keyIn.equals("5") || keyIn.equalsIgnoreCase("Look Up Contract by Tool Code"))
			{
				System.out.println("Please enter the tool code:");
				String searchCode = scan.nextLine();
				inventory.getContractByCode(searchCode);
			}
			else { //no valid menu option received
				System.out.println("Invalid input. Input must be 1, 2, 3, 4, 5, or 6."); 
			}
			printMenuOptions();
			keyIn = scan.nextLine();
		}
		scan.close();
		return;
	}
}

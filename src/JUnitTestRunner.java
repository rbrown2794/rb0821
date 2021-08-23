import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUnitTestRunner {
   public static void main(String[] args) {
      Result myResult = JUnitCore.runClasses(TestJUnit.class);
      for (Failure failure : myResult.getFailures()) {
         System.out.println(failure.toString());
      }
      System.out.println("All Tests Passed: " + myResult.wasSuccessful());
   }
} 

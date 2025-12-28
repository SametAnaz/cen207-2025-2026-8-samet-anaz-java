package com.ucoruh.password;

import java.util.Scanner;
import org.junit.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @brief Unit tests for the AutoLoginManager class.
 *
 * Verifies that the AutoLoginManager's menu method produces the expected output.
 */
public class AutoLoginManagerTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private PasswordManager passwordManager;
  private static final String TEST_FILE = "autologin.txt";

  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    passwordManager = new PasswordManager("test-master-password");
    // Add a test credential to password manager
    passwordManager.addCredential("TestService", "testPassword");

    // Make sure the auto login file doesn't exist
    try {
      Files.deleteIfExists(Paths.get(TEST_FILE));
    } catch (IOException e) {
      // Ignore
    }
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);

    // Clean up auto login file
    try {
      Files.deleteIfExists(Paths.get(TEST_FILE));
    } catch (IOException e) {
      // Ignore
    }
  }

  /**
   * @brief Tests the menu method with a basic input.
   *
   * This test provides input to select option 0 (back to main menu) and verifies
   * that the menu is displayed correctly.
   */
  @Test
  public void testMenuBasicNavigation() {
    // Simulate input to navigate back to main menu
    String input = "0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should contain auto-login menu header",
               output.contains("AUTO-LOGIN FEATURES"));
    assertTrue("Output should contain option to enable auto-login",
               output.contains("Enable Auto-Login"));
  }

  /**
   * @brief Tests showing services with auto-login enabled when none exist.
   */
  @Test
  public void testShowServicesEmpty() {
    // Reset the AutoLoginManager state by reflection
    try {
      // First clear the set
      java.lang.reflect.Field field = AutoLoginManager.class.getDeclaredField("autoLoginServices");
      field.setAccessible(true);
      java.util.Set<String> services = (java.util.Set<String>) field.get(null);
      services.clear();
      // Then reset initialized flag
      field = AutoLoginManager.class.getDeclaredField("initialized");
      field.setAccessible(true);
      field.set(null, false);

      // Make sure no services are enabled for auto-login
      try {
        Files.deleteIfExists(Paths.get(TEST_FILE));
      } catch (IOException e) {
        // Ignore
      }

      // Reset output
      outContent.reset();
      // Directly call the showAutoLoginServices method through reflection
      java.lang.reflect.Method method = AutoLoginManager.class.getDeclaredMethod("showAutoLoginServices");
      method.setAccessible(true);
      method.invoke(null);
      String output = outContent.toString();
      assertTrue("Output should contain 'None'", output.contains("None"));
    } catch (Exception e) {
      fail("Failed to test showAutoLoginServices: " + e.getMessage());
    }
  }

  /**
   * @brief Tests enabling auto-login for a service.
   */
  @Test
  public void testEnableAutoLogin() {
    // First enable auto-login for a service
    AutoLoginManager.enableAutoLogin("TestService");
    // Then check if it's enabled
    assertTrue("TestService should have auto-login enabled",
               AutoLoginManager.isAutoLoginEnabled("TestService"));
  }

  /**
   * @brief Tests disabling auto-login for a service.
   */
  @Test
  public void testDisableAutoLogin() {
    // First enable auto-login for a service
    AutoLoginManager.enableAutoLogin("TestService");
    // Then disable it
    AutoLoginManager.disableAutoLogin("TestService");
    // Then check if it's disabled
    assertFalse("TestService should have auto-login disabled",
                AutoLoginManager.isAutoLoginEnabled("TestService"));
  }

  /**
   * @brief Tests auto-login for a service.
   */
  @Test
  public void testAutoLogin() {
    // First enable auto-login for our test service that has a credential
    AutoLoginManager.enableAutoLogin("TestService");
    // Then try to auto-login
    boolean result = AutoLoginManager.autoLogin("TestService", passwordManager);
    // Auto-login should be successful
    assertTrue("Auto-login should be successful", result);
    assertTrue("Output should contain 'Successfully logged in'",
               outContent.toString().contains("Successfully logged in"));
  }

  /**
   * @brief Tests auto-login failure for a non-existent service.
   */
  @Test
  public void testAutoLoginFailure() {
    // Try to auto-login to a service that isn't enabled
    boolean result = AutoLoginManager.autoLogin("NonExistentService", passwordManager);
    // Auto-login should fail
    assertFalse("Auto-login should fail", result);
  }

  /**
   * @brief Tests enabling auto-login through the menu.
   */
  @Test
  public void testEnableAutoLoginMenu() {
    // Simulate input to enable auto-login for TestService
    String input = "1\nTestService\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should confirm auto-login was enabled",
               output.contains("Auto-login enabled for TestService"));
  }

  /**
   * @brief Tests enabling auto-login for a non-existent service.
   */
  @Test
  public void testEnableAutoLoginMenuNonExistentService() {
    // Simulate input to try to enable auto-login for a non-existent service
    String input = "1\nNonExistentService\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate service wasn't found",
               output.contains("Service not found"));
  }

  /**
   * @brief Tests disabling auto-login through the menu.
   */
  @Test
  public void testDisableAutoLoginMenu() {
    // First enable auto-login
    AutoLoginManager.enableAutoLogin("TestService");
    outContent.reset();
    // Simulate input to disable auto-login
    String input = "2\nTestService\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should confirm auto-login was disabled",
               output.contains("Auto-login disabled for TestService"));
  }

  /**
   * @brief Tests disabling auto-login for a service that doesn't have it enabled.
   */
  @Test
  public void testDisableAutoLoginMenuNotEnabled() {
    // Simulate input to try to disable auto-login for a service that doesn't have it enabled
    String input = "2\nTestService\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate auto-login wasn't enabled",
               output.contains("Auto-login was not enabled"));
  }

  /**
   * @brief Tests simulating auto-login through the menu.
   */
  @Test
  public void testSimulateAutoLoginMenu() {
    // First enable auto-login
    AutoLoginManager.enableAutoLogin("TestService");
    outContent.reset();
    // Simulate input to simulate auto-login
    String input = "4\nTestService\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate successful auto-login",
               output.contains("Successfully logged in"));
  }

  /**
   * @brief Tests handling invalid input in the menu.
   */
  @Test
  public void testMenuInvalidInput() {
    // Simulate invalid input (non-numeric)
    String input = "invalid\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate invalid number",
               output.contains("Invalid number"));
  }

  // ==================== ADDITIONAL COVERAGE TESTS ====================

  /**
   * @brief Tests AutoLoginManager can be instantiated.
   */
  @Test
  public void testAutoLoginManagerConstructor() {
    AutoLoginManager manager = new AutoLoginManager();
    assertNotNull("AutoLoginManager should be instantiable", manager);
  }

  /**
   * @brief Tests showing services with auto-login enabled when services exist.
   */
  @Test
  public void testShowServicesWithExistingServices() {
    // Enable auto-login for services
    AutoLoginManager.enableAutoLogin("Service1");
    AutoLoginManager.enableAutoLogin("Service2");
    outContent.reset();
    // Show services through menu
    String input = "3\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should contain Service1", output.contains("Service1"));
    assertTrue("Output should contain Service2", output.contains("Service2"));
  }

  /**
   * @brief Tests simulate auto-login failure through menu.
   */
  @Test
  public void testSimulateAutoLoginFailureMenu() {
    outContent.reset();
    // Try to simulate auto-login for a service that doesn't have it enabled
    String input = "4\nNonEnabledService\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate auto-login failed",
               output.contains("Auto-login failed"));
  }

  /**
   * @brief Tests invalid choice in menu.
   */
  @Test
  public void testMenuInvalidChoice() {
    String input = "99\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate invalid choice",
               output.contains("Invalid choice"));
  }

  /**
   * @brief Tests negative menu choice.
   */
  @Test
  public void testMenuNegativeChoice() {
    String input = "-1\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should indicate invalid choice",
               output.contains("Invalid choice"));
  }

  /**
   * @brief Tests auto-login when service is enabled but no credential exists.
   */
  @Test
  public void testAutoLoginEnabledButNoCredential() {
    // Enable auto-login for a service that has no credential
    AutoLoginManager.enableAutoLogin("NoCredentialService");
    // Try to auto-login
    boolean result = AutoLoginManager.autoLogin("NoCredentialService", passwordManager);
    // Should fail because no credential
    assertFalse("Auto-login should fail without credential", result);
  }

  /**
   * @brief Tests initialize method loads from existing file.
   */
  @Test
  public void testInitializeLoadsFromFile() {
    try {
      // Create auto-login file with content
      PrintWriter writer = new PrintWriter(new FileWriter(TEST_FILE));
      writer.println("PreExistingService");
      writer.close();
      // Reset initialized flag to force re-initialization
      java.lang.reflect.Field field = AutoLoginManager.class.getDeclaredField("initialized");
      field.setAccessible(true);
      field.set(null, false);
      // Clear existing services
      java.lang.reflect.Field servicesField = AutoLoginManager.class.getDeclaredField("autoLoginServices");
      servicesField.setAccessible(true);
      java.util.Set<String> services = (java.util.Set<String>) servicesField.get(null);
      services.clear();
      // Check if the service was loaded
      assertTrue("PreExistingService should be loaded from file",
                 AutoLoginManager.isAutoLoginEnabled("PreExistingService"));
    } catch (Exception e) {
      fail("Test failed: " + e.getMessage());
    }
  }

  /**
   * @brief Tests showing services displays list header.
   */
  @Test
  public void testShowServicesDisplaysHeader() {
    outContent.reset();
    String input = "3\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Output should contain header",
               output.contains("Services with Auto-Login enabled"));
  }

  /**
   * @brief Tests multiple enable/disable operations.
   */
  @Test
  public void testMultipleEnableDisableOperations() {
    // Enable multiple services
    AutoLoginManager.enableAutoLogin("MultiService1");
    AutoLoginManager.enableAutoLogin("MultiService2");
    AutoLoginManager.enableAutoLogin("MultiService3");
    assertTrue(AutoLoginManager.isAutoLoginEnabled("MultiService1"));
    assertTrue(AutoLoginManager.isAutoLoginEnabled("MultiService2"));
    assertTrue(AutoLoginManager.isAutoLoginEnabled("MultiService3"));
    // Disable one
    AutoLoginManager.disableAutoLogin("MultiService2");
    assertTrue(AutoLoginManager.isAutoLoginEnabled("MultiService1"));
    assertFalse(AutoLoginManager.isAutoLoginEnabled("MultiService2"));
    assertTrue(AutoLoginManager.isAutoLoginEnabled("MultiService3"));
  }

  /**
   * @brief Tests empty input in menu.
   */
  @Test
  public void testMenuEmptyInput() {
    String input = "\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Should handle empty input", output.contains("Invalid number"));
  }

  /**
   * @brief Tests special character input in menu.
   */
  @Test
  public void testMenuSpecialCharInput() {
    String input = "@#$\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Should handle special chars", output.contains("Invalid number"));
  }

  /**
   * @brief Tests isAutoLoginEnabled for service that was never set.
   */
  @Test
  public void testIsAutoLoginEnabledFalse() {
    assertFalse("Should return false for unknown service",
                AutoLoginManager.isAutoLoginEnabled("NeverSetService"));
  }

  /**
   * @brief Tests enabling same service twice.
   */
  @Test
  public void testEnableSameServiceTwice() {
    AutoLoginManager.enableAutoLogin("DuplicateService");
    AutoLoginManager.enableAutoLogin("DuplicateService");
    assertTrue("Service should still be enabled",
               AutoLoginManager.isAutoLoginEnabled("DuplicateService"));
  }

  /**
   * @brief Tests disabling a service that was never enabled.
   */
  @Test
  public void testDisableNeverEnabledService() {
    AutoLoginManager.disableAutoLogin("NeverEnabledService");
    assertFalse("Service should not be enabled",
                AutoLoginManager.isAutoLoginEnabled("NeverEnabledService"));
  }

  /**
   * @brief Tests the menu displays all options.
   */
  @Test
  public void testMenuDisplaysAllOptions() {
    String input = "0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Should show option 1", output.contains("1. Enable Auto-Login"));
    assertTrue("Should show option 2", output.contains("2. Disable Auto-Login"));
    assertTrue("Should show option 3", output.contains("3. Show services"));
    assertTrue("Should show option 4", output.contains("4. Simulate Auto-Login"));
    assertTrue("Should show option 0", output.contains("0. Back to Main Menu"));
  }

  /**
   * @brief Tests auto-login message output.
   */
  @Test
  public void testAutoLoginMessageOutput() {
    AutoLoginManager.enableAutoLogin("TestService");
    outContent.reset();
    AutoLoginManager.autoLogin("TestService", passwordManager);
    String output = outContent.toString();
    assertTrue("Should show logging in message", output.contains("Auto-logging in to"));
  }

  /**
   * @brief Tests multiple menu operations in sequence.
   */
  @Test
  public void testMultipleMenuOperations() {
    // Enable, show, disable, show, exit
    String input = "1\nTestService\n3\n2\nTestService\n3\n0\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    AutoLoginManager.menu(scanner, passwordManager);
    String output = outContent.toString();
    assertTrue("Should show enabled message", output.contains("Auto-login enabled"));
    assertTrue("Should show disabled message", output.contains("Auto-login disabled"));
  }
}

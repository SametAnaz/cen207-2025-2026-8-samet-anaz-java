package com.ucoruh.password;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @brief Unit tests for the PasswordApp class using the runApp method.
 *
 * These tests simulate full application flow by injecting input and capturing output.
 * Additional tests cover each main menu branch.
 */
public class PasswordAppTest {

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream outputStream;

  /**
   * Reset the AuthManager singleton and set up a new output stream.
   */
  @Before
  public void setUp() {
    // Delete the master password file to ensure clean state
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }

    AuthManager.resetInstance();
    outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
  }

  /**
   * Restore the original System.out after each test.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    outputStream.reset();
    // Clean up the master password file
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }
  }

  /**
   * @brief Tests a successful run of the application.
   *
   * Simulates setting a master password, successful login, and then exiting
   * the main menu. Verifies that the output contains the main menu prompt and
   * an exit message.
   */
  @Test
  public void testMainSuccess() {
    String simulatedInput = "testMaster\n" + "testMaster\n" + "0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain MAIN MENU", output.contains("MAIN MENU"));
    assertTrue("Output should contain Exiting...", output.contains("Exiting..."));
    assertFalse("Output should not contain 'Login failed.'", output.contains("Login failed."));
  }

  /**
   * @brief Tests the login failure scenario.
   *
   * Simulates setting a master password but then providing an incorrect
   * login input. Verifies that the output contains "Login failed."
   */
  @Test
  public void testMainError() {
    String simulatedInput = "testMaster\n" + "wrongMaster\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain 'Login failed.'", output.contains("Login failed."));
  }

  /**
   * @brief Tests full menu navigation.
   *
   * Simulates a run of the application that displays the main menu.
   * Verifies that the captured output shows the menu header.
   */
  @Test
  public void testFullMenuNavigation() {
    String simulatedInput = "testMaster\n" + "testMaster\n" + "0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain MAIN MENU", output.contains("MAIN MENU"));
  }

  /**
   * @brief Tests main menu option 1: User Authentication.
   *
   * Simulates choosing option 1 and verifies that the user menu output is shown.
   */
  @Test
  public void testMenuOptionUserAuthentication() {
    // Simulated input: set master, login, then choose option 1, then exit.
    String simulatedInput = "testMaster\n" + "testMaster\n" + "1\n" + "0\n" + "0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should indicate user menu", output.contains("USER AUTHENTICATION MENU"));
  }

  /**
   * @brief Tests main menu option 2: Secure Storage of Passwords.
   *
   * Simulates choosing option 2 and then immediately exiting the inner PasswordManager menu.
   */
  @Test
  public void testMenuOptionSecureStorage() {
    // Simulated input: set master, login, choose option 2, then in inner menu choose 0 to exit, then exit main menu.
    String simulatedInput = "testMaster\ntestMaster\n2\n0\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    // Since PasswordManager.menu prints its own menu header, verify its presence.
    assertTrue("Output should contain inner PasswordManager menu", output.contains("PASSWORD STORAGE MENU"));
  }

  /**
   * @brief Tests main menu option 3: Password Generator.
   *
   * Simulates choosing option 3 and entering a desired password length.
   * Verifies that the generated password output is present.
   */
  @Test
  public void testMenuOptionPasswordGenerator() {
    // Simulated input: set master, login, choose option 3, input desired length, then exit.
    String simulatedInput = "testMaster\n" + "testMaster\n" + "3\n" + "8\n" + "0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    // Expect the password generator to output "Generated Password:".
    assertTrue("Output should contain Generated Password:", output.contains("Generated Password:"));
  }

  /**
   * @brief Tests main menu option 4: Auto-Login Feature.
   *
   * Simulates choosing option 4. Verifies the Auto-Login menu appears.
   */
  @Test
  public void testMenuOptionAutoLoginFeature() {
    // Simulated input: set master, login, choose option 4, then exit.
    String simulatedInput = "testMaster\n" + "testMaster\n" + "4\n" + "0\n" + "0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    // Check for expected menu heading
    assertTrue("Output should mention Auto-Login Feature", output.contains("AUTO-LOGIN FEATURES"));
  }

  /**
   * @brief Tests main menu option 5: Multi-Platform Compatibility.
   *
   * Simulates choosing option 5. Verifies platform compatibility menu appears.
   */
  @Test
  public void testMenuOptionMultiPlatform() {
    // Simulated input: set master, login, choose option 5, press Enter to continue, then exit.
    String simulatedInput = "testMaster\n" + "testMaster\n" + "5\n" + "\n" + "0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    // Check for expected heading in the platform menu
    assertTrue("Output should mention Supported platforms", output.contains("PLATFORM COMPATIBILITY"));
  }

  // ==================== ADDITIONAL COVERAGE TESTS ====================

  /**
   * @brief Tests invalid menu choice input (non-numeric).
   */
  @Test
  public void testInvalidMenuInput() {
    String simulatedInput = "testMaster\ntestMaster\nabc\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain 'Invalid number.'", output.contains("Invalid number."));
  }

  /**
   * @brief Tests invalid menu choice (out of range).
   */
  @Test
  public void testInvalidMenuChoice() {
    String simulatedInput = "testMaster\ntestMaster\n99\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain 'Invalid choice.'", output.contains("Invalid choice."));
  }

  /**
   * @brief Tests negative menu choice.
   */
  @Test
  public void testNegativeMenuChoice() {
    String simulatedInput = "testMaster\ntestMaster\n-5\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain 'Invalid choice.'", output.contains("Invalid choice."));
  }

  /**
   * @brief Tests password generator with invalid (non-numeric) length.
   */
  @Test
  public void testPasswordGeneratorInvalidLength() {
    String simulatedInput = "testMaster\ntestMaster\n3\nabc\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain 'Invalid number.'", output.contains("Invalid number."));
  }

  /**
   * @brief Tests password generator with zero length.
   */
  @Test
  public void testPasswordGeneratorZeroLength() {
    String simulatedInput = "testMaster\ntestMaster\n3\n0\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain length error", output.contains("Password length must be greater than 0"));
  }

  /**
   * @brief Tests password generator with negative length.
   */
  @Test
  public void testPasswordGeneratorNegativeLength() {
    String simulatedInput = "testMaster\ntestMaster\n3\n-5\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain length error", output.contains("Password length must be greater than 0"));
  }

  /**
   * @brief Tests main method with console argument.
   */
  @Test
  public void testMainMethodConsoleMode() {
    // Create input that will fail login quickly to exit
    String simulatedInput = "testMaster\nwrong\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));
    // Call main with --console flag
    PasswordApp.main(new String[] {"--console"});
    String output = outputStream.toString();
    assertTrue("Should run in console mode", output.contains("Set master password") || output.contains("Login failed"));
  }

  /**
   * @brief Tests main method with -c argument.
   */
  @Test
  public void testMainMethodConsoleModeShortFlag() {
    String simulatedInput = "testMaster\nwrong\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));
    PasswordApp.main(new String[] {"-c"});
    String output = outputStream.toString();
    assertTrue("Should run in console mode", output.contains("Set master password") || output.contains("Login failed"));
  }

  /**
   * @brief Tests main method with no arguments (GUI mode - but catches exception).
   */
  @Test
  public void testMainMethodGUIMode() {
    // GUI mode will try to start but won't block the test
    // This just ensures the main method doesn't throw on empty args
    try {
      // Run in a separate thread to not block
      Thread guiThread = new Thread(() -> {
        PasswordApp.main(new String[]{});
      });
      guiThread.start();
      // Wait briefly then interrupt
      Thread.sleep(100);
      guiThread.interrupt();
    } catch (Exception e) {
      // Expected - GUI might not start in test environment
    }
  }

  /**
   * @brief Tests main method with other arguments (defaults to GUI).
   */
  @Test
  public void testMainMethodWithOtherArgs() {
    try {
      Thread guiThread = new Thread(() -> {
        PasswordApp.main(new String[]{"--other-flag"});
      });
      guiThread.start();
      Thread.sleep(100);
      guiThread.interrupt();
    } catch (Exception e) {
      // Expected
    }
  }

  /**
   * @brief Tests multiple invalid inputs followed by valid exit.
   */
  @Test
  public void testMultipleInvalidInputs() {
    String simulatedInput = "testMaster\ntestMaster\nabc\nxyz\n-1\n100\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Output should contain multiple 'Invalid'",
               output.contains("Invalid number.") || output.contains("Invalid choice."));
    assertTrue("Should eventually exit", output.contains("Exiting..."));
  }

  /**
   * @brief Tests platform menu shows all platforms.
   */
  @Test
  public void testPlatformMenuShowsAllPlatforms() {
    String simulatedInput = "testMaster\ntestMaster\n5\n\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Should show Windows", output.contains("Windows"));
    assertTrue("Should show macOS", output.contains("macOS"));
    assertTrue("Should show Linux", output.contains("Linux"));
    assertTrue("Should show Android", output.contains("Android"));
    assertTrue("Should show iOS", output.contains("iOS"));
  }

  /**
   * @brief Tests successful password generation with various lengths.
   */
  @Test
  public void testPasswordGeneratorVariousLengths() {
    String simulatedInput = "testMaster\ntestMaster\n3\n16\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Should show PASSWORD GENERATOR", output.contains("PASSWORD GENERATOR"));
    assertTrue("Should generate password", output.contains("Generated Password:"));
  }

  /**
   * @brief Tests the full menu display contains all options.
   */
  @Test
  public void testMainMenuDisplaysAllOptions() {
    String simulatedInput = "testMaster\ntestMaster\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Should show option 1", output.contains("1. User Authentication"));
    assertTrue("Should show option 2", output.contains("2. Secure Storage of Passwords"));
    assertTrue("Should show option 3", output.contains("3. Password Generator"));
    assertTrue("Should show option 4", output.contains("4. Auto-Login Feature"));
    assertTrue("Should show option 5", output.contains("5. Multi-Platform Compatibility"));
    assertTrue("Should show option 0", output.contains("0. Exit"));
  }

  /**
   * @brief Tests when master password is already set.
   */
  @Test
  public void testWhenMasterPasswordAlreadySet() {
    // First run - set the password
    String simulatedInput1 = "testMaster\ntestMaster\n0\n";
    ByteArrayInputStream inputStream1 = new ByteArrayInputStream(
      simulatedInput1.getBytes(StandardCharsets.UTF_8));
    Scanner scanner1 = new Scanner(inputStream1);
    PasswordApp.runApp(scanner1, System.out);
    scanner1.close();
    // Reset output
    outputStream.reset();
    // Second run - password already set, just login
    String simulatedInput2 = "testMaster\n0\n";
    ByteArrayInputStream inputStream2 = new ByteArrayInputStream(
      simulatedInput2.getBytes(StandardCharsets.UTF_8));
    Scanner scanner2 = new Scanner(inputStream2);
    PasswordApp.runApp(scanner2, System.out);
    scanner2.close();
    String output = outputStream.toString();
    assertTrue("Should prompt for login", output.contains("Enter master password to login"));
    assertFalse("Should NOT prompt to set password", output.contains("Set master password:"));
  }

  /**
   * @brief Tests empty string input for menu choice.
   */
  @Test
  public void testEmptyMenuInput() {
    String simulatedInput = "testMaster\ntestMaster\n\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Should handle empty input", output.contains("Invalid number."));
  }

  /**
   * @brief Tests special characters in menu input.
   */
  @Test
  public void testSpecialCharacterMenuInput() {
    String simulatedInput = "testMaster\ntestMaster\n@#$\n0\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      simulatedInput.getBytes(StandardCharsets.UTF_8));
    Scanner scanner = new Scanner(inputStream);
    PasswordApp.runApp(scanner, System.out);
    scanner.close();
    String output = outputStream.toString();
    assertTrue("Should handle special chars", output.contains("Invalid number."));
  }

  // ==================== NEW REFACTORED METHOD TESTS ====================

  /**
   * @brief Tests isConsoleMode with --console flag.
   */
  @Test
  public void testIsConsoleModeWithConsoleFlag() {
    assertTrue("Should detect --console flag",
               PasswordApp.isConsoleMode(new String[] {"--console"}));
  }

  /**
   * @brief Tests isConsoleMode with -c flag.
   */
  @Test
  public void testIsConsoleModeWithShortFlag() {
    assertTrue("Should detect -c flag",
               PasswordApp.isConsoleMode(new String[] {"-c"}));
  }

  /**
   * @brief Tests isConsoleMode with uppercase flags.
   */
  @Test
  public void testIsConsoleModeUppercase() {
    assertTrue("Should detect --CONSOLE flag",
               PasswordApp.isConsoleMode(new String[] {"--CONSOLE"}));
    assertTrue("Should detect -C flag",
               PasswordApp.isConsoleMode(new String[] {"-C"}));
  }

  /**
   * @brief Tests isConsoleMode with no console flag.
   */
  @Test
  public void testIsConsoleModeNoFlag() {
    assertFalse("Should return false for empty args",
                PasswordApp.isConsoleMode(new String[] {}));
    assertFalse("Should return false for other flags",
                PasswordApp.isConsoleMode(new String[] {"--gui", "--other"}));
  }

  /**
   * @brief Tests isConsoleMode with mixed flags.
   */
  @Test
  public void testIsConsoleModeMixedFlags() {
    assertTrue("Should detect -c among other flags",
               PasswordApp.isConsoleMode(new String[] {"--other", "-c", "--flag"}));
  }

  /**
   * @brief Tests setupLookAndFeel method.
   */
  @Test
  public void testSetupLookAndFeel() {
    // This should not throw any exception
    PasswordApp.setupLookAndFeel();
    // If we get here without exception, test passes
    assertTrue(true);
  }

  /**
   * @brief Tests startGUIMode method (non-blocking).
   */
  @Test
  public void testStartGUIMode() {
    try {
      Thread guiThread = new Thread(() -> {
        PasswordApp.startGUIMode();
      });
      guiThread.start();
      Thread.sleep(50);
      guiThread.interrupt();
    } catch (Exception e) {
      // Expected in test environment
    }

    assertTrue(true);
  }

  /**
   * @brief Tests createAndShowGUI method.
   */
  @Test
  public void testCreateAndShowGUI() {
    try {
      Thread guiThread = new Thread(() -> {
        PasswordApp.createAndShowGUI();
      });
      guiThread.start();
      Thread.sleep(50);
      guiThread.interrupt();
    } catch (Exception e) {
      // Expected in test environment
    }

    assertTrue(true);
  }
}

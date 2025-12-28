package com.ucoruh.password;

import static org.junit.Assert.*;
import java.util.Scanner;
import java.io.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @brief Unit tests for the AuthManager class.
 */
public class AuthManagerTest {

  private AuthManager auth;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  /**
   * Setup before each test.
   */
  @Before
  public void setUp() {
    // Remove existing master password file for clean tests
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }

    AuthManager.resetInstance();
    auth = AuthManager.getInstance();
    System.setOut(new PrintStream(outContent));
  }

  /**
   * Cleanup after each test.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    AuthManager.resetInstance();
    // Clean up test file after tests
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }
  }

  /**
   * Tests creation of master password and login functionality.
   */
  @Test
  public void testCreateAndLogin() {
    // Simulate user input: first setting then verifying the master password.
    String simulatedInput = "testPassword\ntestPassword\n";
    Scanner scanner = new Scanner(simulatedInput);
    auth.createMasterPassword(scanner);
    boolean loginSuccessful = auth.login(scanner);
    assertTrue("User should be able to login with the correct master password", loginSuccessful);
    scanner.close();
  }

  /**
   * Tests failed login with incorrect password.
   */
  @Test
  public void testLoginFailure() {
    // Set a master password
    Scanner setScanner = new Scanner("correctPassword\n");
    auth.createMasterPassword(setScanner);
    setScanner.close();
    // Try to login with wrong password
    Scanner loginScanner = new Scanner("wrongPassword\n");
    boolean loginResult = auth.login(loginScanner);
    loginScanner.close();
    assertFalse("Login should fail with incorrect password", loginResult);
  }

  /**
   * Tests isMasterPasswordSet method for false case.
   */
  @Test
  public void testIsMasterPasswordSetFalse() {
    // Force a clean state by deleting the file and resetting
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }

    AuthManager.resetInstance();
    AuthManager freshAuth = AuthManager.getInstance();
    assertFalse("New AuthManager instance should not have master password set", freshAuth.isMasterPasswordSet());
  }

  /**
   * Tests isMasterPasswordSet method for true case.
   */
  @Test
  public void testIsMasterPasswordSetTrue() {
    // Set master password
    Scanner scanner = new Scanner("somePassword\n");
    auth.createMasterPassword(scanner);
    scanner.close();
    assertTrue("AuthManager should have master password set after createMasterPassword", auth.isMasterPasswordSet());
  }

  /**
   * Tests the resetInstance method.
   */
  @Test
  public void testResetInstance() {
    // Set master password in first instance
    Scanner scanner = new Scanner("testPassword\n");
    auth.createMasterPassword(scanner);
    scanner.close();
    // Reset instance and delete the file
    AuthManager.resetInstance();
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }

    AuthManager newAuth = AuthManager.getInstance();
    assertFalse("New instance after reset should not have master password set", newAuth.isMasterPasswordSet());
  }

  /**
   * Tests getMasterPassword method.
   */
  @Test
  public void testGetMasterPassword() {
    String password = "secretMaster";
    Scanner scanner = new Scanner(password + "\n");
    auth.createMasterPassword(scanner);
    // Login to get the actual master password (not the hash)
    scanner = new Scanner(password + "\n");
    auth.login(scanner);
    scanner.close();
    assertEquals("getMasterPassword should return the correct master password", password, auth.getMasterPassword());
  }

  /**
   * Tests userMenu method output.
   */
  @Test
  public void testUserMenu() {
    // First set up the master password
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Provide input for the menu: select option 0 (back to main menu)
    Scanner scanner = new Scanner("0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("userMenu should output USER AUTHENTICATION MENU",
               output.contains("USER AUTHENTICATION MENU"));
  }

  // ==================== CHANGE MASTER PASSWORD TESTS ====================

  /**
   * Tests successful master password change.
   */
  @Test
  public void testChangeMasterPasswordSuccess() {
    String oldPassword = "oldPassword";
    String newPassword = "newPassword";
    // Set initial password
    Scanner setupScanner = new Scanner(oldPassword + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Login to set plaintext password
    Scanner loginScanner = new Scanner(oldPassword + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Change password: current, new, confirm
    String changeInput = oldPassword + "\n" + newPassword + "\n" + newPassword + "\n";
    Scanner changeScanner = new Scanner(changeInput);
    boolean result = auth.changeMasterPassword(changeScanner);
    changeScanner.close();
    assertTrue("Password change should succeed", result);
    String output = outContent.toString();
    assertTrue("Should show success message", output.contains("Master password changed successfully"));
  }

  /**
   * Tests password change with incorrect current password.
   */
  @Test
  public void testChangeMasterPasswordWrongCurrent() {
    String oldPassword = "oldPassword";
    // Set initial password
    Scanner setupScanner = new Scanner(oldPassword + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    // Try to change with wrong current password
    String changeInput = "wrongCurrent\nnewPass\nnewPass\n";
    Scanner changeScanner = new Scanner(changeInput);
    boolean result = auth.changeMasterPassword(changeScanner);
    changeScanner.close();
    assertFalse("Password change should fail with wrong current", result);
    String output = outContent.toString();
    assertTrue("Should show incorrect password message", output.contains("Incorrect current password"));
  }

  /**
   * Tests password change when new passwords don't match.
   */
  @Test
  public void testChangeMasterPasswordMismatch() {
    String oldPassword = "oldPassword";
    // Set initial password
    Scanner setupScanner = new Scanner(oldPassword + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Login to set plaintext password
    Scanner loginScanner = new Scanner(oldPassword + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Change password with mismatched confirm
    String changeInput = oldPassword + "\nnewPass1\nnewPass2\n";
    Scanner changeScanner = new Scanner(changeInput);
    boolean result = auth.changeMasterPassword(changeScanner);
    changeScanner.close();
    assertFalse("Password change should fail when passwords don't match", result);
    String output = outContent.toString();
    assertTrue("Should show mismatch message", output.contains("Passwords do not match"));
  }

  /**
   * Tests password change with hashed current password verification.
   */
  @Test
  public void testChangeMasterPasswordWithHashedCurrent() {
    String oldPassword = "hashedTest";
    String newPassword = "newHashedPass";
    // Set initial password (this stores the hash)
    Scanner setupScanner = new Scanner(oldPassword + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Don't login - masterPassword remains hashed
    outContent.reset();
    // Change password providing plaintext (will be hashed and compared)
    String changeInput = oldPassword + "\n" + newPassword + "\n" + newPassword + "\n";
    Scanner changeScanner = new Scanner(changeInput);
    boolean result = auth.changeMasterPassword(changeScanner);
    changeScanner.close();
    assertTrue("Password change should succeed with hashed comparison", result);
  }

  // ==================== USER MENU TESTS ====================

  /**
   * Tests userMenu with invalid number input.
   */
  @Test
  public void testUserMenuInvalidNumber() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    // Invalid input then exit
    Scanner scanner = new Scanner("abc\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show invalid number message", output.contains("Invalid number"));
  }

  /**
   * Tests userMenu with invalid choice.
   */
  @Test
  public void testUserMenuInvalidChoice() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    // Invalid choice then exit
    Scanner scanner = new Scanner("99\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show invalid choice message", output.contains("Invalid choice"));
  }

  /**
   * Tests userMenu option 1 - Change Master Password.
   */
  @Test
  public void testUserMenuChangePassword() {
    String password = "testPassword";
    String newPassword = "newTestPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Login to set plaintext
    Scanner loginScanner = new Scanner(password + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Select option 1, provide password change input, then exit
    String menuInput = "1\n" + password + "\n" + newPassword + "\n" + newPassword + "\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show password changed message", output.contains("Master password changed successfully"));
  }

  /**
   * Tests userMenu option 2 - Test Authentication with correct password.
   */
  @Test
  public void testUserMenuTestAuthSuccess() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Login to set plaintext
    Scanner loginScanner = new Scanner(password + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Select option 2, provide correct password, then exit
    String menuInput = "2\n" + password + "\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show authentication successful", output.contains("Authentication successful"));
  }

  /**
   * Tests userMenu option 2 - Test Authentication with wrong password.
   */
  @Test
  public void testUserMenuTestAuthFailed() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    // Select option 2, provide wrong password, then exit
    String menuInput = "2\nwrongPassword\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show authentication failed", output.contains("Authentication failed"));
  }

  /**
   * Tests userMenu option 2 with hashed password verification.
   */
  @Test
  public void testUserMenuTestAuthWithHashedPassword() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Don't login - keep password hashed
    outContent.reset();
    // Select option 2, provide password (will hash and compare), then exit
    String menuInput = "2\n" + password + "\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show authentication successful with hashed comparison",
               output.contains("Authentication successful"));
  }

  /**
   * Tests multiple menu operations.
   */
  @Test
  public void testUserMenuMultipleOperations() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Login to set plaintext
    Scanner loginScanner = new Scanner(password + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Multiple operations: invalid, test auth, invalid choice, exit
    String menuInput = "abc\n2\n" + password + "\n99\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should handle multiple operations", output.contains("USER AUTHENTICATION MENU"));
  }

  // ==================== LOAD/SAVE ERROR HANDLING TESTS ====================

  /**
   * Tests that master password is loaded from file on new instance.
   */
  @Test
  public void testLoadMasterPasswordFromFile() {
    String password = "persistedPassword";
    // Set password in first instance
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Reset instance (but keep file)
    AuthManager.resetInstance();
    AuthManager newAuth = AuthManager.getInstance();
    assertTrue("New instance should load password from file", newAuth.isMasterPasswordSet());
  }

  /**
   * Tests isMasterPasswordSet with empty string.
   */
  @Test
  public void testIsMasterPasswordSetEmptyString() {
    // Create a file with empty content
    try {
      File file = new File("master-password.txt");
      FileWriter writer = new FileWriter(file);
      writer.write("");
      writer.close();
      AuthManager.resetInstance();
      AuthManager emptyAuth = AuthManager.getInstance();
      assertFalse("Empty password file should result in not set", emptyAuth.isMasterPasswordSet());
    } catch (IOException e) {
      fail("Should not throw exception");
    }
  }

  /**
   * Tests login success stores plaintext password.
   */
  @Test
  public void testLoginStoresPlaintext() {
    String password = "plaintextTest";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    Scanner loginScanner = new Scanner(password + "\n");
    boolean success = auth.login(loginScanner);
    loginScanner.close();
    assertTrue("Login should succeed", success);
    assertEquals("After login, getMasterPassword should return plaintext", password, auth.getMasterPassword());
  }

  /**
   * Tests negative menu choice.
   */
  @Test
  public void testUserMenuNegativeChoice() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("-1\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show invalid choice for negative", output.contains("Invalid choice"));
  }

  /**
   * Tests the full flow: create, login, change, verify.
   */
  @Test
  public void testFullAuthenticationFlow() {
    String initialPassword = "initial123";
    String newPassword = "changed456";
    // Create master password
    Scanner createScanner = new Scanner(initialPassword + "\n");
    auth.createMasterPassword(createScanner);
    createScanner.close();
    assertTrue("Password should be set", auth.isMasterPasswordSet());
    // Login
    Scanner loginScanner = new Scanner(initialPassword + "\n");
    assertTrue("Login should succeed", auth.login(loginScanner));
    loginScanner.close();
    // Change password
    String changeInput = initialPassword + "\n" + newPassword + "\n" + newPassword + "\n";
    Scanner changeScanner = new Scanner(changeInput);
    assertTrue("Password change should succeed", auth.changeMasterPassword(changeScanner));
    changeScanner.close();
    // Verify new password works
    AuthManager.resetInstance();
    AuthManager freshAuth = AuthManager.getInstance();
    Scanner verifyScanner = new Scanner(newPassword + "\n");
    assertTrue("Login with new password should succeed", freshAuth.login(verifyScanner));
    verifyScanner.close();
  }

  /**
   * Tests menu shows all options.
   */
  @Test
  public void testUserMenuShowsAllOptions() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show option 1", output.contains("1. Change Master Password"));
    assertTrue("Should show option 2", output.contains("2. Test Authentication"));
    assertTrue("Should show option 0", output.contains("0. Back to Main Menu"));
  }

  /**
   * Tests empty input in menu.
   */
  @Test
  public void testUserMenuEmptyInput() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should handle empty input", output.contains("Invalid number"));
  }

  /**
   * Tests special character input in menu.
   */
  @Test
  public void testUserMenuSpecialCharInput() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("@#$\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should handle special chars", output.contains("Invalid number"));
  }

  // ==================== ADDITIONAL COVERAGE TESTS ====================

  /**
   * Tests changeMasterPassword with plaintext password comparison.
   */
  @Test
  public void testChangeMasterPasswordPlaintextComparison() {
    String password = "plaintextTest";
    String newPassword = "newPlaintext";
    // Set up and login to get plaintext stored
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    Scanner loginScanner = new Scanner(password + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Now change - the current check should match plaintext
    String changeInput = password + "\n" + newPassword + "\n" + newPassword + "\n";
    Scanner changeScanner = new Scanner(changeInput);
    boolean result = auth.changeMasterPassword(changeScanner);
    changeScanner.close();
    assertTrue("Should succeed with plaintext comparison", result);
  }

  /**
   * Tests userMenu option 2 with plaintext password after login.
   */
  @Test
  public void testUserMenuTestAuthWithPlaintextAfterLogin() {
    String password = "testPlaintext";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    // Login to store plaintext
    Scanner loginScanner = new Scanner(password + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Test authentication - should match plaintext directly
    String menuInput = "2\n" + password + "\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show authentication successful", output.contains("Authentication successful"));
  }

  /**
   * Tests multiple consecutive menu operations.
   */
  @Test
  public void testUserMenuConsecutiveOperations() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    Scanner loginScanner = new Scanner(password + "\n");
    auth.login(loginScanner);
    loginScanner.close();
    outContent.reset();
    // Test auth twice, then exit
    String menuInput = "2\n" + password + "\n2\n" + password + "\n0\n";
    Scanner scanner = new Scanner(menuInput);
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    // Should have multiple successes
    int count = output.split("Authentication successful").length - 1;
    assertTrue("Should show multiple auth successes", count >= 2);
  }

  /**
   * Tests getMasterPassword returns null before setup.
   */
  @Test
  public void testGetMasterPasswordBeforeSetup() {
    // Fresh instance without password
    File file = new File("master-password.txt");

    if (file.exists()) {
      file.delete();
    }

    AuthManager.resetInstance();
    AuthManager freshAuth = AuthManager.getInstance();
    String masterPwd = freshAuth.getMasterPassword();
    assertNull("Should be null before setup", masterPwd);
  }

  /**
   * Tests large choice number in menu.
   */
  @Test
  public void testUserMenuLargeChoiceNumber() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("999999\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show invalid choice", output.contains("Invalid choice"));
  }

  /**
   * Tests floating point input in menu.
   */
  @Test
  public void testUserMenuFloatInput() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("1.5\n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show invalid number", output.contains("Invalid number"));
  }

  /**
   * Tests whitespace-only input in menu.
   */
  @Test
  public void testUserMenuWhitespaceInput() {
    String password = "testPassword";
    Scanner setupScanner = new Scanner(password + "\n");
    auth.createMasterPassword(setupScanner);
    setupScanner.close();
    outContent.reset();
    Scanner scanner = new Scanner("   \n0\n");
    auth.userMenu(scanner);
    scanner.close();
    String output = outContent.toString();
    assertTrue("Should show invalid number", output.contains("Invalid number"));
  }
}

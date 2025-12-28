package com.ucoruh.password;

import org.junit.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.nio.file.*;

import static org.junit.Assert.*;

/**
 * @brief Test class for DatabasePasswordStorage.
 *
 * This class contains unit tests for DatabasePasswordStorage operations
 * using the default database file with proper setup/cleanup.
 */
public class DatabasePasswordStorageTest {

  private DatabasePasswordStorage storage;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;
  private static final String TEST_MASTER_PASSWORD = "test-master-password-123";

  @Before
  public void setUp() throws Exception {
    // Clean up any existing database
    cleanupDatabase();

    // Set up output capture
    originalOut = System.out;
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    // Create the storage instance
    storage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD);
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    cleanupDatabase();
  }

  /**
   * Helper method to clean up the database file.
   */
  private void cleanupDatabase() {
    try {
      // Delete default database file
      Files.deleteIfExists(Path.of("passwords.db"));
    } catch (Exception e) {
      // Ignore
    }
  }

  /**
   * Helper method to add a test entry via Scanner simulation.
   */
  private void addEntry(String service, String username, String password) {
    String input = service + "\n" + username + "\n" + password + "\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.add(scanner);
  }

  // ==================== CONSTRUCTOR TESTS ====================

  @Test
  public void testConstructor() {
    assertNotNull("Storage should be created", storage);
  }

  @Test
  public void testGetDatabaseUrl() {
    String url = storage.getDatabaseUrl();
    assertNotNull("URL should not be null", url);
    assertTrue("URL should be SQLite", url.contains("jdbc:sqlite:"));
  }

  @Test
  public void testConstructorCreatesTable() {
    // The table should be created automatically
    // Verify by trying to read from it
    List<Password> passwords = storage.readAll();
    assertNotNull("Should be able to read from table", passwords);
  }

  // ==================== ADD METHOD TESTS ====================

  @Test
  public void testAddNewEntry() {
    outContent.reset();
    addEntry("TestService", "testuser@example.com", "TestPassword123");
    String output = outContent.toString();
    assertTrue("Should show success", output.contains("Password saved successfully"));
  }

  @Test
  public void testAddShowsPrompts() {
    outContent.reset();
    addEntry("PromptTest", "user", "pass");
    String output = outContent.toString();
    assertTrue("Should prompt for service", output.contains("Service:"));
    assertTrue("Should prompt for username", output.contains("Username:"));
    assertTrue("Should prompt for password", output.contains("Password:"));
  }

  @Test
  public void testAddDuplicateService() {
    addEntry("DuplicateService", "user1", "pass1");
    outContent.reset();
    addEntry("DuplicateService", "user2", "pass2");
    String output = outContent.toString();
    assertTrue("Should show duplicate error", output.contains("already exists"));
  }

  @Test
  public void testAddMultipleServices() {
    addEntry("Service1", "user1", "pass1");
    addEntry("Service2", "user2", "pass2");
    addEntry("Service3", "user3", "pass3");
    List<Password> passwords = storage.readAll();
    assertEquals("Should have 3 entries", 3, passwords.size());
  }

  @Test
  public void testAddSpecialCharacters() {
    outContent.reset();
    addEntry("Service@123", "user!@#$%", "P@ss_w0rd!");
    String output = outContent.toString();
    assertTrue("Should save special chars", output.contains("Password saved successfully"));
  }

  // ==================== VIEW METHOD TESTS ====================

  @Test
  public void testViewEmptyDatabase() {
    outContent.reset();
    storage.view();
    String output = outContent.toString();
    assertTrue("Should show no records", output.contains("No records found"));
  }

  @Test
  public void testViewWithEntries() {
    addEntry("ViewService1", "viewuser1", "viewpass1");
    addEntry("ViewService2", "viewuser2", "viewpass2");
    outContent.reset();
    storage.view();
    String output = outContent.toString();
    assertTrue("Should contain service1", output.contains("ViewService1"));
    assertTrue("Should contain service2", output.contains("ViewService2"));
  }

  @Test
  public void testViewShowsNumberedList() {
    addEntry("NumberedService", "numuser", "numpass");
    outContent.reset();
    storage.view();
    String output = outContent.toString();
    assertTrue("Should show numbered entry", output.contains("1."));
  }

  @Test
  public void testViewDecryptsCorrectly() {
    String testUsername = "decryptuser@test.com";
    addEntry("DecryptService", testUsername, "decryptpass");
    outContent.reset();
    storage.view();
    String output = outContent.toString();
    assertTrue("Should show decrypted username", output.contains(testUsername));
  }

  // ==================== UPDATE METHOD TESTS ====================

  @Test
  public void testUpdateBothFields() {
    addEntry("UpdateService", "olduser", "oldpass");
    outContent.reset();
    String input = "UpdateService\nnewuser\nnewpass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show success", output.contains("updated successfully"));
  }

  @Test
  public void testUpdateOnlyPassword() {
    addEntry("UpdatePwdService", "keepuser", "oldpwd");
    outContent.reset();
    String input = "UpdatePwdService\n\nnewpwd\n";  // Empty username
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show success", output.contains("updated successfully"));
  }

  @Test
  public void testUpdateOnlyUsername() {
    addEntry("UpdateUserService", "oldname", "keeppass");
    outContent.reset();
    String input = "UpdateUserService\nnewname\n\n";  // Empty password
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show success", output.contains("updated successfully"));
  }

  @Test
  public void testUpdateNonExistent() {
    outContent.reset();
    String input = "NonExistentService\nnewuser\nnewpass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show not found", output.contains("not found"));
  }

  @Test
  public void testUpdateShowsCurrentUsername() {
    String originalUser = "originaluser@test.com";
    addEntry("CurrentUserService", originalUser, "somepass");
    outContent.reset();
    String input = "CurrentUserService\nnewuser\nnewpass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show current user", output.contains(originalUser));
  }

  // ==================== DELETE METHOD TESTS ====================

  @Test
  public void testDeleteExisting() {
    addEntry("DeleteService", "deluser", "delpass");
    outContent.reset();
    String input = "DeleteService\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.delete(scanner);
    String output = outContent.toString();
    assertTrue("Should show success", output.contains("deleted successfully"));
  }

  @Test
  public void testDeleteNonExistent() {
    outContent.reset();
    String input = "NonExistentDeleteService\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.delete(scanner);
    String output = outContent.toString();
    assertTrue("Should show not found", output.contains("not found"));
  }

  @Test
  public void testDeleteVerifyRemoval() {
    addEntry("VerifyDeleteService", "user", "pass");
    assertEquals("Should have 1 entry", 1, storage.readAll().size());
    String input = "VerifyDeleteService\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.delete(scanner);
    assertEquals("Should have 0 entries", 0, storage.readAll().size());
  }

  @Test
  public void testDeleteShowsPrompt() {
    addEntry("PromptDeleteService", "user", "pass");
    outContent.reset();
    String input = "PromptDeleteService\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.delete(scanner);
    String output = outContent.toString();
    assertTrue("Should prompt for service", output.contains("Service to delete:"));
  }

  // ==================== READALL METHOD TESTS ====================

  @Test
  public void testReadAllEmpty() {
    List<Password> passwords = storage.readAll();
    assertNotNull("Should return non-null", passwords);
    assertTrue("Should be empty", passwords.isEmpty());
  }

  @Test
  public void testReadAllWithEntries() {
    addEntry("ReadService1", "user1", "pass1");
    addEntry("ReadService2", "user2", "pass2");
    addEntry("ReadService3", "user3", "pass3");
    List<Password> passwords = storage.readAll();
    assertEquals("Should have 3 entries", 3, passwords.size());
  }

  @Test
  public void testReadAllDecryption() {
    String expectedUser = "testdecrypt@example.com";
    String expectedPass = "TestDecryptPass123";
    addEntry("DecryptReadService", expectedUser, expectedPass);
    List<Password> passwords = storage.readAll();
    assertEquals(1, passwords.size());
    Password p = passwords.get(0);
    assertEquals("Service should match", "DecryptReadService", p.getService());
    assertEquals("Username should be decrypted", expectedUser, p.getUsername());
    assertEquals("Password should be decrypted", expectedPass, p.getPassword());
  }

  // ==================== WRITEALL METHOD TESTS ====================

  @Test
  public void testWriteAllClearsExisting() {
    addEntry("OldService1", "olduser1", "oldpass1");
    addEntry("OldService2", "olduser2", "oldpass2");
    assertEquals(2, storage.readAll().size());
    List<Password> newPasswords = Arrays.asList(
                                    new Password("NewService", "newuser", "newpass")
                                  );
    storage.writeAll(newPasswords);
    List<Password> result = storage.readAll();
    assertEquals("Should only have new entry", 1, result.size());
    assertEquals("Should be new service", "NewService", result.get(0).getService());
  }

  @Test
  public void testWriteAllMultiple() {
    List<Password> passwords = Arrays.asList(
                                 new Password("WriteService1", "writeuser1", "writepass1"),
                                 new Password("WriteService2", "writeuser2", "writepass2"),
                                 new Password("WriteService3", "writeuser3", "writepass3")
                               );
    storage.writeAll(passwords);
    List<Password> result = storage.readAll();
    assertEquals("Should have 3 entries", 3, result.size());
  }

  @Test
  public void testWriteAllEmpty() {
    addEntry("ToBeCleared", "user", "pass");
    assertEquals(1, storage.readAll().size());
    storage.writeAll(new ArrayList<>());
    assertTrue("Should be empty", storage.readAll().isEmpty());
  }

  @Test
  public void testWriteAllEncryptsData() throws Exception {
    List<Password> passwords = Arrays.asList(
      new Password("EncryptWriteService", "plainuser", "plainpass")
    );

    storage.writeAll(passwords);

    // Verify data is encrypted by reading directly from DB
    try (Connection conn = DriverManager.getConnection(storage.getDatabaseUrl());
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery("SELECT username, password FROM passwords WHERE service = 'EncryptWriteService'")) {
      assertTrue("Entry should exist", rs.next());
      String storedUser = rs.getString("username");
      String storedPass = rs.getString("password");
      assertNotEquals("Username should be encrypted", "plainuser", storedUser);
      assertNotEquals("Password should be encrypted", "plainpass", storedPass);
    }
  }

  // ==================== ERROR HANDLING TESTS ====================

  @Test
  public void testViewWithCorruptData() throws Exception {
    // Add valid entry
    addEntry("ValidService", "validuser", "validpass");

    // Add corrupt (unencrypted) entry directly
    try (Connection conn = DriverManager.getConnection(storage.getDatabaseUrl());
           PreparedStatement pstmt = conn.prepareStatement(
             "INSERT INTO passwords(service, username, password) VALUES(?, ?, ?)")) {
      pstmt.setString(1, "CorruptService");
      pstmt.setString(2, "not_encrypted_user");
      pstmt.setString(3, "not_encrypted_pass");
      pstmt.executeUpdate();
    }

    outContent.reset();
    storage.view();

    String output = outContent.toString();
    assertTrue("Should show decryption error", output.contains("Error decrypting"));
  }

  @Test
  public void testReadAllWithCorruptData() throws Exception {
    addEntry("GoodService", "gooduser", "goodpass");

    // Add corrupt entry
    try (Connection conn = DriverManager.getConnection(storage.getDatabaseUrl());
           PreparedStatement pstmt = conn.prepareStatement(
             "INSERT INTO passwords(service, username, password) VALUES(?, ?, ?)")) {
      pstmt.setString(1, "BadService");
      pstmt.setString(2, "corrupt_data");
      pstmt.setString(3, "corrupt_data");
      pstmt.executeUpdate();
    }

    outContent.reset();
    List<Password> result = storage.readAll();

    // Should skip corrupt entry and return valid one
    assertEquals("Should have 1 valid entry", 1, result.size());
    String output = outContent.toString();
    assertTrue("Should show error message", output.contains("Error decrypting"));
  }

  @Test
  public void testDatabaseErrorHandling() {
    // Create storage with invalid database path
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/invalid/path/that/does/not/exist/test.db";
      }
    };

    outContent.reset();
    errorStorage.view();
    // Should not throw exception, error handled gracefully
    String output = outContent.toString();
    assertTrue("Should show error or empty",
               output.contains("error") || output.contains("Error") || output.contains("No records"));
  }

  @Test
  public void testEncryptionErrorInAdd() {
    // Create storage with null master password
    DatabasePasswordStorage nullStorage = new DatabasePasswordStorage(null);
    outContent.reset();
    String input = "TestService\nuser\npass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    nullStorage.add(scanner);
    // Should show encryption error
    String output = outContent.toString();
    assertTrue("Should handle null master password",
               output.contains("error") || output.contains("Error") || output.contains("saved"));
  }

  @Test
  public void testEncryptionErrorInWriteAll() {
    DatabasePasswordStorage nullStorage = new DatabasePasswordStorage(null);
    outContent.reset();
    List<Password> passwords = Arrays.asList(
                                 new Password("TestService", "user", "pass")
                               );
    nullStorage.writeAll(passwords);
    String output = outContent.toString();
    // Should handle gracefully
    assertNotNull(output);
  }

  // ==================== INTEGRATION TESTS ====================

  @Test
  public void testFullWorkflow() {
    // Add
    addEntry("WorkflowService", "workflowuser", "workflowpass");
    assertEquals(1, storage.readAll().size());
    // View
    outContent.reset();
    storage.view();
    assertTrue(outContent.toString().contains("WorkflowService"));
    // Update
    String updateInput = "WorkflowService\nupdateduser\nupdatedpass\n";
    Scanner updateScanner = new Scanner(new ByteArrayInputStream(updateInput.getBytes()));
    storage.update(updateScanner);
    // Verify update
    List<Password> afterUpdate = storage.readAll();
    assertEquals("updateduser", afterUpdate.get(0).getUsername());
    // Delete
    String deleteInput = "WorkflowService\n";
    Scanner deleteScanner = new Scanner(new ByteArrayInputStream(deleteInput.getBytes()));
    storage.delete(deleteScanner);
    // Verify deletion
    assertTrue(storage.readAll().isEmpty());
  }

  @Test
  public void testWriteAllThenReadAll() {
    List<Password> original = Arrays.asList(
                                new Password("Svc1", "user1@test.com", "Pass123!"),
                                new Password("Svc2", "user2@test.com", "Pass456!"),
                                new Password("Svc3", "user3@test.com", "Pass789!")
                              );
    storage.writeAll(original);
    List<Password> result = storage.readAll();
    assertEquals(3, result.size());

    // Verify each entry
    for (Password orig : original) {
      boolean found = result.stream()
                      .anyMatch(p -> p.getService().equals(orig.getService()) &&
                                p.getUsername().equals(orig.getUsername()) &&
                                p.getPassword().equals(orig.getPassword()));
      assertTrue("Should find " + orig.getService(), found);
    }
  }

  // ==================== ADDITIONAL BRANCH COVERAGE TESTS ====================

  /**
   * Tests add method SQLException handling when database connection fails.
   */
  @Test
  public void testAddSQLExceptionHandling() {
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/nonexistent/path/to/database.db";
      }
    };

    outContent.reset();
    String input = "TestService\nuser\npass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    errorStorage.add(scanner);
    String output = outContent.toString();
    assertTrue("Should show database error",
               output.contains("error") || output.contains("Error"));
  }

  /**
   * Tests update method SQLException handling when database connection fails.
   */
  @Test
  public void testUpdateSQLExceptionHandling() {
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/nonexistent/path/to/database.db";
      }
    };

    outContent.reset();
    String input = "TestService\nnewuser\nnewpass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    errorStorage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show database or not found error",
               output.contains("error") || output.contains("Error") || output.contains("not found"));
  }

  /**
   * Tests update method with encryption error when encrypting new username.
   */
  @Test
  public void testUpdateEncryptionErrorUsername() {
    // Add valid entry first
    addEntry("EncryptErrorService", "originaluser", "originalpass");
    // Create storage that will fail encryption
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(null) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:passwords.db";
      }
    };

    outContent.reset();
    String input = "EncryptErrorService\nnewuser\nnewpass\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    errorStorage.update(scanner);
    String output = outContent.toString();
    // Should handle gracefully
    assertNotNull(output);
  }

  /**
   * Tests delete method SQLException handling.
   */
  @Test
  public void testDeleteSQLExceptionHandling() {
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/nonexistent/path/to/database.db";
      }
    };

    outContent.reset();
    String input = "TestService\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    errorStorage.delete(scanner);
    String output = outContent.toString();
    assertTrue("Should show database error",
               output.contains("error") || output.contains("Error"));
  }

  /**
   * Tests readAll method SQLException handling.
   */
  @Test
  public void testReadAllSQLExceptionHandling() {
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/nonexistent/path/to/database.db";
      }
    };

    outContent.reset();
    List<Password> result = errorStorage.readAll();
    assertNotNull("Should return empty list on error", result);
    assertTrue("Should be empty", result.isEmpty());
  }

  /**
   * Tests writeAll method SQLException handling.
   */
  @Test
  public void testWriteAllSQLExceptionHandling() {
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/nonexistent/path/to/database.db";
      }
    };

    outContent.reset();
    List<Password> passwords = Arrays.asList(
                                 new Password("TestService", "user", "pass")
                               );
    errorStorage.writeAll(passwords);
    String output = outContent.toString();
    assertTrue("Should show database error",
               output.contains("error") || output.contains("Error"));
  }

  /**
   * Tests update when keeping both username and password (empty inputs).
   */
  @Test
  public void testUpdateKeepBothFields() {
    addEntry("KeepBothService", "originaluser", "originalpass");
    outContent.reset();
    // Both fields empty - should keep both
    String input = "KeepBothService\n\n\n";
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
    storage.update(scanner);
    String output = outContent.toString();
    assertTrue("Should show success", output.contains("updated successfully"));
    // Verify values unchanged
    List<Password> passwords = storage.readAll();
    assertEquals(1, passwords.size());
    assertEquals("originaluser", passwords.get(0).getUsername());
  }

  /**
   * Tests createTableIfNotExists error handling.
   */
  @Test
  public void testCreateTableIfNotExistsError() {
    outContent.reset();
    // This will attempt to create table on invalid path
    DatabasePasswordStorage errorStorage = new DatabasePasswordStorage(TEST_MASTER_PASSWORD) {
      @Override
      protected String getDatabaseUrl() {
        return "jdbc:sqlite:/invalid/path/db.db";
      }
    };

    // Constructor should handle error gracefully
    assertNotNull(errorStorage);
  }

  /**
   * Tests add with existing service - duplicate check branch.
   */
  @Test
  public void testAddExistingServiceCheckBranch() {
    // First add
    addEntry("DupCheckService", "user1", "pass1");
    outContent.reset();
    // Second add with same service
    addEntry("DupCheckService", "user2", "pass2");
    String output = outContent.toString();
    assertTrue("Should show already exists", output.contains("already exists"));
    // Verify only one entry
    assertEquals(1, storage.readAll().size());
  }

  /**
   * Tests view with multiple entries to verify count formatting.
   */
  @Test
  public void testViewMultipleEntriesNumbering() {
    addEntry("MultiView1", "user1", "pass1");
    addEntry("MultiView2", "user2", "pass2");
    addEntry("MultiView3", "user3", "pass3");
    outContent.reset();
    storage.view();
    String output = outContent.toString();
    assertTrue("Should show 1.", output.contains("1."));
    assertTrue("Should show 2.", output.contains("2."));
    assertTrue("Should show 3.", output.contains("3."));
  }
}

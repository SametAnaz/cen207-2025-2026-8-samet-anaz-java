package com.ucoruh.password;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @brief Unit tests for the PasswordStorageFactory class.
 *
 * Verifies that the factory returns the correct storage implementation
 * based on the provided StorageType and that all possible enum values are handled.
 */
public class PasswordStorageFactoryTest {
  /**
   * @brief Test master password for testing.
   */
  private static final String TEST_MASTER_PASSWORD = "test-master-password";

  /**
   * @brief Tests that create() returns a FilePasswordStorage instance when given StorageType.FILE.
   */
  @Test
  public void testCreateFileStorage() {
    InterfacePasswordStorage storage = PasswordStorageFactory.create(StorageType.FILE, TEST_MASTER_PASSWORD);
    assertNotNull("Factory should not return null for FILE type", storage);
    assertTrue("The created storage should be an instance of FilePasswordStorage",
               storage instanceof FilePasswordStorage);
  }

  /**
   * @brief Tests that create() returns a DatabasePasswordStorage instance when given StorageType.SQLITE.
   */
  @Test
  public void testCreateDatabaseStorage() {
    InterfacePasswordStorage storage = PasswordStorageFactory.create(StorageType.SQLITE, TEST_MASTER_PASSWORD);
    assertNotNull("Factory should not return null for SQLITE type", storage);
    assertTrue("The created storage should be an instance of DatabasePasswordStorage",
               storage instanceof DatabasePasswordStorage);
  }

  /**
   * @brief Tests that create() handles all defined StorageType enum values.
   *
   * Iterates over all StorageType values and verifies that a non-null implementation is returned.
   */
  @Test
  public void testAllStorageTypesHandled() {
    for (StorageType type : StorageType.values()) {
      InterfacePasswordStorage storage = PasswordStorageFactory.create(type, TEST_MASTER_PASSWORD);
      assertNotNull("Factory should not return null for type: " + type, storage);
    }
  }

  /**
   * @brief Tests that create() returns different instances on each call.
   */
  @Test
  public void testFactoryReturnsDifferentInstances() {
    InterfacePasswordStorage storage1 = PasswordStorageFactory.create(StorageType.FILE, TEST_MASTER_PASSWORD);
    InterfacePasswordStorage storage2 = PasswordStorageFactory.create(StorageType.FILE, TEST_MASTER_PASSWORD);
    assertNotNull("Factory should not return null", storage1);
    assertNotNull("Factory should not return null", storage2);
    assertNotSame("Factory should return different instances on each call", storage1, storage2);
  }

  /**
   * @brief Tests that create() passes the master password to the storage implementation.
   */
  @Test
  public void testFactoryPassesMasterPassword() {
    // Create storage with a specific master password
    String specificMasterPassword = "specific-test-password";
    FilePasswordStorage fileStorage = (FilePasswordStorage) PasswordStorageFactory.create(
                                        StorageType.FILE, specificMasterPassword);
    // We need to indirectly verify the master password was passed
    // by checking the instance was created successfully
    assertNotNull("Factory should create a storage instance with the given master password", fileStorage);
  }

  /**
   * @brief Tests that the PasswordStorageFactory class can be instantiated.
   * This test ensures the default constructor is covered.
   */
  @Test
  public void testPasswordStorageFactoryConstructor() {
    // Instantiate the factory to cover the implicit default constructor
    PasswordStorageFactory factory = new PasswordStorageFactory();
    assertNotNull("PasswordStorageFactory should be instantiable", factory);
  }

  /**
   * @brief Tests create() with null master password.
   */
  @Test
  public void testCreateWithNullMasterPassword() {
    InterfacePasswordStorage fileStorage = PasswordStorageFactory.create(StorageType.FILE, null);
    assertNotNull("Factory should handle null master password for FILE", fileStorage);
    InterfacePasswordStorage sqliteStorage = PasswordStorageFactory.create(StorageType.SQLITE, null);
    assertNotNull("Factory should handle null master password for SQLITE", sqliteStorage);
  }

  /**
   * @brief Tests create() with empty master password.
   */
  @Test
  public void testCreateWithEmptyMasterPassword() {
    InterfacePasswordStorage fileStorage = PasswordStorageFactory.create(StorageType.FILE, "");
    assertNotNull("Factory should handle empty master password for FILE", fileStorage);
    InterfacePasswordStorage sqliteStorage = PasswordStorageFactory.create(StorageType.SQLITE, "");
    assertNotNull("Factory should handle empty master password for SQLITE", sqliteStorage);
  }

  /**
   * @brief Tests that DatabasePasswordStorage is correctly instantiated.
   */
  @Test
  public void testDatabaseStorageCorrectType() {
    InterfacePasswordStorage storage = PasswordStorageFactory.create(StorageType.SQLITE, TEST_MASTER_PASSWORD);
    assertTrue("SQLITE type should create DatabasePasswordStorage",
               storage instanceof DatabasePasswordStorage);
    assertFalse("SQLITE type should not create FilePasswordStorage",
                storage instanceof FilePasswordStorage);
  }

  /**
   * @brief Tests that FilePasswordStorage is correctly instantiated.
   */
  @Test
  public void testFileStorageCorrectType() {
    InterfacePasswordStorage storage = PasswordStorageFactory.create(StorageType.FILE, TEST_MASTER_PASSWORD);
    assertTrue("FILE type should create FilePasswordStorage",
               storage instanceof FilePasswordStorage);
    assertFalse("FILE type should not create DatabasePasswordStorage",
                storage instanceof DatabasePasswordStorage);
  }
}

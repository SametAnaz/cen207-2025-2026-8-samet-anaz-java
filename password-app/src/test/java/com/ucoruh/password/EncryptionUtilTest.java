package com.ucoruh.password;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @brief Test class for EncryptionUtil.
 *
 * This class contains unit tests for the encryption and decryption functionality.
 */
public class EncryptionUtilTest {

  /**
   * @brief Tests the encryption and decryption functionality.
   *
   * This test verifies that a string encrypted with a master password
   * can be correctly decrypted back to the original string.
   */
  @Test
  public void testEncryptDecrypt() throws Exception {
    // Arrange
    String originalData = "test-data-123";
    String masterPassword = "master-password-123";

    // Act
    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    // Assert
    assertNotEquals("Encrypted data should be different from original", originalData, encrypted);
    assertEquals("Decrypted data should match original", originalData, decrypted);
  }

  /**
   * @brief Tests that different master passwords produce different encrypted results.
   */
  @Test
  public void testDifferentMasterPasswords() throws Exception {
    // Arrange
    String data = "test-data-123";
    String masterPassword1 = "master-password-1";
    String masterPassword2 = "master-password-2";

    // Act
    String encrypted1 = EncryptionUtil.encrypt(data, masterPassword1);
    String encrypted2 = EncryptionUtil.encrypt(data, masterPassword2);

    // Assert
    assertNotEquals("Different master passwords should produce different encrypted results", encrypted1, encrypted2);
  }

  /**
   * @brief Tests that the same data with the same master password produces the same encrypted result.
   */
  @Test
  public void testConsistentEncryption() throws Exception {
    // Arrange
    String data = "test-data-123";
    String masterPassword = "master-password-123";

    // Act
    String encrypted1 = EncryptionUtil.encrypt(data, masterPassword);
    String encrypted2 = EncryptionUtil.encrypt(data, masterPassword);

    // Assert
    assertEquals("Same data and master password should produce the same encrypted result", encrypted1, encrypted2);
  }

  /**
   * @brief Tests that wrong master password fails to decrypt correctly.
   */
  @Test
  public void testWrongMasterPassword() throws Exception {
    // Arrange
    String data = "test-data-123";
    String correctMasterPassword = "correct-password";
    String wrongMasterPassword = "wrong-password";

    // Act
    String encrypted = EncryptionUtil.encrypt(data, correctMasterPassword);

    // Assert
    try {
      EncryptionUtil.decrypt(encrypted, wrongMasterPassword);
      fail("Decryption with wrong master password should throw an exception");
    } catch (Exception e) {
      // Expected exception
    }
  }

  /**
   * @brief Tests the hash functionality.
   */
  @Test
  public void testHashString() {
    // Arrange
    String input = "password123";
    // Act
    String hash1 = EncryptionUtil.hashString(input);
    String hash2 = EncryptionUtil.hashString(input);
    // Assert
    assertNotNull("Hash should not be null", hash1);
    assertNotEquals("Hash should be different from input", input, hash1);
    assertEquals("Same input should produce the same hash", hash1, hash2);
    assertEquals("SHA-256 hash should be 64 characters long (hex string)", 64, hash1.length());
  }

  /**
   * @brief Tests empty string encryption and decryption.
   */
  @Test
  public void testEmptyStringEncryptDecrypt() throws Exception {
    // Arrange
    String originalData = "";
    String masterPassword = "master-password-123";

    // Act
    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    // Assert
    assertNotEquals("Encrypted empty string should not be empty", originalData, encrypted);
    assertEquals("Decrypted data should be empty string", originalData, decrypted);
  }

  /**
   * @brief Tests encryption with empty master password.
   */
  @Test
  public void testEmptyMasterPassword() throws Exception {
    // Arrange
    String originalData = "test-data-123";
    String masterPassword = "";

    // Act
    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    // Assert
    assertNotEquals("Encrypted data should be different from original", originalData, encrypted);
    assertEquals("Decrypted data should match original", originalData, decrypted);
  }

  /**
   * @brief Tests hash of empty string.
   */
  @Test
  public void testHashEmptyString() {
    // Arrange
    String input = "";
    // Act
    String hash = EncryptionUtil.hashString(input);
    // Assert
    assertNotNull("Hash of empty string should not be null", hash);
    assertNotEquals("Hash should not be empty", "", hash);
    assertEquals("SHA-256 hash should be 64 characters long", 64, hash.length());
  }

  // ==================== ADDITIONAL COVERAGE TESTS ====================

  /**
   * @brief Tests EncryptionUtil can be instantiated.
   */
  @Test
  public void testEncryptionUtilConstructor() {
    EncryptionUtil util = new EncryptionUtil();
    assertNotNull("EncryptionUtil should be instantiable", util);
  }

  /**
   * @brief Tests hash with various inputs to cover hex padding.
   */
  @Test
  public void testHashStringWithPaddingNeeded() {
    // Test multiple strings to ensure hex padding branch is covered
    // Some bytes will have single digit hex (0-15) and need padding
    String[] testInputs = {"a", "ab", "abc", "test", "1", "12", "123"};

    for (String input : testInputs) {
      String hash = EncryptionUtil.hashString(input);
      assertNotNull("Hash should not be null for: " + input, hash);
      assertEquals("SHA-256 hash should be 64 chars for: " + input, 64, hash.length());
      // Verify all characters are valid hex
      assertTrue("Hash should only contain hex characters",
                 hash.matches("[0-9a-f]+"));
    }
  }

  /**
   * @brief Tests hash produces valid hex output with potential leading zeros.
   */
  @Test
  public void testHashStringHexFormat() {
    // This input produces a hash with bytes that need leading zero padding
    String input = "test-padding-input";
    String hash = EncryptionUtil.hashString(input);
    // Each byte should be represented as 2 hex characters
    // If a byte is < 16, it needs a leading '0'
    assertNotNull(hash);
    assertEquals(64, hash.length());

    // Check that it's all lowercase hex
    for (char c : hash.toCharArray()) {
      assertTrue("Should be hex digit: " + c,
                 (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'));
    }
  }

  /**
   * @brief Tests encryption with special characters.
   */
  @Test
  public void testEncryptDecryptSpecialCharacters() throws Exception {
    String originalData = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
    String masterPassword = "special-chars-password";

    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    assertEquals("Decrypted special chars should match original", originalData, decrypted);
  }

  /**
   * @brief Tests encryption with unicode characters.
   */
  @Test
  public void testEncryptDecryptUnicode() throws Exception {
    String originalData = "Héllo Wörld 日本語 中文";
    String masterPassword = "unicode-password";

    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    assertEquals("Decrypted unicode should match original", originalData, decrypted);
  }

  /**
   * @brief Tests encryption with long data string.
   */
  @Test
  public void testEncryptDecryptLongString() throws Exception {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 1000; i++) {
      sb.append("This is a long test string ");
    }

    String originalData = sb.toString();
    String masterPassword = "long-string-password";

    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    assertEquals("Decrypted long string should match original", originalData, decrypted);
  }

  /**
   * @brief Tests encryption with numbers only.
   */
  @Test
  public void testEncryptDecryptNumbersOnly() throws Exception {
    String originalData = "12345678901234567890";
    String masterPassword = "numbers-password";

    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    assertEquals("Decrypted numbers should match original", originalData, decrypted);
  }

  /**
   * @brief Tests hash with different inputs produce different hashes.
   */
  @Test
  public void testHashStringDifferentInputs() {
    String hash1 = EncryptionUtil.hashString("input1");
    String hash2 = EncryptionUtil.hashString("input2");
    assertNotEquals("Different inputs should produce different hashes", hash1, hash2);
  }

  /**
   * @brief Tests hash with unicode input.
   */
  @Test
  public void testHashStringUnicode() {
    String input = "日本語テスト";
    String hash = EncryptionUtil.hashString(input);
    assertNotNull("Unicode hash should not be null", hash);
    assertEquals("SHA-256 hash should be 64 chars", 64, hash.length());
  }

  /**
   * @brief Tests hash with long input.
   */
  @Test
  public void testHashStringLongInput() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 1000; i++) {
      sb.append("long-input-");
    }

    String input = sb.toString();
    String hash = EncryptionUtil.hashString(input);
    assertNotNull("Long input hash should not be null", hash);
    assertEquals("SHA-256 hash should be 64 chars", 64, hash.length());
  }

  /**
   * @brief Tests encryption with whitespace.
   */
  @Test
  public void testEncryptDecryptWhitespace() throws Exception {
    String originalData = "   spaces   and\ttabs\nand\nnewlines   ";
    String masterPassword = "whitespace-password";

    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, masterPassword);

    assertEquals("Decrypted whitespace should match original", originalData, decrypted);
  }

  /**
   * @brief Tests that encrypted output is Base64.
   */
  @Test
  public void testEncryptedOutputIsBase64() throws Exception {
    String originalData = "test-data";
    String masterPassword = "test-password";

    String encrypted = EncryptionUtil.encrypt(originalData, masterPassword);

    // Base64 characters are A-Z, a-z, 0-9, +, /, =
    assertTrue("Encrypted output should be Base64",
               encrypted.matches("^[A-Za-z0-9+/=]+$"));
  }

  // ==================== ADDITIONAL LINE COVERAGE TESTS ====================

  /**
   * @brief Tests multiple encrypt/decrypt cycles.
   */
  @Test
  public void testMultipleEncryptDecryptCycles() throws Exception {
    String data = "cycle-test-data";
    String password = "cycle-password";

    for (int i = 0; i < 10; i++) {
      String encrypted = EncryptionUtil.encrypt(data, password);
      String decrypted = EncryptionUtil.decrypt(encrypted, password);
      assertEquals("Cycle " + i + " should decrypt correctly", data, decrypted);
    }
  }

  /**
   * @brief Tests hash with single character inputs.
   */
  @Test
  public void testHashSingleCharacters() {
    for (char c = 'a'; c <= 'z'; c++) {
      String hash = EncryptionUtil.hashString(String.valueOf(c));
      assertNotNull(hash);
      assertEquals(64, hash.length());
    }
  }

  /**
   * @brief Tests encrypt with very short master password.
   */
  @Test
  public void testEncryptShortMasterPassword() throws Exception {
    String data = "test-data";
    String shortPassword = "a";

    String encrypted = EncryptionUtil.encrypt(data, shortPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, shortPassword);

    assertEquals(data, decrypted);
  }

  /**
   * @brief Tests encrypt with very long master password.
   */
  @Test
  public void testEncryptLongMasterPassword() throws Exception {
    String data = "test-data";
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 1000; i++) {
      sb.append("longpassword");
    }

    String longPassword = sb.toString();

    String encrypted = EncryptionUtil.encrypt(data, longPassword);
    String decrypted = EncryptionUtil.decrypt(encrypted, longPassword);

    assertEquals(data, decrypted);
  }

  /**
   * @brief Tests hash produces deterministic output.
   */
  @Test
  public void testHashDeterministic() {
    String input = "deterministic-test";
    String hash1 = EncryptionUtil.hashString(input);
    String hash2 = EncryptionUtil.hashString(input);
    String hash3 = EncryptionUtil.hashString(input);
    assertEquals(hash1, hash2);
    assertEquals(hash2, hash3);
  }

  /**
   * @brief Tests encryption produces different output for different data.
   */
  @Test
  public void testEncryptDifferentData() throws Exception {
    String password = "test-password";

    String encrypted1 = EncryptionUtil.encrypt("data1", password);
    String encrypted2 = EncryptionUtil.encrypt("data2", password);

    assertNotEquals(encrypted1, encrypted2);
  }

  /**
   * @brief Tests hash with bytes that produce single hex digit.
   */
  @Test
  public void testHashWithLeadingZeros() {
    // These inputs are chosen to produce hash bytes < 16 (need leading zero)
    String[] inputs = {"zero", "hex", "pad", "test123", "abc123xyz"};

    for (String input : inputs) {
      String hash = EncryptionUtil.hashString(input);
      assertEquals("Hash should be exactly 64 chars", 64, hash.length());
      // All characters should be hex
      assertTrue(hash.matches("[0-9a-f]+"));
    }
  }

  /**
   * @brief Tests encryption with binary-like data string.
   */
  @Test
  public void testEncryptBinaryLikeData() throws Exception {
    String binaryData = "\u0000\u0001\u0002\u0003";
    String password = "binary-password";

    String encrypted = EncryptionUtil.encrypt(binaryData, password);
    String decrypted = EncryptionUtil.decrypt(encrypted, password);

    assertEquals(binaryData, decrypted);
  }

  /**
   * @brief Tests hash of binary-like input.
   */
  @Test
  public void testHashBinaryInput() {
    String binaryInput = "\u0000\u0001\u0002";
    String hash = EncryptionUtil.hashString(binaryInput);
    assertNotNull(hash);
    assertEquals(64, hash.length());
  }

  /**
   * @brief Tests encryption result is not the same as input.
   */
  @Test
  public void testEncryptionChangesData() throws Exception {
    String data = "sensitive-data";
    String password = "secret";

    String encrypted = EncryptionUtil.encrypt(data, password);

    assertNotEquals("Encrypted should differ from original", data, encrypted);
    assertFalse("Original should not be substring of encrypted", encrypted.contains(data));
  }

  /**
   * @brief Tests multiple different passwords on same data.
   */
  @Test
  public void testMultiplePasswordsOnSameData() throws Exception {
    String data = "same-data";

    String enc1 = EncryptionUtil.encrypt(data, "password1");
    String enc2 = EncryptionUtil.encrypt(data, "password2");
    String enc3 = EncryptionUtil.encrypt(data, "password3");

    assertNotEquals(enc1, enc2);
    assertNotEquals(enc2, enc3);
    assertNotEquals(enc1, enc3);
  }
}

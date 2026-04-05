package com.example.contactmanager.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link InputValidator}.
 *
 * Each public method is tested against:
 *  - Valid boundary inputs
 *  - Invalid / edge-case inputs (null, empty, too long, wrong format)
 *
 * These tests run on the JVM with no Android framework dependency.
 */
public class InputValidatorTest {

	// =========================================================================
	//  isValidName
	// =========================================================================

	@Test
	public void isValidName_typicalName_returnsTrue() {
		assertTrue(InputValidator.isValidName("Alice Smith"));
	}

	@Test
	public void isValidName_singleCharacter_returnsTrue() {
		assertTrue(InputValidator.isValidName("A"));
	}

	@Test
	public void isValidName_exactly100Characters_returnsTrue() {
		String name = repeat("a", 100);
		assertTrue(InputValidator.isValidName(name));
	}

	@Test
	public void isValidName_101Characters_returnsFalse() {
		String name = repeat("a", 101);
		assertFalse(InputValidator.isValidName(name));
	}

	@Test
	public void isValidName_null_returnsFalse() {
		assertFalse(InputValidator.isValidName(null));
	}

	@Test
	public void isValidName_emptyString_returnsFalse() {
		assertFalse(InputValidator.isValidName(""));
	}

	@Test
	public void isValidName_onlySpaces_returnsFalse() {
		assertFalse(InputValidator.isValidName("     "));
	}

	@Test
	public void isValidName_leadingAndTrailingSpaces_returnsTrueIfContentIsValid() {
		// "  Bob  " trims to "Bob" which is valid
		assertTrue(InputValidator.isValidName("  Bob  "));
	}

	@Test
	public void isValidName_nameWithNumbers_returnsTrue() {
		assertTrue(InputValidator.isValidName("R2D2"));
	}

	@Test
	public void isValidName_nameWithHyphen_returnsTrue() {
		assertTrue(InputValidator.isValidName("Mary-Jane"));
	}

	// =========================================================================
	//  isValidPhoneNumber
	// =========================================================================

	@Test
	public void isValidPhoneNumber_sevenDigits_returnsTrue() {
		assertTrue(InputValidator.isValidPhoneNumber("5551234"));
	}

	@Test
	public void isValidPhoneNumber_fifteenDigits_returnsTrue() {
		assertTrue(InputValidator.isValidPhoneNumber("123456789012345"));
	}

	@Test
	public void isValidPhoneNumber_withPlusPrefix_returnsTrue() {
		assertTrue(InputValidator.isValidPhoneNumber("+12025551234"));
	}

	@Test
	public void isValidPhoneNumber_withDashes_returnsTrue() {
		// 555-867-5309 → stripped to 5558675309 (10 digits) → valid
		assertTrue(InputValidator.isValidPhoneNumber("555-867-5309"));
	}

	@Test
	public void isValidPhoneNumber_withParenthesesAndSpaces_returnsTrue() {
		assertTrue(InputValidator.isValidPhoneNumber("(202) 555-0147"));
	}

	@Test
	public void isValidPhoneNumber_withDots_returnsTrue() {
		assertTrue(InputValidator.isValidPhoneNumber("555.867.5309"));
	}

	@Test
	public void isValidPhoneNumber_internationalFormat_returnsTrue() {
		assertTrue(InputValidator.isValidPhoneNumber("+44 7911 123456"));
	}

	@Test
	public void isValidPhoneNumber_null_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber(null));
	}

	@Test
	public void isValidPhoneNumber_emptyString_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber(""));
	}

	@Test
	public void isValidPhoneNumber_onlySpaces_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("   "));
	}

	@Test
	public void isValidPhoneNumber_sixDigits_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("123456"));
	}

	@Test
	public void isValidPhoneNumber_sixteenDigits_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("1234567890123456"));
	}

	@Test
	public void isValidPhoneNumber_lettersOnly_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("ABCDEFG"));
	}

	@Test
	public void isValidPhoneNumber_mixedLettersAndDigits_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("555-ABC-1234"));
	}

	@Test
	public void isValidPhoneNumber_justPlusSign_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("+"));
	}

	@Test
	public void isValidPhoneNumber_multiplePlusSigns_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("++12025551234"));
	}

	@Test
	public void isValidPhoneNumber_plusInMiddle_returnsFalse() {
		assertFalse(InputValidator.isValidPhoneNumber("1234+5678"));
	}

	// =========================================================================
	//  isValidGroupName
	// =========================================================================

	@Test
	public void isValidGroupName_typicalName_returnsTrue() {
		assertTrue(InputValidator.isValidGroupName("Friends"));
	}

	@Test
	public void isValidGroupName_singleCharacter_returnsTrue() {
		assertTrue(InputValidator.isValidGroupName("X"));
	}

	@Test
	public void isValidGroupName_exactly50Characters_returnsTrue() {
		String name = repeat("g", 50);
		assertTrue(InputValidator.isValidGroupName(name));
	}

	@Test
	public void isValidGroupName_51Characters_returnsFalse() {
		String name = repeat("g", 51);
		assertFalse(InputValidator.isValidGroupName(name));
	}

	@Test
	public void isValidGroupName_null_returnsFalse() {
		assertFalse(InputValidator.isValidGroupName(null));
	}

	@Test
	public void isValidGroupName_emptyString_returnsFalse() {
		assertFalse(InputValidator.isValidGroupName(""));
	}

	@Test
	public void isValidGroupName_onlySpaces_returnsFalse() {
		assertFalse(InputValidator.isValidGroupName("   "));
	}

	@Test
	public void isValidGroupName_nameWithNumbers_returnsTrue() {
		assertTrue(InputValidator.isValidGroupName("Team 5"));
	}

	// -------------------------------------------------------------------------
	//  Helper
	// -------------------------------------------------------------------------

	private static String repeat(String s, int times) {
		StringBuilder sb = new StringBuilder(s.length() * times);
		for (int i = 0; i < times; i++) sb.append(s);
		return sb.toString();
	}
}
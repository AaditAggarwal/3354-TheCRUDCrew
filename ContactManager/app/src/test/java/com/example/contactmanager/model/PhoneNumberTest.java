package com.example.contactmanager.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link PhoneNumber} model.
 *
 * Covers default state, parameterised construction, and all setters/getters.
 */
public class PhoneNumberTest {

	private PhoneNumber phoneNumber;

	@Before
	public void setUp() {
		phoneNumber = new PhoneNumber();
	}

	// -------------------------------------------------------------------------
	//  Construction
	// -------------------------------------------------------------------------

	@Test
	public void defaultConstructor_typeIsMobile() {
		assertEquals("mobile", phoneNumber.getType());
	}

	@Test
	public void defaultConstructor_numberIsNull() {
		assertNull(phoneNumber.getNumber());
	}

	@Test
	public void defaultConstructor_idIsZero() {
		assertEquals(0L, phoneNumber.getId());
	}

	@Test
	public void parameterisedConstructor_setsAllFields() {
		PhoneNumber p = new PhoneNumber(10L, 99L, "+12025551234", "work");
		assertEquals(10L,           p.getId());
		assertEquals(99L,           p.getContactId());
		assertEquals("+12025551234", p.getNumber());
		assertEquals("work",        p.getType());
	}

	// -------------------------------------------------------------------------
	//  Setters / Getters
	// -------------------------------------------------------------------------

	@Test
	public void setId_roundTrip() {
		phoneNumber.setId(7L);
		assertEquals(7L, phoneNumber.getId());
	}

	@Test
	public void setContactId_roundTrip() {
		phoneNumber.setContactId(42L);
		assertEquals(42L, phoneNumber.getContactId());
	}

	@Test
	public void setNumber_roundTrip() {
		phoneNumber.setNumber("0712345678");
		assertEquals("0712345678", phoneNumber.getNumber());
	}

	@Test
	public void setType_home_roundTrip() {
		phoneNumber.setType("home");
		assertEquals("home", phoneNumber.getType());
	}

	@Test
	public void setType_other_roundTrip() {
		phoneNumber.setType("other");
		assertEquals("other", phoneNumber.getType());
	}

	@Test
	public void setNumber_nullIsAccepted() {
		phoneNumber.setNumber(null);
		assertNull(phoneNumber.getNumber());
	}

	@Test
	public void setType_nullIsAccepted() {
		phoneNumber.setType(null);
		assertNull(phoneNumber.getType());
	}
}
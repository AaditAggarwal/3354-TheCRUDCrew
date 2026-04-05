package com.example.contactmanager.model;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ContactTest {

	private Contact contact;

	@Before
	public void setUp() {
		contact = new Contact();
	}

	// -------------------------------------------------------------------------
	//  Construction
	// -------------------------------------------------------------------------

	@Test
	public void defaultConstructor_setsGroupIdToMinusOne() {
		assertEquals("Default groupId should be -1", -1, contact.getGroupId());
	}

	@Test
	public void defaultConstructor_initializesEmptyPhoneList() {
		assertNotNull("Phone list should not be null", contact.getPhoneNumbers());
		assertTrue("Phone list should be empty", contact.getPhoneNumbers().isEmpty());
	}

	@Test
	public void defaultConstructor_blacklistedIsFalse() {
		assertFalse("New contact should not be blacklisted", contact.isBlacklisted());
	}

	@Test
	public void parameterisedConstructor_setsAllFields() {
		Contact c = new Contact(7L, "Alice", "content://photo/1", true, 3L);
		assertEquals(7L, c.getId());
		assertEquals("Alice", c.getName());
		assertEquals("content://photo/1", c.getPhotoUri());
		assertTrue(c.isBlacklisted());
		assertEquals(3L, c.getGroupId());
	}

	// -------------------------------------------------------------------------
	//  Setters / Getters
	// -------------------------------------------------------------------------

	@Test
	public void setId_andGetId_roundTrip() {
		contact.setId(42L);
		assertEquals(42L, contact.getId());
	}

	@Test
	public void setName_andGetName_roundTrip() {
		contact.setName("Bob");
		assertEquals("Bob", contact.getName());
	}

	@Test
	public void setPhotoUri_andGetPhotoUri_roundTrip() {
		contact.setPhotoUri("content://media/external/images/1");
		assertEquals("content://media/external/images/1", contact.getPhotoUri());
	}

	@Test
	public void setPhotoUri_nullIsAccepted() {
		contact.setPhotoUri(null);
		assertNull(contact.getPhotoUri());
	}

	@Test
	public void setBlacklisted_true_isReturnedByGetter() {
		contact.setBlacklisted(true);
		assertTrue(contact.isBlacklisted());
	}

	@Test
	public void setBlacklisted_false_isReturnedByGetter() {
		contact.setBlacklisted(true);
		contact.setBlacklisted(false);
		assertFalse(contact.isBlacklisted());
	}

	@Test
	public void setGroupId_andGetGroupId_roundTrip() {
		contact.setGroupId(5L);
		assertEquals(5L, contact.getGroupId());
	}

	@Test
	public void setGroupId_minusOne_representsNoGroup() {
		contact.setGroupId(-1L);
		assertEquals(-1L, contact.getGroupId());
	}

	// -------------------------------------------------------------------------
	//  Phone number management
	// -------------------------------------------------------------------------

	@Test
	public void addPhoneNumber_increasesList() {
		PhoneNumber p = new PhoneNumber(0, 0, "555-1234", "mobile");
		contact.addPhoneNumber(p);
		assertEquals(1, contact.getPhoneNumbers().size());
	}

	@Test
	public void addPhoneNumber_multipleNumbers_allStored() {
		contact.addPhoneNumber(new PhoneNumber(0, 0, "555-0001", "mobile"));
		contact.addPhoneNumber(new PhoneNumber(0, 0, "555-0002", "home"));
		contact.addPhoneNumber(new PhoneNumber(0, 0, "555-0003", "work"));
		assertEquals(3, contact.getPhoneNumbers().size());
	}

	@Test
	public void setPhoneNumbers_replacesList() {
		contact.addPhoneNumber(new PhoneNumber(0, 0, "old-number", "mobile"));
		List<PhoneNumber> newList = new ArrayList<>();
		newList.add(new PhoneNumber(0, 0, "new-number", "home"));
		contact.setPhoneNumbers(newList);
		assertEquals(1, contact.getPhoneNumbers().size());
		assertEquals("new-number", contact.getPhoneNumbers().get(0).getNumber());
	}

	// -------------------------------------------------------------------------
	//  getPrimaryPhoneNumber
	// -------------------------------------------------------------------------

	@Test
	public void getPrimaryPhoneNumber_emptyList_returnsNull() {
		assertNull(contact.getPrimaryPhoneNumber());
	}

	@Test
	public void getPrimaryPhoneNumber_singleEntry_returnsIt() {
		contact.addPhoneNumber(new PhoneNumber(0, 0, "123456789", "mobile"));
		assertEquals("123456789", contact.getPrimaryPhoneNumber());
	}

	@Test
	public void getPrimaryPhoneNumber_multipleEntries_returnsFirst() {
		contact.addPhoneNumber(new PhoneNumber(0, 0, "111", "mobile"));
		contact.addPhoneNumber(new PhoneNumber(0, 0, "222", "home"));
		assertEquals("111", contact.getPrimaryPhoneNumber());
	}

	// -------------------------------------------------------------------------
	//  getInitial
	// -------------------------------------------------------------------------

	@Test
	public void getInitial_normalName_returnsUppercaseFirstLetter() {
		contact.setName("charlie");
		assertEquals("C", contact.getInitial());
	}

	@Test
	public void getInitial_alreadyUppercase_unchanged() {
		contact.setName("Diana");
		assertEquals("D", contact.getInitial());
	}

	@Test
	public void getInitial_nullName_returnsQuestionMark() {
		contact.setName(null);
		assertEquals("?", contact.getInitial());
	}

	@Test
	public void getInitial_emptyName_returnsQuestionMark() {
		contact.setName("");
		assertEquals("?", contact.getInitial());
	}

	@Test
	public void getInitial_nameWithLeadingSpace_returnsUppercaseOfFirstChar() {
		// Leading space: first char is ' ', uppercase of ' ' is still ' '
		contact.setName(" Eve");
		assertEquals(" ", contact.getInitial());
	}
}
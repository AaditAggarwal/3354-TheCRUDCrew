package com.example.contactmanager.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroupTest {

	private Group group;

	@Before
	public void setUp() {
		group = new Group();
	}

	// -------------------------------------------------------------------------
	//  Construction
	// -------------------------------------------------------------------------

	@Test
	public void defaultConstructor_idIsZero() {
		assertEquals(0L, group.getId());
	}

	@Test
	public void defaultConstructor_nameIsNull() {
		assertNull(group.getName());
	}

	@Test
	public void defaultConstructor_contactCountIsZero() {
		assertEquals(0, group.getContactCount());
	}

	@Test
	public void parameterisedConstructor_setsIdAndName() {
		Group g = new Group(5L, "Friends");
		assertEquals(5L,       g.getId());
		assertEquals("Friends", g.getName());
	}

	@Test
	public void parameterisedConstructor_contactCountDefaultsToZero() {
		Group g = new Group(1L, "Work");
		assertEquals(0, g.getContactCount());
	}

	// -------------------------------------------------------------------------
	//  Setters / Getters
	// -------------------------------------------------------------------------

	@Test
	public void setId_roundTrip() {
		group.setId(12L);
		assertEquals(12L, group.getId());
	}

	@Test
	public void setName_roundTrip() {
		group.setName("Family");
		assertEquals("Family", group.getName());
	}

	@Test
	public void setContactCount_roundTrip() {
		group.setContactCount(7);
		assertEquals(7, group.getContactCount());
	}

	@Test
	public void setContactCount_zero_isAccepted() {
		group.setContactCount(5);
		group.setContactCount(0);
		assertEquals(0, group.getContactCount());
	}

	@Test
	public void setName_nullIsAccepted() {
		group.setName(null);
		assertNull(group.getName());
	}

	@Test
	public void setName_emptyStringIsAccepted() {
		group.setName("");
		assertEquals("", group.getName());
	}
}
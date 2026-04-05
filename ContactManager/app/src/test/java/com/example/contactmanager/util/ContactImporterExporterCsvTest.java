package com.example.contactmanager.util;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ContactImporterExporterCsvTest {

	// =========================================================================
	//  escapeCsv (private static)
	// =========================================================================

	@Test
	public void escapeCsv_plainValue_returnedUnchanged() throws Exception {
		assertEquals("hello", escape("hello"));
	}

	@Test
	public void escapeCsv_null_returnsEmptyString() throws Exception {
		assertEquals("", escape(null));
	}

	@Test
	public void escapeCsv_emptyString_returnsEmpty() throws Exception {
		assertEquals("", escape(""));
	}

	@Test
	public void escapeCsv_valueWithComma_wrapsInQuotes() throws Exception {
		assertEquals("\"Smith, Alice\"", escape("Smith, Alice"));
	}

	@Test
	public void escapeCsv_valueWithDoubleQuote_escapesAndWraps() throws Exception {
		// Input:  say "hi"
		// Output: "say ""hi"""
		assertEquals("\"say \"\"hi\"\"\"", escape("say \"hi\""));
	}

	@Test
	public void escapeCsv_valueWithNewline_wrapsInQuotes() throws Exception {
		String result = escape("line1\nline2");
		assertTrue(result.startsWith("\"") && result.endsWith("\""));
	}

	@Test
	public void escapeCsv_valueWithCommaAndQuote_escapesCorrectly() throws Exception {
		// Input:  he said "hello, world"
		// Output: "he said ""hello, world"""
		String result = escape("he said \"hello, world\"");
		assertTrue(result.startsWith("\""));
		assertTrue(result.contains("\"\"hello, world\"\""));
	}

	// =========================================================================
	//  parseCsvLine (private static)
	// =========================================================================

	@Test
	public void parseCsvLine_simpleSixFields_parsedCorrectly() throws Exception {
		String[] parts = parse("Alice,+12025551234,mobile,,0,-1");
		assertEquals(6, parts.length);
		assertEquals("Alice",        parts[0]);
		assertEquals("+12025551234", parts[1]);
		assertEquals("mobile",       parts[2]);
		assertEquals("",             parts[3]);
		assertEquals("0",            parts[4]);
		assertEquals("-1",           parts[5]);
	}

	@Test
	public void parseCsvLine_quotedFieldWithComma_treatedAsSingleField() throws Exception {
		String[] parts = parse("\"Smith, Bob\",555-1234,mobile,,0,-1");
		assertEquals(6, parts.length);
		assertEquals("Smith, Bob", parts[0]);
	}

	@Test
	public void parseCsvLine_quotedFieldWithEscapedQuote_unescaped() throws Exception {
		// CSV:  "say ""hi""",555,mobile,,0,-1
		String[] parts = parse("\"say \"\"hi\"\"\",555-0000,mobile,,0,-1");
		assertEquals("say \"hi\"", parts[0]);
	}

	@Test
	public void parseCsvLine_allEmptyFields_returnsEmptyStrings() throws Exception {
		String[] parts = parse(",,,,,");
		assertEquals(6, parts.length);
		for (String p : parts) assertEquals("", p);
	}

	@Test
	public void parseCsvLine_singleField_returnsOneElement() throws Exception {
		String[] parts = parse("OnlyOne");
		assertEquals(1, parts.length);
		assertEquals("OnlyOne", parts[0]);
	}

	@Test
	public void parseCsvLine_quotedEmptyField_returnsEmptyString() throws Exception {
		String[] parts = parse("\"\",555,mobile,,0,-1");
		assertEquals(6, parts.length);
		assertEquals("", parts[0]);
	}

	@Test
	public void parseCsvLine_photoUriField_preservedUnchanged() throws Exception {
		String uri = "content://media/external/images/media/42";
		String[] parts = parse("Alice,555,mobile," + uri + ",0,-1");
		assertEquals(uri, parts[3]);
	}

	// =========================================================================
	//  roundTrip: escape then parse gives back original value
	// =========================================================================

	@Test
	public void roundTrip_plainValue_unchanged() throws Exception {
		String original = "John";
		String csvRow   = escape(original) + ",555,mobile,,0,-1";
		String[] parts  = parse(csvRow);
		assertEquals(original, parts[0]);
	}

	@Test
	public void roundTrip_nameWithComma_unchanged() throws Exception {
		String original = "Doe, Jane";
		String csvRow   = escape(original) + ",555,mobile,,0,-1";
		String[] parts  = parse(csvRow);
		assertEquals(original, parts[0]);
	}

	@Test
	public void roundTrip_nameWithQuotes_unchanged() throws Exception {
		String original = "Bob \"The Builder\"";
		String csvRow   = escape(original) + ",555,mobile,,0,-1";
		String[] parts  = parse(csvRow);
		assertEquals(original, parts[0]);
	}

	// =========================================================================
	//  CSV format contract: header row structure
	// =========================================================================

	@Test
	public void headerRow_parsedFieldNames_matchExpectedOrder() throws Exception {
		String header = "Name,PhoneNumber,PhoneType,PhotoUri,Blacklisted,GroupId";
		String[] fields = parse(header);
		assertEquals(6,              fields.length);
		assertEquals("Name",         fields[0]);
		assertEquals("PhoneNumber",  fields[1]);
		assertEquals("PhoneType",    fields[2]);
		assertEquals("PhotoUri",     fields[3]);
		assertEquals("Blacklisted",  fields[4]);
		assertEquals("GroupId",      fields[5]);
	}

	// =========================================================================
	//  Reflection helpers
	// =========================================================================

	private static String escape(String value) throws Exception {
		Method m = ContactImporterExporter.class.getDeclaredMethod("escapeCsv", String.class);
		m.setAccessible(true);
		return (String) m.invoke(null, value);
	}

	private static String[] parse(String line) throws Exception {
		Method m = ContactImporterExporter.class.getDeclaredMethod("parseCsvLine", String.class);
		m.setAccessible(true);
		return (String[]) m.invoke(null, line);
	}
}
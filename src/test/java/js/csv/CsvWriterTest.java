package js.csv;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import js.lang.ConfigBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CsvWriterTest {
	private CsvWriter writer;
	private StringWriter output;

	@Before
	public void beforeTest() throws Exception {
		ConfigBuilder builder = new ConfigBuilder(new File("fixture/person.xml"));
		CsvDescriptor descriptor = new CsvDescriptor(builder.build());

		output = new StringWriter();
		writer = new CsvWriter(descriptor, output);
	}

	@After
	public void afterTest() throws IOException {
		writer.close();
	}
	
	@Test
	public void write_Standard() throws IOException {
		writer.write(new Person("John Doe", 54));
		assertEquals("Name,Age\r\nJohn Doe,54\r\n", output.toString());
	}
	
	@Test
	public void write_Escape() throws IOException {
		writer.write(new Person("Grand Doe, \"Elder\"", 77));
		assertEquals("Name,Age\r\n\"Grand Doe, \"\"Elder\"\"\",77\r\n", output.toString());
	}
	
	@Test
	public void write_EscapeCRLF() throws IOException {
		writer.write(new Person("Baby Doe\r\nSon of John.", 54));
		assertEquals("Name,Age\r\n\"Baby Doe\r\nSon of John.\",54\r\n", output.toString());
	}
	
	@Test
	public void write_EscapeCommaAndCRLF() throws IOException {
		writer.write(new Person("Baby Doe,\r\nSon of John.", 54));
		assertEquals("Name,Age\r\n\"Baby Doe,\r\nSon of John.\",54\r\n", output.toString());
	}
	
	@Test
	public void write_EscapeQuoteAndCRLF() throws IOException {
		writer.write(new Person("Baby Doe,\r\n\"Son of John\".", 54));
		assertEquals("Name,Age\r\n\"Baby Doe,\r\n\"\"Son of John\"\".\",54\r\n", output.toString());
	}
	
	@Test
	public void write_NullField() throws IOException {
		writer.write(new Person(null, 54));
		assertEquals("Name,Age\r\nnull,54\r\n", output.toString());
	}

	@Test(expected=IllegalArgumentException.class)
	public void write_NullObject() throws IOException {
		writer.write(null);
	}
	
	// --------------------------------------------------------------------------------------------
	// FIXTURE

	@SuppressWarnings("unused")
	private static class Person {
		private final String name;
		private final int age;

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}
}

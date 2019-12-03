package js.tiny.plugin.csv.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import js.csv.CsvDescriptor;
import js.csv.CsvFactory;
import js.csv.CsvWriter;
import js.lang.ConfigBuilder;
import js.tiny.plugin.csv.unit.fixture.Person;
import js.util.Classes;

public class CsvWriterTest {
	private CsvFactory csvFactory;
	private CsvWriter<Person> writer;
	private StringWriter output;

	@Before
	public void beforeTest() throws Exception {
		csvFactory = Classes.loadService(CsvFactory.class);

		ConfigBuilder builder = new ConfigBuilder(new File("fixture/person.xml"));
		CsvDescriptor<Person> descriptor = csvFactory.getDescriptor(builder.build());

		output = new StringWriter();
		writer = csvFactory.getWriter(descriptor, output);
	}

	@After
	public void afterTest() throws IOException {
		writer.close();
	}

	@Test
	public void write_Standard() throws IOException {
		writer.write(new Person("John Doe", 54));
		assertEquals("\"NAME\",\"AGE\"\r\n\"John Doe\",\"54\"\r\n", output.toString());
	}

	@Test
	public void write_Escape() throws IOException {
		writer.write(new Person("Grand Doe, \"Elder\"", 77));
		assertEquals("\"NAME\",\"AGE\"\r\n\"Grand Doe, \"\"Elder\"\"\",\"77\"\r\n", output.toString());
	}

	@Test
	public void write_EscapeCRLF() throws IOException {
		writer.write(new Person("Baby Doe\r\nSon of John.", 54));
		assertEquals("\"NAME\",\"AGE\"\r\n\"Baby Doe\r\nSon of John.\",\"54\"\r\n", output.toString());
	}

	@Test
	public void write_EscapeCommaAndCRLF() throws IOException {
		writer.write(new Person("Baby Doe,\r\nSon of John.", 54));
		assertEquals("\"NAME\",\"AGE\"\r\n\"Baby Doe,\r\nSon of John.\",\"54\"\r\n", output.toString());
	}

	@Test
	public void write_EscapeQuoteAndCRLF() throws IOException {
		writer.write(new Person("Baby Doe,\r\n\"Son of John\".", 54));
		assertEquals("\"NAME\",\"AGE\"\r\n\"Baby Doe,\r\n\"\"Son of John\"\".\",\"54\"\r\n", output.toString());
	}

	@Test
	public void write_NullField() throws IOException {
		writer.write(new Person(null, 54));
		assertEquals("\"NAME\",\"AGE\"\r\n\"null\",\"54\"\r\n", output.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void write_NullObject() throws IOException {
		writer.write(null);
	}
}

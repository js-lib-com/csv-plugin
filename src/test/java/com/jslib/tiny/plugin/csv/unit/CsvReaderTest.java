package com.jslib.tiny.plugin.csv.unit;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jslib.api.csv.CsvFactory;
import com.jslib.api.csv.CsvReader;
import com.jslib.lang.Config;
import com.jslib.lang.ConfigBuilder;
import com.jslib.tiny.plugin.csv.CsvConfig;
import com.jslib.tiny.plugin.csv.unit.fixture.Person;
import com.jslib.util.Classes;

public class CsvReaderTest {
	private static final String DESCRIPTOR = "" + //
			"<csv>" + //
			"	<repository path='fixture' files-pattern='*.xml' />" + //
			"</csv>";

	private static Config config;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		ConfigBuilder builder = new ConfigBuilder(DESCRIPTOR);
		config = builder.build();
	}

	private CsvFactory csvFactory;

	@Before
	public void beforeTest() {
		csvFactory = Classes.loadService(CsvFactory.class);
	}

	@Test
	public void importPersons() throws Exception {
		CsvConfig csvConfig = new CsvConfig();
		csvConfig.config(config);

		InputStream stream = new FileInputStream("fixture/persons.csv");
		CsvReader<Person> reader = csvFactory.getReader(csvConfig.getDescriptor(Person.class), stream);

		List<Person> persons = new ArrayList<>();
		for (Person person : reader) {
			persons.add(person);
		}
		reader.close();

		assertEquals(6, persons.size());
		assertEquals("John Doe", persons.get(0).getName());
		assertEquals(54, persons.get(0).getAge());
		assertEquals("Jane Doe", persons.get(1).getName());
		assertEquals(50, persons.get(1).getAge());
		assertEquals("", persons.get(2).getName());
		assertEquals(64, persons.get(2).getAge());
		assertEquals("Baby Doe", persons.get(3).getName());
		assertEquals(0, persons.get(3).getAge());
		assertEquals("John Doe, Sr.", persons.get(4).getName());
		assertEquals(77, persons.get(4).getAge());
		assertEquals("Lion, \"The Little Cat\"", persons.get(5).getName());
		assertEquals(4, persons.get(5).getAge());
	}

	@Test
	public void escapeNewLine() throws Exception {
		CsvConfig csvConfig = new CsvConfig();
		csvConfig.config(config);
		
		InputStream stream = new FileInputStream("fixture/escape-new-line.csv");
		CsvReader<Person> reader = csvFactory.getReader(csvConfig.getDescriptor(Person.class), stream);

		List<Person> persons = new ArrayList<>();
		for (Person person : reader) {
			persons.add(person);
		}
		reader.close();

		assertEquals(1, persons.size());
		assertEquals("Iulian\r\nRotaru", persons.get(0).getName());
	}
}

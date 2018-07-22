package js.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import js.core.Factory;
import js.unit.TestContext;
import js.util.Strings;

import org.junit.BeforeClass;
import org.junit.Test;

public class CsvReaderTest {
	private static final String DESCRIPTOR = "" + //
			"<test-config>" + //
			"	<managed-classes>" + //
			"		<csv interface='js.csv.CsvConfig' class='js.csv.CsvConfigImpl' />" + //
			"	</managed-classes>" + //
			"	<csv>" + //
			"		<repository path='fixture' files-pattern='*.xml' />" + //
			"	</csv>" + //
			"</test-config>";

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestContext.start(DESCRIPTOR);
	}

	@Test
	public void parseLine() throws IOException {
		String line = Strings.load(new File("fixture/bad-line.csv"));
		List<String> values = CsvReader.parseLine(line, ',');

		assertNotNull(values);
		assertEquals(36, values.size());
	}

	@Test
	public void importPersons() throws IOException {
		CsvConfig config = Factory.getInstance(CsvConfig.class);

		InputStream stream = new FileInputStream("fixture/persons.csv");
		CsvReader<Person> reader = new CsvReader<>(config.getDescriptor(Person.class), stream);

		List<Person> persons = new ArrayList<>();
		for (Person person : reader) {
			persons.add(person);
		}
		reader.close();

		assertEquals(6, persons.size());
		assertEquals("John Doe", persons.get(0).name);
		assertEquals(54, persons.get(0).age);
		assertEquals("Jane Doe", persons.get(1).name);
		assertEquals(50, persons.get(1).age);
		assertEquals("", persons.get(2).name);
		assertEquals(64, persons.get(2).age);
		assertEquals("Baby Doe", persons.get(3).name);
		assertEquals(0, persons.get(3).age);
		assertEquals("John Doe, Sr.", persons.get(4).name);
		assertEquals(77, persons.get(4).age);
		assertEquals("Lion, \"The Little Cat\"", persons.get(5).name);
		assertEquals(4, persons.get(5).age);
	}

	// --------------------------------------------------------------------------------------------
	// FIXTURE

	private static class Person {
		private String name;
		private int age;
	}
}

package js.tiny.plugin.csv.unit;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import js.csv.CsvFactory;
import js.csv.CsvReader;
import js.tiny.container.core.Factory;
import js.tiny.container.unit.TestContext;
import js.tiny.plugin.csv.CsvConfig;
import js.tiny.plugin.csv.unit.fixture.Person;
import js.util.Classes;

public class CsvReaderTest {
	private static final String DESCRIPTOR = "" + //
			"<test-config>" + //
			"	<managed-classes>" + //
			"		<csv class='js.tiny.plugin.csv.CsvConfig' />" + //
			"	</managed-classes>" + //
			"	<csv>" + //
			"		<repository path='fixture' files-pattern='*.xml' />" + //
			"	</csv>" + //
			"</test-config>";

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestContext.start(DESCRIPTOR);
	}

	private CsvFactory csvFactory;

	@Before
	public void beforeTest() {
		csvFactory = Classes.loadService(CsvFactory.class);
	}

	@Test
	public void importPersons() throws IOException {
		CsvConfig config = Factory.getInstance(CsvConfig.class);

		InputStream stream = new FileInputStream("fixture/persons.csv");
		CsvReader<Person> reader = csvFactory.getReader(config.getDescriptor(Person.class), stream);

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
	public void escapeNewLine() throws IOException {
		CsvConfig config = Factory.getInstance(CsvConfig.class);
		InputStream stream = new FileInputStream("fixture/escape-new-line.csv");
		CsvReader<Person> reader = csvFactory.getReader(config.getDescriptor(Person.class), stream);

		List<Person> persons = new ArrayList<>();
		for (Person person : reader) {
			persons.add(person);
		}
		reader.close();

		assertEquals(1, persons.size());
		assertEquals("Iulian\r\nRotaru", persons.get(0).getName());
	}
}

package js.csv;

import js.lang.Config;
import js.lang.ConfigBuilder;
import js.lang.ConfigException;

import org.junit.Test;

@SuppressWarnings("unused")
public class CsvDescriptorTest {
	@Test
	public void constructor() throws ConfigException, CsvException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='js.csv.CsvDescriptorTest$Person'><value name=\"NAME\" property=\"name\" /></csv>");
		Config config = builder.build();
		new CsvDescriptor(config);
	}

	@Test(expected = CsvException.class)
	public void constructor_NoClassAttribute() throws CsvException, ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv></csv>");
		Config config = builder.build();
		new CsvDescriptor(config);
	}

	@Test(expected = CsvException.class)
	public void constructor_MissingClass() throws CsvException, ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='fake.packet.FakeClass'></csv>");
		Config config = builder.build();
		new CsvDescriptor(config);
	}

	@Test(expected = CsvException.class)
	public void constructor_NotInstantible() throws CsvException, ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='js.csv.CsvDescriptor'></csv>");
		Config config = builder.build();
		new CsvDescriptor(config);
	}

	@Test(expected = CsvException.class)
	public void constructor_BadSeparator() throws CsvException, ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='js.csv.CsvDescriptorTest$Person' separator='fake'></csv>");
		Config config = builder.build();
		new CsvDescriptor(config);
	}

	// --------------------------------------------------------------------------------------------
	// FIXTURE

	private static class Person {

	}
}

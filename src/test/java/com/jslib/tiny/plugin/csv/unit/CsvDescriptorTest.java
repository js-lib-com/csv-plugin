package com.jslib.tiny.plugin.csv.unit;

import org.junit.Before;
import org.junit.Test;

import com.jslib.api.csv.CsvFactory;
import com.jslib.lang.Config;
import com.jslib.lang.ConfigBuilder;
import com.jslib.lang.ConfigException;
import com.jslib.util.Classes;

public class CsvDescriptorTest {
	private CsvFactory csvFactory;

	@Before
	public void beforeTest() {
		csvFactory = Classes.loadService(CsvFactory.class);
	}

	@Test
	public void constructor() throws ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='com.jslib.tiny.plugin.csv.unit.fixture.Person'><column field=\"name\" /></csv>");
		Config config = builder.build();
		csvFactory.getDescriptor(config);
	}

	@Test(expected = ConfigException.class)
	public void constructor_NoClassAttribute() throws ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv></csv>");
		Config config = builder.build();
		csvFactory.getDescriptor(config);
	}

	@Test(expected = ConfigException.class)
	public void constructor_MissingClass() throws ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='fake.packet.FakeClass'></csv>");
		Config config = builder.build();
		csvFactory.getDescriptor(config);
	}

	@Test(expected = ConfigException.class)
	public void constructor_NotInstantiable() throws ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='js.csv.CsvDescriptor'></csv>");
		Config config = builder.build();
		csvFactory.getDescriptor(config);
	}

	@Test(expected = ConfigException.class)
	public void constructor_BadDelimiter() throws ConfigException {
		ConfigBuilder builder = new ConfigBuilder("<csv class='js.tiny.plugin.csv.unit.fixture.Person' delimiter='fake'></csv>");
		Config config = builder.build();
		csvFactory.getDescriptor(config);
	}
}

package js.csv;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import js.io.WriterOutputStream;
import js.lang.BugError;
import js.mvc.AbstractView;
import js.mvc.ViewMeta;
import js.util.Classes;

import org.junit.Before;
import org.junit.Test;

public class CsvViewTest {
	private CsvView view;

	@Before
	public void beforeTest() throws Exception {
		view = new CsvView();

		File template = new File("fixture/person.xml");
		ViewMeta meta = new ViewMeta(template, CsvView.class, new Properties());
		Classes.invoke(view, AbstractView.class, "setMeta", meta);
	}

	@Test
	public void getContentType() {
		assertEquals("text/csv;charset=UTF-8;header=present", view.getContentType().getValue());
	}

	@Test
	public void serialize() throws IOException {
		List<Person> model = new ArrayList<>();
		model.add(new Person("John Doe", 54));
		model.add(new Person("Grand Doe, \"Elder\"", 77));
		view.setModel(model);

		StringWriter response = new StringWriter();
		OutputStream stream = new WriterOutputStream(response);
		view.serialize(stream);
		stream.close();

		System.out.println(response.toString());
		assertEquals("Name,Age\r\nJohn Doe,54\r\n\"Grand Doe, \"\"Elder\"\"\",77\r\n", response.toString());
	}

	@Test(expected = BugError.class)
	public void serialize_NullModel() throws IOException {
		view.setModel(null);
		view.serialize(new FileOutputStream("fixture/fake"));
	}

	@Test(expected = BugError.class)
	public void serialize_ModelNotArrayLike() throws IOException {
		view.setModel(new Person());
		view.serialize(new FileOutputStream("fixture/fake"));
	}

	// --------------------------------------------------------------------------------------------
	// FIXTURE

	@SuppressWarnings("unused")
	private static class Person {
		private final String name;
		private final int age;

		public Person() {
			name = null;
			age = 0;
		}

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}
}

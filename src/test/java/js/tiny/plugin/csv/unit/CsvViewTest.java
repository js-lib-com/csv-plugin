package js.tiny.plugin.csv.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import js.io.WriterOutputStream;
import js.lang.BugError;
import js.tiny.container.mvc.AbstractView;
import js.tiny.container.mvc.ViewMeta;
import js.tiny.plugin.csv.CsvView;
import js.tiny.plugin.csv.unit.fixture.Person;
import js.util.Classes;

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
		assertEquals("\"NAME\",\"AGE\"\r\n\"John Doe\",\"54\"\r\n\"Grand Doe, \"\"Elder\"\"\",\"77\"\r\n", response.toString());
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
}

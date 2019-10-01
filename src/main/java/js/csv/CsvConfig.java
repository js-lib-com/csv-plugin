package js.csv;

public interface CsvConfig {
	<T> CsvDescriptor<T> getDescriptor(Class<T> type);
}

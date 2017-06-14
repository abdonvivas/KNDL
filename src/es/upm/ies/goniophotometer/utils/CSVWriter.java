package es.upm.ies.goniophotometer.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class used to write CSV (Comma-Separated Values) files.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class CSVWriter {
	/**
	 * Comma character, represented as a string for convenience.
	 */
	public static final String COMMA = ",";
	/**
	 * Semicolon character, represented as a string for convenience.
	 */
	public static final String SEMICOLON = ";";
	/**
	 * Carriage return (&#92;r) followed by a newline (&#92;n).
	 */
	public static final String NEW_LINE = "\r\n";

	private char separator;
	private String filename;
	private FileWriter writer;

	/**
	 * Creates a new <code>CSVWriter</code> instance with the specified filename
	 * and separator. The filename <b>must not</b> contain the extension, as
	 * this class just writes CSV files.
	 * 
	 * @param filename
	 *            Filename of the CSV file.
	 * @param separator
	 *            Separator used to separate fields.
	 */
	public CSVWriter(String filename, String separator) {
		this.separator = separator.charAt(0);
		this.filename = filename;
	}

	/**
	 * Creates the CSV file.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void createCSV() throws IOException {
		File file;
		file = new File(filename + ".csv");
		file.createNewFile();
		writer = new FileWriter(file);
	}

	/**
	 * Writes a new field (next column of the previously written field, in the
	 * same row) in the CSV file.
	 * 
	 * @param value
	 *            Value of the new field.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void writeField(String value) throws IOException {
		writer.append(value);
		writer.append(separator);
	}

	/**
	 * Jumps to the next row of data.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void nextLine() throws IOException {
		writer.append(NEW_LINE);
	}

	/**
	 * Closes the CSV file.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void closeCSV() throws IOException {
		writer.flush();
		writer.close();
	}

	/**
	 * Returns the separator used by this instance of the class.
	 * 
	 * @return The separator used by this instance of the class.
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * Sets the separator used by this instance of the class.
	 * 
	 * @param separator The separator to be used.
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * Returns the filename of the CSV file, omitting the extension.
	 * 
	 * @return The filename of the CSV file.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the fileName of the CSV file.
	 * 
	 * @param filename The fileName to be used for the CSV file
	 */
	public void setFileName(String filename) {
		this.filename = filename;
	}
}

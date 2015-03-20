package ar.com.yamamoto.hartools;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class TextFile {
	protected final String DEFAULT_CHARSET = "UTF-8";
	protected final String LINE_ENDING = "\r\n";
	
	protected String text;
	protected String filename;
	protected Charset charset;
	protected Boolean writtable;
	
	/**
	 * Constructors
	 * 
	 * @param filename
	 * @param charset
	 * @param write
	 * 		Boolean. Open in write-mode?
	 */
	public TextFile(String filename, String charset, Boolean write)
			throws UnsupportedCharsetException {
		init(filename, charset, write);
	}
	
	public TextFile(String filename, String charset)
			throws UnsupportedCharsetException {
		init(filename, charset, false);
	}
	
	public TextFile(String filename) {
		init(filename, DEFAULT_CHARSET, false);
	}
	
	/**
	 * Constructor helper
	 * 
	 * @param filename
	 * @param charset
	 * @param write
	 */
	private void init(String filename, String charset, Boolean write)
			throws UnsupportedCharsetException {
		text = null;
		
		this.filename = filename;
		this.charset = Charset.forName(charset);
		this.writtable = write;
	}
	
	/**
	 * Open and read file, and store content in
	 * object member 'text'
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readFile()
			throws FileNotFoundException, IOException {
		StringBuilder contentBuffer = new StringBuilder();
		String line;
		
		InputStreamReader fileReader = 
				new InputStreamReader(new FileInputStream(filename), charset);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		while((line = bufferedReader.readLine()) != null) {
			contentBuffer.append(line);
			contentBuffer.append(LINE_ENDING);
		}
		
		bufferedReader.close();
		text = contentBuffer.toString().substring(0, 
				contentBuffer.length() - LINE_ENDING.length());
	}
	
	/**
	 * Write content to file
	 * 
	 * @param content
	 * 		String to write
	 * @throws IOException
	 */
	public void writeToFile(String content)
			throws IOException {
		if(!writtable) {
			throw new IOException("Read-only file");
		}
		
		Writer writer = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(filename), charset));
		
		try {
			writer.write(content);
		} finally {
			writer.close();
		}	
	}
	
	/**
	 * Returns a String with the text read from file
	 * 
	 * @return
	 */
	public String getText() {
		return text;
	}
	
}

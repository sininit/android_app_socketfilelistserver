package top.fols.aapp.socketfilelistserver;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import top.fols.box.io.os.XFile;
import top.fols.box.util.XObjects;
import java.nio.charset.Charset;
public class LogFileOutStream extends OutputStream {

	private File file;
	private XFile out;

	public LogFileOutStream(File file, boolean clearOrigin) {
		this.file = XObjects.requireNonNull(file);
		this.out = new XFile(file);
		if (clearOrigin)
			clear();
	}
	@Override
	public void write(int p1) throws IOException {
		// TODO: Implement this method
		byte[] bs = new byte[]{(byte)p1};
		write(bs);
		bs = null;
	}
	@Override
	public void write(byte[] b) throws IOException {
		// TODO: Implement this method
		this.write(b, 0, b.length);
		b = null;
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO: Implement this method
		if (!this.file.exists()) {
			this.out.close();
			this.out = new XFile(this.file);
		}
		this.out.append(b, off, len);
		b = null;
	}
	@Override
	public void close() {
		// TODO: Implement this method
		this.out.close();
	}
	public long getLogFileLength() {
		return this.file.length();
	}
	public void clear() {
		try {
			this.out.empty();
		} catch (IOException e) {
			e = null;
		}
	}
	public File getLogFile() {
		return this.file;
	}

	public void append(String content) throws RuntimeException{
		byte[] bytes = content.getBytes(Charset.defaultCharset());
		try {
			this.write(bytes);
		} catch (IOException e) { throw new RuntimeException(e); }
		try {
			this.flush();
		} catch (IOException e) { throw new RuntimeException(e); }
	}

}

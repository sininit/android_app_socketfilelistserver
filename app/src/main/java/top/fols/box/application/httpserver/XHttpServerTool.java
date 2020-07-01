package top.fols.box.application.httpserver;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import top.fols.box.io.XStream;
import top.fols.box.io.os.XFile;
import top.fols.box.io.os.XRandomAccessFileInputStream;
import top.fols.box.util.XCycleSpeedLimiter;

public class XHttpServerTool {
	public static InputStream getSocketInputStream(Socket s) {
		try {
			return s.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}
	public static OutputStream getSocketOutputStream(Socket s) {
		try {
			return s.getOutputStream();
		} catch (Exception e) {
			return null;
		}
	}
	public static boolean closeSocket(Socket s) {
		boolean b = true;
		try {
			s.shutdownInput();
		} catch (Exception e) {
			b = false;
		}
		try {
			s.shutdownOutput();
		} catch (Exception e) {
			b = false;
		}
		if (!close(getSocketInputStream(s)))
			b = false;
		if (!close(getSocketOutputStream(s)))
			b = false;
		if (!close(s))
			b = false;
		return b;
	}
	public static boolean close(Closeable c) {
		try {
			if (c == null)
				return false;
			c.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public static final List<String> getFileList(String file) {
		return getFileList(file, true);
	}
	
//	public static final byte[] file2bytes(String file) throws IOException {
//		return new XFile(file).getBytes();
//	}
//	public static final String file2String(String file) throws IOException {
//		byte[] bytes = new XFile(file).getBytes();
//		String coder = XEncodingDetect.getJavaEncode(bytes);
//		String newString = new String(bytes, coder);
//		bytes = null;
//		return newString;
//	}
//	public static final String file2StringNoCheckEncoder(String file) throws IOException {
//		byte[] bytes = new XFile(file).getBytes();
//		String newString = new String(bytes);
//		bytes = null;
//		return newString;
//	}



	/*
	 Get Dir File List
	 获取文件夹文件列表

	 Parameter:filePath 路径,recursion 递增搜索,adddir 列表是否添加文件夹
	 */
	public static List<String> getFileList(String filePath, boolean adddir) {
		return XFile.listFilesSort(new File(filePath), true);
    }
	
	
	
	
	
	
	
	public static long copyFile2Stream(File file, OutputStream out, int bufflen, boolean autoflush, XCycleSpeedLimiter limiter) throws IOException {
		XRandomAccessFileInputStream in = new XRandomAccessFileInputStream(file);
		if (in == null)
			return 0;
		byte[] buff = new byte[bufflen <= 0 ?XStream.DEFAULT_BYTE_BUFF_SIZE: bufflen];
		int read;
		long length = 0;
		while (true) {
			if(limiter != null)
				limiter.waitForFreeLong(buff.length);
			if ((read = in.read(buff)) == -1)
				break;
			length += read;
			if (out == null)
				continue;

			out.write(buff, 0, read);
			if (autoflush)
				out.flush();
		}
		in.close();
		return length;
	}
	public static long copy(InputStream in, OutputStream out, int bufflen, boolean autoflush, XCycleSpeedLimiter limiter) throws IOException {
		if (in == null)
			return 0;
		byte[] buff = new byte[bufflen <= 0 ?XStream.DEFAULT_BYTE_BUFF_SIZE: bufflen];
		int read;
		long length = 0;
		while (true) {
			if(limiter != null)
				limiter.waitForFreeLong(buff.length);
			if ((read = in.read(buff)) == -1)
				break;
			length += read;
			if (out == null)
				continue;
				
			out.write(buff, 0, read);
			if (autoflush)
				out.flush();
		}
		return length;
	}

}

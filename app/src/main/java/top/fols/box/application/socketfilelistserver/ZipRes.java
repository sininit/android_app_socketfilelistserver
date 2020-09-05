package top.fols.box.application.socketfilelistserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import top.fols.box.io.XStream;
import top.fols.box.util.XObjects;
import top.fols.box.io.os.XFile;

public class ZipRes {
	private File file;
	private ZipFile zf;
	private Map<String,byte[]> cache = new HashMap<>();
	private Map<String,ZipEntry> fileList;
	private long maxBuf = -1;
	private long nowBuf = 0;
	public static final char pathSplit = '/';
	
	
	public ZipRes(String path) {
		this(new File(path));
	}
	public ZipRes(File path) {
		this.file = XObjects.requireNonNull(path);
		try {
			this.zf = new ZipFile(this.file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public File getFile() {
		return this.file;
	}


	public void clearCache(){
		this.cache.clear();
	}

	private String fn(String name) {
		name = XFile.getCanonicalRelativePath(name, pathSplit);
		if(name.length() > 0 && name.charAt(0) == pathSplit)
			name = name.substring(1, name.length());
		return name;
	}
	
	
	
	public long length(String name) {
		if (!exist(name = fn(name)))
			return 0;
		return fileList.get(name).getSize();
	}
	public Map<String,ZipEntry> list() {
		if (this.fileList != null)
			return this.fileList;
		Map<String,ZipEntry> newFileList = new HashMap<>();
		Enumeration<ZipEntry> ezes = (Enumeration<ZipEntry>) this.zf.entries();
		while (ezes.hasMoreElements()) {
			ZipEntry ze = ezes.nextElement();
			String name = ze.getName();
			newFileList.put(name, ze);
		}
		return this.fileList = newFileList;
	}
	public boolean exist(String name) {
		return list().containsKey(name = fn(name));
	}
	public byte[] get(String name) {
		if (!exist(name = fn(name)))
			return null;
		if (cache == null)
			cache = new HashMap<>();
		if (!cache.containsKey(name)) {
			byte[] bs = zipFileToBytes(this.zf, name);
			nowBuf += bs.length;
			cache.put(name, bs);

			if (maxBuf > -1 && nowBuf >= maxBuf) {
				cache.clear();
				nowBuf = 0;
			}
		}	
		return cache.get(name);
	}
	public boolean isFile(String name) {
		if (!exist(name = fn(name)))
			return false;
		return !fileList.get(name).isDirectory();
	}
	public boolean isDirectory(String name) {
		if (!exist(name = fn(name)))
			return false;
		return fileList.get(name).isDirectory();
	}




	private static byte[] zipFileToBytes(ZipFile zipfile, String name) {
		try {
			ZipEntry ze = zipfile.getEntry(name);
			InputStream is = zipfile.getInputStream(ze);
			byte[] newBytes = XStream.InputStreamTool.toByteArray(is);
			is.close();
			return newBytes;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}

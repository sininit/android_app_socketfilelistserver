package top.fols.box.application.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import top.fols.box.annotation.XAnnotations;
import top.fols.box.application.httpserver.XHttpServerHeaderValue;
import top.fols.box.application.httpserver.XHttpServerThread;
import top.fols.box.io.base.XByteArrayOutputStream;
import top.fols.box.io.interfaces.XInterfaceReleaseBufferable;
import top.fols.box.statics.XStaticFixedValue;
import top.fols.box.util.XObjects;

public class XHttpServerWriterUtils extends OutputStream implements XInterfaceReleaseBufferable {


	private OutputStream os;
	private double HttpVersion = 1.0D;
	private long code = 200;
	private String state = "OK";
	private long needSendDataLengthl = -1;
	private Map<String,String> ua = new HashMap<>();
	private boolean isWriterHeader = false;

	@Override
	@XAnnotations("will not operate OutputStream")
	public void releaseBuffer() {
		// TODO: Implement this method
		this.ua.clear();
		this.ua = XStaticFixedValue.nullMap;
		this.state = "";
	}


	public XHttpServerWriterUtils(OutputStream os) {
		this.os = XObjects.requireNonNull(os);
	}
	public void setHttpVersion(double s) {
		this.HttpVersion = s <= 0.0D ?0.0D: s;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public void setState(String state) {
		this.state = XObjects.requireNonNull(state);
	}
	public Map<String,String> uaMap() {
		return this.ua;
	}
	@XAnnotations("does not include the packet header")
	public void setNeedSendDataLength(long length) {
		/*
		 设置发送的数据大小
		 不需要计算包头
		 */
		this.needSendDataLengthl = length < 0 ?-1: length;
	}
	public long getNeedSendDataLength() {
		return this.needSendDataLengthl;
	}
	public long alreadySendDataSize = 0;
	public long getAlreadySendDataLength() {
		return this.alreadySendDataSize;
	}
	public XHttpServerWriterUtils putUa(String key, String value) {
		this.uaMap().put(key, value);
		return this;
	}
	public String getUa(String key) {
		return this.uaMap().get(key);
	}
	public boolean containsUa(String key) {
		return this.uaMap().containsKey(key);
	}
	public void removeUa(String key) {
		this.uaMap().remove(key);
	}

	private void writerHeader() {
		if (!isWriterHeader) {
			try {
				byte[] HttpVersionBytes = String.valueOf(HttpVersion).getBytes();
				byte[] codeBytes = String.valueOf(code).getBytes();
				byte[] stateBytes = String.valueOf(state).getBytes();
				byte[] lengthBytes = String.valueOf(needSendDataLengthl).getBytes();

				XByteArrayOutputStream header = new XByteArrayOutputStream();
				header.write(XHttpServerThread.protocolHttpVersionStart);
				header.write(HttpVersionBytes);
				HttpVersionBytes = null;

				header.write(XHttpServerThread.protocolSplitBytes);
				header.write(codeBytes);
				codeBytes = null;

				header.write(XHttpServerThread.protocolSplitBytes);
				header.write(stateBytes);
				stateBytes = null;

				header.write(XHttpServerThread.lineSplit);
				byte[] tmp;
				for (String key:this.ua.keySet()) {
					/*
					 key: value
					 */
					if (key == null || this.ua.get(key) == null)
						continue;
					header.write(tmp = key.getBytes());
					tmp = null;
					header.write(XHttpServerHeaderValue.paramKeyValueSplitBytes);
					header.write(XHttpServerHeaderValue.space);
					header.write(tmp = this.ua.get(key).getBytes());
					tmp = null;
					header.write(XHttpServerThread.lineSplit);
				}
				if (needSendDataLengthl >= 0) {
					header.write(XHttpServerHeaderValue.paramContentLengthKeyBytes);
					header.write(XHttpServerHeaderValue.paramKeyValueSplitBytes);
					header.write(XHttpServerHeaderValue.space);
					header.write(lengthBytes);
					header.write(XHttpServerThread.lineSplit);
				}
				header.write(XHttpServerThread.lineSplit);
				this.os.write(header.getBuff(), 0, header.size());
				this.os.flush();
				
				header.releaseBuffer();
				header = null;
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			isWriterHeader = true;
		}
	}




	public XHttpServerWriterUtils writeFixedMessage(double httpVersion, int code, String state, 
													String message, boolean keepAlive) throws IOException {
		byte[] bytes = message.getBytes();
		this.setNeedSendDataLength(bytes.length);
		this.setCode(code);
		this.setState(state);
		this.setHttpVersion(httpVersion);
		this.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);
		this.write(bytes);
		this.flush();
		bytes = null;
		return this;
	}
	public XHttpServerWriterUtils writeFixedMessage(double httpVersion, int code, String state, 
													byte[] message, boolean keepAlive) throws IOException {
		return writeFixedMessage(httpVersion, code, state, (String)null, keepAlive);
	}
	public XHttpServerWriterUtils writeFixedMessage(double httpVersion, int code, String state, 
													byte[] message,
													String contentTypeCharset, boolean keepAlive) throws IOException {
		byte[] bytes = message;
		this.setNeedSendDataLength(bytes.length);
		this.setCode(code);
		this.setState(state);
		this.setHttpVersion(httpVersion);
		this.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);
		if (contentTypeCharset != null)
			this.uaMap().put(XHttpServerHeaderValue.paramContentTypeKey, "text/html; charset=" + contentTypeCharset);
		this.write(bytes);
		this.flush();
		return this;
	}



	@Override
	public void write(int p1)throws java.io.IOException {
		writerHeader();

		if (needSendDataLengthl >= 0 && alreadySendDataSize + 1 > needSendDataLengthl)
			return;
		os.write(p1);
		alreadySendDataSize++;
	}
	public void write(byte[] b) throws java.io.IOException {
		write(b, 0, b.length);
	}
	public void write(byte[] b, int off, int len) throws java.io.IOException {
		writerHeader();

		if (needSendDataLengthl >= 0 && alreadySendDataSize + len > needSendDataLengthl) {
			len = (int)(needSendDataLengthl - alreadySendDataSize);
			if (len <= 0)
				return;
		}
		os.write(b, off, len);
		alreadySendDataSize += len;
	}
	
	
	
	
	
	
	public XHttpServerWriterUtils w(int p1)throws java.io.IOException {
		write(p1);
		return this;
	}
	public XHttpServerWriterUtils w(byte[] b) throws java.io.IOException {
		write(b, 0, b.length);
		return this;
	}
	public XHttpServerWriterUtils w(byte[] b, int off, int len) throws java.io.IOException {
		write(b, off, len);
		return this;
	}
	
	
	
	public XHttpServerWriterUtils w(String str, String charset) throws java.io.IOException {
		byte[] bytes = str.getBytes(charset);
		write(bytes);
		bytes = null;
		return this;
	}
	public XHttpServerWriterUtils w(String str) throws java.io.IOException {
		byte[] bytes = str.getBytes();
		write(bytes);
		bytes = null;
		return this;
	}
	private static final byte[] TRUE = String.valueOf(Boolean.TRUE).getBytes();
	private static final byte[] FALSE = String.valueOf(Boolean.FALSE).getBytes();
	public XHttpServerWriterUtils w(boolean str) throws java.io.IOException {
		byte[] bytes = str ?TRUE: FALSE;
		write(bytes);
		bytes = null;
		return this;
	}
	public XHttpServerWriterUtils w(Object str) throws java.io.IOException {
		byte[] bytes = String.valueOf(str).getBytes();
		write(bytes);
		bytes = null;
		return this;
	}
	
	
	
	public void flush() throws java.io.IOException {
		os.flush();
	}
	public void close() throws java.io.IOException {
		os.close();
	}

}




	

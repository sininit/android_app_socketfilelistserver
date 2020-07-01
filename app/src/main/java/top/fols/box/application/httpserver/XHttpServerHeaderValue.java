package top.fols.box.application.httpserver;

import top.fols.box.statics.XStaticFixedValue;

public interface XHttpServerHeaderValue {
	public static final char space = ' ';
	public static final String paramKeyValueSplit = ":";
	public static final byte[] paramKeyValueSplitBytes = ":".getBytes();
	
	public static final byte[] paramContentLengthKeyBytes = "Content-Length".getBytes();
	public static final String paramContentLengthKey = "Content-Length";
	public static final byte[] paramContentTypeKeyBytes = "Content-Type".getBytes();
	public static final String paramContentTypeKey = "Content-Type";
	
	public static final String paramConnectionKey = "Connection";
	public static final String paramConnectionValueClose = "close".toLowerCase();
	public static final String paramConnectionValueKeepAlive = "keep-alive".toLowerCase();
	
	
	public static final byte[][] protocolMethodType = new byte[][]{
		"OPTIONS".getBytes(),
		"HEAD".getBytes(),
		"GET".getBytes(),
		"POST".getBytes(),
		"PUT".getBytes(),
		"DELETE".getBytes(),
		"TRACE".getBytes(),
		"CONNECT".getBytes()
	};
	public static final byte protocolSplitChar = ' ';
	public static final byte[] protocolSplitBytes = " ".getBytes();
	public static final byte[] protocolHttpVersionStart = "HTTP/".getBytes();
	
	
	
	
	public static final byte[] lineSplit = XStaticFixedValue.Bytes_NextLineRN();// \r\n
	public static final String lineSplitString = XStaticFixedValue.String_NextLineRN;// \r\n
}

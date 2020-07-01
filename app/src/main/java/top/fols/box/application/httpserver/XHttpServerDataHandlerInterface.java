package top.fols.box.application.httpserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import top.fols.box.annotation.XAnnotations;
import top.fols.box.io.base.XInputStreamFixedLength;
import top.fols.box.io.base.XInputStreamLine;
import top.fols.box.net.XURLConnectionMessageHeader;
public class XHttpServerDataHandlerInterface {
	public static interface Hander {
		@XAnnotations("return false will continue processing the next packet sent by the connection")
		public boolean dealNewData(XHttpServerThread c,Socket socket,
								String protocol, String url, double httpVersion,
								XURLConnectionMessageHeader ua,

								XInputStreamFixedLength<InputStream> rowfixed,
								XInputStreamLine row, OutputStream originOus
								) throws InterruptedException, Throwable; 
	}
}

package top.fols.box.application.httpserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import top.fols.box.application.httpserver.XHttpServerDataHandlerInterface.Hander;
import top.fols.box.application.httpserver.XHttpServerHeaderValue;
import top.fols.box.io.base.XInputStreamFixedLength;
import top.fols.box.net.XURLConnectionMessageHeader;
import top.fols.box.statics.XStaticFixedValue;
import top.fols.box.util.XArrays;
import top.fols.box.util.thread.XFixedThreadPool;
import top.fols.box.io.base.XInputStreamLine;
import top.fols.aapp.socketfilelistserver.MainActivity;
import top.fols.box.util.XExceptionTool;
import top.fols.box.net.XSocket;

/*
 浏览器访问该服务器数据包头为
 GET / HTTP/1.1
 Host: 127.0.0.1:7777
 Connection: keep-alive
 Cache-Control: max-age=0
 Upgrade-Insecure-Requests: 1

 在HTTP1.0中，没有正式规定 Connection:Keep-alive 操作；
 在HTTP1.1中所有连接都是Keep-alive的，也就是默认都是持续连接的（Persistent Connection）。

 Connection有两种状态 keep-alive/close
 Connection: Keep-Alive 请无视大小写
 */
public class XHttpServerThread extends XFixedThreadPool.Run implements XHttpServerHeaderValue {

	@Override
	public void remove() {
		// TODO: Implement this method
		XSocket.tryClose(this.socket);
	}
	
	
	public boolean checkInterrupt() throws InterruptedException {
		// TODO: Implement this method
		if (socket.isClosed())
			throw new InterruptedException();
		if (!socket.isConnected())
			throw new InterruptedException();
		super.ensureNoRemove();
		return false;
	}

	private Hander dataHander;
	private XHttpServer server;
	private Socket socket;
	private InputStream originIns;
	private OutputStream originOus;
	private int maxDataHeaderMaxSize;//请求数据包头最大长度

	private static final byte[] getStartsWithHttpProtocol(byte[] line) {
		if (line == null)
			return null;
		for (byte[] bsi:protocolMethodType)
			if (XArrays.startsWith(line, bsi))
				return (bsi.length + protocolSplitBytes.length <= line.length &&  line[bsi.length - 1 + protocolSplitBytes.length] == protocolSplitChar) ?bsi: null;
		return null;
	}
	public XHttpServerThread(XHttpServer server,
							 Hander packetProcess,
							 Socket socket
							 ) {
		this.server = server;
		this.dataHander = packetProcess;
		this.socket = socket;

		this.originIns = XHttpServerTool.getSocketInputStream(socket);
		this.originOus = XHttpServerTool.getSocketOutputStream(socket);

		this.maxDataHeaderMaxSize = server.getMaxDataHeaderMaxSize();
	}
	@Override
	public void run() {
		// TODO: Implement this method

		String protocol = null;
		String url = null;
		double httpVersion = 0.0D;

		byte[] linebuf;
		XURLConnectionMessageHeader ua = new XURLConnectionMessageHeader();
		XInputStreamFixedLength<InputStream> fixed = new XInputStreamFixedLength<>(originIns, maxDataHeaderMaxSize);
		XInputStreamLine<XInputStreamFixedLength> row = new XInputStreamLine<>(fixed);

		boolean dataHeaderReadComplete;
		boolean read2header;

		top: while (true) {
			/* -------top while------- */
			try {
				/* 读取包头 */
				read2header = false;
				dataHeaderReadComplete = false;
				linebuf = null;
				while (true) {

					if ((linebuf = row.readLine(lineSplit, false)) == null)
						break;
					checkInterrupt() /*  检测该线程是否被停止 */;

					/* 
					 readLine(split,false) 将不会加入split到末尾 
					 也就是读到单独一行\r\n将会返回[]无数据的byte[]数组
					 */
					if (!read2header) {
						byte[] protocolbytes = getStartsWithHttpProtocol(linebuf);
						boolean isProtocol = protocolbytes != null;
						int lastSplitIndex;
						if (!isProtocol)
							continue;
						/* 可能读取到XXX / HTTP/1.1了 */
						read2header = isProtocol;
						protocol = new String(protocolbytes);
						//System.out.println(protocol);
						url = new String(linebuf, protocolbytes.length + protocolSplitBytes.length, 
										 (lastSplitIndex = XArrays.lastIndexOf(linebuf, protocolSplitBytes, linebuf.length, protocolbytes.length + protocolSplitBytes.length))
										 - (protocolbytes.length + protocolSplitBytes.length)
										 );
						//System.out.println(url);
						if (XArrays.startsWith(linebuf, protocolHttpVersionStart, lastSplitIndex + protocolSplitBytes.length))
							httpVersion = Double.parseDouble(new String(linebuf, lastSplitIndex + protocolSplitBytes.length + protocolHttpVersionStart.length, linebuf.length - (lastSplitIndex + protocolSplitBytes.length + protocolHttpVersionStart.length)));
						else
							httpVersion = 0.0D;
						//System.out.println(httpVersion);
						protocolbytes = null;
					} else {
						if (XArrays.equals(linebuf, XStaticFixedValue.nullbyteArray)) {
							/*
							 数据包头读取完毕
							 */
							dataHeaderReadComplete = true;
							linebuf = null;
							break;
						}
						/*
						 获取ua
						 */
						String lineUa = new String(linebuf);
						ua.putAll(lineUa);
					}

					linebuf = null;// 防止内存溢出
				}
				/*
				 开始处理数据
				 */
				if (!dataHeaderReadComplete) {
					/*
					 读不到包头直接断开这个连接 不处理这个链接了 撒由那拉！
					 */
					break top;// 跳出所有循环
				} else {
					checkInterrupt() /*  检测该线程是否被停止 */;

					/*
					 * 设置流剩下需要读取的绝对长度
					 * 因为row有缓存所以计算row的缓存长度
					 */
					String Content_Length = ua.get(XHttpServerHeaderValue.paramContentLengthKey);
					if (Content_Length != null) {
						long contentLength = tolong(Content_Length, -1);
						if (contentLength >= 0) {
							int bufsize = row.getBuff() == null ?0: row.getBuff().length;
							long abslength;
							abslength = contentLength - bufsize;
							abslength = abslength < 0 ?0: abslength;
							fixed.setMaxUseLength(abslength);
							fixed.resetUseLength(); 
							fixed.fixed(true);
						}
					}


					/*
					 * 处理数据包
					 */
					/* ********* */
					boolean deal = dataHander.dealNewData(this, this.socket,
														  protocol, url, httpVersion,
														  ua,
														  fixed, row,
														  originOus);
					if (deal) {
						break top;
					} 
					/* ********* */
				}
			} catch (InterruptedException e0) {
				e0.printStackTrace();
				/*
				 这个线程被停止
				 */
				break top;
			} catch (Throwable e) {
//				try {
//					XRandomAccessFileOutputStream Log = new XRandomAccessFileOutputStream("/sdcard/log.txt");
//					Log.write((XExceptionTool.StackTraceToString(e)+"\n").getBytes());
//					Log.flush();
//					Log.close();
//				} catch (Exception e2) {
//					e2 = null;
//				}
			    MainActivity.ps.append(XExceptionTool.StackTraceToString(e));
				e.printStackTrace();
				break top;
			}
			/* -------top while------- */
		}



		/*
		 * 清除所有数据缓存
		 * 以下方法绝对不能出现任何异常不然可能会出现问题
		 */

		//System.out.println("end thread: "+socket);

		protocol = null;
		url = null;

		if (ua != null)
			ua.reset();
		if (row != null) {
			row.releaseBuffer();
			XHttpServerTool.close(row);//输入流
		}
		XHttpServerTool.closeSocket(this.socket);//Socket
		XHttpServerTool.close(originOus);//输出流
		
		server = null;
		socket = null;
		dataHander = null;
	}
	public static long tolong(String str, long def) {
		try {
			return Long.parseLong(str.trim());
		} catch (Throwable e) {
			return def;
		}
	}

}


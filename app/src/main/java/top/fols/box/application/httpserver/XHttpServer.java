package top.fols.box.application.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import top.fols.box.lang.XObject;
import top.fols.box.statics.XStaticFixedValue;
import top.fols.box.time.XTimeTool;
import top.fols.box.util.XObjects;
import top.fols.box.util.thread.XFixedThreadPool;


public class XHttpServer {



	private PrintStream ps;//log
	private int threadPoolMaxSize;//线程池最大长度
	private XHttpServerDataHandlerInterface.Hander packetProcess;
	private long postNumber;//累计访问数
	private int serverPort,serverBindPort;//绑定端口 服务器绑定的端口
	private SocketWeb server;//服务器
	private int maxDataHeaderMaxSize = 64 * 1024;//数据包头最大读取大小

	public XHttpServerDataHandlerInterface.Hander getDataHandler() {
		return this.packetProcess; 
	}
	public XHttpServer setDataHandler(XHttpServerDataHandlerInterface.Hander create) {
		this.packetProcess = XObjects.requireNonNull(create);
		return this;
	}




	public void log(String content) {
		if (ps == null)
			return;
		ps.write('[');
		ps.print(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis()));
		ps.write(']');
		ps.write(' ');
		ps.print(content);
		if (!content.endsWith(XStaticFixedValue.String_NextLineN))
			ps.write(XStaticFixedValue.Char_NextLineN);
		ps.flush();
	}
	public XHttpServer() {
		super();

	}


	public int getMaxDataHeaderMaxSize() {
		return this.maxDataHeaderMaxSize;
	}
	public XHttpServer setMaxDataHeaderMaxSize(int size) {
		this.maxDataHeaderMaxSize = size < 0 ?0: size;
		return this;
	}


	public XHttpServer setMaxThreadPoolSize(int size) {
		this.threadPoolMaxSize = size <= 0 ?1: size;
		return this;
	}
	public int getMaxThreadPoolSize() {
		return this.threadPoolMaxSize;
	}
	public int getNowThreadPoolSize() {
		return null == this.server ?0: this.server.getNowThreadPoolSize();
	}
	public String getIpAddres() {
		if (this.server == null)
			return null;
		return server.serversocket.getInetAddress().toString();
	}
	public XHttpServer setPort(int port) throws SocketException, IOException {
		int newPort = /*port < -1 ?-1: */ port;
		if (this.serverPort == newPort)
			return this;
		this.serverPort = newPort;
		if (this.server != null && !isStop()) {
			stop();
			start();
		}
		return this;
	}
	public int getPort() {
		return this.serverPort;
	}
	public int getBindPort() {
		return this.serverBindPort;
	}
	public XHttpServer setLogSteam(OutputStream os) {
		this.ps = new PrintStream(os);
		return this;
	}
	public synchronized void postNumberAdd() {this.postNumber++;}
	
	
	
	
	public boolean isStop() {
		return this.server == null || this.server.isStop();
	}
	private Object syncobj = new Object();
	public XHttpServer start() throws SocketException, IOException  {
		if (packetProcess == null) {
			throw new RuntimeException("data hander for null");
		}
		synchronized (syncobj) {
			if (this.server != null && this.server.isStart())
				return this;

			ServerSocket s;
			s = new ServerSocket();
			s.setSoTimeout(XTimeTool.time_1m);
			s.setReuseAddress(true); //设置ServerSocket的选项 该选项必须在服务器绑定端口之前设置才有效
			s.bind(new InetSocketAddress(
					s.getInetAddress(), serverPort));   //与8000端口绑 

			this.serverBindPort = s.getLocalPort();

			this.server = new SocketWeb(this, s, this.threadPoolMaxSize);
			this.server.superIsStop.set(false);
			this.server.start();
		}
		return this;
	}
	public XHttpServer stop() {
		synchronized (syncobj) {
			if (this.server == null || this.server != null && this.server.isStop())
				return this;
			this.server.interrupt();
			this.server.waitStopComplete();
			this.server = null;
		}
		return this;
	}

	public static class SocketWeb extends Thread {
		@Override
		public void interrupt() {
			// TODO: Implement this method
			super.interrupt();
			this.superIsStop.set(true);
			this.pool.removeAll();
			XHttpServerTool.close(serversocket);
		}

		/*
		 线程自动回收 集体停止操作 分运行中线程和等待中线程
		 */
		private XFixedThreadPool pool;
		private XHttpServer superobj;
		private XObject<Boolean> superIsStop;
		private ServerSocket serversocket;
		private boolean executeComplete;

		public SocketWeb(XHttpServer Super, ServerSocket ss, int maxThreadCount) {
			this.pool = new XFixedThreadPool().setMaxRunningCount(maxThreadCount);
			this.pool.setMaxRunningCount(Integer.MAX_VALUE);
			this.superobj = XObjects.requireNonNull(Super);
			this.serversocket = XObjects.requireNonNull(ss);
			this.superIsStop = new XObject<>(true);
			this.executeComplete = false;
		}
		public boolean isStart() {
			return this.superIsStop.get() == false;
		}
		public boolean isStop() {
			return this.superIsStop.get();
		}
		private void waitStopComplete() {
			while (!executeComplete) {
				try {
					sleep(1);
				} catch (InterruptedException e) {
					continue;
				}
			}
		}

		public int getNowThreadPoolSize() {
			return this.pool.getNowRunningCount();
		}


		//端口监听线程
		public void run() {
			Socket lastSocket = null;
			while (true) {
				try {
					if (!isStart()) throw new InterruptedException();
					while (true) {
						if (!isStart()) throw new InterruptedException();
						sleep(0);
						
						if (this.pool.getNowRunningCount() + 1 > this.pool.getMaxRunningCount()) {
							sleep(1);
							continue;
						} else {
							if (!isStart()) throw new InterruptedException();
							sleep(0);

							Socket socket;
							socket = lastSocket = serversocket.accept();
							socket.setKeepAlive(true);

							if (!isStart()) throw new InterruptedException();
							sleep(0);

							//System.out.println("new thread: " + socket);
							//子线程
							XHttpServerThread sdt = null;
							try {
								sdt = new XHttpServerThread(this.superobj,
									this.superobj.getDataHandler(),
									socket
								);
								this.pool.post(sdt);
								this.superobj.postNumberAdd();
							} catch (Exception e) {
								this.pool.remove(sdt);
							}
						}
						//System.out.println(superThreadPoolSizeCalc.get());
					}
				} catch (Exception s) {
					//superobj.log(XExceptionTool.StackTraceToString(s));
					s.printStackTrace();

					if (!isStart()) {
						XHttpServerTool.closeSocket(lastSocket);
						XHttpServerTool.close(serversocket);
						this.pool.removeAll();
						this.executeComplete = true;
						return;
					} else {
						continue;
					}
				}
			}
		}
	}
}

package top.fols.box.application.socketfilelistserver;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import top.fols.aapp.socketfilelistserver.MainActivity;
import top.fols.aapp.socketfilelistserver.Utils;
import top.fols.aapp.utils.XUIHandler;
import top.fols.box.application.httpserver.XHttpServerDataHandlerInterface;
import top.fols.box.application.httpserver.XHttpServerFileTool;
import top.fols.box.application.httpserver.XHttpServerHeaderRangeUtils;
import top.fols.box.application.httpserver.XHttpServerHeaderValue;
import top.fols.box.application.httpserver.XHttpServerThread;
import top.fols.box.application.httpserver.XHttpServerTool;
import top.fols.box.application.httpserver.XHttpServerWriterUtils;
import top.fols.box.io.XStream;
import top.fols.box.io.base.XInputStreamFixedLength;
import top.fols.box.io.base.XInputStreamLine;
import top.fols.box.io.os.XFile;
import top.fols.box.io.os.XRandomAccessFileOutputStream;
import top.fols.box.lang.XString;
import top.fols.box.net.XURL;
import top.fols.box.net.XURLConnectionTool;
import top.fols.box.net.XURLParam;
import top.fols.box.time.XTimeTool;
import top.fols.box.util.XArrays;
import top.fols.box.util.XCycleSpeedLimiter;
import top.fols.box.util.XObjects;
import top.fols.box.util.encode.XURLEncoder;
import top.fols.box.net.XURLConnectionMessageHeader;
import java.nio.charset.Charset;

public class XHttpFileListDataPacketHander implements XHttpServerDataHandlerInterface.Hander {
	public static final double needRes = 1.0D;

	public static final byte[] paramContentRangeKeyBytes = "Content-Range".getBytes();
	public static final String paramContentRangeKey = "Content-Range";
	public static final String paramContentDispositionKey = "Content-Disposition";

	public static final byte[] paramRangeKeyBytes = "Range".getBytes();
	public static final String paramRangeKey = "Range";

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public static final byte[] errorPrint404NotFound = "404 Not Found".getBytes();
	public static final byte[] errorPrint416RangeNotSatisfiable = "416 Range Not Satisfiable".getBytes();

	private XCycleSpeedLimiter upload2UserSpeedLimiter = new XCycleSpeedLimiter();//发送数据给客户的限制器 相当于客户下载速度限制
	private XCycleSpeedLimiter downloadUserDatasSeedLimiter = new XCycleSpeedLimiter();//接收用户下载的文件 相当于上传速度限制

	public XHttpFileListDataPacketHander setUpload2UserSpeedLimit(long speed) {
		this.upload2UserSpeedLimiter.setLimit(speed <=  0 ?false: true);
		this.upload2UserSpeedLimiter.setCycle(XTimeTool.time_1s);
		this.upload2UserSpeedLimiter.setCycleMaxSpeed(speed <= 0 ?8192: speed);
		return this;
	}
	public XCycleSpeedLimiter getUpload2UserSpeedLimit() {
		return this.upload2UserSpeedLimiter;
	}

	public XHttpFileListDataPacketHander setDownloadUserSpeedLimit(long speed) {
		this.downloadUserDatasSeedLimiter.setLimit(speed <=  0 ?false: true);
		this.downloadUserDatasSeedLimiter.setCycle(XTimeTool.time_1s);
		this.downloadUserDatasSeedLimiter.setCycleMaxSpeed(speed <= 0 ?1: speed);
		return this;
	}
	public XCycleSpeedLimiter getDownloadUserSpeedLimit() {
		return this.downloadUserDatasSeedLimiter;
	}


	public ZipRes zr;
	private File resZip;
	public XHttpFileListDataPacketHander setResZipPath(File file) {
		File newFileDir;
		try {
			newFileDir = file.getCanonicalFile();
		} catch (IOException e) {
			newFileDir = file.getAbsoluteFile();
		}
		this.resZip = XObjects.requireNonNull(newFileDir);
		this.zr = new ZipRes(this.resZip);
		return this;
	}
	public File getResZipPath() {
		return this.resZip;
	}

	public void clearResCache() {
		if (this.zr != null) {
			this.zr.clearCache();
		}
	}


	private boolean supportRangeDownload = true;//开启多线程文件下载
	public XHttpFileListDataPacketHander setSupportRangeDownload(boolean b) {
		this.supportRangeDownload = b;
		return this;
	}
	public boolean getSupportRangeDownload() {
		return this.supportRangeDownload;
	}



	private File baseDir;
	public XHttpFileListDataPacketHander setBaseDir(File f) {
		baseDir = f;
		return this;
	}
	public File getBaseDir() {
		return baseDir;
	}

	private boolean supportFileUpload = false;
	public XHttpFileListDataPacketHander setSupportFileUpload(boolean b) {
		this.supportFileUpload = b;
		return this;
	}
	public boolean getSupportFileUpload() {
		return this.supportFileUpload;
	}

	private boolean supportKeepAlive = true;//支持用户更新剪辑版
	public XHttpFileListDataPacketHander setSupportKeepAlive(boolean b) {
		this.supportKeepAlive = b;
		return this;
	}
	public boolean getSupportKeepAlive() {
		return this.supportKeepAlive;
	}

	private boolean defFileListLatticeMode = true;//默认宫格模式
	public XHttpFileListDataPacketHander setFileListLatticeMode(boolean b) {
		this.defFileListLatticeMode = b;
		return this;
	}
	public boolean getFileListLatticeMode() {
		return this.defFileListLatticeMode;
	}



	/* !grow-appd! */
	private boolean supportDownloadApp = true;//支持用户下载app
	public XHttpFileListDataPacketHander setSupportDownloadApp(boolean b) {
		this.supportDownloadApp = b;
		return this;
	}
	public boolean getSupportDownloadApp() {
		return this.supportDownloadApp;
	}





	public boolean dealNewData(XHttpServerThread thread, Socket socket,
							   String protocol,  String url,  double httpVersion,
							   XURLConnectionMessageHeader ua,

							   XInputStreamFixedLength<InputStream> rowfixed,
							   XInputStreamLine row,  OutputStream originOus
							   ) throws InterruptedException, Exception {			
		// TODO: Implement this method

		/*
		 * 应该在服务器中 这里这个功能我懒得写了
		 * Keep- Alive功能对资源利用的影响尤其突出。 此功能为HTTP 1.1预设的功能，HTTP 1.0加上Keep-Aliveheader也可以提供HTTP的持续作用功能。 
		 * Keep-Alive: timeout=5, max=100 
		 * timeout：过期时间5秒（对应httpd.conf里的参数是：KeepAliveTimeout），max是最多一百次请求，强制断掉连接 
		 * 就是在timeout时间内又有新的连接过来，同时max会自动减1，直到为0，强制断掉。见下面的四个图，注意看Date的值（前后时间差都是在5秒之内）！
		 */
		boolean keepAlive = false;
		if (supportKeepAlive) {
			String ConnectionValue = ua.get(XHttpServerHeaderValue.paramConnectionKey);
			if (httpVersion <= 1.0D) {
				keepAlive = ConnectionValue == null ?false: ConnectionValue.toLowerCase().equals(XHttpServerHeaderValue.paramConnectionValueKeepAlive);
			} else if (httpVersion >= 1.1D) {
				//System.out.println(ConnectionValue.toLowerCase());
				/*
				 HTTP1.1中所有连接都被保持，
				 除非在请求头或响应头中指明要关闭：Connection: Close 
				 这也就是为什么Connection: Keep-Alive字段再没有意义的原因。
				 */
				keepAlive = ConnectionValue == null || !ConnectionValue.toLowerCase().equals(XHttpServerHeaderValue.paramConnectionValueClose);
			}
		}

		final String downloadFileUrl = "/d?", downloadFileUrlParamName = "path";
		final String listDirUrl = "/l?", listDirUrlParamName = "path";
		/* !grow-appd! */final String listAppUrl = "/app", downloadAppUrl = "/dapp?", downloadAppUrlParamName = "package";
		final String resUploadHtmlFileName = "upload.html";
		final String PathSplitEncodeChars = "%2F";

		final String fileListLatticeModeParamName = "latticemode";

		/*
		 / sendDealResIndex
		 /index.html sendDealResIndex
		 /*.* sendResData
		 /d?path=* downloadFile
		 /l?path=* listdir
		 /upload.do updateFile !
		 */
		//System.out.println(XURL.getAbsPath(url));
		if ("GET".equals(protocol)) {
			thread.checkInterrupt() /*  检测该线程是否被停止 */;
			rowfixed.fixed(false);// 取消流长度限制器   不直接获取原流是因为row读取的时候有缓冲,实际原流已经读取的数据不止表面上读的这么点

			XURL xurlNde = new XURL(url);
			
			/* !download! */ boolean isDownloadFile = url.startsWith(downloadFileUrl);
			/* !list! */ boolean isList = url.startsWith(listDirUrl) || XURL.PATH_SEPARATOR.equals(xurlNde.getFilePath());
			
			/* !grow-appd! */boolean isListApp = url.equals(listAppUrl);
			/* !grow-appd! */boolean isDownloadApp = url.startsWith(downloadAppUrl);

			if (isList) {
				XURLParam param = null== xurlNde.getParam()?new XURLParam():xurlNde.param();

				/*
				 * 判断Url中是否有这个参数
				 */
				String fileListLatticeModeStr = param.get(fileListLatticeModeParamName);
				boolean fileListLatticeMode = getFileListLatticeMode();
				if ("true".equals(fileListLatticeModeStr)) {
					fileListLatticeMode = true;
				} else if ("false".equals(fileListLatticeModeStr)) {
					fileListLatticeMode = false;
				}

				/*
				 * 返回包头处理
				 */
				BufferedOutputStream bufos = new BufferedOutputStream(originOus, 8192);
				XHttpServerWriterUtils sp;
				sp = new XHttpServerWriterUtils(bufos);
				sp.setHttpVersion(1.1D);
				sp.uaMap().put(XHttpServerHeaderValue.paramContentTypeKey, "text/html; charset=utf-8");

				/*
				 * 处理可能出现的连接 比如你/a/目录下有个/a/b目录, 
				 * 但是访问的地址/a/b网址最后并没有加/
				 * 那么就重定向到 /a/b/
				 */
				if (XURL.PATH_SEPARATOR.equals(xurlNde.getFilePath())) {
					StringBuilder buf;
					buf = new StringBuilder();
					buf.append(listDirUrl).append(listDirUrlParamName).append(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).append(PathSplitEncodeChars);
					buf.append(XURL.PARAM_PROJECT_SEPARATOR).append(fileListLatticeModeParamName).append(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).append(fileListLatticeMode);
					sp.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);
					writeLocation(sp, buf.toString());

					/* 释放和跳出关闭*/{
						sp.releaseBuffer();
						xurlNde = null;
						param.clear();
						if (keepAlive) return false; else return true;/* 跳出这个数据包处理 */
					}
				} 


				/* 
				 **相对路径**
				 */
				String absRelativeLocalDirFilePath = XFile.getCanonicalPath(param.get(listDirUrlParamName));
				if (absRelativeLocalDirFilePath == null)
					absRelativeLocalDirFilePath = XURL.PATH_SEPARATOR;
				File absListdir = new File(baseDir, absRelativeLocalDirFilePath);//需要列表的目录
				boolean isLocalDirFileExist = (absListdir.isDirectory() && absListdir.exists());//需要列表的目录是否存在
				System.out.println("[localdir]" + absListdir + ", [needlist]" + isLocalDirFileExist);

				if (isLocalDirFileExist) {/*  listdir */
					/*
					 * 处理index.html
					 * 直接短链接减少资源损耗
					 */
					keepAlive = false;
					sp.setCode(200);
					sp.setState("OK");
					sp.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);

					sp.w(zr.get("index.part1.html"));

					sp.w("<a href=\"").w(listDirUrl).w(listDirUrlParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(XURLEncoder.encode(absRelativeLocalDirFilePath,(Charset) null)).w("\">")
						.w(new XURL(absRelativeLocalDirFilePath.substring(0, absRelativeLocalDirFilePath.lastIndexOf("/"))).getFileName())
						.w("</a>");

					sp.w(zr.get("index.part2.html"));

					sp.w("<div class=\"nexmoe-item\">");
					sp.w("	  <div class=\"mdui-typo\">");

					sp.w("	  <a href=\"").w("/")/* ?latticemode=true */.w(XURL.PARAM_SYMBOL).w(fileListLatticeModeParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(fileListLatticeMode).w("\">根目录</a>");

					sp.w("&nbsp;&nbsp;&nbsp;&nbsp;");

					String parentDir;
					parentDir = absRelativeLocalDirFilePath.substring(0, absRelativeLocalDirFilePath.length() - XURL.PATH_SEPARATOR.length());
					int last = parentDir.lastIndexOf(XURL.PATH_SEPARATOR);
					if (last > -1) {
						parentDir = parentDir.substring(0, last + XURL.PATH_SEPARATOR.length());
					} else {
						parentDir = XURL.PATH_SEPARATOR;
					}
					if ("".equals(parentDir))
						parentDir = XURL.PATH_SEPARATOR;
					parentDir = XURLEncoder.encode(parentDir, (Charset) null);
					sp.w("	  <a href=\"").w(listDirUrl).w(listDirUrlParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(parentDir).w(XURL.PARAM_PROJECT_SEPARATOR).w(fileListLatticeModeParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(fileListLatticeMode).w("\">上一层</a>");


					sp.w("	 </br></br>");

					sp.w("			<a href=\"").w(absRelativeLocalDirFilePath).w("upload.html\">文件上传</a> &nbsp;&nbsp;&nbsp;&nbsp; ");
					/* !grow-appd! */	
					if (getSupportDownloadApp()){
						sp.w("			<a href=\"").w(listAppUrl).w("\">App下载</a> &nbsp;&nbsp;&nbsp;&nbsp; ");
					}
					sp.w("		</div>");
					sp.w("  </div>");

					sp.w(zr.get("index.part3.html"));

					List<String> ls = XHttpServerTool.getFileList(absListdir.getAbsolutePath());
					for (String name:ls) {
						thread.checkInterrupt();

						File relatively = new File(absRelativeLocalDirFilePath, name);
						File file = new File(baseDir, relatively.getPath());
						//System.out.println(file + " " + file.isDirectory());
						String fileName = file.getName();
						String type = "insert_drive_file";
						boolean isFile = false;
						if (file.isDirectory()) {
							type = "folder_open";
							isFile = false;
						} else if (XHttpServerFileTool.isAudioFile(fileName)) {
							type = "audiotrack";
							isFile = true;
						} else if (XHttpServerFileTool.isImageFile(fileName)) {
							type = "image";
							isFile = true;
						} else {
							isFile = true;
						}	
						//System.out.println(file + "| type:" + type);
						sp.w("	<li class=\"mdui-list-item mdui-ripple\">");
						if (isFile) {
							/* !download! */
							sp.w("		<a href=\"").w(downloadFileUrl).w(downloadFileUrlParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(XURLEncoder.encode(relatively.getPath(),(Charset) null))		.w("\">");
						} else {
							sp.w("		<a href=\"").w(listDirUrl)		.w(listDirUrlParamName)	 .w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(XURLEncoder.encode(absRelativeLocalDirFilePath, (Charset) null)).w(XURLEncoder.encode(fileName, (Charset) null)).w(PathSplitEncodeChars)
								/*
								 * &latticemode=true
								 */
								.w(XURL.PARAM_PROJECT_SEPARATOR).w(fileListLatticeModeParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(fileListLatticeMode)
								.w("\">");
						}
						sp.w("		  <div class=\"mdui-col-xs-12 mdui-col-sm-7 mdui-text-truncate\">");
						sp.w("			<i class=\"mdui-icon material-icons\">").w(type).w("</i>");
						sp.w("	    	<span>").w(fileName).w("</span>");
						sp.w("		  </div>");

						sp.w("		  <div class=\"mdui-col-sm-3 mdui-text-right\">").w(dateFormat.format(file.lastModified())).w("</div>");
						sp.w("		  <div class=\"mdui-col-sm-2 mdui-text-right\">").w(isFile ?XFile.fileUnitFormat(file.length()): "-").w("</div>");
						sp.w("	  	</a>");
						sp.w("	</li>");

						relatively = null;
						file = null;
					}

					ls.clear();
					sp.w(zr.get("index.part4.html"));
					xurlNde = null; 
					sp.flush();
					sp.releaseBuffer();
					sp = null;
					bufos = null;
					return true;
				} else {
					thread.checkInterrupt() /*  检测该线程是否被停止 */;
					sp.writeFixedMessage(1.1D, 404, "Not Found",
										 "not found this dir: " + absRelativeLocalDirFilePath, keepAlive)
						.releaseBuffer();

					xurlNde = null;
					sp = null;
					bufos = null;
					if (keepAlive) return false; else return true;
				}

			} else {/*  download */
				/* !grow-appd! */ if (isListApp && getSupportDownloadApp()) {
					/*
					 * 使用短链接以减少资源损耗
					 * 应用列表
					 */
					keepAlive = false;
					BufferedOutputStream bufOus = new BufferedOutputStream(originOus, 8192);
					XHttpServerWriterUtils sp;
					sp = new XHttpServerWriterUtils(bufOus);
					sp.setHttpVersion(1.1D);
					sp.setCode(200);
					sp.setState("OK");
					sp.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);
					sp.uaMap().put(XHttpServerHeaderValue.paramContentTypeKey,  "text/html; charset=utf-8");

					thread.checkInterrupt() /*  检测该线程是否被停止 */;

					sp.w(zr.get("app.part1.html"));

					List<Grow.AppMessage> ls = Grow.getAppList(MainActivity.getContext());
					for (Grow.AppMessage i:ls) {
						thread.checkInterrupt();

						//System.out.println(file + "| type:" + type);
						sp.w("	<li class=\"mdui-list-item mdui-ripple\">");
						sp.w("		<a href=\"").w(downloadAppUrl).w(downloadAppUrlParamName).w(XURL.PARAM_PROJECT_ASSIGNMENT_SYMBOL).w(i.packageName).w("\">");
						sp.w("		  <div class=\"mdui-col-xs-12 mdui-col-sm-7 mdui-text-truncate\">");
						sp.w("			<i class=\"mdui-icon material-icons\">").w("insert_drive_file").w("</i>");
						sp.w("	    	<span>").w(i.appName).w(" (").w(i.packageName).w(")").w("</span>");
						sp.w("		  </div>");
						sp.w("		  <div class=\"mdui-col-sm-3 mdui-text-right\">").w(dateFormat.format(i.apkFilePath.lastModified())).w("</div>");
						sp.w("		  <div class=\"mdui-col-sm-2 mdui-text-right\">").w(XFile.fileUnitFormat(i.apkFilePath.length())).w("</div>");
						sp.w("	  	</a>");
						sp.w("	</li>");
						i = null;
					}
					ls.clear();

					sp.w(zr.get("app.part4.html"));
					sp.flush();
					sp.releaseBuffer();
					bufOus = null;
					xurlNde = null;
					sp = null;

					return true;
				} else if (isDownloadApp && getSupportDownloadApp()) {
					/*
					 * 下载应用
					 */
					XURLParam param = xurlNde.param();
					String pack = param.get(downloadAppUrlParamName);
					File absFile = Grow.getFile(MainActivity.getContext(), pack);
					Map<String,String> returnUa;
					returnUa = new HashMap<>();
					returnUa.put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);
					returnUa.put(XHttpServerHeaderValue.paramContentTypeKey, XHttpServerFileTool.getMimeType(xurlNde.getFilePath()));

					writerFile(thread, absFile, originOus, ua, returnUa, keepAlive, upload2UserSpeedLimiter, supportRangeDownload);
				} else  if (isDownloadFile) {
					/*
					 * 下载文件
					 */

					/*
					 **相对路径** 
					 */
					XURLParam param = xurlNde.param();
					String relativeDownloadFilePath = XFile.getCanonicalPath((param.get(downloadFileUrlParamName)));
					File absFile = new File(baseDir, relativeDownloadFilePath);
					//System.out.println(local);

					Map<String,String> returnUa;
					returnUa = new HashMap<>();
					returnUa.put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);

					writerFile(thread, absFile, originOus, ua, returnUa, keepAlive, upload2UserSpeedLimiter, supportRangeDownload);
					/* 释放和跳出关闭*/{
						xurlNde = null;
						relativeDownloadFilePath = null;
						absFile = null;
						param.clear();

						if (keepAlive) return false; else return true;
					}
				} else {
					/*
					 * 下载资源
					 */
					XHttpServerWriterUtils sp;
					sp = new XHttpServerWriterUtils(originOus);
					sp.setHttpVersion(1.1D);
					sp.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);
					sp.uaMap().put(XHttpServerHeaderValue.paramContentTypeKey, XHttpServerFileTool.getMimeType(xurlNde.getFilePath()) + "; charset=utf-8");

					/* 
					 **相对路径**
					 */				
					String relativeResFilePath = XFile.getCanonicalPath(xurlNde.getFilePath());
					String resFile = relativeResFilePath;
					boolean resFileExist = zr.exist(resFile) && zr.isFile(resFile);
					/*
					 可以在任何目录中打开 upload.html
					 */
					if (resUploadHtmlFileName.equals(xurlNde.getFileName())) {
						resFile = resUploadHtmlFileName;
						resFileExist = true;
					}
					if (resFileExist) {
						thread.checkInterrupt() /*  检测该线程是否被停止 */;

						sp.setCode(200);
						sp.setState("OK");
						sp.setNeedSendDataLength(zr.length(resFile));
						sp.w(zr.get(resFile));
						sp.flush();
					} else {
						thread.checkInterrupt() /*  检测该线程是否被停止 */;
						sp.writeFixedMessage(1.1D, 404, "Not Found",
											 "not found this res file: " + resFile, keepAlive);
						sp.releaseBuffer();
					}

					/* 释放和跳出关闭*/{
						xurlNde = null;
						sp.releaseBuffer();
						sp = null;

						if (keepAlive) return false; else return true;
					}
				}
			}
		} else if ("POST".equals(protocol)) {
			thread.checkInterrupt() /*  检测该线程是否被停止 */;

			/* 文件上传 
			 随便post地址会自动下载到对应目录 
			 但是必须有 Content-Type: multipart/form-data; boundary
			 POST请求头中禁止使用Keep-Alive
			 */
			keepAlive = false;

			if (!supportFileUpload) {
				XHttpServerWriterUtils sp;
				sp = new XHttpServerWriterUtils(originOus);
				sp.writeFixedMessage(1.1D, 404, "Not Found",
									 "file upload is closed", keepAlive);
				sp.releaseBuffer();
				return true;
			}

			long contentLength = Long.parseLong(ua.get(XHttpServerHeaderValue.paramContentLengthKey));

			XURL xurlNde = new XURL(url);
			boolean isUploadFile = xurlNde.getFileName().equals("upload.do");

			if (isUploadFile) {
				String contentType = ua.get(XHttpServerHeaderValue.paramContentTypeKey);
				String boundary = contentType;
				//Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryhFyeg9RAMEqc50fB
				if (contentType.contains("multipart/form-data") && contentType.contains("boundary")) {
					boundary = boundary.substring(boundary.indexOf("boundary") + "boundary".length() + 1);
				} else {
					return true;
				}

				byte[] tmp;
				StringBuilder buf = new StringBuilder();
				while ((tmp = row.readLine(XHttpServerThread.lineSplit)) != null) {
					contentLength -= tmp.length;
					if (tmp.length == XHttpServerThread.lineSplit.length && XArrays.equals(tmp, XHttpServerThread.lineSplit))
						break;
					buf.append(new String(tmp));
					tmp = null;
				}
				tmp = null;
				String bufString = buf.toString();
				buf = null;

				String uploadParamFileName = XString.submiddle(bufString, paramContentDispositionKey, XHttpServerThread.lineSplitString, 0);
				if (!bufString.contains("filename=\""))
					return true;
				uploadParamFileName = XString.submiddle(uploadParamFileName, "filename=\"", "\"");

				String dir = XURLEncoder.decode(xurlNde.getDir(), (Charset)null);
				/* 
				 **相对路径** 
				 */
				String relativeUploadToFilePath = XFile.getCanonicalPath(new File(dir, uploadParamFileName).getAbsolutePath());
				File absUploadFile = new File(baseDir, relativeUploadToFilePath);

//			System.out.println("post file: " + fileName + " ==> " + file);
//			System.out.println(ua.get(paramContentLengthKey));
//			System.out.println(bufString);
//			System.out.println(new String(XStream.inputstream.toByteArray(row)));
				XHttpServerWriterUtils sp;
				sp = new XHttpServerWriterUtils(originOus);
				sp.uaMap().put(XHttpServerHeaderValue.paramContentTypeKey, "text/html; charset=utf-8");

				if (absUploadFile.exists()) {
					sp.putUa(XHttpServerHeaderValue.paramConnectionValueKeepAlive, XHttpServerHeaderValue.paramConnectionValueClose);
					sp.writeFixedMessage(
						1.1D, 500, "Internal Server Error",
						"the file already exists. file: " + relativeUploadToFilePath, keepAlive);
					sp.releaseBuffer();
					xurlNde = null;
					return true;
				}

				File parentDir=  absUploadFile.getParentFile();
				System.out.println(parentDir);
				if (!parentDir.canWrite()) {
					sp.putUa(XHttpServerHeaderValue.paramConnectionValueKeepAlive, XHttpServerHeaderValue.paramConnectionValueClose);
					sp.writeFixedMessage(
						1.1D, 500, "Internal Server Error",
						"parent directory cannot writer, so cannot create new file. file: " + relativeUploadToFilePath, keepAlive);
					sp.releaseBuffer();
					xurlNde = null;
					return true;
				}

				OutputStream output = new XRandomAccessFileOutputStream(absUploadFile);
				/*下面的注释是一个浏览器发送带附件的请求的全文，所有中文都是说明性的文字*****
				 <HTTP头部内容略>
				 ............
				 <这里有一个空行，表明接下来的内容都是要提交的正文>
				 -----------------------------7d925134501f6
				 Content-Disposition: form-data; name="myfile"; filename="mywork.doc"
				 Content-Type: text/plain

				 <附件正文>
				 <这里有一个空行>
				 -----------------------------7d925134501f6--
				 ****************************************************************/
				byte[] endBytes = (XHttpServerThread.lineSplitString + String.format("--%s", boundary)).getBytes();
				byte[] b = new byte[8192];
				int read = -1;
				while (true) {
					thread.checkInterrupt();

					downloadUserDatasSeedLimiter.waitForFreeLong(b.length);
					if ((read = row.read(b)) == -1)
						break;

					int index;
					if (read >= endBytes.length && (index = XArrays.indexOf(b, endBytes, 0, read)) != -1) {
						byte[] b1 = Arrays.copyOfRange(b, 0, index);
						output.write(b1);
						b1 = null;
						break;
					}	
					output.write(b, 0, read);
				}
				sp.putUa(XHttpServerHeaderValue.paramConnectionValueKeepAlive, XHttpServerHeaderValue.paramConnectionValueClose);
				sp.writeFixedMessage(
					1.1D, 200, "OK",
					"upload complete. file: " + relativeUploadToFilePath, keepAlive);
				sp.releaseBuffer(); 
				xurlNde = null;
				XHttpServerTool.close(output);
				sp = null;
			}  else {
				return true;
			}
		}
		return true;
	}





















	/*
	 * writer 302 location
	 */
	public static final String paramLocationKey = "Location";
	public void writeLocation(XHttpServerWriterUtils sp, String newLink) throws IOException {
		final byte[] bytes = "Moved Permanently".getBytes();
		sp.uaMap().put(paramLocationKey, newLink);
		sp.setNeedSendDataLength(bytes.length);
		sp.setCode(302);
		sp.setState("Moved");
		sp.setHttpVersion(1.0D);
		sp.w(bytes);
		sp.flush();
		return;
	}
	/*
	 * 发送文件
	 * param@thread 线程
	 * param@absFile 绝对本地文件
	 * param@originOus 客户的输出流

	 * param@requestUa 客户请求的Ua
	 * param@ResponseUa 返回给客户的Ua

	 * param@limiter 速度限制器
	 * param@supportRangeDownload 是否支持Range下载
	 */
	public void writerFile(
		XHttpServerThread thread,

		File absFile, 
		OutputStream originOus,

		XURLConnectionMessageHeader RequestUa,
		Map<String,String> ResponseUa,
		boolean keepAlive,

		XCycleSpeedLimiter limiter,

		boolean supportRangeDownload
	) throws IOException, InterruptedException, IOException {
		/*
		 **相对路径** 
		 */
		XHttpServerWriterUtils sp;
		sp = new XHttpServerWriterUtils(originOus);
		sp.setHttpVersion(1.1D);
		sp.uaMap().putAll(ResponseUa);
		sp.uaMap().put(XHttpServerHeaderValue.paramConnectionKey, keepAlive ?XHttpServerHeaderValue.paramConnectionValueKeepAlive: XHttpServerHeaderValue.paramConnectionValueClose);

		if (!absFile.exists() || absFile.isDirectory()) {
			thread.checkInterrupt() /*  检测该线程是否被停止 */;

			sp.writeFixedMessage(1.1D, 404, "Not Found",
								 "not found this file: " + absFile.getPath(), keepAlive);
			sp.releaseBuffer();
		} else if (!absFile.canRead()) {
			thread.checkInterrupt() /*  检测该线程是否被停止 */;

			sp.writeFixedMessage(1.1D, 404, "Not Found",
								 "no permission to read this file: " + absFile.getPath(), keepAlive);
			sp.releaseBuffer();
		} else {
			long filelength = absFile.length();
			String rangeValue = RequestUa.get(paramRangeKey);
			if (rangeValue != null && supportRangeDownload) {
				XHttpServerHeaderRangeUtils range = new XHttpServerHeaderRangeUtils(filelength, rangeValue);

				if (range.isRangeNotSatisfiable()) {
					thread.checkInterrupt() /*  检测该线程是否被停止 */;

					sp.writeFixedMessage(1.1D, 416, "Range Not Satisfiable",
										 errorPrint416RangeNotSatisfiable,
										 "gbk", keepAlive);
					sp.releaseBuffer();
				} else {
					thread.checkInterrupt() /*  检测该线程是否被停止 */;

					sp.setCode(206);
					sp.setState("Partial Content");
					sp.uaMap().put(paramContentDispositionKey, "attachment; filename=" + absFile.getName());
					sp.uaMap().put(paramContentRangeKey, range.getContentRangeValue());
					thread.checkInterrupt() /*  检测该线程是否被停止 */;

					sp.setNeedSendDataLength(range.getAllLength());
					range.writer(absFile, range.getRanges(), sp, limiter);
					sp.flush();
				}
				//System.out.println("get = " + " ==> " + rangeValue + " ==> " + range.getContentRangeValue() + " ==> " + range.getAllLength());
			} else {
				//System.out.println("send " + local + " length=" + filelength);
				sp.setCode(200);
				sp.setState("OK");
				sp.uaMap().put(paramContentDispositionKey, "attachment; filename=" + absFile.getName());
				sp.setNeedSendDataLength(filelength);
				thread.checkInterrupt() /*  检测该线程是否被停止 */;

				XHttpServerTool.copyFile2Stream(absFile, sp, 8192, true, limiter);
				sp.flush();
				//System.out.println("send complete " + local + " length=" + filelength);
			}
		}
		/* 释放和跳出关闭*/{
			absFile = null;
			sp.releaseBuffer();
			sp = null;
		}
	}


}

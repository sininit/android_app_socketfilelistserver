package top.fols.box.application.httpserver;


/*
 Range & Content-Range
 HTTP1.1 协议（RFC2616）开始支持获取文件的部分内容，这为并行下载以及断点续传提供了技术支持。它通过在 Header 里两个参数实现的，客户端发请求时对应的是 Range ，服务器端响应时对应的是 Content-Range。

 Range
 用于请求头中，指定第一个字节的位置和最后一个字节的位置，一般格式：
 Range:(unit=first byte pos)-[last byte pos]
 Range 头部的格式有以下几种情况：

 Range: bytes=0-499 表示第 0-499 字节范围的内容
 Range: bytes=500-999 表示第 500-999 字节范围的内容 
 Range: bytes=500- 表示从第 500 字节开始到文件结束部分的内容
 Range: bytes=-500 表示最后 500 字节的内容 
 Range: bytes=0-0,-1 表示第一个和最后一个字节 
 Range: bytes=500-600,601-999 同时指定几个范围

 Content-Range
 用于响应头中，在发出带 Range 的请求后，服务器会在 Content-Range 头部返回当前接受的范围和文件总大小。一般格式：
 Content-Range: bytes (unit first byte pos) - [last byte pos]/[entity legth]
 例如：
 Content-Range: bytes 0-499/22400
 0－499 是指当前发送的数据的范围，而 22400 则是文件的总大小。

 而在响应完成后，返回的响应头内容也不同：
 HTTP/1.1 200 Ok（不使用断点续传方式） 
 HTTP/1.1 206 Partial Content（使用断点续传方式）
 HTTP/1.1 416 Range Not Satisfiable(范围是不能满足条件的)
 */
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;
import top.fols.box.io.base.XByteArrayInputStream;
import top.fols.box.io.base.XInputStreamFixedLength;
import top.fols.box.io.os.XRandomAccessFileInputStream;
import top.fols.box.lang.XString;
import top.fols.box.statics.XStaticFixedValue;
import top.fols.box.util.XCycleSpeedLimiter;
import top.fols.box.util.XObjects;
import top.fols.box.io.interfaces.XInterfaceReleaseBufferable;
import java.util.ArrayList;

public  class XHttpServerHeaderRangeUtils implements XInterfaceReleaseBufferable {
	private static final String httpRequestRangeHead = "bytes=";
	
	private static final String httpResponseContentRangeHead = "bytes ";
	private static final String httpResponseContentRangeLengthSplit = "/";
	
	private static final String posSplit = ",";
	private static final String posRangeSplit = "-";
	
	private String originRange;
	private List<Range> r = new ArrayList<>();
	private long filelength;
	private boolean rangeNotSatisfiable = false;
	
	@Override
	public void releaseBuffer() {
		// TODO: Implement this method
		r.clear();
		originRange = "";
		r = XStaticFixedValue.nullList;
	}



	public static class Range {
		private long start;
		private boolean existStart;
		private long end;
		private boolean existEnd;
		private long fileLength;
		/*
		 Range: bytes=-1
		 >> Content-Length=[1], Content-Range=[bytes 8751811-8751811/8751812] {0}
		 */
		public long getLength() {
			long length = getAbsEnd() - getAbsStart() + 1;
			return length;
		}
		public boolean isRangeSatisfiable() {
			if (existStart) {
				if (existEnd) {
					/* 0-499 */
					if (end - start + 1 <= fileLength && end >= 0)
						return true;
					return false;
				} else {
					/* 0- */
					if (fileLength - start >= 0 && start >= 0)
						return true;
					return false;
				}
			} else {
				if (existEnd) {
					/* -499 */
					if (end > 0 && end < fileLength)
						return true;
					return false;
				} else {
					/* - 什么鬼*/
					return false;
				}
			}
		}


		public long getAbsStart() {
			if (existStart) {
				/* 0-499 0- */
				return start;
			} else {
				if (existEnd) {
					/* -499 */
					if (end ==  0)
						return 0;
					return fileLength - end;
				} else {
					/* - 什么鬼*/
					return 0;
				}
			}
		}
		public long getAbsEnd() {
			if (existStart) {
				if (existEnd) {
					/* 0-499 */
					return end;
				} else {
					/* 0- */
					return fileLength - 1;
				}
			} else {
				if (existEnd) {
					/* -499 */
					return end;
				} else {
					/* - 什么鬼*/
					return 0;
				}
			}
		}




		@Override
		public String toString() {
			// TODO: Implement this method
			StringBuilder sb = new StringBuilder();
			if (existStart)
				sb.append(getAbsStart());
			sb.append('-');
			if (existEnd)
				sb.append(getAbsEnd());
			return sb.toString();
		}

	}




	public XHttpServerHeaderRangeUtils(long fileLength, String rangeValueorigin) {
		XObjects.requireNonNull(rangeValueorigin);
		rangeValueorigin = rangeValueorigin.trim();
		if (rangeValueorigin.startsWith(httpRequestRangeHead))
			this.originRange = rangeValueorigin = rangeValueorigin.substring(httpRequestRangeHead.length(), rangeValueorigin.length());
		this.filelength = fileLength;
		String rangeValue = new StringBuilder(rangeValueorigin).append(posSplit).toString();
		List<String> l = XString.split(rangeValue, posSplit);
		for (String li:l) {
			if ("".equals(li))
				continue;

			Range ri = new Range();
			String format = li.trim();
			String tmp;
			final String StartAndEndSplitChars = posRangeSplit;

			ri.fileLength = fileLength;
			int splitIndex = format.indexOf(StartAndEndSplitChars);
			if (splitIndex == -1) {
				rangeNotSatisfiable = true;
				return;
			}

			tmp = format.substring(0, splitIndex);
			if (tmp.equals("")) {
				ri.start = 0;
				ri.existStart = false;
			} else {
				ri.start = Long.parseLong(tmp);
				ri.existStart = true;
			}

			tmp = format.substring(splitIndex + StartAndEndSplitChars.length(), format.length());
			if (tmp.equals("")) {
				ri.end = 0;
				ri.existEnd = false;
			} else {
				ri.end = Long.parseLong(tmp);
				ri.existEnd = true;
			}

			if (!ri.isRangeSatisfiable()) {
				rangeNotSatisfiable = true;
				break;
			}
			r.add(ri);
		}
	}
	public boolean isRangeNotSatisfiable() {
		return this.rangeNotSatisfiable;
	}
	public List<Range> getRanges() {
		return this.r;
	}

	@Override
	public String toString() {
		// TODO: Implement this method
		return this.r.toString();
	}




	public String getContentRangeValue() {
		StringBuilder result;
		result = new StringBuilder().append(httpResponseContentRangeHead);
		StringJoiner rsj = new StringJoiner(posSplit);
		for (Range ri:this.r)
			rsj.add(ri.toString());
		result.append(rsj).append(httpResponseContentRangeLengthSplit).append(filelength);
		rsj = null;
		return result.toString();
	}




	public long getAllLength() {
		long allLength = 0;
		for (Range ri:this.r)
			allLength += ri.getLength();
		return allLength;
	}




	public void writer(File file, List<Range> lr, OutputStream os, XCycleSpeedLimiter limiter) throws IOException {
		XRandomAccessFileInputStream rafr = new XRandomAccessFileInputStream(file);
		for (Range lri:lr) {
			rafr.seekIndex(lri.getAbsStart());
			XInputStreamFixedLength<XRandomAccessFileInputStream> xnisfl = new XInputStreamFixedLength<>(rafr, lri.getLength());
			XHttpServerTool.copy(xnisfl, os, 8192, true, limiter);
			xnisfl = null;
		}
		rafr.close();
	}

	public void writer(byte[] bytes, List<Range> lr, OutputStream os, XCycleSpeedLimiter limiter) throws IOException {
		XByteArrayInputStream rafr = new XByteArrayInputStream(bytes);
		for (Range lri:lr) {
			rafr.seekIndex((int)lri.getAbsStart());
			XInputStreamFixedLength<XByteArrayInputStream> xnisfl = new XInputStreamFixedLength<>(rafr, lri.getLength());
			XHttpServerTool.copy(xnisfl, os, 8192, true, limiter);
			xnisfl = null;
		}
		rafr.close();
	}
}

package top.fols.box.application.httpserver;

import java.net.FileNameMap;
import java.net.URLConnection;
import top.fols.box.util.XObjects;

public class XHttpServerFileTool {

	private final static String PREFIX_VIDEO="video/";
	private final static String PREFIX_MUSIC="audio/";
	private final static String PREFIX_IMAGE="image/";

    /**
	 * 可以直接传入文件名获取Mime类型
     * Get the Mime Type from a File
     * @param fileName 文件名
     * @return 返回MIME类型
     * thx https://www.oschina.net/question/571282_223549
     * add by fengwenhua 2017年5月3日09:55:01
     */
    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileName);
        return type;
    }


    /**
     * 根据文件后缀名判断 文件是否是视频文件
     * @param fileName 文件名
     * @return 是否是视频文件
     */
    public static boolean isVedioFile(String fileName) {
        if (XObjects.isEmpty(fileName)) {
			return false;
		} else {
			String mimeType = getMimeType(fileName);
			if (mimeType != null)
				return mimeType.contains(PREFIX_VIDEO);
			return false;
		}
    }


	public static boolean isImageFile(String fileName) {
        if (XObjects.isEmpty(fileName)) {
			return false;
		} else {
			String mimeType = getMimeType(fileName);
			if (mimeType != null)
				return mimeType.contains(PREFIX_IMAGE);
			return false;
		}
    }


    /**
     * 根据文件后缀名判断 文件是否是视频文件
     * @param fileName 文件名
     * @return 是否是视频文件
     */
    public static boolean isAudioFile(String fileName) {
		if (XObjects.isEmpty(fileName)) {
			return false;
		} else {
			String mimeType = getMimeType(fileName);
			if (mimeType != null)
				return mimeType.contains(PREFIX_MUSIC);
			return false;
		}

    }




}

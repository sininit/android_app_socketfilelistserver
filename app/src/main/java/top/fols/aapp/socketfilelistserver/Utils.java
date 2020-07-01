package top.fols.aapp.socketfilelistserver;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import top.fols.box.util.XExceptionTool;
import java.util.ArrayList;  

/** 
 *  
 * 二维码的解析需要借助BufferedImageLuminanceSource类，该类是由Google提供的，可以将该类直接拷贝到源码中使用，当然你也可以自己写个 
 * 解析条形码的基类 
 */  

public final class Utils {  
	public static Result decodeQrcode(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.TRY_HARDER, "UTF8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        android.graphics.Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 100);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;

        //获取到bitmap对象(相册图片对象通过path)
        scanBitmap = BitmapFactory.decodeFile(path, options);
		//输入bitmap解析的二值化结果(就是图片的二进制形式)
		int width = scanBitmap.getWidth();
		int height = scanBitmap.getHeight();


		int[] data = new int[width * height];
		scanBitmap.getPixels(data, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        //再把图片的二进制形式转换成,图片bitmap对象
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        //CodaBarReader codaBarReader= new CodaBarReader();    //codaBarReader  二维码
        try {
            /**创建MultiFormatReader对象,调用decode()获取我们想要的信息,比如条形码的code,二维码的数据等等.这里的MultiFormatReader可以理解为就是一个读取获取数据的类,最核心的就是decode()方法 */
            return  new MultiFormatReader().decode(bitmap1, hints);      //识别条形码
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

	public static Result decodeQrcode(Bitmap scanBitmap) {
        if (scanBitmap == null) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.TRY_HARDER, "UTF8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
     	options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 100);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;

        //获取到bitmap对象(相册图片对象通过path
		//输入bitmap解析的二值化结果(就是图片的二进制形式)
		int width = scanBitmap.getWidth();
		int height = scanBitmap.getHeight();


		int[] data = new int[width * height];
		scanBitmap.getPixels(data, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        //再把图片的二进制形式转换成,图片bitmap对象
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        //CodaBarReader codaBarReader= new CodaBarReader();    //codaBarReader  二维码
        try {
            /**创建MultiFormatReader对象,调用decode()获取我们想要的信息,比如条形码的code,二维码的数据等等.这里的MultiFormatReader可以理解为就是一个读取获取数据的类,最核心的就是decode()方法 */
            return  new MultiFormatReader().decode(bitmap1, hints);      //识别条形码
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }




	public static Bitmap encodeQrcode(String content, int width, int height) {  
		QRCodeWriter qrCodeWriter = new QRCodeWriter();  
		Map<EncodeHintType, String> hints = new HashMap<>();  
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");  
		try {  
			BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);  
			int[] pixels = new int[width * height];  
			for (int i = 0; i < height; i++) {  
				for (int j = 0; j < width; j++) {  
					if (encode.get(j, i)) {  
						pixels[i * width + j] = 0x00000000;  
					} else {  
						pixels[i * width + j] = 0xffffffff;  
					}  
				}  
			}  
			return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);  
		} catch (WriterException e) {  
			e.printStackTrace();  
		}  
		return null;  
	}





	public static void updateDCIM(Context Context, String str) {
        Context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + str)));
    }
	public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    public static Bitmap Bytes2Bitmap(byte[] bArr) {
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }



	public static void openLink(Activity activity, String URL) {
		try {
			Uri uri = Uri.parse(URL);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			activity.startActivity(intent);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(activity.getApplicationContext(), "open link error: " + URL + "\n" + XExceptionTool.StackTraceToString(e), Toast.LENGTH_LONG).show();
		}
	}








	/*
	 * ls /sys/class/net
	 * Android 获取当前设备在局域网的地址
	 */
	public static String getIPV4LANLoopbackAddres() {
		String defHost = "127.0.0.1";
		//	获取第一个回环地址
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet4Address) {
						if (inetAddress.isLoopbackAddress()) {
							return defHost = inetAddress.getHostAddress();
						}
					}
				}
			}

			return defHost;
		} catch (Throwable e) {
			return defHost;
		}
	}
	public static String getIPV4LAN() {
		String defHost = getIPV4LANLoopbackAddres();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet4Address) {
						if (inetAddress.isLoopbackAddress())
							continue;
						String hostAddress = inetAddress.getHostAddress();
						if (inetAddress.isSiteLocalAddress())
							return defHost = hostAddress;
					}
				}
			}

			return defHost;
		} catch (SocketException e) {
			return defHost;
		}
	}
	public static List<String> getIPV4LANList() {
		String defHost = getIPV4LANLoopbackAddres();
		List<String> newList = new ArrayList<>();
		Enumeration<NetworkInterface> networkInterfaces = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet4Address) {
						String hostAddress = inetAddress.getHostAddress();
						newList.add(hostAddress);

						// Test
//						{
//							//System.out.println(networkInterface.getName() + " / " + hostAddress);
//							//return hostAddress;
//							List<Method> newMs = new ArrayList<>();
//							Method[] m = Inet4Address.class.getMethods();
//							for (int i = 0;i < m.length;i++) {
//								Method mi = m[i];
//								if (mi.getName().startsWith("is") && (mi.getParameterTypes() == null || mi.getGenericParameterTypes().length == 0)) 
//									newMs.add(mi);
//							}
//							StringBuilder sb = new StringBuilder();
//							for (Method mi:newMs) {
//								sb.append(mi.getName() + "=" + mi.invoke(inetAddress) + "\n");
//							}
//							newList.add(sb.toString());
//							newList.add(hostAddress + " " + "[" + networkInterface.getDisplayName() + "]");
//							
//						}
					}
				}
			}
			if (newList.size() == 0)
				newList.add(defHost);

			return newList;
		} catch (SocketException e) {
			return newList;
		}

	}



	public static String getIPV6LANLoopbackAddres() {
		String defHost = "[::1]";
		//	获取第一个回环地址
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet6Address) {
						if (inetAddress.isLoopbackAddress()) {
							return defHost = "[" + inetAddress.getHostAddress() + "]";
						}
					}
				}
			}
		} catch (Throwable e) {
			e = null;
		}
		return defHost;
	}
	public static String getIPV6LAN() {
		String defHost = getIPV6LANLoopbackAddres();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet6Address) {
						String hostAddress = inetAddress.getHostAddress();
						if (hostAddress.contains("%"))
							hostAddress = hostAddress.substring(0, hostAddress.indexOf("%"));
						if (inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress())
							continue;
						return defHost = "[" + hostAddress + "]";
					}
				}
			}

		} catch (SocketException e) {
			e = null;
		}
		return defHost;
	}
	public static List<String> getIPV6LANList() {
		String defHost = getIPV6LANLoopbackAddres();
		List<String> newList = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet6Address) {
						String hostAddress = inetAddress.getHostAddress();
						if (hostAddress.contains("%"))
							hostAddress = hostAddress.substring(0, hostAddress.indexOf("%"));
						newList.add("[" + hostAddress + "]");

						if (inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress())
							continue;

						// Test
//						{
//							//System.out.println(networkInterface.getName() + " / " + hostAddress);
//							//return hostAddress;
//							List<Method> newMs = new ArrayList<>();
//							Method[] m = Inet6Address.class.getMethods();
//							for (int i = 0;i < m.length;i++) {
//								Method mi = m[i];
//								if (mi.getName().startsWith("is") && (mi.getParameterTypes() == null || mi.getGenericParameterTypes().length == 0)) 
//									newMs.add(mi);
//							}
//							StringBuilder sb = new StringBuilder();
//							for (Method mi:newMs) {
//								sb.append(mi.getName() + "=" + mi.invoke(inetAddress) + "\n");
//							}
//							newList.add(hostAddress + " " + "[" + networkInterface.getDisplayName() + "]");
//							newList.add(sb.toString());
//
//						}
					}
				}
			}
			if (newList.size() == 0)
				newList.add(defHost);

			return newList;
		} catch (SocketException e) {
			return newList;
		}

	}
	
	 
	 
	 
	 
	 
	 
	 
	 

	public static void setClip(Context context, String text) {
		try {
			//获取剪贴板管理器：  
			ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);  
			// 创建普通字符型ClipData  
			cm.setText(text);
			return;
		} catch (Throwable e) {
			return;
		}
	}
	public static String getClip(Context context) {
		try {
			ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
			return cm.getText().toString();
		} catch (Throwable e) {
			return "";
		}
	}

}

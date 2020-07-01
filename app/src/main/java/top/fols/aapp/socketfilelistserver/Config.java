package top.fols.aapp.socketfilelistserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import top.fols.aapp.socketfilelistserver.Config;
import top.fols.box.io.os.XRandomAccessFileInputStream;
import top.fols.box.io.os.XRandomAccessFileOutputStream;
import top.fols.box.lang.XString;
import top.fols.box.statics.XStaticFixedValue;
import top.fols.box.util.XArrays;
import top.fols.box.util.XFixelArrayFill;
import top.fols.box.util.XObjects;

public class Config {
	public static final String defBaseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final int defWebPort = 7777;
	public static final boolean defMultiThreadDownload = true;
	public static final int defMaxThread = 64;
	public static final long defDownloadUserUploadSpeedLimit = -1;
	public static final long defUploadDataToSpeedLimit = -1;
	public static final boolean defFileUpload = true;
	public static final boolean defClip = false;
	public static final boolean defClipp = false;
	public static final boolean defFileListLatticeMode = true;
	public static final boolean defOpenAppOpenWebServer= false;
	public static final String[] defReCentBaseDir = XStaticFixedValue.nullStringArray;
	public static final boolean defDownloadApp= true;


	public static final String config_BaseDir = "BaseDir";
	public static final String config_WebPort = "WebPort";
	public static final String config_MultiThreadDownload = "MultiThreadDownload";
	public static final String config_MaxMonitorThread = "MaxMonitorThread";
	public static final String config_DownloadUserUploadSpeedLimit= "DownloadUserUploadSpeedLimit";
	public static final String config_UploadDataToSpeedLimit= "UploadDataToSpeedLimit";
	public static final String config_UploadFile= "UploadFile";
	public static final String config_Clip = "Clip";
	public static final String config_Clipp= "Clipp";
	public static final String config_DefFileListLatticeMode = "FileListLatticeMode";//默认文件列表模式 false代表 普通 true为宫格
	public static final String config_OpenAppOpenWebServer= "OpenAppOpenWebServer";
	public static final String config_ReCentBaseDir = "ReCentBaseDir";
	public static final String config_DownloadApp = "DownloadApp";


	public static final String config_Version = "Version";
	public static final double NowVersion = 4.4;

	public static void init() throws IOException {
		File file = Config.getWorkFile(Config.configFileName);
		if (file.exists() == false) {
			file.createNewFile();

			Config.setBaseDir(Config.defBaseDir);
			Config.setWebPort(Config.defWebPort);
			Config.setMultiThreadDownload(Config.defMultiThreadDownload);
			Config.setMaxMonitorThread(Config.defMaxThread);
			Config.setDownloadUserUploadSpeedLimit(Config.defDownloadUserUploadSpeedLimit);
			Config.setUploadDataToSpeedLimit(Config.defUploadDataToSpeedLimit);
			Config.setUploadFile(Config.defFileUpload);
			Config.setFileListLatticeMode(Config.defFileListLatticeMode);
			Config.setOpenAppOpenWebServer(Config.defOpenAppOpenWebServer);
			Config.setReCentBaseDir(Config.defReCentBaseDir);
			Config.setSupportDownloadApp(Config.defDownloadApp);
		}
	}

	public static double toDouble(Object o) {
		try {
			return Double.parseDouble(o.toString());
		} catch (Exception e) {
			return 0;
		}
	}
	public static long toLong(Object o) {
		try {
			return Long.parseLong(o.toString());
		} catch (Exception e) {
			return 0;
		}
	}
	public static int toInt(Object o) {
		try {
			return Integer.parseInt(o.toString());
		} catch (Exception e) {
			return 0;
		}
	}

	public static void setUploadDataToSpeedLimit(long input) {
		setConfig(config_UploadDataToSpeedLimit, String.valueOf(input));
	}
	public static long getUploadDataToSpeedLimit() {
		return toLong(getConfig(config_UploadDataToSpeedLimit));
	}
	public static boolean isUploadDataToSpeedLimit() {
		return getUploadDataToSpeedLimit() > -1;
	}



	public static void setDownloadUserUploadSpeedLimit(long input) {
		setConfig(config_DownloadUserUploadSpeedLimit, String.valueOf(input));
	}
	public static long getDownloadUserUploadSpeedLimit() {
		return toLong(getConfig(config_DownloadUserUploadSpeedLimit));
	}
	public static boolean isDownloadUserUploadSpeedLimit() {
		return getDownloadUserUploadSpeedLimit() > -1;
	}


	public static void setMaxMonitorThread(int input) {
		setConfig(config_MaxMonitorThread, String.valueOf(input));
	}
	public static int getMaxMonitorThread() {
		return toInt(getConfig(config_MaxMonitorThread));
	}

	public static void setMultiThreadDownload(boolean b) {
		setConfig(config_MultiThreadDownload, String.valueOf(b));
	}
	public static boolean getMultiThreadDownload() {
		try {
			return  Boolean.parseBoolean(getConfig(config_MultiThreadDownload));
		} catch (Exception e) {
			return true;
		}
	}



	public static void setOpenAppOpenWebServer(boolean b) {
		setConfig(config_OpenAppOpenWebServer, String.valueOf(b));
	}
	public static boolean getOpenAppOpenWebServer() {
		try {
			return Boolean.parseBoolean(getConfig(config_OpenAppOpenWebServer));
		} catch (Exception e) {
			return false;
		}
	}

	public static void setUploadFile(boolean b) {
		setConfig(config_UploadFile, String.valueOf(b));
	}
	public static boolean getUploadFile() {
		try {
			return Boolean.parseBoolean(getConfig(config_UploadFile));
		} catch (Exception e) {
			return true;
		}
	}

	

	public static void setFileListLatticeMode(boolean b) {
		setConfig(config_DefFileListLatticeMode, String.valueOf(b));
	}
	public static boolean getFileListLatticeMode() {
		try {
			return Boolean.parseBoolean(getConfig(config_DefFileListLatticeMode));
		} catch (Exception e) {
			return false;
		}
	}


	public static void setSupportDownloadApp(boolean b) {
		setConfig(config_DownloadApp, String.valueOf(b));
	}
	public static boolean getSupportDownloadApp() {
		try {
			return Boolean.parseBoolean(getConfig(config_DownloadApp));
		} catch (Exception e) {
			return true;
		}
	}




	public static void addReCentBaseDir(String link) {
		List<String> fill = getReCentBaseDir();

		XFixelArrayFill<String> newFill = new XFixelArrayFill<>(5);
		newFill.right(fill.toArray(new String[fill.size()]));
		if (XArrays.indexOf(newFill.getArray(), link, 0, newFill.length()) <= -1) {
			newFill.right(link);
		}
		setReCentBaseDir(newFill.getArray());
	}
	public static void setReCentBaseDir(Object[] fill) {
		StringBuilder sb = new StringBuilder();
		if (fill != null) {
			for (int i = 0;i < fill.length;i++) {
				if (fill[i] == null)
					continue;
				sb.append(fill[i]).append(XStaticFixedValue.String_NextLineN);
			}
		}
		setConfig(config_ReCentBaseDir, sb.toString());
	}
	public static List<String> getReCentBaseDir() {
		List<String> newList = new ArrayList<>();
		try {
			String config = getConfig(config_ReCentBaseDir);
			List<String> split = XString.split(config, XStaticFixedValue.String_NextLineN);
			for (int i = 0;i < split.size();i++) {
				if (split.get(i) != null) {
					newList.add(split.get(i));
				}
			}
			return newList;
		} catch (Exception e) {
			return newList;
		}
	}




	public static void setWebPort(int Port) {
		setConfig(config_WebPort, String.valueOf(Port));
	}
	public static int getWebPort() {
		return toInt(getConfig(config_WebPort));
	}



	public static void setBaseDir(String Dir) {
		setConfig(config_BaseDir, Dir);
	}
	public static String getBaseDir() {
		return getConfig(config_BaseDir);
	}


	public static void setVersion(double Version) {
		setConfig(config_Version, Double.toString(Version));
	}
	public static double getVersion() {
		return  toDouble(getConfig(config_Version));
	}
	public static double getNowVersion() {
		return NowVersion;
	}

	public static void setConfig(String key, String obj) {
		try {
			key = XObjects.requireNonNull(key);
			obj = XObjects.requireNonNull(obj);
			Properties PropertiesConfig = new Properties();
			InputStream in = new XRandomAccessFileInputStream(getWorkFile(configFileName));
			PropertiesConfig.load(in);
			in.close();
			PropertiesConfig.setProperty(key, obj);
			PropertiesConfig.setProperty(Config.config_Version, String.valueOf(Config.getNowVersion()));
			getWorkFile(configFileName).delete();
			OutputStream out = new XRandomAccessFileOutputStream(getWorkFile(configFileName));
			PropertiesConfig.save(out, "#");

			out.close();
			PropertiesConfig.clear();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static String getConfig(String key) {
		try {
			key = XObjects.requireNonNull(key);
			Properties PropertiesConfig = new Properties();
			InputStream in = new XRandomAccessFileInputStream(getWorkFile(configFileName));
			PropertiesConfig.load(in);
			in.close();
			Object result = PropertiesConfig.get(key);
			PropertiesConfig.clear();
			return result == null ?null: result.toString();
		} catch (IOException e) {
			return null;//throw new RuntimeException(e);
		}

	}
	public static Map getConfig() {
		Map m = new HashMap();
		try {
			Properties PropertiesConfig = new Properties();
			InputStream in = new XRandomAccessFileInputStream(getWorkFile(configFileName));
			PropertiesConfig.load(in);
			Set keys = PropertiesConfig.keySet();
			in.close();
			for (Object key:keys) {
				m.put(key, PropertiesConfig.get(key));
			}
			PropertiesConfig.clear();
			return m;
		} catch (IOException e) {
			return m;//throw new RuntimeException(e);
		}

	}


	public static String configFileName = "config.txt";
	public static File getWorkFile(String fileName) {
		File f = new File(getWorkDir(), fileName);
		return f;
	}
	public static File getLogFile(String fileName) {
		File f = new File(getLogDir(), fileName);
		return f;
	}
	public static final String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String DirPath = new File(Environment.getExternalStorageDirectory() , "/FolsTop/SocketFileListServer").getAbsolutePath();
	public static File getWorkDir() {
		File f = new File(DirPath);
		if (!f.exists())
			f.mkdirs();
		return f;
	}
	public static File getLogDir() {
		File f = new File(getWorkDir(), "log");
		if (!f.exists())
			f.mkdirs();
		return f;
	}











	public static interface BaseDirChange {
		public void change(String newDir);
	}
	public static void setBaseDir(Activity activity, String title, final BaseDirChange callback) {
		final String config = Config.getBaseDir();

		final RelativeLayout relativeLayout = new RelativeLayout(activity);
		relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

		final EditText et = new EditText(activity);
		et.setText(config);
		et.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		et.setId(1);
		et.setPadding(et.getPaddingLeft() + 20, et.getPaddingTop(), et.getPaddingRight(), et.getPaddingBottom());

		RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutparams.addRule(RelativeLayout.BELOW, et.getId());

		final ListView recentBaseDirlist = new ListView(activity);
		recentBaseDirlist.setLayoutParams(layoutparams);
		recentBaseDirlist.setDivider(null);
		final ArrayList<String> xal = new ArrayList<String>();
		xal.addAll(Arrays.asList(Config.getReCentBaseDir().toArray(new String[Config.getReCentBaseDir().size()])));
		ArrayAdapter<String> aa = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
													 xal.toArray());
		recentBaseDirlist.setAdapter(aa);
		aa.notifyDataSetChanged();

		recentBaseDirlist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
					// TODO: Implement this method
					et.setText(xal.get(p3));
				}
			});
		relativeLayout.addView(et);
		relativeLayout.addView(recentBaseDirlist);

		new AlertDialog.Builder(activity)
			.setTitle(title) 
			.setView(relativeLayout) 
			.setPositiveButton(activity.getText(R.string.confirm), new DialogInterface.OnClickListener() {  

				public void onClick(DialogInterface dialog, int which) {
					String input = et.getText().toString(); 

					File file = new File(input);
					if (file.exists() == false) 
						MainActivity.toast("目标不存在");  
					else if (file.isDirectory() == false)
						MainActivity.toast("目标不是目录");  
					else if (file.canRead() == false)
						MainActivity.toast("目标不可读");  
					else {
						if (file.canWrite() == false)
							MainActivity.toast("目标不可写"); 

						Config.setBaseDir(input);
						Config.addReCentBaseDir(input);
						if (callback != null)
							callback.change(input);
						MainActivity.heatSet();
					}
				}  
			})  
			.setNeutralButton("默认", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {

					Config.setBaseDir(Config.defBaseDir);
					if (callback != null)
						callback.change(Config.getBaseDir());
					MainActivity.heatSet();
				}  
			})
			.setNegativeButton(activity.getText(R.string.cancel), null)  
			.show();

	}


}

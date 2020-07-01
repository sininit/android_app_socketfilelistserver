package top.fols.box.application.socketfilelistserver;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Grow {
	public static class AppMessage{
		public String appName;
		public String packageName;
		public File apkFilePath;
	}
	// 应用名 + 包名
	public static List<AppMessage> getAppList(Context context) {
		PackageManager pm = context.getPackageManager();
		// Return a List of all packages that are installed on the device.
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<AppMessage> list = new ArrayList<>();
		for (PackageInfo packageInfo : packages) {
			// 判断系统/非系统应用
//			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
//				System.out.println("MainActivity.getAppList, packageInfo=" + packageInfo.packageName);
//			} else {
//				// 系统应用        
//			}
			String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
    		String packageName = packageInfo.packageName;
			
			AppMessage am;
			am = new AppMessage();
			am.appName = appName;
			am.packageName = packageName;
			am.apkFilePath = new File(packageInfo.applicationInfo.publicSourceDir);
			list.add(am);
			
			am = null;
		}
		return list;
	}
	public static File getFile(Context context,String packageName){
		try {
			return new File(context.getPackageManager().getApplicationInfo(packageName, 0).publicSourceDir);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}
}

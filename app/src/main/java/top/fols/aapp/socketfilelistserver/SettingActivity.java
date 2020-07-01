package top.fols.aapp.socketfilelistserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import top.fols.aapp.simpleListView.Entry;
import top.fols.aapp.simpleListView.EntryAdapter;
import top.fols.aapp.socketfilelistserver.Config;
import top.fols.aapp.socketfilelistserver.MainActivity;
import top.fols.aapp.socketfilelistserver.SettingActivity;
import top.fols.box.util.XExceptionTool;
import top.fols.box.io.os.XFile;

public class SettingActivity extends Activity {
	private List<Entry> fruitList;
	private ListView listView;
	private EntryAdapter adapter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
			setContentView(R.layout.setting);
			setTitle(R.string.setting_name);

			if (listView == null) {
				fruitList = new ArrayList<Entry>(); 
				adapter = new EntryAdapter(SettingActivity.this, fruitList);

				listView = (ListView)findViewById(R.id.settingListView1);
				listView.setAdapter(adapter);

				final Entry entry;
				entry = new Entry();
				entry.title = "启动应用时启动服务器";
				entry.title2 = "" + (Config.getOpenAppOpenWebServer() ?getText(R.string.on): getText(R.string.off));
				entry.title2show = true;
				entry.checkbox = Config.getOpenAppOpenWebServer();
				entry.checkboxShow = true;
				entry.onChange = new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1, boolean p2) {
						// TODO: Implement this method
						Config.setOpenAppOpenWebServer(p2);
						entry.checkbox = p2;
						entry.title2 = "" + (Config.getOpenAppOpenWebServer() ?getText(R.string.on): getText(R.string.off));
						adapter.notifyDataSetChanged();
						MainActivity.heatSet();
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "基础目录";
				entry.title2show = true;
				entry.title2 = Config.getBaseDir();
				entry.checkbox = false;
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						Config.setBaseDir(SettingActivity.this, 
							entry.title,
							new Config.BaseDirChange(){
								@Override
								public void change(String newDir) {
									// TODO: Implement this method
									entry.title2 = newDir;
									adapter.notifyDataSetChanged();
									MainActivity.heatSet();
								}
							});
					}
				};
				fruitList.add(entry);


				entry = new Entry();
				entry.title = "服务器端口";
				entry.title2show = true;
				entry.title2 = "" + Config.getWebPort();
				entry.checkbox = false;
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						final String config = String.valueOf(Config.getWebPort());
						final EditText et = new EditText(SettingActivity.this);
						et.setInputType(InputType.TYPE_CLASS_NUMBER);
						et.setText(config);
						new AlertDialog.Builder(SettingActivity.this)
							.setTitle(entry.title)  
							.setView(et)  
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) {
									String input = et.getText().toString(); 
									int i = Config.toInt(input);
									if ("".equals(input) || null == input) 
										toast("端口不能为空");  
									else if (i < 1 || i > 65535) {
										toast("端口异常");  
									} else {
										Config.setWebPort(i);

										entry.title2 = (String.valueOf(Config.getWebPort()));
										adapter.notifyDataSetChanged();
										toast("修改完毕,请重启应用");
										MainActivity.heatSet();
									}

								}  
							})  
							.setNeutralButton("默认", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) {
									Config.setWebPort(Config.defWebPort);
									entry.title2 = (String.valueOf(Config.getWebPort()));
									adapter.notifyDataSetChanged();
									toast("修改完毕,请重启应用");
									MainActivity.heatSet();
								} 
							})
							.setNegativeButton("取消", null)  
							.show();
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "列表默认宫格模式";
				entry.title2 = "" + (Config.getFileListLatticeMode() ?getText(R.string.on): getText(R.string.off));
				entry.title2show = true;
				entry.checkbox = Config.getFileListLatticeMode();
				entry.checkboxShow = true;
				entry.onChange = new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1, boolean p2) {
						// TODO: Implement this method
						Config.setFileListLatticeMode(p2);
						entry.checkbox = p2;
						entry.title2 = "" + (Config.getFileListLatticeMode() ?getText(R.string.on): getText(R.string.off));
						adapter.notifyDataSetChanged();
						MainActivity.heatSet();
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "允许上传文件";
				entry.title2 = "" + (Config.getUploadFile() ?getText(R.string.on): getText(R.string.off));
				entry.title2show = true;
				entry.checkbox = Config.getUploadFile();
				entry.checkboxShow = true;
				entry.onChange = new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1, boolean p2) {
						// TODO: Implement this method
						Config.setUploadFile(p2);
						entry.checkbox = p2;
						entry.title2 = "" + (Config.getUploadFile() ?getText(R.string.on): getText(R.string.off));
						adapter.notifyDataSetChanged();
						MainActivity.heatSet();
					}
				};
				fruitList.add(entry);


				entry = new Entry();
				entry.title = "多线程文件下载";
				entry.title2 = "" + (Config.getMultiThreadDownload() ?getText(R.string.on): getText(R.string.off));
				entry.title2show = true;
				entry.checkbox = Config.getMultiThreadDownload();
				entry.checkboxShow = true;
				entry.onChange = new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1, boolean p2) {
						// TODO: Implement this method
						Config.setMultiThreadDownload(p2);
						entry.checkbox = p2;
						entry.title2 = "" + (Config.getMultiThreadDownload() ?getText(R.string.on): getText(R.string.off));
						adapter.notifyDataSetChanged();
						MainActivity.heatSet();
					}
				};
				fruitList.add(entry);


				entry = new Entry();
				entry.title = "允许下载App";
				entry.title2 = "" + (Config.getSupportDownloadApp() ?getText(R.string.on): getText(R.string.off));
				entry.title2show = true;
				entry.checkbox = Config.getSupportDownloadApp();
				entry.checkboxShow = true;
				entry.onChange = new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1, boolean p2) {
						// TODO: Implement this method
						Config.setSupportDownloadApp(p2);
						entry.checkbox = p2;
						entry.title2 = "" + (Config.getSupportDownloadApp() ?getText(R.string.on): getText(R.string.off));
						adapter.notifyDataSetChanged();
						MainActivity.heatSet();
					}
				};
				fruitList.add(entry);



				entry = new Entry();
				entry.title = "最大监听线程";
				entry.title2show = true;
				entry.title2 = "" + Config.getMaxMonitorThread();
				entry.checkbox = false;
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						final String config = String.valueOf(Config.getMaxMonitorThread());
						final EditText et = new EditText(SettingActivity.this);
						et.setInputType(InputType.TYPE_CLASS_NUMBER);
						et.setText(config);
						new AlertDialog.Builder(SettingActivity.this)
							.setTitle(entry.title)  
							.setView(et)  
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) {
									String input = et.getText().toString(); 
									int i = Config.toInt(input);

									if ("".equals(input) || null == input) 
										toast("不能为空");  
									else if (i < 1) {
										toast("输入异常");  
									} else {
										Config.setMaxMonitorThread(i);

										entry.title2 = (String.valueOf(Config.getMaxMonitorThread()));
										adapter.notifyDataSetChanged();
										MainActivity.heatSet();
									}
								}
							})
							.setNeutralButton("默认", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) {
									Config.setMaxMonitorThread(Config.defMaxThread);

									entry.title2 = (String.valueOf(Config.getMaxMonitorThread()));
									adapter.notifyDataSetChanged();
									MainActivity.heatSet();
								}  
							})
							.setNegativeButton("取消", null)  
							.show();
					}
				};
				fruitList.add(entry);


				entry = new Entry();
				entry.title = "上传数据给用户速度限制";
				entry.title2show = true;
				String text = null;
				if (Config.isUploadDataToSpeedLimit())
					text = String.valueOf(Config.getUploadDataToSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getUploadDataToSpeedLimit()) + "/s)");
				else
					text = String.valueOf("无限制");
				entry.title2 = text;
				entry.checkbox = false;
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						final RelativeLayout relativeLayout = new RelativeLayout(SettingActivity.this);
						relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
						final TextView TextView = new TextView(SettingActivity.this);
						final SeekBar et = new SeekBar(SettingActivity.this);
						et.setMax(5000);
						et.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
						et.setId(1);

						RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						layoutparams.addRule(RelativeLayout.BELOW, et.getId());
						TextView.setGravity(Gravity.CENTER_HORIZONTAL);
						TextView.setLayoutParams(layoutparams);
						TextView.setText("0B/s");

						et.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
							{
								@Override
								public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
									if (et.getProgress() == et.getMax()) {
										TextView.setText("没有限制");
									} else {
										long speed = formatSeekToSpeed(et.getProgress());
										TextView.setText(XFile.fileUnitFormat(speed) + "/s");
									}
								}
								@Override
								public void onStartTrackingTouch(SeekBar p1) {
									// TODO: Implement this method
								}
								@Override
								public void onStopTrackingTouch(SeekBar p1) {
									// TODO: Implement this method
								}
							});
						int index = formatSpeedToSeek(Config.getUploadDataToSpeedLimit());
						if (index < 0)
							index = et.getMax();
						et.setProgress(index);
						if (et.getProgress() == et.getMax()) {
							TextView.setText("没有限制");
						} else {
							long speed = formatSeekToSpeed(et.getProgress());
							TextView.setText(XFile.fileUnitFormat(speed) + "/s");
						}

						relativeLayout.addView(et);
						relativeLayout.addView(TextView);

						new AlertDialog.Builder(SettingActivity.this)
							.setTitle(entry.title)
							.setView(relativeLayout)  
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) { 
									long i = formatSeekToSpeed(et.getProgress());
									if (et.getProgress() == et.getMax())
										i = -1;
									Config.setUploadDataToSpeedLimit(i);
									String text;
									if (Config.isUploadDataToSpeedLimit())
										text = String.valueOf(Config.getUploadDataToSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getUploadDataToSpeedLimit()) + "/s)");
									else
										text = String.valueOf("无限制");
									entry.title2 = (text);
									adapter.notifyDataSetChanged();
									MainActivity.heatSet();

								}

							})
							.setNeutralButton("默认", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) {
									Config.setUploadDataToSpeedLimit(Config.defUploadDataToSpeedLimit);
									String text;
									if (Config.isUploadDataToSpeedLimit())
										text = String.valueOf(Config.getUploadDataToSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getUploadDataToSpeedLimit()) + "/s)");
									else
										text = String.valueOf("无限制");
									entry.title2 = (text);
									adapter.notifyDataSetChanged();
									MainActivity.heatSet();

								}  
							})
							.setNegativeButton("取消", null)  
							.show();
					}
				};
				fruitList.add(entry);


				entry = new Entry();
				entry.title = "下载用户上传的数据速度限制";
				entry.title2show = true;
				text = null;
				if (Config.isDownloadUserUploadSpeedLimit())
					text = String.valueOf(Config.getDownloadUserUploadSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getDownloadUserUploadSpeedLimit()) + "/s)");
				else
					text = String.valueOf("无限制");
				entry.title2 = text;
				entry.checkbox = false;
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						final RelativeLayout relativeLayout = new RelativeLayout(SettingActivity.this);
						relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
						final TextView TextView = new TextView(SettingActivity.this);
						final SeekBar et = new SeekBar(SettingActivity.this);
						et.setMax(5000);
						et.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
						et.setId(1);

						RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						layoutparams.addRule(RelativeLayout.BELOW, et.getId());
						TextView.setGravity(Gravity.CENTER_HORIZONTAL);
						TextView.setLayoutParams(layoutparams);
						TextView.setText("0B/s");

						et.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
							{
								@Override
								public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
									if (et.getProgress() == et.getMax()) {
										TextView.setText("没有限制");
									} else {
										long speed = formatSeekToSpeed(et.getProgress());
										TextView.setText(XFile.fileUnitFormat(speed) + "/s");
									}
								}
								@Override
								public void onStartTrackingTouch(SeekBar p1) {
									// TODO: Implement this method
								}
								@Override
								public void onStopTrackingTouch(SeekBar p1) {
									// TODO: Implement this method
								}
							});
						int index = formatSpeedToSeek(Config.getDownloadUserUploadSpeedLimit());
						if (index < 0)
							index = et.getMax();
						et.setProgress(index);
						if (et.getProgress() == et.getMax()) {
							TextView.setText("没有限制");
						} else {
							long speed = formatSeekToSpeed(et.getProgress());
							TextView.setText(XFile.fileUnitFormat(speed) + "/s");
						}

						relativeLayout.addView(et);
						relativeLayout.addView(TextView);

						new AlertDialog.Builder(SettingActivity.this)
							.setTitle(entry.title)
							.setView(relativeLayout)  
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) { 
									long i = formatSeekToSpeed(et.getProgress());
									if (et.getProgress() == et.getMax())
										i = -1;
									Config.setDownloadUserUploadSpeedLimit(i);
									String text;
									if (Config.isDownloadUserUploadSpeedLimit())
										text = String.valueOf(Config.getDownloadUserUploadSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getDownloadUserUploadSpeedLimit()) + "/s)");
									else
										text = String.valueOf("无限制");
									entry.title2 = (text);
									adapter.notifyDataSetChanged();
									MainActivity.heatSet();
								}
							})
							.setNeutralButton("默认", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int which) {
									Config.setDownloadUserUploadSpeedLimit(Config.defDownloadUserUploadSpeedLimit);
									String text;
									if (Config.isUploadDataToSpeedLimit())
										text = String.valueOf(Config.getDownloadUserUploadSpeedLimit() + "(" + XFile.fileUnitFormat(Config.getDownloadUserUploadSpeedLimit()) + "/s)");
									else
										text = String.valueOf("无限制");
									entry.title2 = (text);
									adapter.notifyDataSetChanged();
									MainActivity.heatSet();

								}  
							})
							.setNegativeButton("取消", null)  
							.show();
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title2 = "其它";
				entry.title2show = true;
				fruitList.add(entry);

				entry = new Entry();
				long dirLength = 0;
				final File[] fs = Config.getLogDir().listFiles();
				for (int i = 0;fs != null && i < fs.length;i++) {
					dirLength += fs[i].length();
				}
				entry.title = "日志";
				entry.title2show = true;
				entry.title2 = XFile.fileUnitFormat(dirLength) + "(" + (fs == null ?0: fs.length) + ")";
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						new AlertDialog.Builder(SettingActivity.this)
							.setTitle(getText(R.string.delete) + "?")
							.setCancelable(true)
							.setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1, int p2) {
									// TODO: Implement this method
									for (int i = 0;fs != null && i < fs.length;i++) {
										fs[i].delete();
									}
									long dirLength = 0;
									final File[] fs = Config.getLogDir().listFiles();
									for (int i = 0;fs != null && i < fs.length;i++) {
										dirLength += fs[i].length();
									}
									entry.title2 = XFile.fileUnitFormat(dirLength) + "(" + (fs == null ?0: fs.length) + ")";
									adapter.notifyDataSetChanged();
								}
							})
							.show();
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "邮箱";
				entry.title2show = true;
				entry.title2 = "784920843@qq.com";
				entry.checkboxShow = false;
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "版本";
				entry.title2show = true;
				entry.title2 = "" + Config.getNowVersion();
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						Utils.openLink(SettingActivity.this, "https://www.coolapk.com/apk/top.fols.aapp.socketfilelistserver");
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "赞助";
				entry.title2show = true;
				entry.title2 = "" + "alipay(支付宝) 784920843@qq.com";
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						Utils.openLink(SettingActivity.this, "https://mobilecodec.alipay.com/client_download.htm?qrcode=a6x02342sxg3u49xdxf47b6");
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "主页";
				entry.title2show = true;
				entry.title2 = "" + "打开我的主页";
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						Utils.openLink(SettingActivity.this, "http://fols.top");
					}
				};
				fruitList.add(entry);

				entry = new Entry();
				entry.title = "Github";
				entry.title2show = true;
				entry.title2 = "" + "Open Github";
				entry.checkboxShow = false;
				entry.onClick = new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						Utils.openLink(SettingActivity.this, "https://github.com/xiaoxinwangluo/android_app_socketfilelistserver");
					}
				};
				fruitList.add(entry);



				adapter.notifyDataSetChanged();

			}

		} catch (Throwable e) {
			MainActivity.toast(XExceptionTool.StackTraceToString(e));
		}
	}  

	public long formatSeekToSpeed(int seek) {
		return ((long)seek + 1L) * 8192L * 4L;
	}
	public int formatSpeedToSeek(long speed) {
		if (speed < 0)
			return -1;
		else if (speed == 0)
			return 0;
		return (int)(speed / 4L / 8192L - 1L);
	}
	public static void toast(Object o) {
		if (o instanceof Exception) {
			MainActivity.toast((Exception)o);
		} else {
			MainActivity.toast(o);
		}
	}
}

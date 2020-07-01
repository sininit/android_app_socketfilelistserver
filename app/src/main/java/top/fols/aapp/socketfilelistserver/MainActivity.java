package top.fols.aapp.socketfilelistserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import top.fols.aapp.utils.XUIHandler;
import top.fols.aapp.view.alertdialog.SingleSelectAlertDialog;
import top.fols.aapp.view.alertdialog.SingleSelectAlertDialogCallback;
import top.fols.box.application.httpserver.XHttpServer;
import top.fols.box.application.socketfilelistserver.XHttpFileListDataPacketHander;
import top.fols.box.io.XStream;
import top.fols.box.io.os.XFile;
import top.fols.box.util.XExceptionTool;

public class MainActivity extends Activity  implements XUIHandler.MessageDeal {

	private ImageView main_cloud_status_icon = null;
	private TextView main_cloud_status_text = null;
	private Switch main_cloud_status_switch = null;
	private TextView main_baseDir= null;

	private TextView main_link_count = null;
	private TextView main_ipv4_addres = null;
	private TextView main_ipv6_addres = null;
	private TextView main_upload_speed = null;
	private TextView main_download_speed = null;
	private TextView main_log_size = null;

	private void updateUiCloudStatus(boolean b) {
		if (main_cloud_status_icon == null)
			main_cloud_status_icon = (ImageView) findViewById(R.id.main_cloud_icon);
		main_cloud_status_icon.setImageDrawable(getResources().getDrawable(b ?R.drawable.ic_cloud_status_on: R.drawable.ic_cloud_status_off));

		if (main_cloud_status_text == null)
			main_cloud_status_text = (TextView)findViewById(R.id.main_cloud_status);
		main_cloud_status_text.setText(getText(b ?R.string.on: R.string.off));

		if (main_cloud_status_switch == null)
			main_cloud_status_switch = (Switch)findViewById(R.id.main_cloud_switch);
		main_cloud_status_switch.setChecked(b);
	}
	private void updateUiBaseDir(String dir) {
		if (main_baseDir == null)
			main_baseDir = (TextView)findViewById(R.id.main_basedir);
		main_baseDir.setText(dir);
	}
	private void updateUiThreadCount(long count) {
		if (main_link_count == null)
			main_link_count = (TextView)findViewById(R.id.main_link_count);
		main_link_count.setText(count + "");
	}
	private void updateUiIPV4Addres(String addres) {
		if (main_ipv4_addres == null)
			main_ipv4_addres = (TextView)findViewById(R.id.main_ip_v4);
		main_ipv4_addres.setText(addres);
	}
	private void updateUiIPV6Addres(String addres) {
		if (main_ipv6_addres == null)
			main_ipv6_addres = (TextView)findViewById(R.id.main_ip_v6);
		main_ipv6_addres.setText(addres);
	}
	private void updateUiUploadSpeed(String speed) {
		if (main_upload_speed == null)
			main_upload_speed = (TextView)findViewById(R.id.main_speed_upload);
		main_upload_speed.setText(speed + "");
	}
	private void updateUiDownloadSpeed(String speed) {
		if (main_download_speed == null)
			main_download_speed = (TextView)findViewById(R.id.main_speed_download);
		main_download_speed.setText(speed + "");
	}
	private void updateUiLogSize(String size) {
		if (main_log_size == null)
			main_log_size = (TextView)findViewById(R.id.main_log_size);
		main_log_size.setText(size + "");
	}




	private void openServer(boolean isSwitch) {
		try {
			if (isSwitch) {
				server.stop();
				server.start();
				server.log(getText(R.string.server_start) + "");//启动服务器
			} else {
				server.stop();
				server.log(getText(R.string.server_end) + "");//停止服务器
			}
		} catch (BindException e) {
			toast(getText(R.string.server_exception) + ": " + e.getMessage());
			server.log(getText(R.string.server_exception) + ": " + e.getMessage());//服务器异常
		} catch (Exception e) {
			toast(getText(R.string.server_exception) + ": \n" + XExceptionTool.StackTraceToString(e));
			server.log(XExceptionTool.StackTraceToString(e));
		}
	}



	public boolean onCreateOptionsMenu(Menu menu) {
		//调用inflate()方法创建菜单
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //如果返回false，创建的菜单无法显示
        return true;
    }
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			//通过调用item.getItemId()来判断菜单项
			switch (item.getItemId()) {
				case R.id.settingMenu:
					Intent intent = new Intent();
					intent.setClass(getContext(), SettingActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("name", "This is from MainActivity!");
					intent.putExtras(bundle);
					startActivity(intent);

					break;
				case R.id.clearResMeun:
					File zipres = new File(getFilesDir(), "res.zip");
					zipres.delete();
					writerResZip();
					if (fileListDataHander != null) {
						fileListDataHander.clearResCache();
						fileListDataHander.setResZipPath(zipres);
						toast(getText(R.string.write_successful) + "");//写入成功
					}
					break;
				case R.id.copyV4linkMenu:
					Utils.setClip(MainActivity.this, getV4URL());
					toast(getText(R.string.copy_successful) + "");//"复制成功."
					break;
				case R.id.shareV4linkMenu:
					shareMsg(getString(R.string.app_name) + Config.getNowVersion(), /* 分享连接*/getText(R.string.share_link) + "", getV4URL());
					break;
				case R.id.thisPhoneV4LocalAddresMenu:{
						SingleSelectAlertDialog dialog;
						dialog = new SingleSelectAlertDialog();
						dialog.setContext(this);
						final List<String> allHost = Utils.getIPV4LANList();
						for (int i = 0;i < allHost.size();i++) {
							allHost.set(i, formatHttpUrl(allHost.get(i), getPort()));
						}
						String[] str = new String[allHost.size()];
						dialog.setItems(allHost.toArray(str));
						dialog.setCancelable(true);
						dialog.setDirectSelectMode(true);
						dialog.setCallback(new SingleSelectAlertDialogCallback(){
								@Override
								public void selectComplete(SingleSelectAlertDialog obj, String key, int index, boolean isSelected, boolean isNegativeButton) {
									// TODO: Implement this method
									if (key != null && isSelected) {
										showQrCode(allHost.get(index));
									}
								}
							});
						dialog.show();

						break;
					}
				case R.id.copyV6linkMenu:
					Utils.setClip(MainActivity.this, getV6URL());
					toast(getText(R.string.copy_successful) + "");//"复制成功."
					break;
				case R.id.shareV6linkMenu:
					shareMsg(getString(R.string.app_name) + Config.getNowVersion(),  /* 分享连接*/getText(R.string.share_link) + "", getV6URL());
					break;
				case R.id.thisPhoneV6LocalAddresMenu:{
						SingleSelectAlertDialog dialog;
						dialog = new SingleSelectAlertDialog();
						dialog.setContext(this);
						final List<String> allHost = Utils.getIPV6LANList();
						for (int i = 0;i < allHost.size();i++) {
							allHost.set(i, formatHttpUrl(allHost.get(i), getPort()));
						}
						String[] str = new String[allHost.size()];
						dialog.setItems(allHost.toArray(str));
						dialog.setCancelable(true);
						dialog.setDirectSelectMode(true);
						dialog.setCallback(new SingleSelectAlertDialogCallback(){
								@Override
								public void selectComplete(SingleSelectAlertDialog obj, String key, int index, boolean isSelected, boolean isNegativeButton) {
									// TODO: Implement this method
									if (key != null && isSelected) {
										showQrCode(allHost.get(index));
									}
								}
							});
						dialog.show();

						break;
					}
				case R.id.clearLogMenu:
					ps.clear();
					break;
				case R.id.addAppToBaseDir:
					String path = getApplicationContext().getPackageResourcePath();

					File apk = new File(path);
					File f = new File(Config.getBaseDir(), getString(R.string.app_name) + Config.getNowVersion() + ".apk");
					if (f.exists()) {
						toast(getText(R.string.file_already_exist) + ": " + f.getAbsolutePath());
						break;
					}
					if (!new File(Config.getBaseDir()).canWrite()) {
						toast(getText(R.string.no_write_permission) + ": " + f.getAbsolutePath());
						break;
					}
					XStream.copy(new FileInputStream(apk), new FileOutputStream(f));
					toast("OK");
					break;
				case R.id.helpMenu:
					Utils.openLink(MainActivity.this, "https://github.com/xiaoxinwangluo/android_app_socketfilelistserver/blob/master/help.md");
					break;
				default:
			}
		} catch (Exception e) {
			toast(e);
		}
        return true;
    }



	@Override
	public Object dealMessages(Object[] value) {
		if (value.length > 0) {
			int Type = value[0];
			switch (Type) {
				case Type_setTitle:
					setTitle(value[1].toString());
					break;
				case Type_update:
					try {
						updateUiBaseDir(fileListDataHander.getBaseDir().getAbsolutePath());
						updateUiCloudStatus(server.isStop() == false);
						updateUiThreadCount(server.getNowThreadPoolSize());
						updateUiIPV4Addres(getV4Host() + ":" + getPort());
						updateUiIPV6Addres(getV6Host() + ":" + getPort());
						updateUiUploadSpeed(XFile.fileUnitFormat(fileListDataHander.getUpload2UserSpeedLimit().getAverageSpeed()));
						updateUiDownloadSpeed(XFile.fileUnitFormat(fileListDataHander.getDownloadUserSpeedLimit().getAverageSpeed()));
						updateUiLogSize(XFile.fileUnitFormat(ps.getLogFileLength()));
					} catch (Exception e) {
						break;
					}
			}
		}
		return null;
	}

	public void onBackPressed() { 
        new AlertDialog.Builder(this).setTitle(String.format("%s？", getText(R.string.exit))) 
			.setCancelable(true)
            .setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() { 
                @Override 
                public void onClick(DialogInterface dialog, int which) { 
					// 点击“确认”后的操作 
                    server.stop();
					android.os.Process.killProcess(android.os.Process.myPid());
                } 
            }) 
            .show(); 
	} 

	public File writerResZip() throws FileNotFoundException, IOException {
		File zipres = new File(getFilesDir(), "res.zip");
		if (!zipres.exists()) {
			InputStream is = getAssets().open("res.zip");
			OutputStream os = new FileOutputStream(zipres);
			XStream.copy(is, os);
			is.close();
			os.close();
		} 
		return zipres;
	}

	@Override
	protected void onResume() {
		// TODO: Implement this method
		super.onResume();
		context = getApplicationContext();
		handlerHeartbeat();
	}
	public static XUIHandler handlerHeartbeat() {
		if (handler == null) 
			handler = XUIHandler.create();
		return handler;
	}


	private static final int Type_setTitle = 1;
	private static final int Type_update = 2;
	private static final int TYPE_IMAGE_REQUEST_CODE = 4;
	private static final int Type_REQUEST_CODE_CAMERA = 8;
	private static XUIHandler handler;

	private Thread thread = null;
	private Switch switchb = null;
	public static LogFileOutStream ps;

	private static XHttpServer server;
	private static XHttpFileListDataPacketHander fileListDataHander;

	public class thread extends Thread {
		@Override
		public void run() {
			handler.sendMessages(new Object[]{Type_update}, MainActivity.this);
			while (true) {
				try {
					Thread.sleep(1500);
					handler.sendMessages(new Object[]{Type_update}, MainActivity.this);
				} catch (Exception e) {
					continue;
				}

			}
		}
	}
	protected static void heatSet() {
		if (fileListDataHander == null) {
			fileListDataHander = new XHttpFileListDataPacketHander();
			if (server != null) {
				server.setDataHandler(fileListDataHander);
			}
		}
		fileListDataHander.setBaseDir(new File(Config.getBaseDir()));
		fileListDataHander.setSupportRangeDownload(Config.getMultiThreadDownload());
		fileListDataHander.setSupportFileUpload(Config.getUploadFile());

		long speed;
		speed = Config.getDownloadUserUploadSpeedLimit();
		fileListDataHander.getDownloadUserSpeedLimit().setCycleMaxSpeed(speed == -1 ?8192 * 1024: speed);
		fileListDataHander.getDownloadUserSpeedLimit().setLimit(speed != -1);

		speed = Config.getUploadDataToSpeedLimit();
		fileListDataHander.getUpload2UserSpeedLimit().setCycleMaxSpeed(speed == -1 ?8192 * 1024: speed);
		fileListDataHander.getUpload2UserSpeedLimit().setLimit(speed != -1);

		fileListDataHander.setSupportKeepAlive(false);

		fileListDataHander.setFileListLatticeMode(Config.getFileListLatticeMode());
		fileListDataHander.setSupportDownloadApp(Config.getSupportDownloadApp());
		if (server != null) {
			server.setMaxThreadPoolSize(Config.getMaxMonitorThread());

			server.log("Loading Info...");
			Map m = Config.getConfig();
			for (Object Key:m.keySet())
				server.log(Key + "=" + m.get(Key));
		}
	}






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		context = getApplicationContext();
		handlerHeartbeat();

		if (thread == null) {
			thread = new thread();
			thread.start();
		}
		if (ps == null) {
			ps = new LogFileOutStream(Config.getLogFile(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis()) + ".log"), true);
		}
		try {
			Config.init();
			boolean openAppOpenWebServer = Config.getOpenAppOpenWebServer();

			File zipres = writerResZip();

			if (fileListDataHander == null) {
				fileListDataHander = new XHttpFileListDataPacketHander();
			}
			fileListDataHander.setResZipPath(zipres);

			if (server == null) {
				server = new XHttpServer();
			}
			server.setDataHandler(fileListDataHander);
			server.setPort(Config.getWebPort());
			server.setLogSteam(ps);
			heatSet();

			switchb = (Switch)findViewById(R.id.main_cloud_switch);
			switchb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton p1, boolean p2) {
						// TODO: Implement this method
						if (p1.isPressed() == false)
							return;
						openServer(p2);
					}
				});
			openServer(openAppOpenWebServer);
			updateUiCloudStatus(openAppOpenWebServer);
			main_cloud_status_text.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						openServer(switchb.isChecked() == false);
					}
				});



			((TextView)findViewById(R.id.main_ip_v4)).setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View p1) {
						Utils.openLink(MainActivity.this, getV4URL());
					}
				}
			);
			((ImageView)findViewById(R.id.main_ip_v4_icon)).setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View p1) {
						final String V4URL = getV4URL();
						showQrCode(V4URL);
					}
				});


			((TextView)findViewById(R.id.main_ip_v6)).setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View p1) {
						Utils.openLink(MainActivity.this, getV6URL());
					}
				}
			);
			((ImageView)findViewById(R.id.main_ip_v6_icon)).setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View p1) {
						final String V6URL = getV6URL();
						showQrCode(V6URL);
					}
				});




			((TextView)findViewById(R.id.main_log_size)).setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						ScrollView sv = new ScrollView(MainActivity.this);
						sv.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT));

						TextView text = new TextView(MainActivity.this);
						text.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT));
						text.setTextIsSelectable(true);

						StringBuilder sb = new StringBuilder();
						long logLength = ps.getLogFileLength();
						if (logLength <= 1L * 1024L * 1024L) {
							sb.append(new XFile(ps.getLogFile()).toString());
						} else {
							try {
								sb.append(new String(XFile.getBytes(ps.getLogFile(), 0,(int)(1L * 1024L * 1024L))));
							} catch (Throwable e) {
								sb.append("load log file error." + "\n" + XExceptionTool.StackTraceToString(e));
							}
						}
						text.setText(sb);
						sb = null;
						sv.addView(text);

						new AlertDialog.Builder(MainActivity.this)
							.setView(sv) 
							.setCancelable(true)
							.create()
							.show();
					}
				});


			final TextView basedirview = (TextView)findViewById(R.id.main_basedir);
			basedirview.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						Config.setBaseDir(MainActivity.this, 
							MainActivity.this.getText(R.string.config_basedir) + "",
							new Config.BaseDirChange(){
								@Override
								public void change(String newDir) {
									// TODO: Implement this method
									basedirview.setText(newDir);
								}
							});
					}
				});
		} catch (Exception e) {
			toast(e);
		}
    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case TYPE_IMAGE_REQUEST_CODE://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
				if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
					try {
						Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
						String[] filePathColumn = {MediaStore.Images.Media.DATA};
						Cursor cursor = getContentResolver().query(selectedImage,
																   filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
						cursor.moveToFirst();
						int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						String path = cursor.getString(columnIndex); //获取照片路径
						cursor.close();

						String URL = Utils.decodeQrcode(path).toString();
						Utils.openLink(this, URL);
					} catch (Exception e) {
						// TODO Auto-generatedcatch block
						toast(e);
					}
				}
				break;

			case Type_REQUEST_CODE_CAMERA://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
				if (requestCode ==  Type_REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
					try {
						Bundle bundle = data.getExtras();
						// 获取相机返回的数据，并转换为Bitmap图片格式，这是缩略图
						Bitmap bitmap = (Bitmap) bundle.get("data");
						String URL = Utils.decodeQrcode(bitmap).toString();

						Utils.openLink(this, URL);
					} catch (Exception e) {
						// TODO Auto-generatedcatch block
						toast(e);
					}

				}
				break;

		}
	}




	public void showQrCode(final String ul) {
		final RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
		relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

		final Bitmap bmp = Utils. encodeQrcode(ul, 720, 720);
		final ImageView Image = new ImageView(MainActivity.this);
		Image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		Image.setImageBitmap(bmp);
		Image.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View p1) {
					Utils.openLink(MainActivity.this, ul);
					return true;
				}
			});
		Image.setId(1);

		TextView TextView = new TextView(MainActivity.this);
		RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutparams.addRule(RelativeLayout.BELOW, Image.getId());
		TextView.setGravity(Gravity.CENTER_HORIZONTAL);
		TextView.setLayoutParams(layoutparams);
		TextView.setText(Html.fromHtml(String.format("<a href='%s'>%s</a>", ul, ul)));
		TextView.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					// TODO: Implement this method
					Utils.openLink(MainActivity.this, ul);
				}
			});
		relativeLayout.addView(Image);
		relativeLayout.addView(TextView);


		AlertDialog.Builder buider = new AlertDialog.Builder(MainActivity.this)
			.setTitle("Qrcode") 
			.setView(relativeLayout)  
			.setPositiveButton(getText(R.string.shooting) + "", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent, Type_REQUEST_CODE_CAMERA);
				}
			})
			.setNegativeButton(getText(R.string.select_imaeg) + "", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
					intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。  
					//intent.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(intent, TYPE_IMAGE_REQUEST_CODE);
				}
			}).setNeutralButton(getText(R.string.save) + "", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2) {
					try {
						File file = new File(Config.SDPath, System.currentTimeMillis() + ".png");
						byte[] byteArray = Utils.Bitmap2Bytes(bmp);
						new XFile(file).append(byteArray);
						toast(getText(R.string.write_successful) + ": " + file.getAbsolutePath());
						Utils.updateDCIM(getApplicationContext(), file.getAbsolutePath());
					} catch (IOException e) {
						toast(XExceptionTool.StackTraceToString(e));
					}
				}
			});
		buider.show();

	}



	private static Context context = null;
	public static void toast(Object obj) {
		Toast.makeText(getContext(), obj == null ?"": obj.toString(), Toast.LENGTH_LONG).show();
	}
	public static void toast(Exception obj) {
		toast(XExceptionTool.StackTraceToString(obj));
	}
	public static Context getContext() {
		return context;
	}






	public String getV4Host() {
		return Utils.getIPV4LAN();
	}
	public String getV6Host() {
		return Utils.getIPV6LAN();
	}
	public int getPort() {
		return server.getBindPort();
	}
	public String getV4URL() {
		return formatHttpUrl(getV4Host(), String.valueOf(getPort()));
	}
	public String getV6URL() {
		return formatHttpUrl(getV6Host(), String.valueOf(getPort()));
	}
	public static String formatHttpUrl(Object host, Object port) {
		return String.format("http://%s:%s", host, port);
	}

	public void shareMsg(String activityTitle, String msgTitle, String msgText) {  
        shareMsg(activityTitle, msgTitle, msgText, null);
    }
	public void shareMsg(String activityTitle, String msgTitle, String msgText,  
						 String imgPath) {  
        Intent intent = new Intent(Intent.ACTION_SEND);  
        if (imgPath == null || imgPath.equals("")) {  
            intent.setType("text/plain"); // 纯文本  
        } else {  
            File f = new File(imgPath);  
            if (f != null && f.exists() && f.isFile()) {  
                intent.setType("image/jpg");  
				Uri u = Uri.fromFile(f);  
                intent.putExtra(Intent.EXTRA_STREAM, u);  
            }  
        }  
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);  
        intent.putExtra(Intent.EXTRA_TEXT, msgText);  
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        startActivity(Intent.createChooser(intent, activityTitle));  
    } 





}

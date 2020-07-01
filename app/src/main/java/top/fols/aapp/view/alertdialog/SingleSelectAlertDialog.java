package top.fols.aapp.view.alertdialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import top.fols.box.annotation.XAnnotations;
public class SingleSelectAlertDialog {

	private String[] items;
	private Object defSelect;

	private String title;
	private String positiveButton;
	private String negativeButton;
	private boolean cancelable = true;
	private Context context;
	private SingleSelectAlertDialogCallback callback;
	private boolean directSelectMode;

	private boolean contenChange;
	private int index0 = -1;
	public SingleSelectAlertDialog() {
		super();
	}


	public SingleSelectAlertDialog setItems(String... items) {
		this.contenChange = true;
		this.items = items;
		return this;
	}
	@XAnnotations("Type String(value) or Integer(index)")
	public SingleSelectAlertDialog setDefSelect(Object def) {
		this.contenChange = true;
		this.defSelect = def;
		return this;
	}
	
	public Context getContext(){
		return this.context;
	}
	public SingleSelectAlertDialog setDirectSelectMode(boolean b) {
		this.directSelectMode = b;
		return this;
	}
	public SingleSelectAlertDialog setTitle(String title) {
		this.title = title;
		return this;
	}
	public SingleSelectAlertDialog setPositiveButton(String title) {
		this.positiveButton = title;
		return this;
	}
	public SingleSelectAlertDialog setNegativeButton(String title) {
		this.negativeButton = title;
		return this;
	}
	public SingleSelectAlertDialog setCancelable(boolean b) {
		this.cancelable = b;
		return this;
	}
	public SingleSelectAlertDialog setContext(Context v) {
		this.context = v;
		return this;
	}
	public SingleSelectAlertDialog setCallback(SingleSelectAlertDialogCallback callback) {
		this.callback = callback == null ?SingleSelectAlertDialogCallback.defSingleSelectAlertDialogCallback: callback;
		return this;
	}
	
	
	public Object getDefSelect() {
		return this.defSelect;
	}
	public int getDefSelectIndex() {
		if (!contenChange) {
			return index0;
		}
		int index = -1;
		if (defSelect instanceof Integer) {
			index = ((Integer)defSelect).intValue();
			if (index < -1)
				index = -1;
		} else {
			int i = 0;
			if (defSelect == null) {
				for (String s:items) {
					if (s == null) {
						index = i;
						break;
					}
					i++;
				}
			} else {
				for (String s:items) {
					if (defSelect.equals(s)) {
						index = i;
						break;
					}
					i++;
				}
			}
		}
		this.contenChange = false;
		return index0 = index;
	}




	private static class ObjectData {
		public ObjectData() {}
		public ObjectData(Object obj) {this.i = obj;}

		public Object i;
		public int toInt() {return (int)i;}
		public boolean toBoolean() {return (boolean)i;}

		@Override
		public boolean equals(Object obj) {
			// TODO: Implement this method
			return obj == null ? i == null: obj.equals(i);
		}
	}
	private AlertDialog alertDialogObj;
	public void show() {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		if (directSelectMode) {
			final ObjectData newSelect = new ObjectData(-1);
			final int size = items.length;
			alertBuilder.setCancelable(cancelable);
			alertBuilder.setTitle(title);
			alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						newSelect.i = i;
						alertDialogObj.dismiss();
					}
				});
			alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener(){
					@Override
					public void onDismiss(DialogInterface p1) {
						// TODO: Implement this method
						int selectIndex = newSelect.toInt();
						callback.selectComplete(SingleSelectAlertDialog.this, selectIndex < 0 || selectIndex >= size ?null: items[selectIndex], 
												selectIndex,
												newSelect.toInt() == -1 ?false: true,
												newSelect.toInt() == -1);
						alertDialogObj = null;
					}
				});
			alertDialogObj = alertBuilder.create();
			alertDialogObj.show();
		} else {
			final int defSelect = getDefSelectIndex();
			final int size = items.length;
			alertBuilder.setCancelable(cancelable);
			alertBuilder.setTitle(title);
			final ObjectData 
				newSelect = new ObjectData(defSelect),//选择项目引索
				isSelect = new ObjectData(false),//选择过别的项目
				isNegative = new ObjectData(false);//未选择或者结果没变
			
			alertBuilder.setSingleChoiceItems(items, defSelect, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						newSelect.i = i;
						isSelect.i = true;
					}
				});
			alertBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						isNegative.i = false;
						alertDialogObj.dismiss();
					}
				});
			alertBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						newSelect.i = -1;
						isSelect.i = false;
						isNegative.i = true;
						alertDialogObj.dismiss();
					}
				});
			alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener(){
					@Override
					public void onDismiss(DialogInterface p1) {
						// TODO: Implement this method
						int selectIndex = newSelect.toInt();
						if (defSelect == selectIndex)
							isNegative.i = true;
						callback.selectComplete(SingleSelectAlertDialog.this, selectIndex < 0 || selectIndex >= size ?null: items[selectIndex], selectIndex, isSelect.toBoolean(), isNegative.toBoolean());
						alertDialogObj = null;
					}
				});
			alertDialogObj = alertBuilder.create();
			alertDialogObj.show();
		}



	}


}

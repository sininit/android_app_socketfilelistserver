package top.fols.aapp.view.alertdialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import top.fols.box.util.XObjects;

public class MultiSelectAlertDialog {
	private String[] items;
	private int[] defSelect;

	private String title;
	private String positiveButton;
	private String negativeButton;
	private boolean cancelable = true;
	private Context context;
	private MultiSelectAlertDialogCallback callback;

	private boolean contenChange;
	private boolean[] index0;
	public MultiSelectAlertDialog() {
		super();
	}



	public MultiSelectAlertDialog setItems(String... items) {
		this.contenChange = true;
		this.items = items;
		return this;
	}
	public MultiSelectAlertDialog setDefSelect(int[] def) {
		this.contenChange = true;
		this.defSelect = def;
		return this;
	}

	public Context getContext() {
		return this.context;
	}
	public MultiSelectAlertDialog setTitle(String title) {
		this.title = title;
		return this;
	}
	public MultiSelectAlertDialog setPositiveButton(String title) {
		this.positiveButton = title;
		return this;
	}
	public MultiSelectAlertDialog setNegativeButton(String title) {
		this.negativeButton = title;
		return this;
	}
	public MultiSelectAlertDialog setCancelable(boolean b) {
		this.cancelable = b;
		return this;
	}
	public MultiSelectAlertDialog setContext(Context v) {
		this.context = v;
		return this;
	}
	public MultiSelectAlertDialog setCallback(MultiSelectAlertDialogCallback callback) {
		this.callback = callback == null ?MultiSelectAlertDialogCallback.defMultiSelectAlertDialogCallback: callback;
		return this;
	}
	public int[] getDefSelect() {
		return this.defSelect;
	}
	public boolean[] getDefSelectIndexBooleans() {
		if (!contenChange)
			return this.index0;
		final boolean[] newSelect = new boolean[items.length];
		for (int i = 0;i < defSelect.length;i++) 
			if (defSelect[i] > -1 && defSelect[i] < newSelect.length)
				newSelect[defSelect[i]] = true;
		this.index0 = newSelect;
		this.contenChange = false;
		return this.index0;
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
		alertBuilder.setCancelable(cancelable);
		alertBuilder.setTitle(title);
		final ObjectData 
			isSelect = new ObjectData(false),//选择过别的项目
			isNegativeButton = new ObjectData(false);//未选择或者结果没变
		final boolean[] defSelect = getDefSelectIndexBooleans();
		final boolean[] newSelect = Arrays.copyOf(defSelect, defSelect.length);
		alertBuilder.setMultiChoiceItems(items, newSelect, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2, boolean p3) {
					// TODO: Implement this method
					newSelect[p2] = p3;
					isSelect.i = true;
				}
			});
		alertBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					isNegativeButton.i = false;
					alertDialogObj.dismiss();
				}
			});
		alertBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					isSelect.i = false;
					isNegativeButton.i = true;
					alertDialogObj.dismiss();
				}
			});
		alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface p1) {
					// TODO: Implement this method
					List<Integer> newIndex = new ArrayList<>();
					for (int i = 0;i < newSelect.length;i++) {
						if (newSelect[i])
							newIndex.add(i);
					}
					int[] newIndex2 = XObjects.tointArray(newIndex);

					String[] selectKeys = new String[newIndex2.length];
					for (int i:newIndex2)
						selectKeys[i] = items[i];

					if (Arrays.equals(defSelect, newSelect))
						isNegativeButton.i = true;
					callback.selectComplete(MultiSelectAlertDialog.this, selectKeys, newIndex2, isSelect.toBoolean(), isNegativeButton.toBoolean());
					alertDialogObj = null;
				}
			});
		alertDialogObj = alertBuilder.create();
		alertDialogObj.show();
	}


}

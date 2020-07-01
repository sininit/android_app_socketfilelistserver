package top.fols.aapp.simpleListView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

public class Entry {
	public String title;
	public boolean titleshow = true;
	
	public String title2;
	public boolean title2show = false;
	public boolean checkbox = false;
	public boolean checkboxShow = false;
	
	public View.OnClickListener onClick = null;
	public CompoundButton.OnCheckedChangeListener onChange = null;
}  

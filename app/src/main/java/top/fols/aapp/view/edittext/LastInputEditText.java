package top.fols.aapp.view.edittext;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

public class LastInputEditText extends EditText {  

    public LastInputEditText(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  

    public LastInputEditText(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  

    public LastInputEditText(Context context) {  
        super(context);  
    }  

    @Override  
    protected void onSelectionChanged(int selStart, int selEnd) {  
        super.onSelectionChanged(selStart, selEnd);    
    }
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		// TODO: Implement this method
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		
		
		
	}


	public void append(CharSequence text, int color) {
		// TODO: Implement this method
		SpannableString ss = new SpannableString(text);
		ss.setSpan(new ForegroundColorSpan(color), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		super.append(ss);
	}

	@Override
	public void append(CharSequence text, int start, int end) {
		// TODO: Implement this method
		super.append(text, start, end);
	}


}  

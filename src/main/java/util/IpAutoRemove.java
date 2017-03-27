package util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Bertie on 17/3/15.
 */

public class IpAutoRemove implements TextWatcher {
    public EditText cur;
    public EditText next;
    public int n;
    public IpAutoRemove(EditText cur,EditText next,int n) {
        super();
        this.cur = cur;
        this.next = next;
        this.n = n;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.length() == n)
        {
            next.requestFocus();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        // TODO Auto-generated method stub

    }
}

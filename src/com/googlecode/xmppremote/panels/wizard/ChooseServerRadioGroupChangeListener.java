package com.googlecode.xmppremote.panels.wizard;

import com.googlecode.xmppremote.R;

import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

public class ChooseServerRadioGroupChangeListener implements OnCheckedChangeListener {
    
    private Spinner mSpinner;
    private EditText mTextServer;
    
    public ChooseServerRadioGroupChangeListener(Spinner spinner, EditText textServer) {
        mSpinner = spinner;
        mTextServer = textServer;
    }

    
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
        case R.id.radioChooseServer:
            mSpinner.setEnabled(true);
            mTextServer.setEnabled(false);
            break;
        case R.id.radioManualServer:
            mTextServer.setEnabled(true);
            mSpinner.setEnabled(false);
            break;
        }
    }

}

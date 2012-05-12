package com.googlecode.gtalksms.panels.wizard;

import com.googlecode.gtalksms.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;

public class ChooseMethodNextButtonClickListener implements OnClickListener {
    
    private Wizard mWizard;
    private RadioGroup mRg;

    public ChooseMethodNextButtonClickListener(Wizard wizard, RadioGroup rg) {
        mWizard = wizard;
        mRg = rg;
    }
    
    
    public void onClick(View v) {
        int checkedButton = mRg.getCheckedRadioButtonId();
        mWizard.mChoosenMethod = checkedButton;
        switch (checkedButton) {
            case R.id.radioDifferentAccount:
                mWizard.initView(Wizard.VIEW_CREATE_CHOOSE_SERVER);
                break;
            case R.id.radioSameAccount:
                mWizard.initView(Wizard.VIEW_SAME_ACCOUNT);
                break;
            case R.id.radioExsistingAccount:
                mWizard.initView(Wizard.VIEW_EXISTING_ACCOUNT);
                break;
            default:
                throw new IllegalStateException();
        }
    }
}

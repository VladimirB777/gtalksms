package com.googlecode.xmppremote.receivers;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.R;
import com.googlecode.xmppremote.SettingsManager;
import com.googlecode.xmppremote.data.contacts.ContactsManager;
import com.googlecode.xmppremote.tools.Tools;

public class PhoneCallListener extends PhoneStateListener {
    public PhoneCallListener(MainService svc) {
        super();
        this.svc = svc;
        settingsMgr = SettingsManager.getSettingsManager(svc);
    }

    private MainService svc;
    private SettingsManager settingsMgr;
    
    // Android seems to send the intent not only once per call
    // but every 10 seconds for ongoing ringing
    // we prevent multiple "is calling" notifications with this boolean
    private static boolean manageIncoming = true;

    /**
     * incomingNumber is null when the caller ID is hidden
     * 
     * @param state
     * @param incomingNumber
     */
    public void onCallStateChanged(int state, String incomingNumber) {
        if (MainService.IsRunning) {
            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                manageIncoming = true;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                manageIncoming = true;
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                if (settingsMgr.debugLog)
                    Log.d(Tools.LOG_TAG, "PhoneCallListener Call State Ringing with incomingNumber=" + incomingNumber + " manageIncoming=" + manageIncoming);
                if (manageIncoming) {
                    manageIncoming = false;
                    String contact = ContactsManager.getContactName(svc, incomingNumber);
                    // Display the incoming number with the contact name only
                    // if it is not null (and therefore known) and if the contact
                    // name could be determined
                    if ((incomingNumber != null) && !contact.equals(incomingNumber)) {
                        contact = contact + " ( " + incomingNumber + " )";
                    }
                    svc.send(svc.getString(R.string.chat_is_calling, contact), null);
                }
                break;
            default:
                break;
            }
        }
    }
}

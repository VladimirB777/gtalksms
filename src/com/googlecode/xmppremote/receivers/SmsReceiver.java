package com.googlecode.xmppremote.receivers;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.googlecode.xmppremote.Log;
import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.SettingsManager;
import com.googlecode.xmppremote.tools.Tools;


public class SmsReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Map<String, String> msg = RetrieveMessages(intent);

        if (msg == null) {
            // unable to retrieve SMS
            return;
        } else if (MainService.IsRunning) {
            // send all SMS via XMPP by sender
            for (String sender : msg.keySet()) {
                Intent svcintent = Tools.newSvcIntent(context, MainService.ACTION_SMS_RECEIVED, msg.get(sender), null);
                svcintent.putExtra("sender", sender);
                Log.i("SmsReceiver: Issuing service intent for incoming SMS. sender=" + sender + " message=" + Tools.shortenMessage(msg.get(sender)));
                context.startService(svcintent);
            }
            // MainService is not active, test if we find a SMS with the
            // magic word to start xmppremote if so.
        } else {
            String magicWord = SettingsManager.getSettingsManager(context).smsMagicWord.trim().toLowerCase();

            for (String sender : msg.keySet()) {
                String message = msg.get(sender);
                if (message.trim().toLowerCase().compareTo(magicWord) == 0) {
                    Log.i("Connection command received by SMS from " + sender + " issuing intent " + MainService.ACTION_CONNECT);
                    Tools.startSvcIntent(context, MainService.ACTION_CONNECT);
                }
            }
        }
    }
    
    private static Map<String, String> RetrieveMessages(Intent intent) {
        Map<String, String> msg = null; 
        SmsMessage[] msgs = null;
        Bundle bundle = intent.getExtras();
        
        if (bundle != null && bundle.containsKey("pdus")) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                int nbrOfpdus = pdus.length;
                msg = new HashMap<String, String>(nbrOfpdus);
                msgs = new SmsMessage[nbrOfpdus];
                
                // There can be multiple SMS from multiple senders, there can be a maximum of nbrOfpdus different senders
                // However, send long SMS of same sender in one message
                for (int i = 0; i < nbrOfpdus; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    
                    String originatinAddress = msgs[i].getOriginatingAddress();
                    
                    // Check if index with number exists                    
                    if (!msg.containsKey(originatinAddress)) { 
                        // Index with number doesn't exist                                               
                        // Save string into associative array with sender number as index
                        msg.put(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody()); 
                        
                    } else {    
                        // Number has been there, add content but consider that
                        // msg.get(originatinAddress) already contains sms:sndrNbr:previousparts of SMS, 
                        // so just add the part of the current PDU
                        String previousparts = msg.get(originatinAddress);
                        String msgString = previousparts + msgs[i].getMessageBody();
                        msg.put(originatinAddress, msgString);
                    }
                }
            }
        }
        
        return msg;
    }
}

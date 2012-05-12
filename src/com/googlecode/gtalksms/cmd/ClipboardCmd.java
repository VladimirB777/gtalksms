package com.googlecode.gtalksms.cmd;

import android.app.Service;
import android.text.ClipboardManager;
import android.util.Log;

import com.googlecode.gtalksms.MainService;
import com.googlecode.gtalksms.R;
import com.googlecode.gtalksms.tools.Tools;


// The Honeycomb (API >= 11) ClipboardManager (andriod.content.ClipboardManager)
// extends the "older" ClipboardManager (android.text.ClipboardManager), therefore
// the older manager provides the same subset of methods
// We don't care which ClipboardManager is returned by getSystemService(),
// because we only use String operations with the clipboard
@SuppressWarnings("deprecation")


public class ClipboardCmd extends CommandHandlerBase {
    ClipboardManager mOldClipboardMgr;
    
    public ClipboardCmd(MainService mainService) {
        super(mainService, CommandHandlerBase.TYPE_COPY, new Cmd("clipboard", "copy"));
        mOldClipboardMgr = (ClipboardManager) mainService.getSystemService(Service.CLIPBOARD_SERVICE);
    }
    
    @Override
    public void execute(Command cmd) {
        try {
        	String text = cmd.getAllArguments();
            if (text.length() > 0) {
                mOldClipboardMgr.setText(text);
                cmd.respond(getString(R.string.chat_text_copied));
            } else if (mOldClipboardMgr.getText().length() > 0) {
                cmd.respond(getString(R.string.chat_clipboard, mOldClipboardMgr.getText()));
            }
        } catch (Exception ex) {
            Log.w(Tools.LOG_TAG, "Clipboard error", ex);
            send(R.string.chat_error_clipboard);
        }
    }

    @Override
    protected void initializeSubCommands() {
        mCommandMap.get("clipboard").setHelp(R.string.chat_help_copy, "#text#");              
    }
}

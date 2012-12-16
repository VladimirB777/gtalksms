package com.googlecode.xmppremote.cmd;

import android.content.Intent;
import android.net.Uri;

import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.R;

public class UrlsCmd extends CommandHandlerBase {
    public UrlsCmd(MainService mainService) {
        super(mainService, CommandHandlerBase.TYPE_COPY, new Cmd("http", "https"));
    }
    
    @Override
    public void execute(Command c) {
        Intent target = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com/search?q=" + c.getOriginalCommand().substring(c.getOriginalCommand().indexOf(":")+1)));
        Intent intent = Intent.createChooser(target, getString(R.string.chat_choose_activity));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sContext.startActivity(intent);
    }

    @Override
    protected void initializeSubCommands() {
        mCommandMap.get("http").setHelp(R.string.chat_help_urls, "#url#");   
    }
}
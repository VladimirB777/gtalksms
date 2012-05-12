package com.googlecode.gtalksms.cmd;

import com.googlecode.gtalksms.MainService;

public class ToastCmd extends CommandHandlerBase {

    public ToastCmd(MainService mainService) {
        super(mainService, CommandHandlerBase.TYPE_INTERNAL, new Cmd("toast"));
        // TODO if your command needs references, init them here
    }

    protected void execute(String cmd, String args) {        
        if (!args.equals("")) {
            MainService.displayToast(args, null, false);
        }
    }
    
    @Override
    protected void initializeSubCommands() {
    }
}

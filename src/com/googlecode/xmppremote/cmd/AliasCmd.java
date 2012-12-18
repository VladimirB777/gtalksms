package com.googlecode.xmppremote.cmd;

import java.util.ArrayList;

import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.R;
import com.googlecode.xmppremote.data.phone.Phone;
import com.googlecode.xmppremote.databases.AliasHelper;
import com.googlecode.xmppremote.xmpp.XmppMsg;

/**
 * XMPP Frontend command class for the alias feature This Class handles the
 * inserting/updating/deleting of aliases via XMPP Commands
 * 
 * @author 
 * 
 */
public class AliasCmd extends CommandHandlerBase {
    private AliasHelper aliasHelper;

    public AliasCmd(MainService mainService) {
        super(mainService, CommandHandlerBase.TYPE_CONTACTS, new Cmd("alias"));
        this.aliasHelper = AliasHelper.getAliasHelper(sContext);
    }

    protected void initializeSubCommands() {
        Cmd alias = mCommandMap.get("alias");
        alias.setHelp(R.string.chat_help_alias_general, "subCommand:argument");
        
        alias.AddSubCmd("add", R.string.chat_help_alias_add, "#aliasname#:#contact#");
        alias.AddSubCmd("show",R.string.chat_help_alias_show, "#aliasname#");
        alias.AddSubCmd("del",R.string.chat_help_alias_del, "#aliasname#");
    }
    
    @Override
    protected void execute(String cmd, String args) {
        if (args.equals("")) {
            sendHelp();
        } else {
            String[] subCommand = splitArgs(args);
            if (subCommand[0].equals("add")) {
                add(subCommand);
            } else if (subCommand[0].equals("del")) {
                del(subCommand);
            } else if (subCommand[0].equals("show")) {
                show(subCommand);
            } else {
                sendHelp();
            }
        }
    }
    
    private void add(String[] subCommand) {
        if (subCommand.length < 3) {
            send(R.string.chat_error_more_arguments, subCommand[0]);
        } else if (Phone.isCellPhoneNumber(subCommand[2])) {
                if (aliasHelper.addAliasByNumber(subCommand[1], subCommand[2])) {
                    send(R.string.chat_alias_add_by_number, subCommand[1], subCommand[2]);
                } else {
                    send(R.string.chat_alias_invalid_char);
                }
            } else {
                ArrayList<Phone> res = aliasHelper.addAliasByName(subCommand[1], subCommand[2]);
                if (res == null) {
                    send(R.string.chat_alias_invalid_char);
                } else if (res.size() != 1) {
                    send(R.string.chat_error_unkown_name);
                } else {
                    Phone p = res.get(0);
                    send(R.string.chat_alias_add_by_name, subCommand[1], p.getContactName(), p.getNumber());
                }
            }
    }
    
    private void del(String[] subCommand) {
        if (subCommand.length < 2) {
            send(R.string.chat_error_more_arguments, subCommand[0]);
        } else if (aliasHelper.deleteAlias(subCommand[1])) {
            send(R.string.chat_alias_del_suc, subCommand[1]);
        } else {
            send(R.string.chat_alias_del_fail, subCommand[1]);
        }
    }
    
    private void show(String[] subCommand) {
        if (subCommand.length < 2) {
            send(R.string.chat_error_more_arguments, subCommand[0]);
        } else if (subCommand[1].equals("all")) {
            String[][] aliases = aliasHelper.getAllAliases();
            if (aliases == null) {
                send(R.string.chat_alias_empty);
            } else {
                XmppMsg msg = new XmppMsg();
                for (int i = 0; i < aliases.length; i++) {
                    msg.appendBold("Alias: " + aliases[i][0] + " ");
                    if (aliases[i][2] == null) {
                        msg.appendLine(aliases[i][1]);
                    } else {
                        msg.append(aliases[i][1] + " - ");
                        msg.appendLine(aliases[i][2]);
                    }
                }
                send(msg);
            }
        } else {
            String[] res = aliasHelper.getAliasOrNull(subCommand[1]);
            if (res == null) {
                send(R.string.chat_alias_show_non_existent, subCommand[1]);
            } else if (res.length == 2) {
                send("\'" + res[0] + "\' -> " + res[1]);
            } else {
                send("\'" + res[0] + "\' - " + res[2] + " -> " + res[1]);
            }
        }
    }
}

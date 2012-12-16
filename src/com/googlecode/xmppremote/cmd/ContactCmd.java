package com.googlecode.xmppremote.cmd;

import java.util.ArrayList;

import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.R;
import com.googlecode.xmppremote.data.contacts.Contact;
import com.googlecode.xmppremote.data.contacts.ContactAddress;
import com.googlecode.xmppremote.data.contacts.ContactsManager;
import com.googlecode.xmppremote.data.phone.Phone;
import com.googlecode.xmppremote.xmpp.XmppMsg;

public class ContactCmd extends CommandHandlerBase {
    public ContactCmd(MainService mainService) {
        super(mainService, CommandHandlerBase.TYPE_CONTACTS, new Cmd("contact"));
    }
   
    @Override
    protected void execute(String cmd, String searchedText) {
    
        ArrayList<Contact> contacts = ContactsManager.getMatchingContacts(sContext, searchedText);

        if (contacts.size() > 0) {
            XmppMsg strContact = new XmppMsg();

            if (contacts.size() > 1) {
                strContact.appendLine(getString(R.string.chat_contact_found, contacts.size(), searchedText));
            }
            
            for (Contact contact : contacts) {
                strContact.appendBoldLine(contact.name);

                // strContact.append(Tools.LineSep + "Id : " + contact.id);
                // strContact.append(Tools.LineSep + "Raw Ids : " + TextUtils.join(" ",
                // contact.rawIds));

                ArrayList<Phone> mobilePhones = ContactsManager.getPhones(sContext, contact.ids);
                if (mobilePhones.size() > 0) {
                    strContact.appendItalicLine(getString(R.string.chat_phones));
                    for (Phone phone : mobilePhones) {
                        strContact.append(phone.getLabel() + " - " + phone.getCleanNumber());
                        // append an asterix to mark the default number
                        if (phone.isDefaultNumber()) {
                            strContact.appendBold(" *");
                        }
                        strContact.newLine();
                    }
                }

                ArrayList<ContactAddress> emails = ContactsManager.getEmailAddresses(sContext, contact.ids);
                if (emails.size() > 0) {
                    strContact.appendItalicLine(getString(R.string.chat_emails));
                    for (ContactAddress email : emails) {
                        strContact.appendLine((email.label != null ? email.label + " - " : "") + email.address);
                    }
                }

                ArrayList<ContactAddress> addresses = ContactsManager.getPostalAddresses(sContext, contact.ids);
                if (addresses.size() > 0) {
                    strContact.appendItalicLine(getString(R.string.chat_addresses));
                    for (ContactAddress address : addresses) {
                        strContact.appendLine((address.label != null ? address.label + " - " : "") + address.address);
                    }
                }
            }
            send(strContact);
        } else {
            send(R.string.chat_no_match_for, searchedText);
        }
    }

    @Override
    protected void initializeSubCommands() {
        mCommandMap.get("contact").setHelp(R.string.chat_help_contact, "#contact#");        
    }
}

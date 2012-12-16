package com.googlecode.xmppremote.xmpp;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;

import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.SettingsManager;
import com.googlecode.xmppremote.XmppManager;
import com.googlecode.xmppremote.tools.GoogleAnalyticsHelper;

public class XmppBuddies implements RosterListener {
    
    private static Context sContext;
    private static XMPPConnection sConnection;
    private static XmppBuddies sXmppBuddies;
    private static SettingsManager sSettings;
    private static Roster sRoster;
    
    private XmppBuddies(Context context) {
        sContext = context;
        sSettings = SettingsManager.getSettingsManager(context);

    }

    public void registerListener(XmppManager xmppMgr) {
        XmppConnectionChangeListener listener = new XmppConnectionChangeListener() {
            public void newConnection(XMPPConnection connection) {
                sConnection = connection;
                sRoster = connection.getRoster();
                checkNotificationAddressRoster();
            }
        };
        xmppMgr.registerConnectionChangeListener(listener);
    }
    
    public static XmppBuddies getInstance(Context ctx) {
        if (sXmppBuddies == null) {
            sXmppBuddies = new XmppBuddies(ctx);
        }
        return sXmppBuddies;
    }

//    public void addFriend(String userID) {
//        Roster roster = null;
//        String nickname = null;
//
//        nickname = StringUtils.parseBareAddress(userID);
//
//        roster = _connection.getRoster();
//        if (!roster.contains(userID)) {
//            try {
//                roster.createEntry(userID, nickname, null);
//            } catch (XMPPException e) {
//                System.err.println("Error in adding friend");
//            }
//        }
//
//        return;
//    }
    
    /**
     * retrieves the current xmpp rooster
     * and sends a broadcast ACTION_XMPP_PRESENCE_CHANGED
     * for every friend
     * does nothing if we are offline
     * 
     * @return
     */
    public ArrayList<XmppFriend> retrieveFriendList() {
        
        ArrayList<XmppFriend> friends = new ArrayList<XmppFriend>();

        if (sConnection != null && sConnection.isAuthenticated()) {
            try {
                String userID = null;
                String status = null;
                Roster roster = sConnection.getRoster();

                for (RosterEntry r : roster.getEntries()) {
                    userID = r.getUser();
                    status = retrieveStatusMessage(userID);
                    friends.add(new XmppFriend(userID, r.getName(), status, retrieveState(userID)));
                }

                sendFriendList(friends);
            } catch (Exception ex) {
                GoogleAnalyticsHelper.trackAndLogWarning("Failed to retrieve Xmpp Friend list", ex);
            }
        }
        
        return friends;
    }
    
    /**
     * sends an XMPP_PRESENCE_CHANGED intent for every known xmpp rooster item (friend)
     * with the actual status information
     * 
     * @param list
     */
    public void sendFriendList(ArrayList<XmppFriend> list) {
        
        for (XmppFriend xmppFriend : list) {
            Intent intent = new Intent(MainService.ACTION_XMPP_PRESENCE_CHANGED);
            intent.putExtra("userid", xmppFriend.mId);
            intent.putExtra("name", xmppFriend.mName == null ? xmppFriend.mId : xmppFriend.mName);
            intent.putExtra("status", xmppFriend.mStatus);
            intent.putExtra("state", xmppFriend.mState);
            sContext.sendBroadcast(intent);
        }
    }
    
    /**
     * returns the status message for a given bare or full JID
     * 
     * @param userID
     * @return
     */
    public String retrieveStatusMessage(String userID) {
        String userStatus = ""; // default return value

        try {
            userStatus = sConnection.getRoster().getPresence(userID).getStatus();
        } catch (NullPointerException e) {
            GoogleAnalyticsHelper.trackAndLogError("Invalid connection or user in retrieveStatus() - NPE");
            userStatus = "";
        }
        // Server may set their status to null; we want empty string
        if (userStatus == null) {
            userStatus = "";
        }

        return userStatus;
    }

    public int retrieveState(String userID) {
        int userState = XmppFriend.OFFLINE; // default return value
        Presence userFromServer = null;

        try {
            userFromServer = sConnection.getRoster().getPresence(userID);
            userState = retrieveState(userFromServer.getMode(), userFromServer.isAvailable());
        } catch (NullPointerException e) {
            GoogleAnalyticsHelper.trackAndLogError("retrieveState(): Invalid connection or user - NPE");
        }

        return userState;
    }
    
    /**
     * Maps the smack internal userMode enums into our int status mode flags
     * 
     * @param userMode
     * @param isOnline
     * @return
     */
    // TODO do we need the isOnline boolean?
    // Mode.available should be an equivalent
    public int retrieveState(Mode userMode, boolean isOnline) {
        int userState = XmppFriend.OFFLINE; // default return value
        
        if (userMode == Mode.dnd) {
            userState = XmppFriend.BUSY;
        } else if (userMode == Mode.away
                || userMode == Mode.xa) {
            userState = XmppFriend.AWAY;
        } else if (isOnline) {
            userState = XmppFriend.ONLINE;
        }

        return userState;
    }

    
    public void entriesAdded(Collection<String> addresses) {
    }

    
    public void entriesDeleted(Collection<String> addresses) {
    }

    public void entriesUpdated(Collection<String> addresses) {
    }

    // carefull, this method does also get called by the SmackListener Thread
        
    public void presenceChanged(Presence presence) {
        String bareUserId = StringUtils.parseBareAddress(presence.getFrom());
        
        Intent intent = new Intent(MainService.ACTION_XMPP_PRESENCE_CHANGED);
        intent.putExtra("userid", bareUserId);
        intent.putExtra("fullid", presence.getFrom());
        intent.putExtra("state", retrieveState(presence.getMode(), presence.isAvailable()));
        intent.putExtra("status", presence.getStatus());
        sContext.sendBroadcast(intent);
        
        // TODO Make this a general intent action.NOTIFICATION_ADDRESS_AVAILABLE
        // and handle it for example within XmppPresenceStatus
        // if the notification address is/has become available, update the resource status string
        if (bareUserId.equals(sSettings.notifiedAddress) && presence.isAvailable()) {
            intent = new Intent(MainService.ACTION_COMMAND);
            intent.setClass(sContext, MainService.class);
            intent.putExtra("cmd", "batt");
            intent.putExtra("args", "silent");
            MainService.sendToServiceHandler(intent);
        }       
    }
    
    /**
     * Checks if the notification address is available
     * return also true if no roster is loaded
     * @return
     */
    public boolean isNotificationAddressAvailable() {
        if (sRoster != null) {
            // getPresence retrieves eventually the status of the notified Address in an internal data structure cache
            // thus avoiding an extra data packet
            Presence presence = sRoster.getPresence(sSettings.notifiedAddress);
            return presence.isAvailable();
        }
        return true;
    }
    
    private void checkNotificationAddressRoster() {
        if (sRoster != null && sSettings.useDifferentAccount) {
            if (!sRoster.contains(sSettings.notifiedAddress)) {
                try {
                    // this sends a new subscription request to the other side
                    sRoster.createEntry(sSettings.notifiedAddress, sSettings.notifiedAddress, null);
                } catch (XMPPException e) { /* Ignore */  }
            } else {
                RosterEntry rosterEntry = sRoster.getEntry(sSettings.notifiedAddress);
                RosterPacket.ItemType type = rosterEntry.getType();
                switch (type) {
                case both:
                    break;
                case from:
                    requestSubscription(sSettings.notifiedAddress, sConnection);
                    break;
                case to:
                    grantSubscription(sSettings.notifiedAddress, sConnection);
                    break;
                case none:
                    grantSubscription(sSettings.notifiedAddress, sConnection);
                    requestSubscription(sSettings.notifiedAddress, sConnection);
                    break;
                default:
                    break;
                }
                
            }
        }
    }
    
    /**
     * grants the given JID the subscription (e.g. viewing your online state)
     * 
     * @param jid
     * @param connection
     */
    public static void grantSubscription(String jid, XMPPConnection connection) {
        Presence presence = new Presence(Presence.Type.subscribed);
        sendPresenceTo(jid, presence, connection);
    }
    
    /**
     * request the subscription from a given JID
     * 
     * @param jid
     * @param connection
     */
    public static void requestSubscription(String jid, XMPPConnection connection) {
        Presence presence = new Presence(Presence.Type.subscribe);
        sendPresenceTo(jid, presence, connection);
    }
    
    private static void sendPresenceTo(String to, Presence presence, XMPPConnection connection) {
        presence.setTo(to);
        connection.sendPacket(presence); 
    }
}

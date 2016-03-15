package com.app.xmpp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.os.RemoteException;

import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.DashBoardDriver;
import com.cabily.cabilydriver.Utils.SessionManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 */
public class ChatingService extends IntentService implements ChatManagerListener, ChatMessageListener {
    private static final String ACTION_FOO = "com.casperon.smackclient.action.FOO";
    private static AbstractXMPPConnection connection;
    private static boolean isConnected;
    private static SessionManager session;
    private static ChatHandler chatHandler;
    private static Chat chat;
    private  static ChatManager chatManager;
    static boolean isChatEnabled;
    private static Messenger chatMessenger;


    public static void startDriverAction(Context context) {
        Intent intent = new Intent(context, ChatingService.class);
        intent.setAction(ACTION_FOO);
        session = new SessionManager(context);
        context.startService(intent);
    }

    public ChatingService() {
        super("ChatingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                handleActionFoo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

    private void handleActionFoo() {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
        }
        configBuilder.setHost(ServiceConstant.XMPP_HOST_URL);//http://192.168.1.116/67.219.149.186
        configBuilder.setServiceName(ServiceConstant.XMPP_SERVICE_NAME);
        //sec_key
        connection = new XMPPTCPConnection(configBuilder.build());
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                isConnected = true;
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
            }

            @Override
            public void connectionClosed() {
                isConnected = false;
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                isConnected = false;
            }

            @Override
            public void reconnectionSuccessful() {
                isConnected = true;
            }

            @Override
            public void reconnectingIn(int seconds) {
            }

            @Override
            public void reconnectionFailed(Exception e) {
                isConnected = false;
            }
        });
        connection.setPacketReplyTimeout(30000);
        try {
            connection.connect();
        } catch (SmackException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        try {
            String userName = "";
            String password = "";
            if (session != null && session.getUserDetails() != null) {
                userName = session.getUserDetails().get(SessionManager.KEY_DRIVERID);
                password = session.getUserDetails().get(SessionManager.KEY_SEC_KEY);
            }
            if (userName.length() > 0 && password.length() > 0) {
                connection.login(userName, password);
                chatManager = ChatManager.getInstanceFor(connection);
                chatManager.addChatListener(this);
                System.out.println("-------------Xmpp response conectd---------------------");
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processMessage(Chat chat, final Message message) {
        try {
            if (chatHandler == null) {
                chatHandler = new ChatHandler(getApplicationContext(), this);
            }
            HashMap<String, String> online = session.getOnlineDetails();
            String  checkonline = online.get(SessionManager.KEY_ONLINE);
            if (checkonline.equalsIgnoreCase("1"))
            {
                chatHandler.onHandleChatMessage(message);
            }
        } catch (Exception e) {
        }
    }

    public void processMessage(Message message) {
        if (chatMessenger != null) {
            android.os.Message chatMessage = android.os.Message.obtain();
            chatMessage.obj = message.getBody();
            try {
                chatMessenger.send(chatMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        chat.addMessageListener(this);
    }

    public static Chat createChat(String chatID) {
        synchronized (ChatingService.class) {
            if (chatID != null && chatManager != null) {
                chat = chatManager.createChat(chatID);
            }
            return chat;
        }
    }

    public static void closeConnection(){
        //isConnected = false;
        //if(connection != null){
        //connection.disconnect();
       //}
       //connection = null;
       //chatManager = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void setChatMessenger(Messenger messenger) {
        chatMessenger = messenger;
    }

    public static void enableChat() {
        isChatEnabled = true;
    }

    public static void disableChat() {
        isChatEnabled = false;
    }
}

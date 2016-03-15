package com.app.xmpp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.AdsPage;
import com.cabily.cabilydriver.DriverAlertActivity;
import com.cabily.cabilydriver.NewTripAlert;
import com.cabily.cabilydriver.PushNotificationAlert;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import java.net.URLDecoder;

/**
 * Created by user88 on 11/4/2015.
 */
public class ChatHandler {

    private Context context;
    private IntentService service;
    public ChatHandler(Context context, IntentService service) {
        this.context = context;
        this.service = service;
    }

    public void onHandleChatMessage(Message message) {
        try {
            String data = URLDecoder.decode(message.getBody(), "UTF-8");
            System.out.println("-------------Xmpp response---------------------" + data);
            JSONObject messageObject = new JSONObject(data);
            String action = (String) messageObject.get(ServiceConstant.ACTION_LABEL);
            if (ServiceConstant.ACTION_TAG_RIDE_REQUEST.equalsIgnoreCase(action)) {
                rideRequest(data);
            } else if (ServiceConstant.ACTION_TAG_RIDE_CANCELLED.equalsIgnoreCase(action)) {
                rideCancelled(messageObject);
            }else if (ServiceConstant.ACTION_TAG_RECEIVE_CASH.equalsIgnoreCase(action)){
                receiveCash(messageObject);
            }else if(ServiceConstant.ACTION_TAG_PAYMENT_PAID.equalsIgnoreCase(action)){
                paymentPaid(messageObject);
            }else if(ServiceConstant.ACTION_TAG_NEW_TRIP.equalsIgnoreCase(action)){
                newTipAlert(messageObject);
            }else if(ServiceConstant.pushNotification_Ads.equalsIgnoreCase(action)){
                display_Ads(messageObject);
            }

        } catch (Exception e) {
        }
    }

    private void rideRequest(String  message) {
        Intent intent = new Intent(context, DriverAlertActivity.class);
        intent.putExtra(DriverAlertActivity.EXTRA,message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(intent);
    }

    private void rideCancelled(JSONObject messageObject) throws Exception {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.finish.BeginTrip");
        context.sendBroadcast(broadcastIntent);

        Intent intent = new Intent(context, PushNotificationAlert.class);
        intent.putExtra("Message", messageObject.getString("message"));
        intent.putExtra("Action", messageObject.getString("action"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(intent);
    }

    private void receiveCash(JSONObject messageObject) throws Exception {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.finish.OtpPage");
        context.sendBroadcast(broadcastIntent);
        System.out.println("rideId----------------xmpp" + messageObject.getString("key1"));

        Intent intent = new Intent(context, PushNotificationAlert.class);
        intent.putExtra("Message", messageObject.getString("message"));
        intent.putExtra("Action", messageObject.getString("action"));
        intent.putExtra("amount", messageObject.getString("key3"));
        intent.putExtra("RideId", messageObject.getString("key1"));
        intent.putExtra("Currencycode",messageObject.getString("key4"));

        System.out.println("currncyputex---------------" + messageObject.getString("key4"));
        System.out.println("amountputex---------------" + messageObject.getString("key3"));


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(intent);
    }


    private void paymentPaid(JSONObject messageObject) throws Exception {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.finish.OtpPage");
        context.sendBroadcast(broadcastIntent);

        Intent broadcastIntent_payment = new Intent();
        broadcastIntent_payment.setAction("com.finish.PaymentPage");
        context.sendBroadcast(broadcastIntent_payment);

        Intent broadcastIntent_paymenttrip = new Intent();
        broadcastIntent_paymenttrip.setAction("com.finish.tripsummerydetail");
        context.sendBroadcast(broadcastIntent_paymenttrip);


        Intent intent = new Intent(context, PushNotificationAlert.class);
        intent.putExtra("Message", messageObject.getString("message"));
        intent.putExtra("Action", messageObject.getString("action"));
        intent.putExtra("RideId", messageObject.getString("key1"));


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(intent);
    }


    private void newTipAlert(JSONObject messageObject) throws Exception {

        System.out.println("---------------inside new trip------------------");

        Intent intent = new Intent(context, NewTripAlert.class);
        intent.putExtra("Message", messageObject.getString("message"));
        intent.putExtra("Action", messageObject.getString("action"));
        intent.putExtra("Username", messageObject.getString("key1"));
        intent.putExtra("Mobilenumber", messageObject.getString("key3"));
        intent.putExtra("UserImage", messageObject.getString("key4"));
        intent.putExtra("UserRating", messageObject.getString("key5"));
        intent.putExtra("RideId", messageObject.getString("key6"));
        intent.putExtra("UserPickuplocation", messageObject.getString("key7"));
        intent.putExtra("UserPickupTime", messageObject.getString("key10"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(intent);

    }


    private void display_Ads(JSONObject messageObject) throws Exception {
        Intent i1 = new Intent(context, AdsPage.class);
        i1.putExtra("AdsTitle", messageObject.getString(ServiceConstant.Ads_title));
        i1.putExtra("AdsMessage", messageObject.getString(ServiceConstant.Ads_Message));
        if (messageObject.has(ServiceConstant.Ads_image)) {
            i1.putExtra("AdsBanner", messageObject.getString(ServiceConstant.Ads_image));
        }
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }


}


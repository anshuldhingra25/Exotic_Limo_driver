package com.app.cardscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import io.card.payment.CardIOActivity;

/**
 */
public class CardScanner {

    public static final int MY_SCAN_REQUEST_CODE = 12;
    Activity context;
    public CardScanner(Activity context){
        this.context = context;
    }

    public void startScanActivityResult() {
        Intent scanIntent = new Intent(context, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        context.startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
    }

}

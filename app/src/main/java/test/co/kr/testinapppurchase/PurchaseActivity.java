package test.co.kr.testinapppurchase;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public abstract class PurchaseActivity extends AppCompatActivity {
    private static String TYPE_PURCHASE = "inapp";
    private static String TYPE_SUBSCRIBE = "subs";
    private static int REQUEST_CODE = 1001;

    @NonNull
    private String developerPayload = "";

    private IInAppBillingService billingService;
    private Intent billingIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND")
            .setPackage("com.android.vending");

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            billingService = IInAppBillingService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            billingService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(billingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingService != null) unbindService(serviceConnection);
    }

    protected void purchaseItem(String itemId) {
        purchaseItem(itemId, UUID.randomUUID().toString());
    }

    protected void purchaseItem(String itemId, @NonNull String payload) {
        if (billingService == null) return;
        Bundle bundle = null;
        try {
            bundle = billingService.getBuyIntent(3, getPackageName(), itemId, TYPE_PURCHASE, developerPayload = payload);
        } catch (RemoteException e) {
            onPurchaseException(e);
        }

        if (bundle == null) return;
        PendingIntent pendingIntent = bundle.getParcelable("BUY_INTENT");

        if (pendingIntent == null) return;
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            onPurchaseException(e);
        }
    }

    protected void subscribeItem(String itemId) {
        subscribeItem(itemId, UUID.randomUUID().toString());
    }

    protected void subscribeItem(String itemId, @NonNull String payload) {
        if (billingService == null) return;
        Bundle bundle = null;
        try {
            bundle = billingService.getBuyIntent(3, getPackageName(), itemId, TYPE_SUBSCRIBE, payload);
        } catch (RemoteException e) {
            onPurchaseException(e);
        }

        if (bundle == null) return;
        PendingIntent pendingIntent = bundle.getParcelable("BUY_INTENT");

        if (pendingIntent == null) return;
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            onPurchaseException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE) return;
        int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
        String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
        String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
        if (resultCode == RESULT_OK) try {
            JSONObject jo = new JSONObject(purchaseData);
            String sku = jo.getString("productId");
            String token = jo.getString("purchaseToken");
            onPurchaseSuccess(sku, token, purchaseData);
        } catch (JSONException e) {
            onPurchaseException(e);
        }
    }


    protected void consume(String token) {
        new Consume().execute(token);
    }

    protected String[] getItemTokens() {
        ArrayList<String> data = getPurchaseItems();
        if (data != null) {
            int size = data.size();
            String[] result = new String[size];
            for (int i = 0; i < size; i++)
                try {
                    result[i] = new JSONObject(data.get(i)).getString("purchaseToken");
                } catch (JSONException e) {
                    onPurchaseException(e);
                }
            return result;
        }
        return null;
    }

    protected ArrayList<String> getPurchaseItems() {
        if (billingService != null) try {
            Bundle bundle = billingService.getPurchases(3, getPackageName(), TYPE_PURCHASE, null);
            if (bundle != null)
                return bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
        } catch (RemoteException e) {
            onPurchaseException(e);
        }
        return null;
    }

    protected ArrayList<String> getPurchasableItems() {
        if (billingService != null) try {
            ArrayList<String> skuList = new ArrayList<>();
            skuList.add("premiumUp");
            Bundle query = new Bundle();
            query.putStringArrayList("ITEM_ID_LIST", skuList);
            Bundle bundle = billingService.getSkuDetails(3, getPackageName(), TYPE_SUBSCRIBE, query);
            return bundle.getStringArrayList("ITEM_ID_LIST");
        } catch (RemoteException e) {
            onPurchaseException(e);
        }
        return null;
    }

    protected abstract String licenceKey();

    abstract void onPurchaseSuccess(String itemId, String token, String dataSignature);

    abstract void onConsumeSuccess(Integer response);

    protected void onPurchaseException(Exception e) {
        // optional override
    }

    private class Consume extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... strings) {
            int result = -1;
            try {
                result = billingService.consumePurchase(3, getPackageName(), strings[0]);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer response) {
            onConsumeSuccess(response);
        }
    }
}

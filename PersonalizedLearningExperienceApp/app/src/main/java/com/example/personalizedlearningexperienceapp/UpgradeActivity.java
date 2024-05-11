package com.example.personalizedlearningexperienceapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONException;
import org.json.JSONObject;

public class UpgradeActivity extends AppCompatActivity {
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private PaymentsClient paymentsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        paymentsClient = Wallet.getPaymentsClient(this,
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build());

        Button btnStarter = findViewById(R.id.btnStarter);
        Button btnIntermediate = findViewById(R.id.btnIntermediate);
        Button btnAdvanced = findViewById(R.id.btnAdvanced);
        Button backButton = findViewById(R.id.backButton);

        btnStarter.setOnClickListener(view -> requestPayment(500));  // $5.00 for Starter
        btnIntermediate.setOnClickListener(view -> requestPayment(1500));  // $15.00 for Intermediate
        btnAdvanced.setOnClickListener(view -> requestPayment(2500));  // $25.00 for Advanced

        checkGooglePaySupport();

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(UpgradeActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkGooglePaySupport() {
        Log.d("Google Pay Setup", "Checking Google Pay support...");
        String isReadyToPayJson = "{\"allowedPaymentMethods\":[{\"type\":\"CARD\",\"parameters\":{\"allowedAuthMethods\":[\"PAN_ONLY\",\"CRYPTOGRAM_3DS\"],\"allowedCardNetworks\":[\"AMEX\",\"DISCOVER\",\"MASTERCARD\",\"VISA\"]}}]}";
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson);
        paymentsClient.isReadyToPay(request).addOnCompleteListener(task -> {
            try {
                boolean result = task.getResult(ApiException.class);
                Log.d("Google Pay Setup", "Google Pay availability check completed. Result: " + result);
                if (result) {
                    Log.i("Google Pay Setup", "Google Pay is available.");
                } else {
                    Log.i("Google Pay Setup", "Google Pay is not available.");
                }
            } catch (ApiException exception) {
                Log.e("Google Pay Setup", "isReadyToPay API exception", exception);
            }
        }).addOnFailureListener(e -> Log.e("Google Pay Setup", "isReadyToPay failed", e));
    }

    private void requestPayment(int priceCents) {
        PaymentDataRequest request = createPaymentDataRequest(priceCents);
        AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request),
                this,
                LOAD_PAYMENT_DATA_REQUEST_CODE
        );
    }

    private PaymentDataRequest createPaymentDataRequest(int priceCents) {
        @SuppressLint("DefaultLocale") String price = String.format("%.2f", priceCents / 100.0);
        String paymentDataRequestJson = String.format(
                "{\"apiVersion\": 2, \"apiVersionMinor\": 0, " +
                        "\"allowedPaymentMethods\": [{\"type\": \"CARD\", " +
                        "\"parameters\": {\"allowedAuthMethods\": [\"PAN_ONLY\", \"CRYPTOGRAM_3DS\"], " +
                        "\"allowedCardNetworks\": [\"AMEX\", \"DISCOVER\", \"JCB\", \"MASTERCARD\", \"VISA\"]}, " +
                        "\"tokenizationSpecification\": {\"type\": \"PAYMENT_GATEWAY\", " +
                        "\"parameters\": {\"gateway\": \"paypal\", \"paypalId\": \"exampleMerchantId\"}}}], " +
                        "\"transactionInfo\": {\"totalPrice\": \"%s\", \"totalPriceStatus\": \"FINAL\", \"currencyCode\": \"USD\"}, " +
                        "\"merchantInfo\": {\"merchantName\": \"Personalize Learning App\"}}", price);
        return PaymentDataRequest.fromJson(paymentDataRequestJson);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            Log.d("Google Pay", "Received onActivityResult for Google Pay. requestCode: " + requestCode + ", resultCode: " + resultCode);
            handlePaymentData(resultCode, data);
        }
    }

    private void handlePaymentData(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            PaymentData paymentData = PaymentData.getFromIntent(data);
            if (paymentData != null) {
                try {
                    JSONObject paymentMethodData = new JSONObject(paymentData.toJson()).getJSONObject("paymentMethodData");
                    String token = paymentMethodData.getJSONObject("tokenizationData").getString("token");
                    Log.d("Google Pay", "Payment token: " + token);
                } catch (JSONException e) {
                    Log.e("Google Pay", "PaymentData JSON parsing error", e);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d("Google Pay", "User cancelled the payment.");
        } else if (resultCode == AutoResolveHelper.RESULT_ERROR) {
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            if (status != null) {
                Log.e("Google Pay", "Error with Google Pay API call. Status code: " + status.getStatusCode() + ", Message: " + status.getStatusMessage());
            }
        }
    }
}

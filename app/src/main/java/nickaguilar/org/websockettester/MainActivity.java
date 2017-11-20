package nickaguilar.org.websockettester;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {
    private TextView txtOutput, txtCostBasis, txtCurrentOwned, txtProfit, txtAmountOwned;
    private OkHttpClient client;

    private final class EchoWebSocketListener extends WebSocketListener {

        private static final int NORMAL_CLOSURE_STATUS = 1000;
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("{\"type\": \"subscribe\",\"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-USD\"] }]}");
            //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            String price = "", time = "";
            try {
                JSONObject json = new JSONObject(text);
                price = json.getString("price");
                time = json.getString("time");

            }catch (JSONException e){
                e.printStackTrace();
            }
            Log.d("Status", price + " " + time);
            output(price);


        }
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtOutput = (TextView) findViewById(R.id.price);
            txtOutput.setText("Price: ");
        txtCostBasis = (TextView)findViewById(R.id.txtCostBasis);
        txtAmountOwned = (TextView)findViewById(R.id.txtAmountOwned);
        txtCurrentOwned = (TextView)findViewById(R.id.txtValue);
        txtProfit = (TextView)findViewById(R.id.txtProfit);

        client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://ws-feed.gdax.com").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);

    }

    String lastPrice = "";

    final double currentOwned = 0.1450855;
    final double costBasis = 1000;
    double currentValue = 0;
    double profit = 0;
    double dPrice = 0;

    DecimalFormat number = new DecimalFormat("###,###.00");
    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!txt.equals(lastPrice)) {

                    if(!txt.equals("")) {
                        dPrice = Double.parseDouble(txt);
                        txtOutput.setText("Price: $" + number.format(dPrice));

                        if(!lastPrice.equals("")) {

                            if (dPrice > Double.parseDouble(lastPrice)) {
                                txtOutput.setTextColor(Color.GREEN);
                            } else if (dPrice < Double.parseDouble(lastPrice)) {
                                txtOutput.setTextColor(Color.RED);
                            }
                        }

                        //calculateStatistics(txt);
                        currentValue = currentOwned * dPrice;
                        profit = currentValue - costBasis;

                        txtCostBasis.setText("Cost Basis: " + number.format(costBasis));
                        txtAmountOwned.setText("BTC Owned: " + currentOwned);
                        txtCurrentOwned.setText("Current Value: $" + number.format(currentValue));
                        txtProfit.setText("Profit: $" + number.format(profit));

                        currentValue = 0;
                        profit = 0;
                    }
                    Log.d("Message", txtOutput.getText().toString());
                }
                else{//if the newly updated price is the same as the previous, don't do anything
                    Log.d("Message", "Same as before");
                }

                lastPrice = txt;
            }
        });
    }

    public void btnAdd_onClick(View oView){
        Intent oIntent = new Intent("org.nickaguilar.websockettester.addNewActivity");
    }

}
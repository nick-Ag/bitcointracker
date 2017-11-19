package nickaguilar.org.websockettester;
import android.content.Intent;
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



public class MainActivity extends AppCompatActivity {
    private Button start;
    private TextView txtOutput, txtCostBasis, txtCurrentOwned, txtProfit;
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
        start = (Button) findViewById(R.id.start);
        txtOutput = (TextView) findViewById(R.id.price);
            txtOutput.setText("Price: ");
        txtCostBasis = (TextView)findViewById(R.id.txtCostBasis);
        txtCurrentOwned = (TextView)findViewById(R.id.txtValue);
        txtProfit = (TextView)findViewById(R.id.txtProfit);

        client = new OkHttpClient();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
    }
    private void start() {
        Request request = new Request.Builder().url("wss://ws-feed.gdax.com").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);
        //client.dispatcher().executorService().shutdown();
    }
    String lastPrice = "";

    final double currentOwned = 0.1450855;
    final double costBasis = 1000;
    double currentValue = 0;
    double profit = 0;
    double dPrice = 0;

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!txt.equals(lastPrice)) {
                    txtOutput.setText("Price: $" + txt);
                    if(!txt.equals("")) {
                        dPrice = Double.parseDouble(txt);


                        //calculateStatistics(txt);
                        currentValue = currentOwned * dPrice;
                        profit = currentValue - costBasis;

                        txtCostBasis.setText("Cost Basis: " + String.valueOf(costBasis));
                        txtCurrentOwned.setText("Current Value: " + String.valueOf(currentValue));
                        txtProfit.setText("Profit: " + String.valueOf(profit));

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
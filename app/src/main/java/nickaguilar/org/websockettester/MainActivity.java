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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {
    //create new global view objects to use in the app
    private TextView txtPrice, txtCostBasis, txtValue, txtProfit, txtAmountOwned, txtPercentProfit;
    private OkHttpClient client; //creates a OkHttpClient object to use in various places

    //This class handles communications with the websocket
    private final class EchoWebSocketListener extends WebSocketListener {

        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        //onOpen method is responsible for sending a message once the connection is established
        public void onOpen(WebSocket webSocket, Response response) {
            //sends the gdax websocket the subscription message
            webSocket.send("{\"type\": \"subscribe\",\"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-USD\"] }]}");
        }

        @Override
        //handles what to do when a message is recieved from the websocket
        public void onMessage(WebSocket webSocket, String text) {
                String price = "", time = ""; //two strings initialized to hold key values from the JSON recieved from the websocket

                try {
                    JSONObject json = new JSONObject(text); //use a JSONObject to parse thru the JSON and grab a couple values
                    price = json.getString("price"); //get the price value and store it in a string
                    time = json.getString("time"); //get the time value

                }catch (JSONException e){
                    e.printStackTrace();
                }
                Log.d("Status", price + " " + time); //log the 2 key values recieved from the websocket to the log

                //the first message recieved from the websocket generally has a null value for price
                //this makes sure that a null value isn't passed on, which would eventually cause a null string exception when things get calculated
                if(!price.equals("")) { //only passes on the recieved price if it is not null
                    output(price); //passes on the price to the main thread for processing
                }
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

        //initializes each view
        txtPrice = (TextView) findViewById(R.id.price);
        txtCostBasis = (TextView)findViewById(R.id.txtCostBasis);
        txtAmountOwned = (TextView)findViewById(R.id.txtAmountOwned);
        txtValue = (TextView)findViewById(R.id.txtValue);
        txtProfit = (TextView)findViewById(R.id.txtProfit);
        txtPercentProfit = (TextView)findViewById(R.id.txtPercentProfit);

        //Initialize each textView with loading text
        txtPrice.setText("Price: Loading...");
        txtCostBasis.setText("Cost Basis: Loading...");
        txtAmountOwned.setText("BTC Owned: Loading...");
        txtValue.setText("Current Total Value: Loading...");
        txtProfit.setText("Current Profit: Loading...");
        txtPercentProfit.setText("% Profit: Loading...");

        //initializes the client, and connects to the websocket
        client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://ws-feed.gdax.com").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);

    }

    String lastPrice = ""; //holds the last price of BTC to compare to the new price
    final double currentOwned = 0.1450855; //hardcoded values for amount owned and cost basis, will change
    final double costBasis = 1000;          //to be user defined
    //initializes value, profits, and price to 0
    double currentValue = 0;
    double profit = 0;
    double dPrice = 0;
    double percentProfit = 0;

    //Decimal format object initialized to eventually format money values to look nice
    DecimalFormat number = new DecimalFormat("###,###.00");

    private void output(final String txt) { //this method handles all processing when a new price is received
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!txt.equals(lastPrice)) { //if the price is the same as the last price processed, do nothing and save some processing power

                    dPrice = Double.parseDouble(txt); //hold the incoming price as a double
                    txtPrice.setText("Price: $" + number.format(dPrice)); //set the text to the new price

                    if(!lastPrice.equals("")) { //if the last price is null, dont do anything

                        if (dPrice > Double.parseDouble(lastPrice)) { //if the price just went up, change the color to green
                            txtPrice.setTextColor(Color.GREEN);
                        } else if (dPrice < Double.parseDouble(lastPrice)) { //else change it to red
                            txtPrice.setTextColor(Color.RED);
                        }
                    }

                    //calculate the profits and value and output that all
                    currentValue = currentOwned * dPrice; //current total value of investment is the current price * amount owned
                    profit = currentValue - costBasis; //current profit is current value - initial costs
                    percentProfit = 100 * ( (currentValue - costBasis) / costBasis );

                    //set all the views to reflect the new calculated values
                    txtCostBasis.setText("Cost Basis:\t\t $" + number.format(costBasis));
                    txtAmountOwned.setText("BTC Owned:\t\t " + currentOwned);
                    txtValue.setText("Current Value:\t\t $" + number.format(currentValue));
                    txtProfit.setText("Profit:\t\t $" + number.format(profit));
                    txtPercentProfit.setText("% Profit:\t\t " + number.format(percentProfit) + "%");

                    //idk if these are needed
                    currentValue = 0;
                    profit = 0;

                    //log the price
                    Log.d("Message", txtPrice.getText().toString());
                }
                else{//if the newly updated price is the same as the previous, don't do anything
                    Log.d("Message", "Same as before");
                }

                //set the last price variable to whatever the price is for the next loop thru this method
                lastPrice = txt;
            }
        });
    }

    public void btnAdd_onClick(View oView){
        Intent oIntent = new Intent("org.nickaguilar.websockettester.addNewActivity");
        startActivity(oIntent);
    }

}
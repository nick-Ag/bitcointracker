package nickaguilar.org.websockettester;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
    public String currentCurrencyViewed = "bitcoincash";

    private TextView txtPrice, txtCostBasis, txtValue, txtProfit, txtAmountOwned, txtPercentProfit;
    private OkHttpClient client; //creates a OkHttpClient object to use in various places
    DBManager db;

    double currentOwned = 0;
    double costBasis = 0;

    //Decimal format object initialized to eventually format money values to look nice
    DecimalFormat number = new DecimalFormat("###,###.00");
    DecimalFormat bitcoinFormat = new DecimalFormat("###,###.000000");
    //This class handles communications with the websocket
    private final class EchoWebSocketListener extends WebSocketListener {

        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        //onOpen method is responsible for sending a message once the connection is established
        public void onOpen(WebSocket webSocket, Response response) {
            //sends the gdax websocket the subscription message
            String currencyCodeForMessage = "BTC";

            switch (currentCurrencyViewed){
                case "bitcoin":
                    currencyCodeForMessage = "BTC";
                    break;
                case "ethereum":
                    currencyCodeForMessage = "ETH";
                    break;
                case "bitcoincash":
                    currencyCodeForMessage = "BCH";
                    break;
            }
            webSocket.send("{\"type\": \"subscribe\",\"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"" + currencyCodeForMessage + "-USD\"] }]}");
        }

        @Override
        //handles what to do when a message is recieved from the websocket
        public void onMessage(WebSocket webSocket, String text) {
            String price = "", time = "", product_id = ""; //three strings initialized to hold key values from the JSON recieved from the websocket

            try {
                JSONObject json = new JSONObject(text); //use a JSONObject to parse thru the JSON and grab a couple values
                price = json.getString("price"); //get the price value and store it in a string
                time = json.getString("time"); //get the time value
                product_id = json.getString("product_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("Status", product_id + "" + price + " " + time); //log the 2 key values recieved from the websocket to the log

            //the first message recieved from the websocket generally has a null value for price
            //this makes sure that a null value isn't passed on, which would eventually cause a null string exception when things get calculated
            if (!price.equals("")) { //only passes on the recieved price if it is not null
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

    //simple method to set the text of each textView to "loading..." when the app is waiting for a new price
    public void setTextToLoading(){
        //initializes each view
        txtPrice = (TextView) findViewById(R.id.price);
        txtCostBasis = (TextView) findViewById(R.id.txtCostBasis);
        txtAmountOwned = (TextView) findViewById(R.id.txtAmountOwned);
        txtValue = (TextView) findViewById(R.id.txtValue);
        txtProfit = (TextView) findViewById(R.id.txtProfit);
        txtPercentProfit = (TextView) findViewById(R.id.txtPercentProfit);

        //Initialize each textView with loading text
        txtPrice.setText("Price: Loading...");
        txtCostBasis.setText("Cost Basis: Loading...");
        txtAmountOwned.setText("BTC Owned: Loading...");
        txtValue.setText("Current Total Value: Loading...");
        txtProfit.setText("Current Profit: Loading...");
        txtPercentProfit.setText("% Profit: Loading...");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set all the text views to "loading..." while the price loads
        setTextToLoading();

        //initializes the client, and connects to the websocket
        client = new OkHttpClient();
        final Request request = new Request.Builder().url("wss://ws-feed.gdax.com").build();
        final EchoWebSocketListener listener = new EchoWebSocketListener();
        final WebSocket ws = client.newWebSocket(request, listener);

        //instantiates db
        db = new DBManager(this);

        //Gets the current stored purchase details
        processDataBaseItems();

        //set up the bottom navigation bars on click listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener(){
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item){

                        String sTitle = item.getTitle().toString().toLowerCase().replaceAll("\\s+","");
                        Log.d("Var", sTitle);

                        //If the title from the pressed item doesnt equal the current title, do the stuff
                        if(!sTitle.equals(currentCurrencyViewed)) {
                            setTextToLoading();
                            switch (item.getItemId()) {

                                case R.id.menu_btc:
                                    currentCurrencyViewed = "bitcoin"; //set the current viewed, when the websocket reconnects it will fetch this currencies price
                                    client.dispatcher().cancelAll(); //cancel the current connection with the websocket
                                    client.newWebSocket(request, listener); //reconnect the websocket
                                    break;
                                case R.id.menu_bcash:
                                    currentCurrencyViewed = "bitcoincash";
                                    client.dispatcher().cancelAll();
                                    client.newWebSocket(request, listener);
                                    break;
                                case R.id.menu_eth:
                                    currentCurrencyViewed = "ethereum";
                                    client.dispatcher().cancelAll();
                                    client.newWebSocket(request, listener);
                                    break;
                            }
                            nukeTableLayout();
                            processDataBaseItems();
                        }
                            Log.d("Currency", currentCurrencyViewed);
                            return true;
                    }
                });
    }

    //Function will remove every listing from the tablelayout so that
    //it can be reloaded. Doesn't remove anything from the DB
    public void nukeTableLayout(){

        TableLayout table = (TableLayout)findViewById(R.id.tableMain);
        TableRow titleBar = (TableRow)table.getChildAt(0); //saves the title bar so it can be re-added after the nuke
        table.removeAllViews(); //nuke table
        table.addView(titleBar); //readd the titlebar
    }

    //method will open the database, then retrieve all records. It will sum the amount of bitcoin bought, and the amount the user has spent on that bitcoin
    public void processDataBaseItems() {
        TableLayout table = (TableLayout) findViewById(R.id.tableMain);

        db.open();

        currentOwned = 0;
        costBasis = 0;

        Cursor c = db.getAllRecords();
        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                if ( c.getString(c.getColumnIndex("currency")).equals(currentCurrencyViewed)
                        && !c.getString(c.getColumnIndex("amountBought")).equals("")) { //if the record is the same as currentViewedCurrency and is not null

                    Log.d("Database", c.getColumnIndex("_id") + "\t\t" + c.getColumnIndex("currency") + "\t" + c.getColumnIndex("amountBought"));

                    currentOwned += Double.parseDouble(c.getString(c.getColumnIndex("amountBought"))); // sum each purchase
                    costBasis += Double.parseDouble(c.getString(c.getColumnIndex("costBasis")));

                    Double amountBought = Double.parseDouble(c.getString(c.getColumnIndex("amountBought")));
                    Double purchaseCost = Double.parseDouble(c.getString(c.getColumnIndex("costBasis")));

                    TableRow row = new TableRow(this);
                    TableRow.LayoutParams layout = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT);
                    layout.gravity = Gravity.CENTER;
                    row.setLayoutParams(layout);

                    TextView viewAmountBought = new TextView(this);
                    viewAmountBought.setText(bitcoinFormat.format(amountBought));
                    TextView viewCostBasis = new TextView(this);
                    viewCostBasis.setText(number.format(purchaseCost));
                    TextView viewDate = new TextView(this);
                    viewDate.setText("12/07/2017");
                    Button deleteButton = new Button(this);
                    deleteButton.setText("Delete");

                    deleteButton.setTag(c.getString(c.getColumnIndex("_id")));
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View oView) {
                            //will delete the corresponding purchase record listing

                            //first pop up a "are you sure dialog"

                            //if they are sure
                            //delete the row in the table
                            View thisRow = (View)oView.getParent();
                            ((ViewGroup)thisRow).removeAllViews();
                            //delete the listing from the database

                            db.open();
                            db.deletePurchase((String)oView.getTag());
                            db.close();

                            //redo the processDatabase items, so that the costBasis, and amountBought global variables dont include the purchase the user just deleted
                            processDataBaseItems();
                            //else
                            //close the dialog and do nothing
                        }
                    });



                    row.addView(viewAmountBought);
                    row.addView(viewCostBasis);
                    row.addView(viewDate);
                    row.addView(deleteButton);

                    Log.d("DB", c.getString(c.getColumnIndex("_id")));

                    //add a row for the purchase to the table layout
                    table.addView(row);
                }
            }
        }
        Log.d("Debug", String.valueOf(currentOwned));
        Log.d("Debug", String.valueOf(costBasis));
        db.close();
    }

    String lastPrice = ""; //holds the last price of BTC to compare to the new price
    //    final double currentOwned = 0.1450855; //hardcoded values for amount owned and cost basis, will change
//    final double costBasis = 1000;          //to be user defined
    //initializes value, profits, and price to 0
    double currentValue = 0;
    double profit = 0;
    double dPrice = 0;
    double percentProfit = 0;

    private void output(final String txt) { //this method handles all processing when a new price is received
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!txt.equals(lastPrice)) { //if the price is the same as the last price processed, do nothing and save some processing power

                    try {
                        dPrice = Double.parseDouble(txt); //hold the incoming price as a double
                    }
                    catch (java.lang.NumberFormatException e){
                        return;
                    }

                    txtPrice.setText("Price: $" + number.format(dPrice)); //set the text to the new price

                    if (!lastPrice.equals("")) { //if the last price is null, dont do anything

                        if (dPrice > Double.parseDouble(lastPrice)) { //if the price just went up, change the color to green
                            txtPrice.setTextColor(Color.GREEN);
                        } else if (dPrice < Double.parseDouble(lastPrice)) { //else change it to red
                            txtPrice.setTextColor(Color.RED);
                        }
                    }

                    //calculate the profits and value and output that all
                    currentValue = currentOwned * dPrice; //current total value of investment is the current price * amount owned
                    profit = currentValue - costBasis; //current profit is current value - initial costs
                    percentProfit = 100 * ((currentValue - costBasis) / costBasis);

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
                } else {//if the newly updated price is the same as the previous, don't do anything
                    Log.d("Message", "Same as before");
                }

                //set the last price variable to whatever the price is for the next loop thru this method
                lastPrice = txt;
            }
        });
    }

    //When the add new purchase button is clicked, this method starts the addNewActivity activty
    public void btnAdd_onClick(View oView) {
        Intent oIntent = new Intent("org.nickaguilar.websockettester.addNewActivity");
        //passes the current currency to the activity so it knows what currency type it is adding
        oIntent.putExtra("currency", currentCurrencyViewed);
        startActivityForResult(oIntent, 1);
    }


    //When the addNewActivity finishes, this method fires
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == 0) { //if the user entered values for cost and amount bought...
            Log.d("Debug", "inside onActivityResult");
            processDataBaseItems();
        }
    }
}
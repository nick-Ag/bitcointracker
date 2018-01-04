package nickaguilar.org.websockettester;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class addNewActivity extends Activity {

    EditText etxtBasis, etxtAmountBought, etxtPurchasePrice;
    TextView txtTitle;
    DBManager db;
    String currency = "bitcoin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);

        etxtPurchasePrice = (EditText)findViewById(R.id.etxtPurchasePrice);
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        //temp set purchase price field to invisible
        etxtPurchasePrice.setVisibility(View.INVISIBLE);

        Intent oIntent = getIntent();
        Bundle b = oIntent.getExtras();

        if(b != null){
            currency = (String)b.get("currency");
            txtTitle.setText("Enter new " + currency + " purchase details.");
        }

        db = new DBManager(this);
    }

    public void btnAddNew_onClick(View oView){
        etxtBasis = (EditText)findViewById(R.id.etxtBasis);
        etxtAmountBought = (EditText)findViewById(R.id.etxtAmountBought);


        String sBasis = etxtBasis.getText().toString();
        String sAmountBought = etxtAmountBought.getText().toString();
        //String sPurchasePrice = etxtPurchasePrice.getText().toString();

        if(sBasis.isEmpty() || sAmountBought.isEmpty()){
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            Log.d("Status", "Fill all fields");
        }
        else{
            Log.d("Status", "Else");

            db.open();
            db.newPurchase(currency, sAmountBought, sBasis, "0", /*purchase price*/String.valueOf(Double.parseDouble(sBasis) / Double.parseDouble(sAmountBought)),
                    "11:05PM", "12/05/2017");
            db.close();

            Intent oIntent = new Intent();
            setResult(0, oIntent);

            finish();
        }


    }
}

package nickaguilar.org.websockettester;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class addNewActivity extends Activity {

    EditText etxtBasis, etxtAmountBought, etxtPurchasePrice;
    DBManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);

        etxtPurchasePrice = (EditText)findViewById(R.id.etxtPurchasePrice);
        //temp set purchase price field to invisible
        etxtPurchasePrice.setVisibility(View.INVISIBLE);

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
            db.newPurchase(sAmountBought, sBasis, "0", String.valueOf(Double.parseDouble(sBasis) / Double.parseDouble(sAmountBought)), "11:05PM", "12/05/2017");
            db.close();

//            MainActivity main = new MainActivity();
//            main.db = new DBManager(this);
//            main.processDataBaseItems();
            Intent oIntent = new Intent();
            setResult(0, oIntent);

            finish();
        }


    }
}

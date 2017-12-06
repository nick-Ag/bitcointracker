package nickaguilar.org.websockettester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class addNewActivity extends Activity {

    EditText etxtBasis, etxtAmountBought, etxtPurchasePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);
    }

    public void btnAddNew_onClick(View oView){
        etxtBasis = (EditText)findViewById(R.id.etxtBasis);
        etxtAmountBought = (EditText)findViewById(R.id.etxtAmountBought);
        etxtPurchasePrice = (EditText)findViewById(R.id.etxtPurchasePrice);

        String sBasis = etxtBasis.getText().toString();
        String sAmountBought = etxtAmountBought.getText().toString();
        String sPurchasePrice = etxtPurchasePrice.getText().toString();

        if(sBasis.isEmpty() || sAmountBought.isEmpty() || sPurchasePrice.isEmpty()){
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            Log.d("Status", "Fill all fields");
        }
        else{
            Log.d("Status", "Else");
            Intent oIntent = new Intent("org.nickaguilar.websockettester.MainActivity");
            startActivity(oIntent);
        }


    }
}

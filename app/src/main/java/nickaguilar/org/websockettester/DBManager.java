package nickaguilar.org.websockettester;

/**
 * Created by Nick on 10/10/2017.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager {

    static final String KEY_ROWID = "_id";
    static final String KEY_CURRENCY = "currency"; //which currency the record holds
    static final String KEY_AMOUNTBOUGHT = "amountBought";
    static final String KEY_COSTBASIS = "costBasis";
    static final String KEY_FEE = "fee";
    static final String KEY_PURCHASEPRICE = "purchasePrice";
    static final String KEY_TIME = "time";
    static final String KEY_DATE = "date";

    static final String TAG = "DBAdapter";
    static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_TABLE = "purchaseHistory";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE =
            "create table "+ DATABASE_TABLE +" (_id integer primary key autoincrement, currency text not null, "
                    + "amountBought text not null, costBasis text not null, fee text not null, purchasePrice text not null," +
                    "time text, date text);"; // this string is SQL code
            //the contacts line^ is the name of the table| name is the name of a field| autoincrement function will automatically assign a number to id
    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBManager(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    public DBManager open() throws SQLException {

        db = DBHelper.getWritableDatabase();
        return this; //'this' refers to the whole class. it returns the whole class not just the db object
    }
    public void close(){
        DBHelper.close(); //this closes the connection
    }

    public long newPurchase(String currency, String amountBought, String costBasis, String fee, String purchasePrice, String time, String date){

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CURRENCY, currency);
        initialValues.put(KEY_AMOUNTBOUGHT, amountBought);
        initialValues.put(KEY_COSTBASIS, costBasis);
        initialValues.put(KEY_FEE, fee);
        initialValues.put(KEY_PURCHASEPRICE, purchasePrice);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_DATE, date);

        return db.insert(DATABASE_TABLE, null, initialValues);

    }
    public boolean deletePurchase(long rowID){
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowID, null) > 0; // do > 0 so that it returns a bool telling the
        //caller whether or not it was successful. If it returns a greater number it successfully deleted
    }
    public boolean deletePurchase(String rowID){
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowID, null) > 0; // do > 0 so that it returns a bool telling the
        //caller whether or not it was successful. If it returns a greater number it successfully deleted
    }
    //get all records
    public Cursor getAllRecords(){ //returns a cursor so that the database can be iterated thru
        return db.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_CURRENCY, KEY_AMOUNTBOUGHT, KEY_COSTBASIS, KEY_FEE, KEY_PURCHASEPRICE, KEY_TIME, KEY_DATE}, null, null, null, null, null);
}
    public Cursor getRecord(long rowID) throws SQLException{
        Cursor oCursor =
                db.query(true, DATABASE_TABLE, new String[]{KEY_ROWID, KEY_CURRENCY, KEY_AMOUNTBOUGHT, KEY_COSTBASIS, KEY_FEE, KEY_PURCHASEPRICE, KEY_TIME, KEY_DATE}, KEY_ROWID + "=" + rowID,
                        null, null, null, null, null);

        if(oCursor != null){
            oCursor.moveToFirst();
        }
        return oCursor;
    }
    public boolean updateRecord(long rowID, String currency, String amountBought, String costBasis, String fee, String purchasePrice, String time, String date){
        ContentValues args = new ContentValues();
        args.put(KEY_CURRENCY, currency);
        args.put(KEY_AMOUNTBOUGHT, amountBought);
        args.put(KEY_COSTBASIS, costBasis);
        args.put(KEY_FEE, fee);
        args.put(KEY_PURCHASEPRICE, purchasePrice);
        args.put(KEY_TIME, time);
        args.put(KEY_DATE, date);

        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowID, null) > 0;
    }

}
package com.example.jojo0.myrestaurants;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class AffichageResto extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 7;
    private static final int CALL_PERMISSION_REQUEST_CODE = 8;
    private static final int WR_EXT_STOR_PERMISSION_REQUEST_CODE = 9;
    private static final int WR_CONTACTS_PERMISSION_REQUEST_CODE = 10;

    private int noteResto;
    private String photoPath;
    private ImageView imgV;
    private TextView tvNom, tvAddr, tvPhone, tvWeb, tvCout, tvHoraire, tvType, tvGPS;
    private Spinner mSpinner;
    private ArrayAdapter<CharSequence> sAdapter;
    private ListView lv;
    private String[] from;
    private int [] to;
    private SimpleCursorAdapter cAdapter;
    ContentResolver resolver;

    final AccessBase db = new AccessBase(this);

    private static final int LOADER_ID = 1;
    private static final int LOADER_ID_COMM = 2;

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private static final String[] PROJECTION = new String[] {"_id",AccessBase.RESTAURANT_NOM,AccessBase.RESTAURANT_ADDR
        ,AccessBase.RESTAURANT_NUMPHONE,AccessBase.RESTAURANT_SITEWEB,AccessBase.RESTAURANT_NOTE,AccessBase.RESTAURANT_GPS
        ,AccessBase.RESTAURANT_COUTMOYREPAS,AccessBase.RESTAURANT_HORAIRE,AccessBase.RESTAURANT_TYPE,AccessBase.RESTAURANT_DATA};
    private static final String[] PROJECTION_COMM = new String[] {"_id",AccessBase.AUTEURCOMM,AccessBase.TEXTCOMM};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affichage_resto);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        photoPath=null;
        noteResto=-1;
        resolver = getContentResolver();

        imgV = (ImageView) findViewById(R.id.imageView);
        tvNom = (TextView) findViewById(R.id.textViewAffichageNom);
        tvAddr = (TextView) findViewById(R.id.textViewAffichageAddr);
        tvPhone = (TextView) findViewById(R.id.textViewAffichagePhone);
        tvWeb = (TextView) findViewById(R.id.textViewAffichageWeb);
        tvCout = (TextView) findViewById(R.id.textViewAffichageCout);
        tvHoraire = (TextView) findViewById(R.id.textViewAffichageHoraires);
        tvType = (TextView) findViewById(R.id.textViewAffichageType);
        tvGPS = (TextView) findViewById(R.id.textViewAffichageGPS);
        mSpinner = (Spinner) findViewById(R.id.note_spinner);

        Intent i=getIntent();
        tvAddr.setText(i.getStringExtra("addr"));

        sAdapter = ArrayAdapter.createFromResource(this, R.array.note_array,
                android.R.layout.simple_spinner_item);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(sAdapter);

        lv = (ListView) findViewById(R.id.listViewComm);

        from = new String[]{AccessBase.AUTEURCOMM,AccessBase.TEXTCOMM};
        to = new int[]{R.id.textViewListAuteur,R.id.textViewListComm};

        cAdapter = new SimpleCursorAdapter(this,R.layout.list_item_comm, null,
                from,to,0);
        lv.setAdapter(cAdapter);

        mCallbacks = this;
        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, mCallbacks);
        lm.initLoader(LOADER_ID_COMM, null, mCallbacks);

        // gestion permissions
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_COARSE_LOCATION);
        PermissionUtils.requestPermission(this, WR_CONTACTS_PERMISSION_REQUEST_CODE, Manifest.permission.WRITE_CONTACTS);
        PermissionUtils.requestPermission(this, WR_EXT_STOR_PERMISSION_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionUtils.requestPermission(this, CALL_PERMISSION_REQUEST_CODE, Manifest.permission.CALL_PHONE);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getItemAtPosition(position).toString().trim().equals("" + noteResto)) {
                    ContentValues mValues = new ContentValues();
                    noteResto = Integer.parseInt(parent.getItemAtPosition(position).toString());
                    mValues.put(AccessBase.RESTAURANT_NOTE, noteResto);
                    boolean upd = db.updateLigne(resolver, mValues, tvAddr.getText().toString());
                    if (upd)
                        Toast.makeText(getApplicationContext(), "MAJ note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "Vous n'avez rien selectionné", Toast.LENGTH_SHORT).show();
            }
        });

        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        tvHoraire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                if (Build.VERSION.SDK_INT >= 14) {
                    Intent calendar = new Intent(Intent.ACTION_EDIT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis())
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis() + 60 * 60 * 1000)
                            .putExtra(CalendarContract.Events.TITLE, "Rendez-vous restaurant: " + tvNom.getText().toString())
                            .putExtra(CalendarContract.Events.DESCRIPTION, "RDV prit via l'application MyRestaurants")
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, tvAddr.getText().toString());
                    startActivity(calendar);
                } else {
                    Intent calendar = new Intent(Intent.ACTION_EDIT);
                    calendar.setType("vnd.android.cursor.item/event");
                    calendar.putExtra("beginTime", cal.getTimeInMillis());
                    calendar.putExtra("allDay", true);
                    calendar.putExtra("rrule", "FREQ=YEARLY");
                    calendar.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
                    calendar.putExtra("title", "Rendez-vous restaurant: " + tvNom.getText().toString());
                    calendar.putExtra("eventLocation", tvAddr.getText().toString());
                    startActivity(calendar);
                }
            }
        });

        tvWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tvWeb.getText().toString()!=getString(R.string.tvWeb))
                    new MonChargeur().execute("http://www." + tvWeb.getText().toString());
            }
        });

        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + tvPhone.getText().toString()));
                    startActivity(intent);
                } catch (IllegalStateException e) {
                    Log.e("PHONE_CALL", "ERROR on phone number given");
                } catch (SecurityException e) {
                    Toast.makeText(getApplicationContext(), "Vous n'avez pas les droits requis", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.streetview:cbll=" + tvGPS.getText().toString());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e)
        {
            Log.e("RETURN_TO","ERROR on return button");
        }

    }

    //rafraichissement de l'activity quand on y revient.
    @Override
    public void onRestart()
    {
        super.onRestart();
        getLoaderManager().restartLoader(LOADER_ID, null, mCallbacks);
        getLoaderManager().restartLoader(LOADER_ID_COMM, null, mCallbacks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_affichage_resto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_supprimer) {
//            getLoaderManager().restartLoader(LOADER_ID,null,mCallbacks);
            new AlertDialog.Builder(this)
                    .setTitle("Supprimer Restaurant")
                    .setMessage("Êtes-vous sur de vouloir supprimer ce restaurant ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Boolean del=db.supprimerLigne(resolver, tvAddr.getText().toString());
                            if(del)
                            {
                                Toast.makeText(getApplicationContext(),"Restaurant supprimé",Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                finish();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Error suppression Restaurant",Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }else if (id == R.id.action_modifier) {
            Intent modifResto = new Intent(this, ModifierResto.class);
            modifResto.putExtra("note",noteResto);
            modifResto.putExtra("nom",tvNom.getText().toString());
            modifResto.putExtra("addr",tvAddr.getText().toString());
            modifResto.putExtra("phone", tvPhone.getText().toString());
            modifResto.putExtra("web", tvWeb.getText().toString());
            modifResto.putExtra("cout", tvCout.getText().toString());
            modifResto.putExtra("horaire", tvHoraire.getText().toString());
            modifResto.putExtra("type", tvType.getText().toString());
            modifResto.putExtra("photo", photoPath);
            startActivity(modifResto);
        }else if (id == R.id.action_commenter) {
            Intent ajoutComm = new Intent(this, AjoutCommentaire.class);
            ajoutComm.putExtra("addr",tvAddr.getText().toString());
            startActivity(ajoutComm);
        }else if (id == R.id.action_envoyerSMS) {
            Intent envoieSMS = new Intent(Intent.ACTION_VIEW);
            envoieSMS.setData(Uri.parse("sms:"));
            envoieSMS.setType("vnd.android-dir/mms-sms");
            envoieSMS.putExtra(Intent.EXTRA_TEXT, "Vous êtes invité au restaurant: "+tvNom.getText().toString()
                    +". Veuillez me recontacter pour prendre date.");
            startActivity(envoieSMS);
        }else if (id == R.id.action_ajoutContact) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            //------------------------------------------------------ Names
            if (tvNom.getText().toString().length() != 0) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                "Restaurant " + tvNom.getText().toString()).build());
            }

            //------------------------------------------------------ Home Numbers
            if (tvPhone.getText().toString().length() != 0) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, tvPhone.getText().toString())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                        .build());
            }
            // Asking the Contact provider to create a new contact
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(getApplicationContext(), "Contact Ajouté.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Vous n'avez pas les droits requis", Toast.LENGTH_SHORT).show();
                Log.e("ADD_CONTACTS", "Exception: " + e.getMessage());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader=null;
        switch (id){
            case LOADER_ID:
                cursorLoader = new CursorLoader(getApplicationContext(), AccessBase.CONTENT_URI_RESTO,
                        PROJECTION, null, new String[] {tvAddr.getText().toString()}, null);
                break;
            case LOADER_ID_COMM:
                cursorLoader = new CursorLoader(getApplicationContext(), AccessBase.CONTENT_URI_COMM,
                        PROJECTION_COMM, null, new String[] {tvAddr.getText().toString()}, null);
                break;
            default:
                Log.e("CURSORLOADER", "Error on CursorLoader id");
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with data
                if (cursor != null && cursor.getCount() > 0) {
                    if(cursor.moveToFirst())
                    {
                        int nomIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_NOM);
                        int phoneIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_NUMPHONE);
                        int webIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_SITEWEB);
                        int coutIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_COUTMOYREPAS);
                        int horaireIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_HORAIRE);
                        int typeIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_TYPE);
                        int gpsIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_GPS);
                        int photoIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_DATA);
                        int noteIndex = cursor.getColumnIndex(AccessBase.RESTAURANT_NOTE);

                        tvNom.setText(cursor.getString(nomIndex));
                        tvPhone.setText("0"+cursor.getInt(phoneIndex));
                        if(cursor.getString(webIndex)==null)
                            tvWeb.setText(getString(R.string.tvWeb));
                        else
                            tvWeb.setText(cursor.getString(webIndex));
                        tvWeb.setClickable(true);
                        tvCout.setText(String.format(Locale.FRANCE,"%f",cursor.getFloat(coutIndex)));
                        tvHoraire.setText(cursor.getString(horaireIndex));
                        tvHoraire.setClickable(true);
                        tvType.setText(cursor.getString(typeIndex));
                        tvGPS.setText(cursor.getString(gpsIndex));
                        photoPath=cursor.getString(photoIndex);
                        imgV.setImageBitmap(getScaledBitmap(photoPath, 800, 800));
                        noteResto=cursor.getInt(noteIndex);
                        mSpinner.setSelection(sAdapter.getPosition(""+noteResto));
                    }
                }
                break;
            case LOADER_ID_COMM:
                try{
                    cAdapter.swapCursor(cursor);
                }catch (IllegalArgumentException e)
                {
                    Log.e("ERROR_CURSOR_COMM","Error on column's name: " + e.getMessage());
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cAdapter.swapCursor(null);
    }

    //Redimmensionnement image
    private Bitmap getScaledBitmap(String picturePath, int width, int height) {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, sizeOptions);

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(picturePath, sizeOptions);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }


    class MonChargeur extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params)
        {
            try{
                URL url=new URL(params[0]);
                HttpURLConnection huc =  (HttpURLConnection)  url.openConnection();
                huc.setRequestMethod ("GET");
                huc.connect () ;
                int code = huc.getResponseCode() ;

                if(code==200)
                    return true;
            }
            catch (MalformedURLException mfurle)
            {
                Log.e("URL_EXCEPTION","Problème url entrée. "+mfurle);
            }
            catch (IOException ioe)
            {
                Log.e("HTTP_URL_CONNEXION","Problème connexion. "+ioe);
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if(result)
            {
                try {
                    Intent WebResto = new Intent(Intent.ACTION_VIEW);
                    WebResto.setData(Uri.parse("http://www." + tvWeb.getText().toString()));
                    startActivity(WebResto);
                } catch (ActivityNotFoundException e) {
                    Log.e("ACCESS_SITEWEB","ERROR on uri parsed");
                }
            }
        }
    }

    /**
     * Method handling the result of the different asked permission to the User
     * Always finish the current activity at the end if negative answer and display toast
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CALL_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(this,
                            R.string.call_phone_permission_required_toast,
                            Toast.LENGTH_SHORT)
                            .show();
                    this.finish();
                }
                return;
            }
            case WR_CONTACTS_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.WRITE_CONTACTS)) {
                    Toast.makeText(this,
                            R.string.contacts_permission_required_toast,
                            Toast.LENGTH_SHORT)
                            .show();
                    this.finish();
                }
                return;
            }
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this,
                            R.string.location_permission_required_toast,
                            Toast.LENGTH_SHORT)
                            .show();
                    this.finish();
                }
                return;
            }
            case WR_EXT_STOR_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this,
                            R.string.location_permission_required_toast,
                            Toast.LENGTH_SHORT)
                            .show();
                    this.finish();
                }
                return;
            }
            default: {
                Toast.makeText(this,
                        R.string.permission_required_toast,
                        Toast.LENGTH_SHORT)
                        .show();
                this.finish();
                return;
            }
        }
    }
}

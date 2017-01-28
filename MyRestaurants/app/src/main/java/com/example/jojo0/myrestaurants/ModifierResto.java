package com.example.jojo0.myrestaurants;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ModifierResto extends AppCompatActivity {

    public static final int REQUEST_CAMERA = 501;
    public static final int SELECT_FILE = 502;

    private int noteResto;
    private boolean photoPrise;
    private String filePath, addrResto;
    private EditText eTNom, eTPhone, eTWeb, eTCout, eTHoraire, eTType;
    private Spinner mSpinner;
    private ArrayAdapter<CharSequence> sAdapter;
    ContentResolver resolver;
    final AccessBase db = new AccessBase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_resto);

        resolver = getContentResolver();
        photoPrise=false;
        filePath=null;
        eTNom = (EditText) findViewById(R.id.editTextModifNom);
        eTPhone = (EditText) findViewById(R.id.editTextModifPhone);
        eTWeb = (EditText) findViewById(R.id.editTextModifWeb);
        eTCout = (EditText) findViewById(R.id.editTextModifCoutMoyRepas);
        eTHoraire = (EditText) findViewById(R.id.editTextModifHoraire);
        eTType = (EditText) findViewById(R.id.editTextModifTypeResto);

        Intent intent=getIntent();
        noteResto=intent.getIntExtra("note",0);
        eTNom.setText(intent.getStringExtra("nom"));
        eTPhone.setText(intent.getStringExtra("phone"));
        eTWeb.setText(intent.getStringExtra("web"));
        eTCout.setText(intent.getStringExtra("cout"));
        eTHoraire.setText(intent.getStringExtra("horaire"));
        eTType.setText(intent.getStringExtra("type"));
        filePath=intent.getStringExtra("photo");
        addrResto=intent.getStringExtra("addr");

        mSpinner = (Spinner) findViewById(R.id.modif_spinner);

        sAdapter = ArrayAdapter.createFromResource(this, R.array.note_array,
                android.R.layout.simple_spinner_item);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(sAdapter);
        mSpinner.setSelection(sAdapter.getPosition(""+noteResto));
    }

    public void modifierRestaurant(View v)
    {
        try {
            if(photoPrise)
                getLastImage();
            if (eTNom.getText().toString().trim().length() == 0 || eTPhone.getText().toString().trim().length() == 0
                    || eTHoraire.getText().toString().trim().length() == 0 || eTType.getText().toString().trim().length() == 0
                    || eTWeb.getText().toString().trim().length() == 0 || eTCout.getText().toString().trim().length() == 0
                    || filePath == null)
            {
                Toast.makeText(this, "Evitez les champs vide.", Toast.LENGTH_SHORT).show();
            }
            else {
                String nomResto = eTNom.getText().toString().trim();
                int phoneResto = Integer.parseInt(eTPhone.getText().toString().trim());
                String webResto = eTWeb.getText().toString().trim();
                float coutResto = Float.parseFloat(eTCout.getText().toString().trim());
                String horaireResto = eTHoraire.getText().toString().trim();
                String typeResto = eTType.getText().toString().trim();
                noteResto = Integer.parseInt(String.valueOf(mSpinner.getSelectedItem()));
                ContentValues mValues=new ContentValues();
                mValues.put(AccessBase.RESTAURANT_NOM, nomResto);
                mValues.put(AccessBase.RESTAURANT_NUMPHONE, phoneResto);
                mValues.put(AccessBase.RESTAURANT_SITEWEB, webResto);
                mValues.put(AccessBase.RESTAURANT_COUTMOYREPAS, coutResto);
                mValues.put(AccessBase.RESTAURANT_HORAIRE, horaireResto);
                mValues.put(AccessBase.RESTAURANT_TYPE, typeResto);
                mValues.put(AccessBase.RESTAURANT_NOTE, noteResto);
                mValues.put(AccessBase.RESTAURANT_DATA, filePath);
                if (db.updateLigne(resolver, mValues, addrResto))
                {
                    Toast.makeText(this, "Update done.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                    Toast.makeText(this, "Update error.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez remplir tout les champs.", Toast.LENGTH_SHORT).show();
            Log.e("ERROR_FORMU", e.getMessage());
        }
    }

    public void photoCamera(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public void photoGallery(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA)
                photoPrise=true;
            else if (requestCode == SELECT_FILE)
            {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                filePath=cursor.getString(column_index);
            }
        }
    }//onActivityResult

    private void getLastImage()
    {
        try{
            final ContentResolver cr = getContentResolver();
            final String[] p1 = new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DATE_TAKEN
            };
            Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");
            if(c1!=null)
            {
                if (c1.moveToFirst())
                {
                    filePath = c1.getString(0);
                    c1.close();
                }
            }
        }catch (SecurityException e){
            Toast.makeText(this, "Vous n'avez pas les droits.", Toast.LENGTH_SHORT).show();
        }

    }
}

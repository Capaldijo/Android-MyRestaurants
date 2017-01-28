package com.example.jojo0.myrestaurants;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class FormulaireResto extends AppCompatActivity {

    private static final int WR_EXT_STOR_PERMISSION_REQUEST_CODE = 9;

    public static final int REQUEST_CAMERA = 501;
    public static final int SELECT_FILE = 502;

    private EditText eTNom, eTAddr, eTPhone, eTCout, eTHoraire, eTType;
    private String filePath;
    Boolean photoPrise;

    final AccessBase db = new AccessBase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire_resto);

        // gestion permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
            PermissionUtils.requestPermission(this, WR_EXT_STOR_PERMISSION_REQUEST_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        filePath = null;
        photoPrise=false;
        eTNom = (EditText) findViewById(R.id.editTextNom);
        eTAddr = (EditText) findViewById(R.id.editTextAddr);
        eTPhone = (EditText) findViewById(R.id.editTextPhone);
        eTCout = (EditText) findViewById(R.id.editTextCoutMoyRepas);
        eTHoraire = (EditText) findViewById(R.id.editTextHoraire);
        eTType = (EditText) findViewById(R.id.editTextTypeResto);
    }

    public void ajouterRestaurant(View v) {
        ContentResolver resolver = getContentResolver();

        try {
            if(photoPrise)
                getLastImage();
            String nomResto = eTNom.getText().toString().trim();
            String addrResto = eTAddr.getText().toString().trim();
            int phoneResto = Integer.parseInt(eTPhone.getText().toString().trim());
            float coutResto = Float.parseFloat(eTCout.getText().toString().trim());
            String horaireResto = eTHoraire.getText().toString().trim();
            String typeResto = eTType.getText().toString().trim();

            if (nomResto.length() == 0 || addrResto.length() == 0 || horaireResto.length() == 0
                    || eTCout.getText().toString().trim().length() == 0 || eTPhone.getText().toString().trim().length() == 0
                    || typeResto.length() == 0 || filePath == null)
                Toast.makeText(this, "Evitez les champs vide.", Toast.LENGTH_SHORT).show();
            else {
                if (db.ajouterLigne(resolver, nomResto, addrResto, phoneResto, coutResto, horaireResto, typeResto, filePath))
                {
                    Toast.makeText(this, "Insert done.", Toast.LENGTH_SHORT).show();
                    eTNom.setText("");
                    eTAddr.setText("");
                    eTPhone.setText("");
                    eTCout.setText("");
                    eTHoraire.setText("");
                    eTType.setText("");
                    filePath=null;
                }
                else
                    Toast.makeText(this, "Insert error.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez remplir tout les champs.", Toast.LENGTH_SHORT).show();
            Log.e("ERROR_FORMU",e.getMessage());
        }
    }//ajouterRestaurant

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
        long dateIMG=0;
        try{
            final ContentResolver cr = getContentResolver();
            final String[] p1 = new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DATE_TAKEN
            };
            // Recup img externe
            Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");
            if(c1!=null)
            {
                if (c1.moveToFirst())
                {
                    filePath = c1.getString(0);
                    dateIMG=Long.parseLong(c1.getString(1));
                    Log.e("DATE_IMG1",c1.getString(1));
                    c1.close();
                }
            }
            // recup img interne
            Cursor c2 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");
            if(c2!=null)
            {
                if (c2.moveToFirst())
                {
                    if(dateIMG < Long.parseLong(c2.getString(1)))
                        filePath = c2.getString(0);
                    Log.e("DATE_IMG2",c2.getString(1));
                    c2.close();
                }
            }

        }catch (SecurityException e){
            Toast.makeText(this,"Vous n'avez pas les droits.",Toast.LENGTH_SHORT).show();
        }

    }

    // gestion réponse permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WR_EXT_STOR_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this,
                            R.string.storage_permission_required_toast,
                            Toast.LENGTH_SHORT)
                            .show();
                    this.finish();
                }
                return;
            }
        }
    }
}

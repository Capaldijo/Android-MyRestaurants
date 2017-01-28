package com.example.jojo0.myrestaurants;

import android.content.ContentResolver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AjoutCommentaire extends AppCompatActivity {

    private EditText eTAuteur, eTComm;
    private String addr, auteur, comm;
    private ContentResolver resolver;
    final AccessBase db = new AccessBase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_commentaire);

        resolver=getContentResolver();

        addr=getIntent().getStringExtra("addr");
        eTAuteur = (EditText) findViewById(R.id.editTextAuteur);
        eTComm = (EditText) findViewById(R.id.editTextComm);
    }

    public void ajouterCommentaire(View v)
    {
        auteur=eTAuteur.getText().toString().trim();
        comm=eTComm.getText().toString().trim();
        if(auteur.length() == 0 || comm.length() == 0)
            Toast.makeText(this,"Evitez les champs vide.", Toast.LENGTH_SHORT).show();
        else
        {
            if(db.ajouterLigneComm(resolver, auteur, comm, addr, System.currentTimeMillis()))
            {
                Toast.makeText(getApplicationContext(),"Insert Done", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
                Toast.makeText(getApplicationContext(),"Insert ERROR", Toast.LENGTH_SHORT).show();
        }
    }
}

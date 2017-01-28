package com.example.jojo0.contentproviderresto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Base extends SQLiteOpenHelper {

    private static final String DBNAME = "resto_db";
    private static int VERSION = 9;

    private static final String RESTO = "resto";
    private static final String COMMENTAIRES = "comm";

    private static final String NOM = "nom_resto";
    private static final String ADDR = "address";
    private static final String NUMPHONE = "num_tel";
    private static final String SITEWEB = "site_web";
    private static final String NOTE = "note";
    private static final String GPS = "gps";
    private static final String COUTMOYREPAS = "cout_moy_repas";
    private static final String HORAIRE = "horaire";
    private static final String TYPE = "type_repas";
    private static final String DATA = "data";

    private static final String AUTEURCOMM = "auteur";
    private static final String TEXTCOMM = "commentaire";
    private static final String DATECOMM = "date";
    private static final String IDREF = "_idref";

    private static final String CREATE_ADRESSE = "create table " + RESTO +
            " ( _id Integer, " + NOM + " String not null, " + ADDR + " String not null , " + NUMPHONE +
            " Integer not null, " + SITEWEB + " String, " + NOTE + " Integer, " +
            GPS + " String, " + COUTMOYREPAS + " REAL not null, " + HORAIRE + " String not null, " +
            TYPE + " String not null, " + DATA + " String not null, PRIMARY KEY("+ADDR+") );";

    private static final String CREATE_ADRESSE2 = "create table " + COMMENTAIRES +
            " ( _id Integer primary key, " + AUTEURCOMM + " String not null, "
            + TEXTCOMM + " Text not null, " + DATECOMM + " Integer not null, "
            + IDREF + " String, " + " FOREIGN KEY (" + IDREF + ") REFERENCES "
            + RESTO + " (" + ADDR + "));";


    public Base(Context context)
    {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ADRESSE);
        db.execSQL(CREATE_ADRESSE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion >= oldVersion)
        {
            db.execSQL("drop table if exists " + RESTO);
            db.execSQL("drop table if exists " + COMMENTAIRES);
            onCreate(db);
        }
    }

    public String getRESTO(){
        return RESTO;
    }

    public String getCOMMENTAIRES() {
        return COMMENTAIRES;
    }

    public String getNOM() {
        return NOM;
    }

    public String getADDR() {
        return ADDR;
    }

    public String getNUMPHONE() {
        return NUMPHONE;
    }

    public String getSITEWEB() {
        return SITEWEB;
    }

    public String getNOTE() {
        return NOTE;
    }

    public String getGPS() {
        return GPS;
    }

    public String getCOUTMOYREPAS() {
        return COUTMOYREPAS;
    }

    public String getHORAIRE() {
        return HORAIRE;
    }

    public String getTYPE() {
        return TYPE;
    }

    public String getDATA() {
        return DATA;
    }

    public String getAUTEURCOMM() {
        return AUTEURCOMM;
    }

    public String getTEXTCOMM() {
        return TEXTCOMM;
    }

    public String getDATECOMM() {
        return DATECOMM;
    }

    public String getIDREF() {
        return IDREF;
    }
}

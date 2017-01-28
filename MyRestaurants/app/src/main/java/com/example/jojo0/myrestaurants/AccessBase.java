package com.example.jojo0.myrestaurants;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;

/**
 * Created by jojo0 on 24/12/2015.
 */

public class AccessBase {

    public static final Uri CONTENT_URI = Uri.parse("content://com.example.jojo0.myRestaurants/tout");
    public static final Uri CONTENT_URI_RESTO = Uri.parse("content://com.example.jojo0.myRestaurants/affichage_resto/");
    public static final Uri CONTENT_URI_MINI = Uri.parse("content://com.example.jojo0.myRestaurants/mini/");
    public static final Uri CONTENT_URI_NOM = Uri.parse("content://com.example.jojo0.myRestaurants/recherche_nom/");
    public static final Uri CONTENT_URI_NOTE = Uri.parse("content://com.example.jojo0.myRestaurants/recherche_note/");
    public static final Uri CONTENT_URI_TYPE = Uri.parse("content://com.example.jojo0.myRestaurants/recherche_type/");
    public static final Uri CONTENT_URI_COUT = Uri.parse("content://com.example.jojo0.myRestaurants/recherche_cout/");
    public static final Uri CONTENT_URI_COMM = Uri.parse("content://com.example.jojo0.myRestaurants/commentaires/");

    public static final String RESTAURANT_NOM = "nom_resto";
    public static final String RESTAURANT_ADDR = "address";
    public static final String RESTAURANT_NUMPHONE = "num_tel";
    public static final String RESTAURANT_SITEWEB = "site_web";
    public static final String RESTAURANT_NOTE = "note";
    public static final String RESTAURANT_GPS = "gps";
    public static final String RESTAURANT_COUTMOYREPAS = "cout_moy_repas";
    public static final String RESTAURANT_HORAIRE = "horaire";
    public static final String RESTAURANT_TYPE = "type_repas";
    public static final String RESTAURANT_DATA = "data";
    public static final String AUTEURCOMM = "auteur";
    public static final String TEXTCOMM = "commentaire";
    public static final String DATECOMM = "date";
    public static final String IDREF = "_idref";

    private Context mContext;

    public AccessBase(Context context){
        mContext=context;
    }


    public boolean ajouterLigne(ContentResolver resolver, String nom, String addr, int phone, float cout,
                                String horaires, String type, String photoPath)
    {
        ContentValues row = new ContentValues( );
        String gps = getLocationFromAddress(mContext,addr);
        row.put(RESTAURANT_NOM, nom);
        row.put(RESTAURANT_ADDR, addr);
        row.put(RESTAURANT_NUMPHONE, phone);
        row.put(RESTAURANT_COUTMOYREPAS, cout);
        row.put(RESTAURANT_HORAIRE, horaires);
        row.put(RESTAURANT_TYPE, type);
        row.put(RESTAURANT_DATA, photoPath);
        row.put(RESTAURANT_NOTE, 0);
        row.put(RESTAURANT_GPS, gps);
        try{
            if((resolver.insert(CONTENT_URI, row))!=null)
                return true;
        }
        catch (IllegalArgumentException e)
        {
            Log.e("INSERT_RESTO:", "ERROR URI " +e);
        }

        return false;
    }

    public boolean ajouterLigneComm(ContentResolver resolver, String auteur, String comm, String addr, long date)
    {
        ContentValues row = new ContentValues( );
        row.put(AUTEURCOMM, auteur);
        row.put(TEXTCOMM, comm);
        row.put(IDREF, addr);
        row.put(DATECOMM, date);
        try{
            if((resolver.insert(CONTENT_URI_COMM, row))!=null)
                return true;
        }
        catch (IllegalArgumentException e)
        {
            Log.e("INSERT_RESTO:", "ERROR URI " +e);
        }

        return false;
    }

    public boolean supprimerLigne(ContentResolver resolver, String addr)
    {
        String[] mSelectionArgs = new String[]{addr};
        try{
            if(resolver.delete(AccessBase.CONTENT_URI_RESTO,null,mSelectionArgs)!=0 &&
                    resolver.delete(AccessBase.CONTENT_URI_COMM,null,mSelectionArgs)!=-1)
                return true;
        }catch (IllegalArgumentException e)
        {
            Log.e("DELETE_RESTO:", "ERROR ARGUMENTS " +e);
        }
        return false;
    }

    public boolean updateLigne(ContentResolver resolver, ContentValues values ,String addr)
    {
        String mSelection = AccessBase.RESTAURANT_ADDR + " LIKE ?";
        String[] mSelectionArgs = new String[]{addr};
        try{
            if(resolver.update(AccessBase.CONTENT_URI_RESTO, values, mSelection, mSelectionArgs)!=0)
                return true;
        }catch (IllegalArgumentException e)
        {
            Log.e("DELETE_RESTO:", "ERROR ARGUMENTS " +e);
        }
        return false;
    }

    public String getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        String gps="null";

        try {
            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocationName(strAddress, 10);
            gps=adresses.get(0).getLatitude()+","+adresses.get(0).getLongitude();
            Log.e("GPS",""+gps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gps;
    }
}

package com.example.jojo0.contentproviderresto;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class MyContentProvider extends ContentProvider {

    private static final int All_ADR = 1;
    private static final int One_ADR_NOM = 2;
    private static final int One_ADR_TYPE = 3;
    private static final int One_ADR_NOTE = 4;
    private static final int One_ADR_COUT = 5;
    private static final int One_ADR_MINI = 6;
    private static final int One_ADR_COMM = 7;
    private static final int One_ADR_RESTO = 8;
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final Uri CONTENT_URI = Uri.parse("content://com.example.jojo0.myRestaurants/tout");
    private static final Uri CONTENT_URI_COMM = Uri.parse("content://com.example.jojo0.myRestaurants/commentaires/");
    private Base base;
    private SQLiteDatabase db;

    public MyContentProvider() {
    }

    static {
        matcher.addURI("com.example.jojo0.myRestaurants","tout",All_ADR);

        matcher.addURI("com.example.jojo0.myRestaurants","affichage_resto",One_ADR_RESTO);

        matcher.addURI("com.example.jojo0.myRestaurants","recherche_nom/",One_ADR_NOM);

        matcher.addURI("com.example.jojo0.myRestaurants","recherche_type/",One_ADR_TYPE);

        matcher.addURI("com.example.jojo0.myRestaurants","recherche_note/",One_ADR_NOTE);

        matcher.addURI("com.example.jojo0.myRestaurants","recherche_cout/",One_ADR_COUT);

        matcher.addURI("com.example.jojo0.myRestaurants","mini",One_ADR_MINI);

        matcher.addURI("com.example.jojo0.myRestaurants","commentaires/",One_ADR_COMM);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int mRowsDeleted=0;
        if(matcher.match(uri) == 8 )
        {
            String mSelection = base.getRESTO() + " LIKE ?";
            mRowsDeleted = db.delete(base.getRESTO(),mSelection,selectionArgs);
        }
        else if(matcher.match(uri) == 7 )
        {
            String mSelection = base.getIDREF() + " LIKE ?";
            mRowsDeleted = db.delete(base.getCOMMENTAIRES(),mSelection,selectionArgs);
        }

        return mRowsDeleted;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        db = base.getWritableDatabase();
        if(matcher.match(uri) == 1 )
        {
            long id = db.insert(base.getRESTO(),null,values);
            if(id != -1)
                return ContentUris.withAppendedId(CONTENT_URI, id); //On return L'URI associé à l'id
        }
        else if(matcher.match(uri) == 7)
        {
            long id = db.insert(base.getCOMMENTAIRES(),null,values);
            if(id != -1)
                return ContentUris.withAppendedId(CONTENT_URI_COMM, id);
        }

        return null;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        try{
            base = new Base(getContext());
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) throws SQLiteException{
        // TODO: Implement this to handle query requests from clients.
        db=base.getReadableDatabase();

        int code = matcher.match(uri);
        Cursor c = null;
        switch (code)
        {
            case All_ADR:
                c=db.rawQuery("SELECT * FROM " + base.getRESTO(),null);
                break;
            case One_ADR_NOM:
                if(selectionArgs!=null)
                    c=db.rawQuery("SELECT _id, " +  base.getNOM() + ", " + base.getADDR() + " FROM " + base.getRESTO()
                            + " WHERE " + base.getNOM() + " LIKE '%" + selectionArgs[0] + "%'",null);
                break;
            case One_ADR_TYPE:
                if(selectionArgs!=null)
                    c=db.rawQuery("SELECT _id, " +  base.getNOM() + ", " + base.getADDR() + " FROM " + base.getRESTO()
                            + " WHERE " + base.getTYPE() + " LIKE '%" + selectionArgs[0] + "%'",null);
                break;
            case One_ADR_NOTE:
                if(selectionArgs!=null)
                {
                    try{
                        int note = Integer.parseInt(selectionArgs[0]);
                        c=db.rawQuery("SELECT _id, " +  base.getNOM() + ", " + base.getADDR() + " FROM " + base.getRESTO()
                                + " WHERE " + base.getNOTE() + " LIKE '" + note + "'",null);
                    }catch (IllegalArgumentException e)
                    {
                        Log.e("RAWQUERY","ERROR on Argument");
                    }
                }
                break;
            case One_ADR_COUT:
                if(selectionArgs!=null)
                {
                    try{
                        float cout = Float.parseFloat(selectionArgs[0]);
                        c=db.rawQuery("SELECT _id, " +  base.getNOM() + ", " + base.getADDR() + " FROM " + base.getRESTO()
                                + " WHERE " + base.getCOUTMOYREPAS() + " <= '" + cout + "'",null);
                    }catch (IllegalArgumentException e){
                        Log.e("RAWQUERY","ERROR on Argument");
                    }
                }
                break;
            case One_ADR_MINI:
                c=db.rawQuery("SELECT _id, " + base.getNOM() + ", " + base.getADDR() +  " FROM " + base.getRESTO(),null);
                break;
            case One_ADR_COMM:
                if(selectionArgs!=null)
                    c=db.rawQuery("SELECT _id, " + base.getAUTEURCOMM() + ", " + base.getTEXTCOMM() + " FROM "
                            + base.getCOMMENTAIRES() + " WHERE " + base.getIDREF() + " LIKE '" + selectionArgs[0]
                            + "' ORDER BY " + base.getIDREF() + " DESC",null);
                break;
            case One_ADR_RESTO:
                if(selectionArgs!=null)
                    c=db.rawQuery("SELECT * FROM " + base.getRESTO()
                            + " WHERE " + base.getADDR() + " LIKE '" + selectionArgs[0] + "'",null);
                break;
            default:
                Log.e("ERROR_URI:","Tombé dans default, pb uri");
                break;
        }
        return c;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        String mSelection = base.getADDR() + " LIKE ?";
        int mRowsUpdated = db.update(base.getRESTO(), values, mSelection, selectionArgs);
        return mRowsUpdated;
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}

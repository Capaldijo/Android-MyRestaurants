package com.example.jojo0.myrestaurants;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private EditText etSearch;
    private ListView lv;
    private String[] from;
    private int [] to;
    private SimpleCursorAdapter cAdapter;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;

    private static final String[] PROJECTION = new String[] {"_id",AccessBase.RESTAURANT_NOM,AccessBase.RESTAURANT_ADDR};
    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int LOADER_ID = 1;
    private static final int LOADER_ID_NOM = 2;
    private static final int LOADER_ID_NOTE = 3;
    private static final int LOADER_ID_TYPE = 4;
    private static final int LOADER_ID_COUT = 5;
    // The callbacks through which we will interact with the LoaderManager.
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etSearch = (EditText) findViewById(R.id.editTextSearch);
        spinner = (Spinner) findViewById(R.id.search_spinner);
        //implémentation du spinner
        adapter = ArrayAdapter.createFromResource(this,R.array.search_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        lv = (ListView) findViewById(R.id.listView);

        from = new String[]{AccessBase.RESTAURANT_NOM,AccessBase.RESTAURANT_ADDR};
        to = new int[]{R.id.textViewListNom,R.id.textViewListAddr};

        cAdapter = new SimpleCursorAdapter(this,R.layout.list_item, null,
                from,to,0);

        lv.setAdapter(cAdapter);

        mCallbacks = this;

        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, mCallbacks);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                Intent affichageResto = new Intent(getApplicationContext(), AffichageResto.class);
                affichageResto.putExtra("addr",cursor.getString(2));
                startActivity(affichageResto);
            }
        });
    }

    //rafraichissement de l'activity quand on y revient.
    @Override
    public void onRestart()
    {
        super.onRestart();
        getLoaderManager().restartLoader(LOADER_ID,null,mCallbacks);
    }

    public void searchQuery(View v)
    {
        switch (String.valueOf(spinner.getSelectedItem())){
            case "nom":
                if(etSearch.getText().toString().trim().length() == 0)
                    Toast.makeText(this,"Veuillez rentrer un nom",Toast.LENGTH_SHORT).show();
                else
                    getLoaderManager().restartLoader(LOADER_ID_NOM,null,mCallbacks);
                break;
            case "note":
                if(etSearch.getText().toString().trim().length() == 0)
                    Toast.makeText(this,"Veuillez rentrer une note",Toast.LENGTH_SHORT).show();
                else
                    getLoaderManager().restartLoader(LOADER_ID_NOTE,null,mCallbacks);
                break;
            case "type cuisine":
                if(etSearch.getText().toString().trim().length() == 0)
                    Toast.makeText(this,"Veuillez rentrer un type de cuisine",Toast.LENGTH_SHORT).show();
                else
                    getLoaderManager().restartLoader(LOADER_ID_TYPE,null,mCallbacks);
                break;
            case "cout moyen":
                if(etSearch.getText().toString().trim().length() == 0)
                    Toast.makeText(this,"Veuillez rentrer un cout moyen",Toast.LENGTH_SHORT).show();
                else
                    getLoaderManager().restartLoader(LOADER_ID_COUT,null,mCallbacks);
                break;
            default:
                Toast.makeText(this,"ERROR on spinner",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_recharger) {
            getLoaderManager().restartLoader(LOADER_ID,null,mCallbacks);
        }
        else if (id == R.id.action_ajoutResto) {
            Intent iii = new Intent(this,FormulaireResto.class);
            startActivity(iii);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader=null;
        switch (id){
            case LOADER_ID:
                cursorLoader = new CursorLoader(MainActivity.this, AccessBase.CONTENT_URI_MINI,
                        PROJECTION, null, null, null);
                break;
            case LOADER_ID_NOM:
                cursorLoader = new CursorLoader(MainActivity.this, AccessBase.CONTENT_URI_NOM,
                        PROJECTION, null, new String[] {etSearch.getText().toString().trim()}, null);
                break;
            case LOADER_ID_NOTE:
                cursorLoader = new CursorLoader(MainActivity.this, AccessBase.CONTENT_URI_NOTE,
                        PROJECTION, null, new String[] {etSearch.getText().toString().trim()}, null);
                break;
            case LOADER_ID_TYPE:
                cursorLoader = new CursorLoader(MainActivity.this, AccessBase.CONTENT_URI_TYPE,
                        PROJECTION, null, new String[] {etSearch.getText().toString().trim()}, null);
                break;
            case LOADER_ID_COUT:
                cursorLoader = new CursorLoader(MainActivity.this, AccessBase.CONTENT_URI_COUT,
                        PROJECTION, null, new String[] {etSearch.getText().toString().trim()}, null);
                break;
            default:
                Log.e("CURSORLOADER","Error on CursorLoader id");
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {
            default:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                try{
                    cAdapter.swapCursor(cursor);
                }catch (IllegalArgumentException e)
                {
                    Log.e("ERROR_CURSOR_COMM","Error on column's name: " + e.getMessage());
                }
                break;
        }
        // The listview now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        cAdapter.swapCursor(null);
    }
}

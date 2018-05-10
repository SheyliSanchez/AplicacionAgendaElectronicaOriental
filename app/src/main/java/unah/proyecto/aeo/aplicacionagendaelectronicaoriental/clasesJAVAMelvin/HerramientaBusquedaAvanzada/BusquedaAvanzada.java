package unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.HerramientaBusquedaAvanzada;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.R;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAAlan.ActivityCategorias;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAAlan.Panel_de_Control;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.AdministracionDePerfilesAdmin.AdaptadorPersonalizadoSpinner;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.PerfilesBreves.AdaptadorPerfilBreve;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.AdministracionDePerfilesAdmin.ModeloSpinner;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.PerfilesBreves.ListaDeContactos;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.PerfilesBreves.PerfilBreve;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.AcercaDe;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.Login;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.PanelDeControlUsuarios;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.Sesion;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.SesionUsuario;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.provider.AEODbHelper;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.provider.CategoriasContract;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.provider.PerfilesContract;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.provider.RegionesContract;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.web.Perfil;

public class BusquedaAvanzada extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    /**********************************************************************************************
     *                                       DECLARACIÓN DE VARIABLES
     **********************************************************************************************/
    TextInputEditText contactoABuscar;
    Spinner categoria, region;
    ImageButton btnbusqueda;
    AEODbHelper aeoDbHelper;
    AdaptadorParaBusquedaAvanzada adaptadorPerfilBreve;
    Cursor cursorCategoria, cursorPerfiles, cursorRegion;
    ArrayList<PerfilBreve> listaOrganizaciones;
    RecyclerView contenedor;
    AdaptadorPersonalizadoSpinner adaptadorSpinerCategorias,adaptadorSpinerRegiones;
    ArrayList<ModeloSpinner> listaCategorias, listaRegiones;
    boolean unaRegionSeleccionada, unaCategoriaSeleccionada;
    int id_categoria, id_region;
    ArrayList<PerfilBreve> lista =new ArrayList<PerfilBreve>();

    //preferencias
    private Sesion sesion;
    private SesionUsuario sesionUsuario;
    int id_usu=-1;

    /**********************************************************************************************
     *                                      MÉTODO ONCREATE
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         /* *********************************************************
            SETEO DE LOS ELEMENTOS DE LA UI
        ********************************************************* */
        super.onCreate(savedInstanceState);

        //envio de clase actual para las preferencias
        sesion = new Sesion(this);
        sesionUsuario = new SesionUsuario(this);
        SharedPreferences preferences = getSharedPreferences("credencial", Context.MODE_PRIVATE);
        id_usu  = preferences.getInt("usuario_ingreso",id_usu);
        //

        setContentView(R.layout.activity_busqueda_avanzada);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (sesion.logindim() || sesionUsuario.logindimUsuario()){
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.inflateMenu(R.menu.menu_tercero);
            navigationView.setNavigationItemSelectedListener(this);
        }else {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.inflateMenu(R.menu.activity_principal_drawer);
            navigationView.setNavigationItemSelectedListener(this);
        }


        /* *********************************************************
            INICIALIZACION DE LOS COMPONENTES VISUALES A UTILIZAR
        ********************************************************* */

        contactoABuscar = findViewById(R.id.contacto_a_buscar);
        categoria = findViewById(R.id.spinercategoria);
        region = findViewById(R.id.spinerregionesbuscar);
        btnbusqueda = findViewById(R.id.boton_busqueda_avanzada);
        aeoDbHelper = new AEODbHelper(getApplicationContext());
        listaCategorias = new ArrayList<ModeloSpinner>();
        listaRegiones = new ArrayList<ModeloSpinner>();
        listaOrganizaciones = new ArrayList<PerfilBreve>();
        contenedor = (RecyclerView) findViewById(R.id.recyclerbusquedaAvanzada);
        contenedor.setHasFixedSize(true);
        LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext());
        layout.setOrientation(LinearLayoutManager.VERTICAL);
        contenedor.setLayoutManager(layout);

        /* *********************************************************
            LLAMADO AL MÉTODO QUE LLENA LAS LISTAS
        ********************************************************* */
        consultarBaseDeDatosParaArmarListas();

        /* *********************************************************
            INICIALIZACION DE LOS ADAPTADORES PASANDO LAS LISTA YA
            LLENAS
        ********************************************************* */
        adaptadorPerfilBreve = new AdaptadorParaBusquedaAvanzada(listaOrganizaciones);
        adaptadorSpinerCategorias = new AdaptadorPersonalizadoSpinner(this,R.layout.plantilla_spiners_personalizados_id_nombre,R.id.item_id_spinner,listaCategorias);
        adaptadorSpinerRegiones = new AdaptadorPersonalizadoSpinner(this,R.layout.plantilla_spiners_personalizados_id_nombre,R.id.item_id_spinner,listaRegiones);

        /* *********************************************************
            CONFIGURACION DE LOS ADAPTADORES PARA LOS COMPONENTES
        ********************************************************* */
        categoria.setAdapter(adaptadorSpinerCategorias);
        region.setAdapter(adaptadorSpinerRegiones);
        contenedor.setAdapter(adaptadorPerfilBreve);

        /* *********************************************************
            OBTIENE LOS ID'S DE LOS ELEMENTOS SELECCIONADOS EN LOS
            SPINNER
        ********************************************************* */

        for(int i=0; i < adaptadorSpinerCategorias.getCount(); i++) {
            if (id_categoria == adaptadorSpinerCategorias.getItem(i).getId()) {
                categoria.setSelection(i);
                break;
            }
        }

        for(int i=0; i < adaptadorSpinerRegiones.getCount(); i++) {
            if(id_region==adaptadorSpinerRegiones.getItem(i).getId()){
                region.setSelection(i);
                break;
            }
        }

        /* *******************************************************************************************
                            SETEO DE LISTENER'S
        ***************************************************************************************** */
        categoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_categoria = listaCategorias.get(position).getId();
                filtrosOffline();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                unaCategoriaSeleccionada=false;
            }
        });

        region.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_region = listaRegiones.get(position).getId();
                filtrosOffline();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                unaRegionSeleccionada=false;
            }
        });

        btnbusqueda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filtrosOffline();
                
            }
        });
    }

     /********************************************************************************************
                            METODO QUE CONSULTA A LA BASE DE DATOS Y LLENA LAS LISTAS
        ******************************************************************************************/

    public void consultarBaseDeDatosParaArmarListas(){
        listaCategorias.add(new ModeloSpinner("Todas las Categorias",-1000));
        listaRegiones.add(new ModeloSpinner("Todas las Regiones",-1000));
        SQLiteDatabase db=aeoDbHelper.getWritableDatabase();


        String[] perfil ={
                PerfilesContract.ContactosEntry.COLUMN_PERFILID,
                PerfilesContract.ContactosEntry.COLUMN_NOMBRE,
                PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO,
                PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR,
                PerfilesContract.ContactosEntry.COLUMN_NOMBRE_REGION,
                PerfilesContract.ContactosEntry.COLUMN_IMAGEN_PATH
        };

        String[] categorias ={
                CategoriasContract.CategoriasEntry.COLUMN_NOMBRE_CATEGORIA,
                CategoriasContract.CategoriasEntry.COLUMN_ID_CATEGORIA
        };

        String[] regiones ={
                RegionesContract.RegionesEntry.COLUMN_NOMBRE_REGION,
                RegionesContract.RegionesEntry.COLUMN_ID_REGION
        };

        cursorRegion = db.query(RegionesContract.RegionesEntry.TABLE_NAME,
                regiones,
                null,
                null,
                null,
                null,
                null);

        cursorPerfiles = db.query(PerfilesContract.ContactosEntry.TABLE_NAME,
                perfil,
                PerfilesContract.ContactosEntry.COLUMN_ESTADO+" = ?",
                new String[]{"2"},
                null,
                null,
                null);

        cursorCategoria=db.query(CategoriasContract.CategoriasEntry.TABLE_NAME,
                categorias,
                null,
                null,
                null,
                null,
                null);

        while (cursorRegion.moveToNext()){
            ModeloSpinner modeloSpinner = new
                    ModeloSpinner(cursorRegion.
                    getString(cursorRegion.
                            getColumnIndex(RegionesContract.
                                    RegionesEntry.
                                    COLUMN_NOMBRE_REGION)),
                    Integer.valueOf(cursorRegion.
                            getString(cursorRegion.
                                    getColumnIndex(RegionesContract.
                                            RegionesEntry.
                                            COLUMN_ID_REGION))));

            listaRegiones.add(modeloSpinner);
        }

        while (cursorCategoria.moveToNext()){
            ModeloSpinner modeloSpinner = new
                    ModeloSpinner(cursorCategoria.
                    getString(cursorCategoria.
                            getColumnIndex(CategoriasContract.
                                    CategoriasEntry.
                                    COLUMN_NOMBRE_CATEGORIA)),
                    Integer.valueOf(cursorCategoria.
                            getString(cursorCategoria.
                                    getColumnIndex(CategoriasContract.
                                            CategoriasEntry.
                                            COLUMN_ID_CATEGORIA))));
            listaCategorias.add(modeloSpinner);
        }

        while (cursorPerfiles.moveToNext()){
            PerfilBreve perfilBreve = new PerfilBreve();

            perfilBreve.setId(Integer.valueOf
                    (cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_PERFILID))));
            perfilBreve.setNombre(
                    cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NOMBRE)));
            perfilBreve.setDireccion(
                    cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NOMBRE_REGION)));
            perfilBreve.setImagen(
                    cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_IMAGEN_PATH)));

            if(!cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO)).isEmpty()){
                perfilBreve.setNumeroTelefono(
                        cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO)));
            }else {
                perfilBreve.setNumeroTelefono(
                        cursorPerfiles.getString(cursorPerfiles.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR)));
            }


            listaOrganizaciones.add(perfilBreve);

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent();
            setResult(ActivityCategorias.RESULT_CANCELED,intent);
            setResult(ListaDeContactos.RESULT_CANCELED,intent);
            super.onBackPressed();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==100 && resultCode==RESULT_OK){
            this.recreate();
        }else if (requestCode==200 && resultCode==RESULT_OK){
            this.recreate();
        }else if (requestCode==200 && resultCode==RESULT_CANCELED){
            this.recreate();
        }else if (requestCode==300 && resultCode==RESULT_CANCELED){
            this.recreate();
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.principaldos) {
            // Handle the camera action
            startActivity(new Intent(getBaseContext(), ActivityCategorias.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        } else if (id == R.id.acercadeinfodos) {
            Intent intent = new Intent(this,AcercaDe.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.login) {
            if (sesion.logindim()){
                Intent intent = new Intent(BusquedaAvanzada.this,Panel_de_Control.class);
                intent.putExtra("usuario_ingreso",id_usu);
                sesionUsuario.setLoginUsuario(false);
                startActivityForResult(intent,100);
                //startActivity(new Intent(ActivityCategorias.this,Panel_de_Control.class));
                //startActivity(intent);
                //finish();
            }else{
                if (sesionUsuario.logindimUsuario()){
                    Intent intent = new Intent(BusquedaAvanzada.this,PanelDeControlUsuarios.class);
                    intent.putExtra("id",id_usu);
                    sesion.setLogin(false);
                    startActivityForResult(intent,300);
                    //startActivity(new Intent(ActivityCategorias.this,PanelDeControlUsuarios.class));
                    //startActivity(intent);
                    //finish();
                }else {
                    Intent intent = new Intent(this, Login.class);
                    startActivityForResult(intent,100);
                    //finish();
                }

            }

        }else if (id == R.id.cerrarsecion){

            if (sesion.logindim()) {
                sesion.setLogin(false);
                Intent intent = new Intent(this,Login.class);
                startActivityForResult(intent,200);
                //startActivity(new Intent(this, Login.class));
                //finish();
            }else {
                if(sesionUsuario.logindimUsuario()){
                    sesionUsuario.setLoginUsuario(false);
                    Intent intent = new Intent(this,Login.class);
                    startActivityForResult(intent,200);
                    //startActivity(new Intent(this, Login.class));
                    //finish();
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /********************************************************************************************
                                    FILTROS PARA LA BÚSQUEDA AVANZADA
       ******************************************************************************************/
    public void filtrosOffline(){
        if(!contactoABuscar.getText().toString().isEmpty()){
            SQLiteDatabase db;
            ArrayList<PerfilBreve> lista =new ArrayList<PerfilBreve>();
            db=aeoDbHelper.getWritableDatabase();

            Cursor cursorBusqueda;
            String[] projectionPerfil ={
                    PerfilesContract.ContactosEntry.COLUMN_PERFILID,
                    PerfilesContract.ContactosEntry.COLUMN_NOMBRE,
                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO,
                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR,
                    PerfilesContract.ContactosEntry.COLUMN_NOMBRE_REGION,
                    PerfilesContract.ContactosEntry.COLUMN_IMAGEN_PATH
            };

            if(id_region==-1000){
                if(id_categoria==-1000){
                    cursorBusqueda =  db.query(PerfilesContract.ContactosEntry.TABLE_NAME,
                            projectionPerfil,
                            PerfilesContract.ContactosEntry.COLUMN_NOMBRE+ " like ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=?    or "+
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO +" like ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=?    or "+
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR+" like ?  and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=?",
                            new String[]{"%"+contactoABuscar.getText().toString()+"%","2",
                                    "%"+ contactoABuscar.getText().toString()+"%","2",
                                    "%"+contactoABuscar.getText().toString()+"%","2"},
                            null,
                            null,
                            null
                    );

                    recorrerCursor(cursorBusqueda,lista);

                    adaptadorPerfilBreve.setFilter(lista);
                }else if(id_categoria!=-1000){
                    cursorBusqueda = db.query(PerfilesContract.ContactosEntry.TABLE_NAME,
                            projectionPerfil,
                            PerfilesContract.ContactosEntry.COLUMN_NOMBRE + " like ? and id_categoria= ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=? or " +
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO + " like ?  and id_categoria= ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=? or " +
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR + " like ? and id_categoria= ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=?",
                            new String[]{"%" + contactoABuscar.getText().toString() + "%", "" + id_categoria, "2",
                                    "%" + contactoABuscar.getText().toString() + "%", "" + id_categoria,"2",
                                    "%" + contactoABuscar.getText().toString() + "%", "" + id_categoria,"2"},
                            null,
                            null,
                            null
                    );

                    recorrerCursor(cursorBusqueda,lista);

                    adaptadorPerfilBreve.setFilter(lista);
                }

            }else if(id_region!=-1000){

                if(id_categoria==-1000) {
                    cursorBusqueda =  db.query(PerfilesContract.ContactosEntry.TABLE_NAME,
                            projectionPerfil,
                            PerfilesContract.ContactosEntry.COLUMN_NOMBRE+ " like ? and "+PerfilesContract.ContactosEntry.COLUMN_ID_REGION+"= ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=? or "+
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO +" like ?  and "+PerfilesContract.ContactosEntry.COLUMN_ID_REGION+"= ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=? or " +
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR+" like ? and "+PerfilesContract.ContactosEntry.COLUMN_ID_REGION+"= ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=?",
                            new String[]{"%"+contactoABuscar.getText().toString()+"%", ""+id_region,"2",
                                    "%"+ contactoABuscar.getText().toString()+"%", ""+id_region,"2",
                                    "%"+contactoABuscar.getText().toString()+"%", ""+id_region,"2"},
                            null,
                            null,
                            null
                    );

                    recorrerCursor(cursorBusqueda,lista);

                    adaptadorPerfilBreve.setFilter(lista);
                }else if(id_categoria!=-1000){
                    cursorBusqueda = db.query(PerfilesContract.ContactosEntry.TABLE_NAME,
                            projectionPerfil,
                            PerfilesContract.ContactosEntry.COLUMN_NOMBRE + " like ? and " + PerfilesContract.ContactosEntry.COLUMN_ID_REGION + " = ? and " + PerfilesContract.ContactosEntry.COLUMN_CATEGORIA + " = ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=? or " +
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO + " like ? and " + PerfilesContract.ContactosEntry.COLUMN_ID_REGION + " = ? and " + PerfilesContract.ContactosEntry.COLUMN_CATEGORIA + " = ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=? or " +
                                    PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR + " like ? and " + PerfilesContract.ContactosEntry.COLUMN_ID_REGION + " = ? and " + PerfilesContract.ContactosEntry.COLUMN_CATEGORIA + " = ? and "+PerfilesContract.ContactosEntry.COLUMN_ESTADO+"=?",
                            new String[]{"%" + contactoABuscar.getText().toString() + "%", String.valueOf(id_region), String.valueOf(id_categoria),"2",
                                    "%" + contactoABuscar.getText().toString() + "%", String.valueOf(id_region), String.valueOf(id_categoria),"2",
                                    "%" + contactoABuscar.getText().toString() + "%", String.valueOf(id_region), String.valueOf(id_categoria),"2"},
                            null,
                            null,
                            null
                    );


                    recorrerCursor(cursorBusqueda,lista);
                    adaptadorPerfilBreve.setFilter(lista);
                }
            }

            db.close();
        }else{
            contactoABuscar.setError("No ha ingresado contacto a buscar");
        }

    }

     /********************************************************************************************
                        MÉTODO PARA RECORRER EL CURSOR SETEADO POR LOS FILTROS

        ******************************************************************************************/


    public void recorrerCursor(Cursor cursorBusqueda,ArrayList<PerfilBreve> arrayList) {
        while (cursorBusqueda.moveToNext()) {
            PerfilBreve perfilBreve = new PerfilBreve();

            perfilBreve.setId(Integer.valueOf
                    (cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_PERFILID))));
            perfilBreve.setNombre(
                    cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NOMBRE)));
            perfilBreve.setDireccion(
                    cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NOMBRE_REGION)));
            perfilBreve.setImagen(
                    cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_IMAGEN_PATH)));

            if (!cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO)).isEmpty()) {
                perfilBreve.setNumeroTelefono(
                        cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NUMERO_TELEFONO)));
            } else {
                perfilBreve.setNumeroTelefono(
                        cursorBusqueda.getString(cursorBusqueda.getColumnIndex(PerfilesContract.ContactosEntry.COLUMN_NUMERO_CELULAR)));
            }

            arrayList.add(perfilBreve);
        }

    }
}
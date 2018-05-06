package unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin.AdministracionDePerfilesAdmin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.R;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAAlan.ActivityCategorias;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAAlan.Panel_de_Control;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.AcercaDe;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.EditarUsuario;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.Login;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.PanelDeControlUsuarios;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.Sesion;
import unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAVirgilio.SesionUsuario;

public class AdministracionDePerfiles extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //Declaración de atributos
    ArrayList<Fuente_mostrarPerfiles> mostrar_perfiles;
    private ListView lista;
    private int perfilselecionado = -1;
    int id_contacto;
    String nombre_organizacion, imagen, usuariopropietario;
    public AdaptadorMostrarPerfiles adaptadorMostrarPerfiles;
    private  Object mActionMode;
    int idperf;
    ProgressBar barra;
    FloatingActionButton botonNuevoPerfil;
    private Sesion sesion;
    private SesionUsuario sesionUsuario;
    int id_usuario_resibido_usuario;
    int id_usu=-1;
    private static final int PASAR_A_EDITAR = 100;
    private static final int PASAR_A_NUEVO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administracion_de_perfiles);

        //envio de clase actual para las preferencias
        sesion = new Sesion(this);
        sesionUsuario = new SesionUsuario(this);
        SharedPreferences preferences = getSharedPreferences("credencial", Context.MODE_PRIVATE);
        id_usu  = preferences.getInt("usuario_ingreso",id_usu);
        //
        //inicialización de componentes gráficos
        barra = findViewById(R.id.progressBarPerfiles);
        lista = (ListView) findViewById(R.id.listviewperfiles);
        mostrar_perfiles= new ArrayList<Fuente_mostrarPerfiles>();

        //Ejecución de la clase AsyncTask llenarLista
        new llenarLista().execute();

        //Se establece el listener al mantener presionado un item del listview
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //se inicializa la variable con la posición del item seleccionado del listview
                perfilselecionado = position;
                Fuente_mostrarPerfiles per = mostrar_perfiles.get(perfilselecionado);
                Intent in = new Intent(getApplicationContext(),EditarPerfil.class);
                in.putExtra("id",per.getId());
                startActivityForResult(in,PASAR_A_EDITAR);
            }
        });


        //se establece el listener del boton agregar perfil
        botonNuevoPerfil = (FloatingActionButton) findViewById(R.id.botonNuevoPerfil);
        botonNuevoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent i= new Intent(getApplicationContext(),NuevoPerfil.class);
                startActivityForResult(i,PASAR_A_NUEVO);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PASAR_A_EDITAR && resultCode == RESULT_OK){

            mostrar_perfiles.clear();
            new llenarLista().execute();
            adaptadorMostrarPerfiles.notifyDataSetChanged();
        }else if(requestCode == PASAR_A_NUEVO && resultCode == RESULT_OK){
            mostrar_perfiles.clear();
            new llenarLista().execute();
            adaptadorMostrarPerfiles.notifyDataSetChanged();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_administracion_de_perfiles_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menusolicitudesnuevas) {
            Intent i = new Intent(getApplicationContext(),NuevasSolicitudes.class);
            //envia intent ala actividad editar perfil
            if (getIntent().getExtras()!=null){
                id_usuario_resibido_usuario  = getIntent().getExtras().getInt("usuario_ingreso");
                i.putExtra("id",id_usuario_resibido_usuario);
            }
            startActivity(i);
            finish();
        } else if (id == R.id.menusolicitudesrechazadas) {
            Intent i = new Intent(getApplicationContext(),SolicitudesRechazadas.class);
            //envia intent ala actividad editar perfil
            if (getIntent().getExtras()!=null){
                id_usuario_resibido_usuario  = getIntent().getExtras().getInt("usuario_ingreso");
                i.putExtra("id",id_usuario_resibido_usuario);
            }
            startActivity(i);
            finish();
        } else if (id == R.id.menuperfileliminados) {
            Intent i = new Intent(getApplicationContext(),PerfilesEliminados.class);
            //envia intent ala actividad editar perfil
            if (getIntent().getExtras()!=null){
                id_usuario_resibido_usuario  = getIntent().getExtras().getInt("usuario_ingreso");
                i.putExtra("id",id_usuario_resibido_usuario);
            }
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.principaldos) {
            startActivity(new Intent(getBaseContext(), ActivityCategorias.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();

        } else if (id == R.id.acercadeinfodos) {
            Intent intent = new Intent(this,AcercaDe.class);
            startActivity(intent);
            finish();

        }else if (id == R.id.login) {

            if (sesion.logindim()){
                Intent intent = new Intent(this,Panel_de_Control.class);
                intent.putExtra("usuario_ingreso",id_usu);
                //startActivity(new Intent(PanelDeControlUsuarios.this,Panel_de_Control.class));
                startActivity(intent);
                finish();
            }else{
                if (sesionUsuario.logindimUsuario()){
                    Intent intent = new Intent(this,PanelDeControlUsuarios.class);
                    intent.putExtra("id",id_usu);
                    //startActivity(new Intent(PanelDeControlUsuarios.this,PanelDeControlUsuarios.class));
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(this, Login.class);
                    startActivity(intent);
                    finish();
                }

            }

        }else if (id ==R.id.cerrarsecion){
            //cerrar secion y borrado de preferencias
            if (sesion.logindim()) {
                sesion.setLogin(false);
                startActivity(new Intent(this, Login.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }else {
                //cerrar secion y borrado de preferencias
                if(sesionUsuario.logindimUsuario()){
                    sesionUsuario.setLoginUsuario(false);
                    startActivity(new Intent(this, Login.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }
            }

        }else if (id == R.id.ediciondeCuenta){
            Intent intent = new Intent(this,EditarUsuario.class);
            if (getIntent().getExtras()!=null){
                    id_usuario_resibido_usuario  = getIntent().getExtras().getInt("usuario_ingreso");
                //id_usuario_resibido_usuario = getIntent().getExtras().getInt("id");
                intent.putExtra("id",id_usuario_resibido_usuario);
                startActivity(intent);
                finish();
            }else {
                Toast.makeText(getApplicationContext(),"Error en id de usuario",Toast.LENGTH_SHORT).show();
            }


        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //clase que se conecta al webservice y trae los registros solicitados
    private class llenarLista extends AsyncTask<String, Integer, Boolean> {
        private llenarLista(){}
        //variable booleana para controlar el resultado de las ejecuciones
        boolean resul = true;

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                //se conecta al webservice y almacena el resultado en un array tipo json
                JSONArray respJSON = new JSONArray(EntityUtils.toString(new DefaultHttpClient().execute(new HttpPost("http://aeo.web-hn.com/consultarPerfilesParaAdministracionPerfiles.php?id_estado=2")).getEntity()));
                //recorre el array para asignar los resultados a las variables
                for (int i = 0; i < respJSON.length(); i++) {
                    id_contacto = respJSON.getJSONObject(i).getInt("id_contacto");
                    nombre_organizacion = respJSON.getJSONObject(i).getString("nombre_organizacion");
                    imagen = respJSON.getJSONObject(i).getString("imagen");
                    usuariopropietario = respJSON.getJSONObject(i).getString("nombre_usuario");
                    mostrar_perfiles.add(new Fuente_mostrarPerfiles(id_contacto, nombre_organizacion,imagen, usuariopropietario));

                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                resul = false;
            }
            return resul;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //muestra el progreso en la barra de progreso
            barra.setProgress(values[0]);
        }

        protected void onPostExecute(Boolean result) {

            if (result) {
                //hace invisible la barra
                barra.setVisibility(View.INVISIBLE);
                //inicializa el adaptador
                adaptadorMostrarPerfiles = new AdaptadorMostrarPerfiles( mostrar_perfiles,getApplicationContext());
                //establece el adaptador al listview
                lista.setAdapter(adaptadorMostrarPerfiles);
                return;
            }else{
                //muestra mensaje si se produce un error al ejercutar la consulta al webservice
                Toast.makeText(getApplicationContext(), "Problemas de conexión", Toast.LENGTH_SHORT).show();
            }

        }


    }

    //metodo para eliminar un perfil

    public void removerperfil(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Perfil");
        String fmt= getResources().getString(R.string.eliminarPerfil);
        builder.setMessage(String.format(fmt,mostrar_perfiles.get(pos).getPerfil()));
        builder.setPositiveButton(R.string.eliminar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //  llama a la clase que borra el perfil de la base de datos remota


                new eliminarPerfil().execute();

            }
        });

        builder.setNegativeButton(R.string.canselar,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });
        builder.create().show();

    }

    //clase AsyncTask que se conecta al webservice que ejecuta la consulta para borrar el perfil

    private class eliminarPerfil extends AsyncTask<String, Integer, Boolean> {
        private eliminarPerfil(){}
        boolean resul = true;

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                //Se obtiene el id del perfil que se va a eliminar
                Fuente_mostrarPerfiles perf = mostrar_perfiles.get(perfilselecionado);
                idperf=perf.getId();
                //se ejecuta la consulta al webservice y se pasa el id del perfil seleccionado
                EntityUtils.toString(new DefaultHttpClient().execute(new HttpPost("http://aeo.web-hn.com/eliminarPerfil.php?id_contacto="+idperf)).getEntity());
                resul = true;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                resul = false;
            }
            return resul;

        }

        protected void onPostExecute(Boolean result) {

            if (resul) {
                Toast.makeText(getApplicationContext(),"Perfil Eliminado",Toast.LENGTH_SHORT).show();
                mostrar_perfiles.removeAll(mostrar_perfiles);
                new llenarLista().execute();
                adaptadorMostrarPerfiles.notifyDataSetChanged();
            }else {
                Toast.makeText(getApplicationContext(), "Problemas de conexión", Toast.LENGTH_SHORT).show();
            }
        }


    }


    /*

    private ActionMode.Callback amc = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.opciones,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId()== R.id.EliminarItem){
                removerperfil(perfilselecionado);
                mode.finish();

            }else if(item.getItemId()==R.id.EditarItem){
                Fuente_mostrarPerfiles per = mostrar_perfiles.get(perfilselecionado);
                Intent in = new Intent(getApplicationContext(),EditarPerfil.class);
                in.putExtra("id",per.getId());
                startActivity(in);
                mode.finish();
                finish();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };*/
}
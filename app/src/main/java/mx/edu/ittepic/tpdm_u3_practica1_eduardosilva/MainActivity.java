package mx.edu.ittepic.tpdm_u3_practica1_eduardosilva;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button pedido,cobrar,cvs,salir;
    List<Map> platillos,bebidas,comandas;
    DatabaseReference basedatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pedido=findViewById(R.id.pedido);
        cobrar=findViewById(R.id.cobrar);
        cvs=findViewById(R.id.cvs);
        salir=findViewById(R.id.salir);
        basedatos=FirebaseDatabase.getInstance().getReference();
        platillos=bebidas=comandas=new ArrayList<>();

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,LevantarPedido.class);
                startActivity(i);
            }
        });

        cobrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(MainActivity.this,Cobrar.class);
                startActivity(i);
            }
        });

        cvs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportar();
            }
        });
        basedatos.child("Platillo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(MainActivity.this, "No hay platillos agregados", Toast.LENGTH_LONG).show();
                    return;
                }
                for (final DataSnapshot otro:dataSnapshot.getChildren()){
                    basedatos.child("Platillo").child(otro.getKey()).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Platillo platillo = dataSnapshot.getValue(Platillo.class);
                            if(platillo!=null) {
                                Map<String, Object> datos = new HashMap<>();
                                datos.put("nombre", platillo.getNombre());
                                datos.put("precio", platillo.getPrecio());
                                datos.put("id", otro.getKey().toString());
                                platillos.add(datos);
                            }
                        }
                        public void onCancelled(@NonNull DatabaseError databaseError){

                        }


                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        basedatos.child("Bebida").addValueEventListener(new ValueEventListener(){
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                if (dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(MainActivity.this, "No hay bebidas", Toast.LENGTH_LONG).show();
                    return;
                }
                for (final DataSnapshot otro:dataSnapshot.getChildren()){
                    basedatos.child("Bebida").child(otro.getKey()).addValueEventListener(new ValueEventListener(){
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                            Bebida bebida=dataSnapshot.getValue(Bebida.class);
                            if (bebida!=null){
                                Map<String,Object> datos=new HashMap<>();
                                datos.put("nombre",bebida.getNombre());
                                datos.put("precio",bebida.getPrecio());
                                datos.put("id",otro.getKey());
                                bebidas.add(datos);
                            }
                        }
                        public void onCancelled(@NonNull DatabaseError databaseError){

                        }
                    });
                }
            }
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
        basedatos.child("Comanda").addValueEventListener(new ValueEventListener(){
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                if (dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(MainActivity.this, "No hay comandas agregadas", Toast.LENGTH_LONG).show();
                    return;
                }
                for (final DataSnapshot otro:dataSnapshot.getChildren()){
                    basedatos.child("Comanda").child(otro.getKey()).addValueEventListener(new ValueEventListener(){
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                            Comanda comanda=dataSnapshot.getValue(Comanda.class);
                            if (comanda!=null){
                                Map<String,Object> datos=new HashMap<>();
                                datos.put("fecha",comanda.getFecha());
                                datos.put("estatus",comanda.getEstatus());
                                datos.put("platillos",comanda.getPlatillos());
                                datos.put("bebidas",comanda.getBebidas());
                                datos.put("nomesa",comanda.getNomesa());
                                datos.put("total",comanda.getTotal());
                                datos.put("id",otro.getKey());
                            }
                        }
                        public void onCancelled(@NonNull DatabaseError databaseError){

                        }
                    });
                }
            }
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });

    }

    private void exportar() {
        String cadPlatillos="";
        for (Map<String,Object> datos:platillos){
            cadPlatillos+=datos.get("id")+","+datos.get("nombre")+","+datos.get("precio")+"\n";
        }
        String cadBebidas="";
        for (Map<String,Object> datos:bebidas){
            cadBebidas+=datos.get("id")+","+datos.get("nombre")+","+datos.get("precio")+"\n";
        }
        String cadComandas="";
        for (Map<String,Object>datos:comandas){
            cadComandas+=datos.get("id")+","+datos.get("nomesa")+","+datos.get("platillos")+","+datos.get("bebidas")+","+datos.get("total")+"\n";
        }
        try{
            OutputStreamWriter osw=new OutputStreamWriter(openFileOutput("platillos.csv",MODE_PRIVATE));
            OutputStreamWriter osw1=new OutputStreamWriter(openFileOutput("bebidas.csv",MODE_PRIVATE));
            OutputStreamWriter osw2=new OutputStreamWriter(openFileOutput("comandas.csv",MODE_PRIVATE));
            osw.write(cadPlatillos);
            osw1.write(cadBebidas);
            osw2.write(cadComandas);
            osw.close();
            osw1.close();
            osw2.close();
            Toast.makeText(this, "Exportado con Exito", Toast.LENGTH_LONG).show();
        }catch(Exception e){
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show();
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
        if (id == R.id.platillos) {
            Intent i=new Intent(MainActivity.this,Agregar_Bebida_Platillo.class);
            i.putExtra("tipo","Platillo");
            startActivity(i);
            return true;
        }
        if (id == R.id.bebidas) {
            Intent i =new Intent(MainActivity.this,Agregar_Bebida_Platillo.class);
            i.putExtra("tipo","Bebida");
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

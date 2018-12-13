package mx.edu.ittepic.tpdm_u3_practica1_eduardosilva;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cobrar extends AppCompatActivity {
    EditText noMesa,total;
    Button buscar, cobrar;
    ListView lista;
    List<Map> comandas;
    DatabaseReference basedatos;
    int encontrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobrar);

        noMesa =findViewById(R.id.noMesa);
        total=findViewById(R.id.total);
        buscar=findViewById(R.id.buscar);
        lista=findViewById(R.id.listaComanda);
        cobrar =findViewById(R.id.cobrar);
        comandas=new ArrayList<>();

        basedatos=FirebaseDatabase.getInstance().getReference();
        basedatos.child("Comanda").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(Cobrar.this,"No hay datos para mostrar",Toast.LENGTH_LONG).show();
                    return;
                }
                for (final DataSnapshot otro:dataSnapshot.getChildren()){
                    basedatos.child("Comanda").child(otro.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Comanda comanda=dataSnapshot.getValue(Comanda.class);
                            if (comanda!=null){
                                Map<String,Object> dato=new HashMap<>();
                                dato.put("fecha",comanda.getFecha());
                                dato.put("estatus",comanda.getEstatus());
                                dato.put("bebidas",comanda.getBebidas());
                                dato.put("nomesa",comanda.getNomesa());
                                dato.put("platillos",comanda.getPlatillos());
                                dato.put("total",comanda.getTotal());
                                dato.put("id",otro.getKey().toString());
                                comandas.add(dato);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numero = Integer.parseInt(noMesa.getText().toString());
                for (int i=0;i<comandas.size();i++) {
                    Map<String, Object> comandaActual = comandas.get(i);
                    if (comandaActual.get("estatus").toString().equals("pendiente") && Integer.parseInt(comandaActual.get("nomesa").toString())==numero) {
                        cargarDatosComanda(i);
                        encontrado=i;
                        return;
                    }
                }
                Toast.makeText(Cobrar.this,"No se encontraron comandas por pagar",Toast.LENGTH_LONG).show();
            }
        });

        cobrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> modificar=comandas.get(encontrado);
                modificar.put("estatus","pagado");
                String id=modificar.get("id").toString();
                modificar.remove("id");
                basedatos.child("Comanda").child(id).updateChildren(modificar);
                Toast.makeText(Cobrar.this,"Comanda Pagada",Toast.LENGTH_LONG).show();
                noMesa.setText("");
                lista.setAdapter(null);
                total.setText("");
            }
        });
    }

    private void cargarDatosComanda(int i) {
        Map<String,Object> comandaSeleccionada=comandas.get(i);
        List datos=new ArrayList();
        datos.add("No. Mesa: "+comandaSeleccionada.get("nomesa").toString());
        datos.add("Fecha: "+comandaSeleccionada.get("fecha").toString());
        datos.add("Estatus:  "+comandaSeleccionada.get("estatus").toString());
        String platillos[]=comandaSeleccionada.get("platillos").toString().split("&");
        datos.add(platillos[0]);
        for (int j=1;j<platillos.length;j++){
            String []data=platillos[j].split(",");
            Log.e("Error",data[0]);
            datos.add(data[1]+" x "+data[0]+ " -> $"+data[2]);
        }
        String bebidas[]=comandaSeleccionada.get("bebidas").toString().split("&");
        datos.add(bebidas[0]);
        for (int j=1;j<bebidas.length;j++){
            String []data=bebidas[j].split(",");
            datos.add(data[1]+" x "+data[0]+ " -> $"+data[2]);
        }
        total.setText(comandaSeleccionada.get("total").toString());
        ArrayAdapter<String> adaptador =new ArrayAdapter<>(Cobrar.this,android.R.layout.simple_list_item_1,datos);
        lista.setAdapter(adaptador);
    }
}

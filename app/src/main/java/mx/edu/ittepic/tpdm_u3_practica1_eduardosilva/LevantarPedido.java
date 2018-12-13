package mx.edu.ittepic.tpdm_u3_practica1_eduardosilva;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevantarPedido extends AppCompatActivity {
    Spinner spinPlatillos, spinBebidas;
    EditText noMesa, cantidadP, cantidadB;
    Button addBebida, addPlatillo,guardar;
    ListView lista;
    List<Map> platillos,bebidas,listaFinal;
    DatabaseReference basedatos;
    ArrayAdapter<String> adaptador;
    List elementos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levantar_pedido);

        spinBebidas =findViewById(R.id.spinBebidas);
        spinPlatillos =findViewById(R.id.spinPlatillos);
        noMesa =findViewById(R.id.noMesa);
        cantidadB =findViewById(R.id.cantidadB);
        cantidadP =findViewById(R.id.cantidadP);
        addBebida =findViewById(R.id.addBebida);
        addPlatillo =findViewById(R.id.addPlatillos);
        guardar=findViewById(R.id.guardarPedido);
        lista=findViewById(R.id.listaPedido);
        basedatos=FirebaseDatabase.getInstance().getReference();
        platillos=new ArrayList<>();
        bebidas=new ArrayList<>();
        elementos=new ArrayList();
        listaFinal=new ArrayList<>();
        adaptador=new ArrayAdapter<>(LevantarPedido.this,android.R.layout.simple_list_item_1,elementos);
        lista.setAdapter(adaptador);

        basedatos.child("Platillo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                consultaMenu("Platillo", dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        basedatos.child("Bebida").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                consultaMenu("Bebida",dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        addPlatillo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String platillo= spinPlatillos.getSelectedItem().toString();
                int cantidad=Integer.parseInt(cantidadP.getText().toString());
                elementos.add(platillo+"\n"+cantidad);
                adaptador.notifyDataSetChanged();
                Map<String,Object> dato=new HashMap<>();
                Map<String,Object> datos=new HashMap<>();
                datos.put("platillo",platillo);
                datos.put("cantidad",cantidad);
                float precio=Float.parseFloat(platillos.get(spinPlatillos.getSelectedItemPosition()).get("precio").toString());
                datos.put("total",precio*cantidad);
                dato.put("platillo",datos);
                listaFinal.add(dato);
                spinPlatillos.setSelection(0);
                cantidadP.setText("");
            }
        });

        addBebida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bebida= spinBebidas.getSelectedItem().toString();
                int cantidad=Integer.parseInt(cantidadB.getText().toString());
                elementos.add(bebida+"\n"+cantidad);
                adaptador.notifyDataSetChanged();
                Map<String,Object> dato=new HashMap<>();
                Map<String,Object> datos=new HashMap<>();
                datos.put("bebida",bebida);
                datos.put("cantidad",cantidad);
                float precio=Float.parseFloat(bebidas.get(spinBebidas.getSelectedItemPosition()).get("precio").toString());
                datos.put("total",precio*cantidad);
                dato.put("bebida",datos);
                listaFinal.add(dato);
                spinBebidas.setSelection(0);
                cantidadB.setText("");
            }
        });

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String []seleccion=elementos.get(position).toString().split("\n");
                AlertDialog.Builder alerta=new AlertDialog.Builder(LevantarPedido.this);
                alerta.setTitle("Detalle")
                        .setMessage("Desea eliminar "+seleccion[0]+" del pedido?")
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                elementos.remove(position);
                                adaptador.notifyDataSetChanged();
                                listaFinal.remove(position);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("NO",null).show();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object>dato=new HashMap<>();
                dato.put("fecha",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                dato.put("estatus","pendiente");
                dato.put("nomesa",Integer.parseInt(noMesa.getText().toString()));
                float total=0;
                String cadPlato,cadBebi;cadPlato="Platillos: ";cadBebi="Bebidas: ";
                for (int i=0;i<listaFinal.size();i++){
                    if (listaFinal.get(i).containsKey("bebida")) {
                        Map<String,Object> datos=(Map)listaFinal.get(i).get("bebida");
                        cadBebi+="&"+datos.get("bebida").toString()+",";
                        cadBebi+=datos.get("cantidad").toString()+",";
                        float tot=Float.parseFloat(datos.get("total").toString());
                        total+=tot;
                        cadBebi+=tot;

                    }
                    else if (listaFinal.get(i).containsKey("platillo")) {
                        Map<String,Object> datos=(Map)listaFinal.get(i).get("platillo");
                        Log.e("Entro a platillos",datos.get("platillo").toString());
                        cadPlato+="&"+datos.get("platillo").toString()+",";
                        cadPlato+=datos.get("cantidad").toString()+",";
                        float tot=Float.parseFloat(datos.get("total").toString());
                        total+=tot;
                        cadPlato+=tot;
                    }
                }
                dato.put("platillos",cadPlato);
                dato.put("bebidas",cadBebi);
                dato.put("total",total);
                basedatos.child("Comanda").push().setValue(dato);
                noMesa.setText("");
                cantidadB.setText("");
                cantidadP.setText("");
                spinBebidas.setSelection(0);
                spinPlatillos.setSelection(0);
                lista.setAdapter(null);
                Toast.makeText(LevantarPedido.this, "Pedido Guardado", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void consultaMenu(final String tPedido, DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount()<=0){
            Toast.makeText(LevantarPedido.this,"No hay datos que mostrar",Toast.LENGTH_LONG).show();
            return;
        }
        for (final DataSnapshot otro:dataSnapshot.getChildren()){
            basedatos.child(tPedido).child(otro.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (tPedido.equals("Platillo")){
                        Platillo platillo=dataSnapshot.getValue(Platillo.class);
                        if (platillo!=null){
                            Map<String,Object> xx=new HashMap<>();
                            xx.put("id",otro.getKey());
                            xx.put("nombre",platillo.getNombre());
                            xx.put("precio",platillo.getPrecio());
                            platillos.add(xx);
                            cargarSpinner(tPedido);
                        }
                    }
                    else if (tPedido.equals("Bebida")){
                        Bebida bebida=dataSnapshot.getValue(Bebida.class);
                        if (bebida!=null){
                            Map<String,Object> xx=new HashMap<>();
                            xx.put("id",otro.getKey());
                            xx.put("nombre",bebida.getNombre());
                            xx.put("precio",bebida.getPrecio());
                            bebidas.add(xx);
                            cargarSpinner(tPedido);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void cargarSpinner(String alimento) {
        if (alimento.equals("Platillo")){
            String []vector=new String[platillos.size()];
            for(int i=0;i< vector.length;i++){
                Map<String,Object> ww=new HashMap<>();
                ww=platillos.get(i);
                vector[i]=ww.get("nombre").toString();
            }
            ArrayAdapter<String> list1=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vector);
            spinPlatillos.setAdapter(list1);
        }
        else if (alimento.equals("Bebida")){
            String []vector=new String[bebidas.size()];
            for(int i=0;i< vector.length;i++){
                Map<String,Object> ww=new HashMap<>();
                ww=bebidas.get(i);
                vector[i]=ww.get("nombre").toString();
            }
            ArrayAdapter<String> list1=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vector);
            spinBebidas.setAdapter(list1);
        }
    }

    public void onStart(){
        super.onStart();
        cargarSpinner("Platillo");
        cargarSpinner("Bebida");
    }
}

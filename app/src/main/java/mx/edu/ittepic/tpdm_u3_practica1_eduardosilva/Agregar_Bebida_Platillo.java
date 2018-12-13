package mx.edu.ittepic.tpdm_u3_practica1_eduardosilva;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class Agregar_Bebida_Platillo extends AppCompatActivity {
    TextView titulo;
    Button insertar,eliminar;
    EditText nombre,precio;
    DatabaseReference basedatos;
    ListView lista;
    List<Map> alimentosLocal;
    String tipo;
    int posicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar__bebida__platillo);

        titulo=findViewById(R.id.tituloAlimento);
        insertar=findViewById(R.id.agregarplatillo);
        eliminar=findViewById(R.id.borrarplatillo);
        nombre=findViewById(R.id.nombreplatillo);
        precio=findViewById(R.id.precioplatillo);
        basedatos=FirebaseDatabase.getInstance().getReference();
        lista=findViewById(R.id.listaplatillos);

        tipo=getIntent().getExtras().get("tipo").toString();
        alimentosLocal=new ArrayList<>();
        titulo.setText(tipo);

        basedatos.child(tipo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                alimentosLocal=new ArrayList<>();
                if (dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(Agregar_Bebida_Platillo.this,"No hay nada en el Menu",Toast.LENGTH_SHORT).show();
                    return ;
                }
                for (final DataSnapshot otro:dataSnapshot.getChildren()){
                    basedatos.child(tipo).child(otro.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (tipo.equals("Platillo")){
                                Platillo platillo=dataSnapshot.getValue(Platillo.class);
                                if (platillo!=null){
                                    Map<String,Object> xx=new HashMap<>();
                                    xx.put("id",otro.getKey());
                                    xx.put("precio",Float.parseFloat(platillo.getPrecio()+""));
                                    xx.put("nombre",platillo.getNombre());
                                    alimentosLocal.add(xx);
                                    cargarMenu();
                                }
                            }
                            else if (tipo.equals("Bebida")){
                                Bebida bebida=dataSnapshot.getValue(Bebida.class);
                                if (bebida!=null){
                                    Map<String,Object> xx=new HashMap<>();
                                    xx.put("id",otro.getKey());
                                    xx.put("precio",bebida.getPrecio());
                                    xx.put("nombre",bebida.getNombre());
                                    alimentosLocal.add(xx);
                                    cargarMenu();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> data=new HashMap<>();
                data.put("nombre",nombre.getText().toString());
                data.put("precio",Float.parseFloat(precio.getText().toString()));
                basedatos.child(tipo).push().setValue(data);
                Toast.makeText(Agregar_Bebida_Platillo.this,"Guardado Correctamente",Toast.LENGTH_LONG);
                nombre.setText("");
                precio.setText("");
            }
        });
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,Object> datos=alimentosLocal.get(position);
                nombre.setText(datos.get("nombre").toString());
                precio.setText(datos.get("precio").toString());
                eliminar.setEnabled(true);
                insertar.setEnabled(false);
                posicion=position;
            }
        });
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object>datos=alimentosLocal.get(posicion);
                basedatos.child(tipo).child(datos.get("id").toString() ).removeValue();
                nombre.setText("");
                precio.setText("");
                Toast.makeText(Agregar_Bebida_Platillo.this, "Eliminado Correctamente", Toast.LENGTH_SHORT).show();
                cargarMenu();
            }
        });
    }

    private void cargarMenu() {
        String []vector=new String[alimentosLocal.size()];
        for(int i=0;i< vector.length;i++){
            Map<String,Object> ww=new HashMap<>();
            ww=alimentosLocal.get(i);
            vector[i]=ww.get("nombre").toString()+"\n"+Float.parseFloat(ww.get("precio").toString());
        }
        ArrayAdapter<String> list1=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vector);
        lista.setAdapter(list1);
    }
}

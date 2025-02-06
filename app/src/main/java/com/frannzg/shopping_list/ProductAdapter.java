package com.frannzg.shopping_list;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProductAdapter extends android.widget.BaseAdapter {

    private final Context context;
    private final List<Product> productList;
    private final DatabaseReference productRef;

    public ProductAdapter(Context context, List<Product> productList, String listId, String userId) {
        this.context = context;
        this.productList = productList;

        // Referencia a Firebase
        this.productRef = FirebaseDatabase.getInstance().getReference("shopping_list")
                .child(userId)
                .child(listId)
                .child("products");
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
            holder = new ViewHolder();
            holder.textViewName = convertView.findViewById(R.id.textViewProductName);
            holder.checkBoxBought = convertView.findViewById(R.id.checkBoxBought);
            holder.buttonDelete = convertView.findViewById(R.id.buttonDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = productList.get(position);

        // Configurar el texto y estado del producto
        holder.textViewName.setText(product.getName());
        holder.checkBoxBought.setOnCheckedChangeListener(null);
        holder.checkBoxBought.setChecked(product.isBought());

        // Actualizar el estado del producto en Firebase
        holder.checkBoxBought.setOnCheckedChangeListener((buttonView, isChecked) -> {
            product.setBought(isChecked);
            productRef.child(product.getId()).setValue(product)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Botón para eliminar producto
        holder.buttonDelete.setOnClickListener(v -> {
            productRef.child(product.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reloadList(); // Recargar lista tras eliminar
                } else {
                    Toast.makeText(context, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return convertView;
    }

    private void reloadList() {
        productRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                productList.clear();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                notifyDataSetChanged(); // Actualizar la vista
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(context, "Error al recargar la lista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ViewHolder {
        TextView textViewName;
        CheckBox checkBoxBought;
        ImageButton buttonDelete;
    }
}

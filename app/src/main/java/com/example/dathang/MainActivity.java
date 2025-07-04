package com.example.dathang;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String[] orderStatuses = {"Đang chế biến", "Đã phục vụ", "Đã thanh toán"};
    int selectedStatusIndex = 0;

    ArrayList<OrderActivity.OrderItem> orderList = new ArrayList<>();        // Danh sách gốc
    ArrayList<OrderActivity.OrderItem> filteredList = new ArrayList<>();     // Danh sách sau khi lọc
    OrderAdapter adapter;

    final int ORDER_REQUEST_CODE = 1001;
    final int EDIT_ORDER_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.order_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, filteredList);  // Adapter dùng danh sách đã lọc
        recyclerView.setAdapter(adapter);

        // Tìm kiếm đơn hàng
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterOrders(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterOrders(newText);
                return true;
            }
        });

        // Ban đầu hiển thị toàn bộ danh sách
        filterOrders("");
    }

    public void goToOrder(View view) {
        Intent intent = new Intent(this, OrderActivity.class);
        startActivityForResult(intent, ORDER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            OrderActivity.OrderItem order = (OrderActivity.OrderItem) data.getSerializableExtra("order");
            int position = data.getIntExtra("position", -1);

            if (requestCode == ORDER_REQUEST_CODE) {
                orderList.add(order);
            } else if (requestCode == EDIT_ORDER_REQUEST_CODE && position >= 0) {
                orderList.set(position, order);
            }

            // Cập nhật lại giao diện với danh sách mới
            filterOrders("");
        }
    }

    public void editOrderStatus(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa trạng thái đơn hàng");
        builder.setSingleChoiceItems(orderStatuses, selectedStatusIndex, (dialog, which) -> {
            selectedStatusIndex = which;
        });

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String newStatus = orderStatuses[selectedStatusIndex];
            OrderActivity.OrderItem item = filteredList.get(position);
            item.status = newStatus;
            adapter.notifyItemChanged(position);
            Toast.makeText(MainActivity.this, "Trạng thái mới: " + newStatus, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Huỷ", null);
        builder.show();
    }

    public void confirmDelete(int position) {
        OrderActivity.OrderItem itemToRemove = filteredList.get(position);
        orderList.remove(itemToRemove);
        filterOrders("");  // Cập nhật lại danh sách lọc

        Toast.makeText(MainActivity.this, "Đã huỷ đơn hàng", Toast.LENGTH_SHORT).show();
    }

    private void filterOrders(String keyword) {
        filteredList.clear();
        if (keyword.trim().isEmpty()) {
            filteredList.addAll(orderList);
        } else {
            for (OrderActivity.OrderItem order : orderList) {
                if (order.name.toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // ------------------ Adapter hiển thị đơn hàng ------------------
    public static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

        ArrayList<OrderActivity.OrderItem> orders;
        MainActivity context;

        public OrderAdapter(MainActivity context, ArrayList<OrderActivity.OrderItem> orders) {
            this.context = context;
            this.orders = orders;
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView summaryText, statusText;
            View editBtn, deleteBtn;

            public OrderViewHolder(View itemView) {
                super(itemView);
                summaryText = itemView.findViewById(R.id.order_summary_text);
                statusText = itemView.findViewById(R.id.order_status_text);
                editBtn = itemView.findViewById(R.id.edit_button);
                deleteBtn = itemView.findViewById(R.id.delete_button);
            }
        }

        @Override
        public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
            return new OrderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            OrderActivity.OrderItem item = orders.get(position);
            String summary = "Name: " + item.name +
                    " | Quantity: " + item.quantity +
                    " | Add Whipped Cream: " + item.hasWhippedCream +
                    " | Add Chocolate: " + item.hasChocolate +
                    " | $" + item.price;
            holder.summaryText.setText(summary);
            holder.statusText.setText("Trạng thái: " + item.status);

            holder.editBtn.setOnClickListener(v -> context.editOrderStatus(holder.getAdapterPosition()));
            holder.deleteBtn.setOnClickListener(v -> context.confirmDelete(holder.getAdapterPosition()));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderActivity.class);
                intent.putExtra("edit_order", item);
                intent.putExtra("position", context.orderList.indexOf(item));  // ← để update đúng vị trí trong danh sách gốc
                context.startActivityForResult(intent, context.EDIT_ORDER_REQUEST_CODE);
            });
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
    }
}

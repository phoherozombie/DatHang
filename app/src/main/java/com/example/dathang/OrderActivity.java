package com.example.dathang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    int quantity = 2;
    final int PRICE_OF_COFFEE = 4;
    final double PRICE_OF_WHIPPED_CREAM = 0.5;
    final int PRICE_OF_CHOCOLATE = 1;

    boolean hasOrderedOnce = false;
    OrderItem currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        displayQuantity(quantity);

        // 👇 Nhận dữ liệu nếu là chỉnh sửa đơn hàng
        Intent intent = getIntent();
        OrderItem editOrder = (OrderItem) intent.getSerializableExtra("edit_order");
        if (editOrder != null) {
            currentOrder = editOrder;
            hasOrderedOnce = true;
            quantity = currentOrder.quantity;
            displayQuantity(quantity);

            ((EditText) findViewById(R.id.name_edit_text)).setText(currentOrder.name);
            ((CheckBox) findViewById(R.id.whipped_cream_check_box)).setChecked(currentOrder.hasWhippedCream);
            ((CheckBox) findViewById(R.id.chocolate_check_box)).setChecked(currentOrder.hasChocolate);

            String summary = "Tên: " + currentOrder.name +
                    "\nSố lượng: " + currentOrder.quantity +
                    "\nWhipped Cream: " + (currentOrder.hasWhippedCream ? "Có" : "Không") +
                    "\nChocolate: " + (currentOrder.hasChocolate ? "Có" : "Không") +
                    "\nGiá: $" + currentOrder.price +
                    "\nTrạng thái: " + currentOrder.status;

            TextView summaryView = findViewById(R.id.order_summary_text_view);
            summaryView.setText(summary);
            summaryView.setVisibility(View.VISIBLE);
        }
    }

    public void increment(View view) {
        if (quantity >= 100) {
            Toast.makeText(this, "You cannot have more than 100 coffees", Toast.LENGTH_SHORT).show();
            return;
        }
        quantity++;
        displayQuantity(quantity);
    }

    public void decrement(View view) {
        if (quantity <= 1) {
            Toast.makeText(this, "You cannot have less than 1 coffee", Toast.LENGTH_SHORT).show();
            return;
        }
        quantity--;
        displayQuantity(quantity);
    }

    public void submitOrder(View view) {
        CheckBox whippedCreamCheckBox = findViewById(R.id.whipped_cream_check_box);
        CheckBox chocolateCheckBox = findViewById(R.id.chocolate_check_box);
        EditText nameEditText = findViewById(R.id.name_edit_text);
        TextView orderSummaryTextView = findViewById(R.id.order_summary_text_view);

        String nameInput = nameEditText.getText().toString().trim();
        if (nameInput.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasWhippedCream = whippedCreamCheckBox.isChecked();
        boolean hasChocolate = chocolateCheckBox.isChecked();
        int price = calculatePrice(hasWhippedCream, hasChocolate, quantity, PRICE_OF_COFFEE);

        currentOrder = new OrderItem(nameInput, hasWhippedCream, hasChocolate, quantity, price, currentOrder != null ? currentOrder.status : "Đang chế biến");

        if (!hasOrderedOnce) {
            String summary = "Tên: " + nameInput +
                    "\nSố lượng: " + quantity +
                    "\nWhipped Cream: " + (hasWhippedCream ? "Có" : "Không") +
                    "\nChocolate: " + (hasChocolate ? "Có" : "Không") +
                    "\nGiá: $" + price +
                    "\nTrạng thái: " + currentOrder.status;

            orderSummaryTextView.setText(summary);
            orderSummaryTextView.setVisibility(View.VISIBLE);

            hasOrderedOnce = true;
            Toast.makeText(this, "Xác nhận lại lần nữa để đặt hàng", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("order", currentOrder);

            int position = getIntent().getIntExtra("position", -1);
            resultIntent.putExtra("position", position);

            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    public void displayQuantity(int numberOfCoffees) {
        ((TextView) findViewById(R.id.qunaity_text_view)).setText(String.valueOf(numberOfCoffees));
    }

    private int calculatePrice(boolean addWhippedCream, boolean addChocolate, int quantity, int basePrice) {
        if (addWhippedCream) basePrice += PRICE_OF_WHIPPED_CREAM;
        if (addChocolate) basePrice += PRICE_OF_CHOCOLATE;
        return quantity * basePrice;
    }

    public static class OrderItem implements Serializable {
        String name;
        boolean hasWhippedCream;
        boolean hasChocolate;
        int quantity;
        int price;
        String status;

        public OrderItem(String name, boolean hasWhippedCream, boolean hasChocolate, int quantity, int price, String status) {
            this.name = name;
            this.hasWhippedCream = hasWhippedCream;
            this.hasChocolate = hasChocolate;
            this.quantity = quantity;
            this.price = price;
            this.status = status;
        }

        public OrderItem(String name, boolean hasWhippedCream, boolean hasChocolate, int quantity, int price) {
            this(name, hasWhippedCream, hasChocolate, quantity, price, "Đang chế biến");
        }
    }
}

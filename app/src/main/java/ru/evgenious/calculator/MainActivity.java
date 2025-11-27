package ru.evgenious.calculator;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private EditText display;
    private String currentInput = "0";
    private static final String KEY_CURRENT_INPUT = "current_input";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Убираем ActionBar только в горизонтальной ориентации
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        }

        // Восстановление состояния при повороте экрана
        if (savedInstanceState != null) {
            currentInput = savedInstanceState.getString(KEY_CURRENT_INPUT, "0");
        }

        initializeViews();
        setupNumberButtons();
        setupFunctionButtons();
        updateDisplay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_INPUT, currentInput);
    }

    private void initializeViews() {
        display = findViewById(R.id.display);
    }

    private void setupNumberButtons() {
        int[] numberButtonIds = {
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
                R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7,
                R.id.button_8, R.id.button_9, R.id.button_dot
        };

        for (int id : numberButtonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNumberButtonClick(((Button) v).getText().toString());
                }
            });
        }
    }

    private void setupFunctionButtons() {
        // Кнопка очистки
        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentInput = "0";
                updateDisplay();
            }
        });

        // Кнопка удаления последнего символа
        findViewById(R.id.button_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteButtonClick();
            }
        });

        // Операции (пока только отображние)
        int[] operationButtonIds = {
                R.id.button_plus, R.id.button_minus, R.id.button_multiply, R.id.button_divide
        };

        for (int id : operationButtonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperationButtonClick(((Button) v).getText().toString());
                }
            });
        }

        // Кнопка "=" (заглушка под будущий функционал в следующих ДЗ)
        findViewById(R.id.button_equals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ...
            }
        });
    }


    private void onNumberButtonClick(String digit) {
        if (currentInput.equals("0")) {
            if (digit.equals(".")) {
                currentInput = "0.";
            } else {
                currentInput = digit;
            }
        } else {
            // Проверка на повторение точки
            // (TODO: надо доработать чтобы можно было ставить точку в другом числе)
            if (digit.equals(".") && currentInput.contains(".")) {
                return;
            }
            currentInput += digit;
        }
        updateDisplay();
    }

    private void onOperationButtonClick(String operation) {
        // добавляем операцию к текущему вводу
        currentInput += " " + operation + " ";
        updateDisplay();
    }

    private void onDeleteButtonClick() {
        if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else {
            currentInput = "0";
        }
        updateDisplay();
    }

    private void updateDisplay() {
        display.setText(currentInput);
    }


}
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
    private String currentOperator = "";
    private double operand1 = 0;
    private boolean waitingForOperand = false;
    private boolean calculated = false;

    private static final String KEY_CURRENT_INPUT = "current_input";
    private static final String KEY_CURRENT_OPERATOR = "current_operator";
    private static final String KEY_OPERAND1 = "operand1";
    private static final String KEY_WAITING_FOR_OPERAND = "waiting_for_operand";
    private static final String KEY_CALCULATED = "calculated";

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
            currentOperator = savedInstanceState.getString(KEY_CURRENT_OPERATOR, "");
            operand1 = savedInstanceState.getDouble(KEY_OPERAND1, 0);
            waitingForOperand = savedInstanceState.getBoolean(KEY_WAITING_FOR_OPERAND, false);
            calculated = savedInstanceState.getBoolean(KEY_CALCULATED, false);
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
        outState.putString(KEY_CURRENT_OPERATOR, currentOperator);
        outState.putDouble(KEY_OPERAND1, operand1);
        outState.putBoolean(KEY_WAITING_FOR_OPERAND, waitingForOperand);
        outState.putBoolean(KEY_CALCULATED, calculated);
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
                onClearButtonClick();
            }
        });

        // Кнопка удаления последнего символа
        findViewById(R.id.button_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteButtonClick();
            }
        });

        // Операции
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

        // Кнопка "="
        findViewById(R.id.button_equals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqualsButtonClick();
            }
        });
    }

    private void onNumberButtonClick(String digit) {
        if (calculated) {
            currentInput = "0";
            calculated = false;
        }

        if (waitingForOperand) {
            currentInput = "0";
            waitingForOperand = false;
        }

        if (currentInput.equals("0")) {
            if (digit.equals(".")) {
                currentInput = "0.";
            } else {
                currentInput = digit;
            }
        } else {
            // Проверка на повторение точки
            if (digit.equals(".") && currentInput.contains(".")) {
                return;
            }
            currentInput += digit;
        }
        updateDisplay();
    }

    private void onOperationButtonClick(String operation) {
        if (waitingForOperand) {
            currentOperator = operation;
            return;
        }

        if (!currentOperator.isEmpty() && !waitingForOperand) {
            calculate();
        }

        try {
            operand1 = Double.parseDouble(currentInput);
            currentOperator = operation;
            waitingForOperand = true;
            calculated = false;
        } catch (NumberFormatException e) {
            currentInput = "Error";
            updateDisplay();
            resetCalculator();
        }
    }

    private void onEqualsButtonClick() {
        if (currentOperator.isEmpty() || waitingForOperand) {
            return;
        }

        calculate();
        currentOperator = "";
        calculated = true;
    }

    private void onClearButtonClick() {
        resetCalculator();
        currentInput = "0";
        updateDisplay();
    }

    private void onDeleteButtonClick() {
        if (calculated || waitingForOperand) {
            currentInput = "0";
            waitingForOperand = false;
            calculated = false;
        } else if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else {
            currentInput = "0";
        }
        updateDisplay();
    }

    private void calculate() {
        try {
            double operand2 = Double.parseDouble(currentInput);
            double result = 0;

            switch (currentOperator) {
                case "+":
                    result = operand1 + operand2;
                    break;
                case "-":
                    result = operand1 - operand2;
                    break;
                case "×":
                    result = operand1 * operand2;
                    break;
                case "/":
                    if (operand2 == 0) {
                        currentInput = "Деление на 0";
                        updateDisplay();
                        resetCalculator();
                        return;
                    }
                    result = operand1 / operand2;
                    break;
            }

            // Форматируем результат для отображения в текствью
            if (result == (long) result) {
                currentInput = String.valueOf((long) result);
            } else {
                currentInput = String.valueOf(result);
                // Убираем лишние нули в конце  дроби
                currentInput = currentInput.replaceAll("0*$", "").replaceAll("\\.$", "");
            }

            operand1 = result;
            waitingForOperand = true;
            updateDisplay();

        } catch (NumberFormatException e) {
            currentInput = "Ошибка";
            updateDisplay();
            resetCalculator();
        }
    }

    private void resetCalculator() {
        currentInput = "0";
        currentOperator = "";
        operand1 = 0;
        waitingForOperand = false;
        calculated = false;
    }

    private void updateDisplay() {
        display.setText(currentInput);
    }
}
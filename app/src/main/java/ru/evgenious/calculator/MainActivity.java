package ru.evgenious.calculator;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import ru.evgenious.calculator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String currentInput = "0"; // текущее значение в окне
    private String currentOperator = ""; // текущий оператор
    private double operand1 = 0; // первый операнд
    private boolean waitingForOperand = false; // флаг ждем ли оператор
    private boolean calculated = false; // посчитано?

    private static final String KEY_CURRENT_INPUT = "current_input";
    private static final String KEY_CURRENT_OPERATOR = "current_operator";
    private static final String KEY_OPERAND1 = "operand1";
    private static final String KEY_WAITING_FOR_OPERAND = "waiting_for_operand";
    private static final String KEY_CALCULATED = "calculated";

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Скрываем ActionBar в ландшафте
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } else {
            drawerToggle = new ActionBarDrawerToggle(
                    this,
                    binding.drawerLayout,
                    0,
                    0
            );
            binding.drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();

            // Включаем кнопку "гамбургер"
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

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

        // Обработка пунктов меню
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработка нажатия на кнопку гамбургер
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void initializeViews() {}

    private void setupNumberButtons() {
        int[] numberButtonIds = {
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
                R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7,
                R.id.button_8, R.id.button_9, R.id.button_dot
        };

        for (int id : numberButtonIds) {
            Button button = binding.getRoot().findViewById(id);
            button.setOnClickListener(v -> onNumberButtonClick(((Button) v).getText().toString()));
        }
    }

    private void setupFunctionButtons() {
        binding.buttonClear.setOnClickListener(v -> onClearButtonClick());
        binding.buttonDel.setOnClickListener(v -> onDeleteButtonClick());

        int[] operationButtonIds = {R.id.button_plus, R.id.button_minus, R.id.button_multiply, R.id.button_divide};
        for (int id : operationButtonIds) {
            Button button = binding.getRoot().findViewById(id);
            button.setOnClickListener(v -> onOperationButtonClick(((Button) v).getText().toString()));
        }

        binding.buttonEquals.setOnClickListener(v -> onEqualsButtonClick());
    }

    private void onNumberButtonClick(String digit) {
        if (calculated) { currentInput = "0"; calculated = false; }
        if (waitingForOperand) { currentInput = "0"; waitingForOperand = false; }

        if (currentInput.equals("0")) {
            currentInput = digit.equals(".") ? "0." : digit;
        } else {
            if (digit.equals(".") && currentInput.contains(".")) return;
            currentInput += digit;
        }
        updateDisplay();
    }

    private void onOperationButtonClick(String operation) {
        if (waitingForOperand) { currentOperator = operation; return; }
        if (!currentOperator.isEmpty()) calculate();

        try {
            operand1 = Double.parseDouble(currentInput);
            currentOperator = operation;
            waitingForOperand = true;
            calculated = false;
        } catch (NumberFormatException e) {
            currentInput = getString(R.string.error);
            updateDisplay();
            resetCalculator();
        }
    }

    private void onEqualsButtonClick() {
        if (currentOperator.isEmpty() || waitingForOperand) return;
        calculate();
        currentOperator = "";
        calculated = true;
    }

    private void onClearButtonClick() { resetCalculator(); currentInput = "0"; updateDisplay(); }

    private void onDeleteButtonClick() {
        if (calculated || waitingForOperand) { currentInput = "0"; waitingForOperand = false; calculated = false; }
        else if (currentInput.length() > 1) currentInput = currentInput.substring(0, currentInput.length() - 1);
        else currentInput = "0";
        updateDisplay();
    }

    private void calculate() {
        try {
            double operand2 = Double.parseDouble(currentInput);
            double result = 0;
            switch (currentOperator) {
                case "+": result = operand1 + operand2; break;
                case "-": result = operand1 - operand2; break;
                case "×": result = operand1 * operand2; break;
                case "/":
                    if (operand2 == 0) {
                        showDivisionByZeroMessage();
                        return;
                    }
                    result = operand1 / operand2;
                    break;
            }
            if (result == (long) result) currentInput = String.valueOf((long) result);
            else currentInput = String.valueOf(result).replaceAll("0*$", "").replaceAll("\\.$", "");
            operand1 = result;
            waitingForOperand = true;
            updateDisplay();
        } catch (NumberFormatException e) {
            currentInput = getString(R.string.error);
            updateDisplay();
            resetCalculator();
        }
    }
    // выводим всплывающее сообщение при попыттке делить на 0
    private void showDivisionByZeroMessage() {
        String previousInput = currentInput;
        String previousOperator = currentOperator;

        resetCalculator();
        currentInput = getString(R.string.uncknown);
        updateDisplay();

        // Показываем Snackbar
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(
                        binding.getRoot(),
                        R.string.devideBy0,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                );

        snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        snackbar.show();
    }

    private void resetCalculator() {
        currentInput = "0";
        currentOperator = "";
        operand1 = 0;
        waitingForOperand = false;
        calculated = false;
    }

    private void updateDisplay() { binding.display.setText(currentInput); }
}

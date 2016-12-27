package com.nathanko.calculator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    EditText num1_edittext;
    EditText num2_edittext;
    String input;
    TextView bufferField;
    TextView inputField;
    String buffer;
    Button add_button;
    Button mult_button;
    int op = -1; //0,1,2,3 -> + - * /
    boolean newInput;

    int lastOp;
    String lastInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bufferField = (TextView) findViewById(R.id.buffer_textview);
        inputField = (TextView) findViewById(R.id.input_textview);
        input = "";
        lastInput = "0";
        buffer = "";
        newInput = false;

        add_button = (Button) findViewById(R.id.add_button);
        mult_button = (Button) findViewById(R.id.mult_button);
    }

    public void evalNum(View v) {
        lastOp = -1;

        int num = -1;
        switch (v.getId()) {
            case R.id.numbutton_9:
                num++;
            case R.id.numbutton_8:
                num++;
            case R.id.numbutton_7:
                num++;
            case R.id.numbutton_6:
                num++;
            case R.id.numbutton_5:
                num++;
            case R.id.numbutton_4:
                num++;
            case R.id.numbutton_3:
                num++;
            case R.id.numbutton_2:
                num++;
            case R.id.numbutton_1:
                num++;
            case R.id.numbutton_0:
                num++;
                break;
            default:
                break;
        }

        if (newInput || input.length() == 0) {
            input = "";
            newInput = false;
        }

        if (num >= 0) {
            input += num;
        } else if (v.getId() == R.id.numbutton_dot && !input.contains(".")) {
            if (input.length() == 0) {
                input += "0";
            }
            input += ".";
        } else if (v.getId() == R.id.numbutton_neg) {
            if (input.contains("-")) {
                if (input.length() > 1) {
                    input = input.substring(1);
                } else {
                    input = "";
                }
            } else {
                input = "-" + input;
            }
        }


        bufferField.setText(buffer + " " + opToString());
        inputField.setText(input);

        showDebug();
    }
/*
    public void loadBuffer(String numstr) {
        if (numstr.length() > 0) {
            buffer = Double.parseDouble(numstr);
        }
    }*/

    public double toDouble(String str) {
        double num = 0;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid input.", Toast.LENGTH_SHORT).show();
        }
        return num;

    }

    public void opEval(View v) {

        if (input.length() > 0) {
            if (buffer.length() > 0) {
                eval(v);
            }
            buffer = input;
            input = "";
        }

        switch (v.getId()) {
            case R.id.add_button:
                op = 0;
                break;
            case R.id.sub_button:
                op = 1;
                break;
            case R.id.mult_button:
                op = 2;
                break;
            case R.id.div_button:
                op = 3;
                break;
            default:
                break;
        }
        bufferField.setText(buffer + " " + opToString());
        inputField.setText(input);
        showDebug();
    }

    public String opToString() {
        switch (op) {
            case 0:
                return "+";
            case 1:
                return "-";
            case 2:
                return "x";
            case 3:
                return "/";
            default:
                return "";
        }
    }

    public void eval(View v) {
        String ans;
        Log.v("DEBUG", "buffer.length() " + buffer + " " + buffer.length());
        if (buffer.length() == 0) {
            buffer = input;
            op = lastOp;
            input = lastInput;
        }
        showDebug();
        if (Double.isNaN(toDouble(buffer)) || Double.isNaN(toDouble(input))) {
            ans = "" + toDouble(buffer) / toDouble(input);
            Toast.makeText(this, "The quotient is indeterminate.", Toast.LENGTH_SHORT).show();
        } else if (Double.isInfinite(toDouble(buffer)) || Double.isInfinite(toDouble(input))) {
            ans = "" + toDouble(buffer) / toDouble(input);
            Toast.makeText(this, "The quotient is undefined.", Toast.LENGTH_SHORT).show();
        } else {
            ans = evaluate();
        }

        lastOp = op;
        lastInput = input;

        buffer = "";
        input = ans + "";
        op = -1;
        inputField.setText(input);
        newInput = true;
        bufferField.setText(buffer);
        showDebug();
    }

    public String evaluate() {
        BigDecimal ans = BigDecimal.valueOf(toDouble(buffer));
        if (buffer.length() > 0 && input.length() > 0) {
            switch (op) {
                case 0:
                    ans = BigDecimal.valueOf(toDouble(buffer)).add(BigDecimal.valueOf(toDouble(input)));
                    break;
                case 1:
                    ans = BigDecimal.valueOf(toDouble(buffer)).subtract(BigDecimal.valueOf(toDouble(input)));
                    break;
                case 2:
                    ans = BigDecimal.valueOf(toDouble(buffer)).multiply(BigDecimal.valueOf(toDouble(input)));
                    break;
                case 3:
                    if (toDouble(input) == 0) {
                        if (toDouble(buffer) == 0) {
                            Toast.makeText(this, "The quotient is indeterminate.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "The quotient is undefined.", Toast.LENGTH_SHORT).show();
                        }
                        return "" + toDouble(buffer) / toDouble(input);
                    } else {
                        ans = BigDecimal.valueOf(toDouble(buffer)).divide(BigDecimal.valueOf(toDouble(input)), 15, RoundingMode.HALF_UP);
                    }
                    break;
                default:
                    break;
            }
        }

        ans = round(ans, 11);
        ans = ans.stripTrailingZeros();

        if (toDouble(ans.toString())==0){
            return "0";
        }
        else {
            return ans.toString();
        }
    }

    public  BigDecimal round(BigDecimal num, int n) {

        int newScale = n - num.precision() + num.scale();
        return num.setScale(newScale, RoundingMode.HALF_UP);
    }

    public void clear(View v) {
        buffer = "";
        input = "";
        op = -1;
        bufferField.setText(buffer);
        inputField.setText(input);
    }

    public static String NUMBER = "com.nathanko.calculator.NUMBER";

    public void save(View v) {
        Intent viewNumber = new Intent(this, ViewNumber.class);
        viewNumber.putExtra(NUMBER, input);
        startActivity(viewNumber);
    }

    public void showDebug() {
        Log.v("DEBUG", "buffer " + buffer + "\top " + op + "\tinput " + input + "\tlastInput " + lastInput + "\tlastOp " + lastOp + "\tnewInput " + newInput);
    }

}
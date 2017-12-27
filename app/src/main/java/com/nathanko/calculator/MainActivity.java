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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    EditText num1_edittext;
    EditText num2_edittext;
    TextView bufferField;   // upper text field
    TextView inputField;    // lower text field, where current input is entered
    String inputText;       // text of inputField
    String bufferText;      // text of bufferField
    String lastInputText;   // previous lastInputText

//    Button add_button;
//    Button mult_button;

    int op = -1;    // current operator, 0: +, 1: -, 2: *, 3: /
    int lastOp;     // previous operator

    boolean overwriteInputNextPress; // whether to overwrite (vs. append to) the inputField on next button


    public static String NUMBER = "com.nathanko.calculator.NUMBER"; // where to store extra for inspectNum intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bufferField = findViewById(R.id.buffer_textview);
        inputField = findViewById(R.id.input_textview);
        inputText = "";
        lastInputText = "0";
        bufferText = "";
        overwriteInputNextPress = false;

//        add_button = (Button) findViewById(R.id.add_button);
//        mult_button = (Button) findViewById(R.id.mult_button);
    }

    // Number button is pressed
    public void pressNumBtn(View v) {
        lastOp = -1; //set to "null"

        int num = Integer.parseInt(((Button)v).getText().toString());

        if (overwriteInputNextPress || inputText.length() == 0) {
            //Overwrite inputText
            inputText = "";
            overwriteInputNextPress = false;
        }

        //Append digit pressed to inputText, update the inputField
        inputText += num;
        inputField.setText(inputText);

        Log.v("DEBUG", "pressNum");
        showDebug();
    }

    // Non-number button is pressed
    public void pressDotOrNegBtn(View v) {
        lastOp = -1; //set to "null"

        if (overwriteInputNextPress || inputText.length() == 0) {
            //Overwrite inputText
            inputText = "";
            overwriteInputNextPress = false;
        }

        if (v.getId() == R.id.numbutton_dot && !inputText.contains(".")) {
            //Decimal point button was pressed (if inputText already contains a decimal point, do nothing)
            if (inputText.length() == 0) {
                //Add leading zero before decimal point if necessary
                inputText += "0";
            }
            inputText += ".";
        } else if (v.getId() == R.id.numbutton_neg) {
            //Toggle the negative button in inputText
            if (inputText.contains("-")) {
                inputText = inputText.substring(1);
            } else {
                inputText = "-" + inputText;
            }
        }

        //Update the inputField
        inputField.setText(inputText);

        Log.v("DEBUG", "pressOther");
        showDebug();
    }

    public double toDouble(String str) {
        double num = 0;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid input.", Toast.LENGTH_SHORT).show();
        }
        return num;

    }

    public void pressOpBtn(View v) {

        if (inputText.length() > 0) {
            if (bufferText.length() > 0) {
                eval(v);
            }
            bufferText = inputText;
            inputText = "";
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

        bufferField.setText(bufferText + " " + opToString());
        inputField.setText(inputText);
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
        Log.v("DEBUG", "bufferText.length() " + bufferText + " " + bufferText.length());
        if (bufferText.length() == 0) {
            // If bufferText is empty and user presses the equals button,
            // apply the previous operation to the current inputText
            bufferText = inputText;
            op = lastOp;
            inputText = lastInputText;
        }
        showDebug();

        //If either operand (bufferText or inputText) is NaN or Inf, so is the answer
        if (Double.isNaN(toDouble(bufferText)) || Double.isNaN(toDouble(inputText))) {
            ans = String.valueOf(Double.NaN);
            Toast.makeText(this, "The quotient is indeterminate.", Toast.LENGTH_SHORT).show();
        } else if (Double.isInfinite(toDouble(bufferText)) || Double.isInfinite(toDouble(inputText))) {
            ans = String.valueOf(toDouble(bufferText) / toDouble(inputText));
            Toast.makeText(this, "The quotient is undefined.", Toast.LENGTH_SHORT).show();
        } else {
            ans = evaluate();
        }

        //set lastOp as op and lastInputText as inputText for next use
        lastOp = op;
        lastInputText = inputText;

        overwriteInputNextPress = true;

        bufferText = "";
        inputText = String.valueOf(ans);
        inputField.setText(inputText);
        bufferField.setText(bufferText);

        showDebug();
    }

    public String evaluate() {

        // apply op to bufferText and inputText
        BigDecimal ans = BigDecimal.valueOf(toDouble(bufferText));
        if (bufferText.length() > 0 && inputText.length() > 0) {
            switch (op) {
                case 0:
                    ans = BigDecimal.valueOf(toDouble(bufferText)).add(BigDecimal.valueOf(toDouble(inputText)));
                    break;
                case 1:
                    ans = BigDecimal.valueOf(toDouble(bufferText)).subtract(BigDecimal.valueOf(toDouble(inputText)));
                    break;
                case 2:
                    ans = BigDecimal.valueOf(toDouble(bufferText)).multiply(BigDecimal.valueOf(toDouble(inputText)));
                    break;
                case 3:
                    if (toDouble(inputText) == 0) {
                        if (toDouble(bufferText) == 0) {
                            Toast.makeText(this, "The quotient is indeterminate.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "The quotient is undefined.", Toast.LENGTH_SHORT).show();
                        }
                        return String.valueOf(toDouble(bufferText) / toDouble(inputText));
                    } else {
                        ans = BigDecimal.valueOf(toDouble(bufferText)).divide(BigDecimal.valueOf(toDouble(inputText)), 15, RoundingMode.HALF_UP);
                    }
                    break;
                default:
                    break;
            }
        }

        //Round ans to 11 significant figures
        ans = round(ans,11);
        ans = ans.stripTrailingZeros();

        if (toDouble(ans.toString())==0){
            return "0";
        }
        else {
            return ans.toString();
        }
    }

    //Round num to n significant figures
    public  BigDecimal round(BigDecimal num, int n) {
        int newScale = n - num.precision() + num.scale();
        return num.setScale(newScale, RoundingMode.HALF_UP);
    }


    public void inspectNum(View v) {
        Intent i = new Intent(this, ViewNumber.class);
        i.putExtra(NUMBER, inputText);
        startActivity(i);
    }

    public void showDebug() {
        Log.v("DEBUG", "bufferText " + bufferText + "\top " + op + "\tinputText " + inputText + "\tlastInputText " + lastInputText + "\tlastOp " + lastOp + "\toverwriteInputNextPress " + overwriteInputNextPress);
    }

}
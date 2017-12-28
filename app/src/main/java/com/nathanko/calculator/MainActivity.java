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

    int operator = -1;    // current operator, 0: +, 1: -, 2: *, 3: /
    int lastOperator;     // previous operator

    boolean overwriteInputNextPress; // whether to overwrite (vs. append to) the inputField on next button


    public static final String NUMBER = "com.nathanko.calculator.NUMBER"; // where to store extra for inspectNum intent
    final int SCALE = 7; //number of digits displayed in inputField

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

    }

    // Number button is pressed
    public void pressNumBtn(View v) {
        lastOperator = -1; //set to "null"

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
    }

    // Non-number button is pressed
    public void pressDotOrNegBtn(View v) {
        lastOperator = -1; //set to "null"

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
    }

    // Operator button is pressed
    public void pressOpBtn(View v) {

        if (inputText.length() > 0) {
            if (bufferText.length() > 0) {
                pressEqualsBtn(v);
            }
            bufferText = inputText;
            inputText = "";
        }

        switch (v.getId()) {
            case R.id.add_button:
                operator = 0;
                break;
            case R.id.sub_button:
                operator = 1;
                break;
            case R.id.mult_button:
                operator = 2;
                break;
            case R.id.div_button:
                operator = 3;
                break;
            default:
                break;
        }

        String display;
        try {
            //Round ans to 12 significant figures
            BigDecimal ans = BigDecimal.valueOf(toDouble(bufferText));
            BigDecimal ansDisplayed = ans.setScale(SCALE - ans.precision() + ans.scale(), RoundingMode.HALF_UP).stripTrailingZeros();
            if (ansDisplayed.compareTo(BigDecimal.ZERO) == 0) {
                display = "0";
            } else {
                try {
                    //strip trailing zeros after decimal point
                    display = String.valueOf(ansDisplayed.intValueExact()) + " " + opToString();
                }
                catch (Exception e){
                    display = ansDisplayed.stripTrailingZeros().toString() + " " + opToString();
                }
            }
        }
        catch (Exception e){
            display = bufferText;
        }
        bufferField.setText(display);


        inputField.setText(inputText);
    }

    // Equals sign is pressed
    public void pressEqualsBtn(View v) {
        BigDecimal ans = new BigDecimal("0");
        double arithError = Double.NaN; //Nan, +Inf, or -Inf

        Log.v("DEBUG", "bufferText.length() " + bufferText + " " + bufferText.length());
        if (bufferText.length() == 0) {
            // If bufferText is empty and user presses the equals button,
            // apply the previous operation to the current inputText
            bufferText = inputText;
            operator = lastOperator;
            inputText = lastInputText;
        }

        //If either operand (bufferText or inputText) is NaN or Inf, so is the answer
        if (Double.isNaN(toDouble(bufferText)) || Double.isNaN(toDouble(inputText))) {
            // any indeterminate operand means answer is also indeterminate
            ans = null; // NaN
            arithError = Double.NaN;
            Toast.makeText(this, "The quotient is indeterminate.", Toast.LENGTH_SHORT).show();
        } else if (Double.isInfinite(toDouble(bufferText)) || Double.isInfinite(toDouble(inputText))) {
            // any undefined operand means answer is also undefined
            ans = null; // +Inf or -Inf
            arithError = toDouble(bufferText)/toDouble(inputText);
            Toast.makeText(this, "The quotient is undefined.", Toast.LENGTH_SHORT).show();
        } else {
            // apply operator to bufferText and inputText
            if (bufferText.length() > 0 && inputText.length() > 0) {
                switch (operator) {
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
                            // denominator is zero
                            if (toDouble(bufferText) == 0) {
                                // numerator is also zero, so answer is indeterminate
                                Toast.makeText(this, "The quotient is indeterminate.", Toast.LENGTH_SHORT).show();
                            } else {
                                // numerator is not also zero, so answer is undefined
                                Toast.makeText(this, "The quotient is undefined.", Toast.LENGTH_SHORT).show();
                            }
                            ans = null; // either Nan, +Inf, or -Inf
                            arithError = toDouble(bufferText)/toDouble(inputText);
                        } else {
                            //Divide with 256 digits of precision
                            ans = BigDecimal.valueOf(toDouble(bufferText)).divide(BigDecimal.valueOf(toDouble(inputText)), 256, RoundingMode.HALF_UP);
                        }
                        break;
                    default:
                        ans = BigDecimal.valueOf(toDouble(bufferText));
                        break;
                }
            }
        }


        //set lastOperator as operator and lastInputText as inputText for next use
        lastOperator = operator;
        lastInputText = inputText;

        overwriteInputNextPress = true;

        bufferText = "";
        bufferField.setText(bufferText);

        if (ans == null){
            //NaN, +Inf, or -Inf
            inputText = arithError + "";
            inputField.setText(inputText);
        }
        else {
            inputText = ans.toString(); //Save accurate (unrounded) value

            //Round ans to 12 significant figures
            BigDecimal ansDisplayed = ans.setScale(SCALE - ans.precision() + ans.scale(), RoundingMode.HALF_UP);
            if (ansDisplayed.compareTo(BigDecimal.ZERO) == 0) {
                inputField.setText("0");
            } else {
                try {
                    //strip trailing zeros after decimal point
                    inputField.setText(String.valueOf(ansDisplayed.intValueExact()));
                }
                catch (Exception e){
                    inputField.setText(ansDisplayed.stripTrailingZeros().toString());
                }

            }
        }

        Log.v("Debug", inputField.getText().toString()+"\t"+inputText);

    }


    public void inspectNum(View v) {
        Intent i = new Intent(this, ViewNumber.class);
        i.putExtra(NUMBER, inputText);
        startActivity(i);
    }

    public double toDouble(String str) {
        double num = 0;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            //Toast.makeText(this, "Invalid input.", Toast.LENGTH_SHORT).show();
        }
        return num;
    }

    public String opToString() {
        switch (operator) {
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



}
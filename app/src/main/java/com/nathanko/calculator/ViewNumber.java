package com.nathanko.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ViewNumber extends AppCompatActivity {

    String numstr;
    double num;
    String isInt;
    String isEven;
    String isPrime;
    String factors = "1, ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_number);

        numstr = getIntent().getStringExtra(MainActivity.NUMBER);
        num = toDouble(numstr);
        TextView num_textview = (TextView) findViewById(R.id.num_textview);

        if (num == (int) num) {
            isInt = "yes";
        } else {
            isInt = "no";
        }

        if (isInt.equals("no")) {
            isEven = "n/a";
        } else if (((int) num) % 2 == 0) {
            isEven = "yes";
        } else {
            isEven = "no";
        }

        if (isInt.equals("no")) {
            isPrime = "n/a";
        } else if (isPrime((int) num)) {
            isPrime = "yes";
        } else {
            isPrime = "no";
        }

        ((TextView) findViewById(R.id.isInt_textview)).setText(isInt);
        ((TextView) findViewById(R.id.isEven_textview)).setText(isEven);
        ((TextView) findViewById(R.id.isPrime_textview)).setText(isPrime);
        if (isInt.equals("no")) {
            num_textview.setText(num + "");
            ((TextView) findViewById(R.id.even_textview)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.isEven_textview)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.prime_textview)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.isPrime_textview)).setVisibility(View.GONE);

        } else {
            num_textview.setText((int) num + "");
        }
        if (isPrime.equals("no")) {
            ((TextView) findViewById(R.id.thefactors_textview)).setText(factors);
        } else {
            ((TextView) findViewById(R.id.factors_textview)).setVisibility(View.GONE);
        }

    }


    public double toDouble(String str) {
        double num = 0;
        try {
            num = Double.parseDouble(str);
        } catch (IllegalStateException e) {
        } catch (NumberFormatException e) {
        }
        return num;

    }

    public boolean isPrime(int num) {
        boolean prime = true;

        if (num <= 1) {
            prime = false; //non-positives are not prime
        }

        for (int i = 2; i < Math.abs(num); i++) {
            if (num % i == 0) {
                prime = false; //not prime
                factors += i + ", ";
            }
        }
            factors += Math.abs(num);

        if (num == 1) {
            factors = "1";
        }

        if (num == 0) {
            factors = "";
        }

        return prime; //is prime
    }
}

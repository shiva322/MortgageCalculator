package com.example.group7.mortgage_calculator_lab2;


import android.content.ContentValues;
import android.database.Cursor;
import android.location.Address;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalculationFragment extends Fragment {
    public View calcView;
    public double radioTerms_value=15,monthly_amt,loan_amt, propPrice=0,dwnPmt=0,apr;
    public static int propId=0;
    public static boolean isNewRecord = false;

    public CalculationFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }


    public ViewPager mViewPager;
    MainActivity.SectionsPagerAdapter  pagerAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        calcView = inflater.inflate(R.layout.fragment_calculation, container, false);
        mViewPager = (ViewPager) container;
        pagerAdapter =  (MainActivity.SectionsPagerAdapter) mViewPager.getAdapter();

        propId = (int)getArguments().get("propId");
        isNewRecord =  (boolean) getArguments().get("isNewRecord");

        final EditText et_street = calcView.findViewById(R.id.street);
        final EditText et_city = calcView.findViewById(R.id.city);
        final EditText et_zipcode= calcView.findViewById(R.id.zipcode);
        final EditText et_propPrice = calcView.findViewById(R.id.propPrice);
        final EditText et_downpmt = calcView.findViewById(R.id.downPmt);
        final EditText et_apr = calcView.findViewById(R.id.apr);
        final TextView tv_monthly =  calcView.findViewById(R.id.monthly);
        final TextView tv_monthlytxt =  calcView.findViewById(R.id.monthlyTxt);
        final RadioGroup radioGroup_terms = calcView.findViewById(R.id.rg_terms);



        final Spinner spinner = (Spinner) calcView.findViewById(R.id.propType_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.proptype_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        final Spinner spinner1 = (Spinner) calcView.findViewById(R.id.state_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this.getContext(),
                R.array.state_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner1.setAdapter(adapter1);

        final Button button_new =  calcView.findViewById(R.id.button_new);
        button_new.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                et_street.setText("");
                et_propPrice.setError(null);

                et_city.setText("");
                et_city.setError(null);

                et_zipcode.setText("");
                et_zipcode.setError(null);

                et_propPrice.setText("");
                et_propPrice.setError(null);

                et_downpmt.setText("");
                et_downpmt.setError(null);

                et_apr.setText("");

                spinner.setSelection(0);
                spinner1.setSelection(0);

                tv_monthlytxt.setText("");
                tv_monthly.setText("");


                radioTerms_value=15;
                monthly_amt=0;
                loan_amt=0;
                propPrice=0;
                dwnPmt=0;
                propId=0;

                radioGroup_terms.check(R.id.radio_fifteen);

            }
        });


        radioGroup_terms.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if(checkedId==R.id.radio_thirty){
                    radioTerms_value=30;
                }
                else radioTerms_value=15;
            }
        });

        final Button button_save =  calcView.findViewById(R.id.button_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                et_propPrice.setError(null);
                et_apr.setError(null);
                if(TextUtils.isEmpty(et_street.getText().toString())){
                    et_street.setError("This field cannot be empty");
                }

                else if(TextUtils.isEmpty(et_city.getText().toString())){
                    et_city.setError("This field cannot be empty");
                }

                else if(!(TextUtils.isEmpty(et_street.getText().toString())) && !(TextUtils.isEmpty(et_city.getText().toString()))) {

                    List<Address> adr;
                    Geocoder coder = new Geocoder(getActivity().getApplicationContext());
                    String address = et_street.getText().toString() + ", " + et_city.getText().toString() + ", " +
                            spinner1.getSelectedItem().toString() + " "+ et_zipcode.getText().toString();
                    LatLng p = null;

                    try {
                        adr = coder.getFromLocationName(address,5);
                        Log.v("adr",adr.toString());
                        System.out.println(adr);
                        Address location = adr.get(0);
                        p = new LatLng(location.getLatitude(), location.getLongitude());

                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }

                    if(!calculate())
                        return;

                    if(p==null){
                        et_street.setError("Enter Proper Address");
                        et_city.setError("Enter Proper Address");
                    }

                    else if(propId>0){

                        et_street.setError(null);
                        et_city.setError(null);

                        SaveDataHelper mDbHelper = new SaveDataHelper(getActivity().getApplicationContext());
                        // Gets the data repository in write mode
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_PROPTYPE, spinner.getSelectedItem().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_STREET, et_street.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_CITY, et_city.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_STATE, spinner1.getSelectedItem().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_ZIPCODE, et_zipcode.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_PROPRICE, et_propPrice.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_DWNPMT, et_downpmt.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_TERMS,radioTerms_value);
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_LOANAMT, loan_amt);
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_APR, et_apr.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_MPMT, monthly_amt);

                        db.update(SaveDataContract.SaveDataEntry.TABLE_NAME, values, "_id="+propId,null);
                        tv_monthlytxt.setText(" Mortgage Calculation SAVED.");
                        tv_monthly.setText("");
                    }

                    else{
                        et_street.setError(null);
                        et_city.setError(null);

                        SaveDataHelper mDbHelper = new SaveDataHelper(getActivity().getApplicationContext());
                        // Gets the data repository in write mode
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_PROPTYPE, spinner.getSelectedItem().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_STREET, et_street.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_CITY, et_city.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_STATE, spinner1.getSelectedItem().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_ZIPCODE, et_zipcode.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_PROPRICE, et_propPrice.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_DWNPMT, et_downpmt.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_TERMS,radioTerms_value);
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_LOANAMT, loan_amt);
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_APR, et_apr.getText().toString());
                        values.put(SaveDataContract.SaveDataEntry.COLUMN_NAME_MPMT, monthly_amt);


                        // Insert the new row, returning the primary key value of the new row
                        long newRowId = db.insert(SaveDataContract.SaveDataEntry.TABLE_NAME, null, values);
                        if (newRowId != -1) {
                            pagerAdapter.isNewRecord = true;
                            Log.v("row", "Inserted");

                            pagerAdapter.notifyDataSetChanged();
                            Log.v("Info", "Maps fragment is notified");
                        } else
                            Log.v("row", "Not Inserted");

                    }}    }
        });

        final Button button_calc =  calcView.findViewById(R.id.button_calc);
        button_calc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button Calc
                et_street.setError(null);
                et_city.setError(null);
                if(calculate())
                {
                    tv_monthlytxt.setText("Your Monthly Payment is : ");
                    tv_monthly.setText(monthly_amt + "");
                }
                }
        });
        return calcView;
    }


    public boolean calculate()
    {
        final EditText et_propPrice = calcView.findViewById(R.id.propPrice);
        final EditText et_downpmt = calcView.findViewById(R.id.downPmt);
        final EditText et_apr = calcView.findViewById(R.id.apr);
        final TextView tv_monthly =  calcView.findViewById(R.id.monthly);

        if(TextUtils.isEmpty(et_propPrice.getText().toString())){
            et_propPrice.setError("This field cannot be empty");
            return false;
        }
        else {
            propPrice = Double.parseDouble(et_propPrice.getText().toString());
        }
        if(!(TextUtils.isEmpty(et_downpmt.getText().toString()))){
            dwnPmt = Double.parseDouble(et_downpmt.getText().toString());
        }
        if (dwnPmt > propPrice)

        {
            et_downpmt.setError("Down Payment cannot be greater than property price");
            return false;
        }

        if(TextUtils.isEmpty(et_apr.getText().toString())){
            et_apr.setError("This field cannot be empty");
            return false;

        }
        else {
            apr = Double.parseDouble(et_apr.getText().toString());
        }
        if(!(TextUtils.isEmpty(et_propPrice.getText().toString())) &&  !(TextUtils.isEmpty(et_apr.getText().toString())) && propPrice>=dwnPmt) {
            apr = apr / 1200;
            double terms = radioTerms_value * 12;
            Log.v("apr", apr + "");
            Log.v("xxx", Math.pow(1 + apr, terms) + " ");
            Log.v("thly", terms + "");

            loan_amt = propPrice - dwnPmt;

            monthly_amt = (loan_amt) * ((apr * (Math.pow(1 + apr, terms))) / (Math.pow(1 + apr, terms) - 1));

            monthly_amt = (double)Math.round(monthly_amt * 100d) / 100d;
            Log.v("monthly", monthly_amt + "");
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        final TextView tv_monthlyTxt =  calcView.findViewById(R.id.monthlyTxt);
        if(propId!=0) {
            SaveDataHelper mDbHelper = new SaveDataHelper(getActivity().getApplicationContext());
            SQLiteDatabase writableDb = mDbHelper.getWritableDatabase();
            Cursor cursorProperties = writableDb.
                    rawQuery(
                            "SELECT * FROM " + SaveDataContract.SaveDataEntry.TABLE_NAME + " WHERE _ID = ?", new String[]{String.valueOf(propId)});

            if (cursorProperties.moveToFirst()) {

                final EditText et_street = calcView.findViewById(R.id.street);
                final EditText et_city = calcView.findViewById(R.id.city);
                final EditText et_zipcode= calcView.findViewById(R.id.zipcode);
                final EditText et_propPrice = calcView.findViewById(R.id.propPrice);
                final EditText et_downpmt = calcView.findViewById(R.id.downPmt);
                final EditText et_apr = calcView.findViewById(R.id.apr);




                final Spinner spinner = (Spinner) calcView.findViewById(R.id.propType_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                        R.array.proptype_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                Log.v("sp",cursorProperties.getString(1));
                int spinnerPosition = adapter.getPosition(cursorProperties.getString(1));
                Log.v("pos",spinnerPosition+"");
                spinner.setSelection(spinnerPosition);


                final Spinner spinner1 = (Spinner) calcView.findViewById(R.id.state_spinner);
                ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this.getContext(),
                        R.array.state_array, android.R.layout.simple_spinner_item);
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner1.setAdapter(adapter1);
                int spinnerPosition1 = adapter1.getPosition(cursorProperties.getString(4));
                spinner1.setSelection(spinnerPosition1);

                final RadioButton rb1 = (RadioButton) calcView.findViewById(R.id.radio_fifteen);
                final RadioButton rb2 = (RadioButton) calcView.findViewById(R.id.radio_thirty);
                Log.v("terms",cursorProperties.getString(8));
                if(Double.parseDouble(cursorProperties.getString(8))==15.0){
                    rb1.setChecked(true);
                }
                else {
                    rb2.setChecked(true);
                }

                Log.v("curs",cursorProperties.getString(1));
                Log.v("curs",cursorProperties.getString(0));

                et_propPrice.setText("1");
                et_city.setText(cursorProperties.getString(3));
                et_street.setText(cursorProperties.getString(2));
                et_zipcode.setText(cursorProperties.getString(5));
                et_propPrice.setText(cursorProperties.getString(6));
                et_downpmt.setText(cursorProperties.getString(7));
                et_apr.setText(cursorProperties.getString(10));
                isNewRecord = false;
                //et_city.setText(cursorProperties.getString(3));


            }

        }
    else if(isNewRecord){
            tv_monthlyTxt.setText(" New Mortgage Calculation SAVED.");
        }

    }


}
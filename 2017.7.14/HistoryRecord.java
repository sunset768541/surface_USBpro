package com.example.datausb;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by sunset on 16/5/5.
 */
public class HistoryRecord extends android.app.Fragment {

        private Spinner spinner;
        private Spinner spinner2;
        private Spinner spinner3;
        private ListView listView;
        private HistoryThreeDimensionModel showdata;
        ArrayAdapter <String>adapter;

        String yea;
        String mon;
        String da;
        String [] datalist;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.historyrecord, container, false);

            return view;
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            showdata=new HistoryThreeDimensionModel();
            listView=(ListView)getActivity().findViewById(R.id.listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("点击了", datalist[position].toString());
                    String ss = DataWR.SDcardPath + yea + "/" + mon + "/" + da + "/" + datalist[position].toString();
                    Log.e("D",ss);
                    try {
                        DataRD.HAVE_READ_FINISEH=true;
                        while (DataRD.SHOW_DATA_THREAT_FLAG){
                            Log.e("show线程执行中","");
                        }
                       if (DataRD.iniReadDataFile(ss))
                        showdata.threadstart();
                        else  Toast.makeText(((Main) getActivity()).getApplication(), "读取数据文件损坏", Toast.LENGTH_SHORT).show();

                    }
                    catch (Exception e){
                        Log.e("读取失败",e.toString());
                    }
                    }
                }

                );
                spinner=(Spinner)

                getActivity()

                .

                findViewById(R.id.spinner);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

                {
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        yea = spinner.getSelectedItem().toString();
                        mon = spinner2.getSelectedItem().toString();
                        da = spinner3.getSelectedItem().toString();
                        datalist = DataWR.read(yea, mon, da, ((Main) getActivity()).getApplication());
                        String ss = null;
                        if (datalist != null) {
                            ss = datalist[1] + datalist[2] + datalist[datalist.length - 1];
                            adapter = new ArrayAdapter<String>(((Main) getActivity()).getApplication(), R.layout.lis, datalist);
                            listView.setAdapter(adapter);
                        } else {
                            Toast.makeText(((Main) getActivity()).getApplication(), "当前日期没有数据存储", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("ss", yea + "-" + mon + "-" + da + "-" + ss);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            spinner2=(Spinner)getActivity().findViewById(R.id.spinner2);
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    yea = spinner.getSelectedItem().toString();
                    mon = spinner2.getSelectedItem().toString();
                    da = spinner3.getSelectedItem().toString();
                    datalist = DataWR.read(yea, mon, da, ((Main) getActivity()).getApplication());
                    String ss = null;
                    if (datalist != null) {
                        ss = datalist[1] + datalist[2] + datalist[datalist.length - 1];
                        adapter = new ArrayAdapter<String>(((Main) getActivity()).getApplication(), R.layout.lis, datalist);
                        listView.setAdapter(adapter);
                    } else {
                        Toast.makeText(((Main) getActivity()).getApplication(), "当前日期没有数据存储", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("ss", yea + "-" + mon + "-" + da + "-" + ss);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinner3=(Spinner)getActivity().findViewById(R.id.spinner3);
            spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    yea = spinner.getSelectedItem().toString();
                    mon = spinner2.getSelectedItem().toString();
                    da = spinner3.getSelectedItem().toString();
                    datalist = DataWR.read(yea, mon, da, ((Main) getActivity()).getApplication());
                    String ss = null;
                    if (datalist != null) {
                        ss = datalist[1] + datalist[2] + datalist[datalist.length - 1];
                        adapter = new ArrayAdapter<String>(((Main) getActivity()).getApplication(), R.layout.lis, datalist);
                        listView.setAdapter(adapter);
                    } else {
                        Toast.makeText(((Main) getActivity()).getApplication(), "当前日期没有数据存储", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("ss", yea + "-" + mon + "-" + da + "-" + ss);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.showdataframe, showdata, "showdata");//datamodel为fragment的tag值，用来在数据处理线程中找到当前的fragment
            //transaction.addToBackStack(null);
            transaction.commit();

        }



}

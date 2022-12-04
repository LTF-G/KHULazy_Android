package com.example.khulazy.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.khulazy.LoginActivity;
import com.example.khulazy.MainActivity;
import com.example.khulazy.R;
import com.example.khulazy.RegisterActivity;
import com.example.khulazy.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DashboardFragment extends Fragment {
    private View view;
    private TextView username;
    private String accesstoken;
    private String refreshtoken;
    private String TAG = "dashboard";
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        username = view.findViewById(R.id.user_name);

        try {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            String usernames = sharedPreferences.getString("userid", "no name");
            accesstoken = sharedPreferences.getString("accessToken", "no token");
            refreshtoken = sharedPreferences.getString("refreshToken", "no token");
            username.setText(usernames + "님의");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String page = "https://43.201.130.48:8484/sleep";
                    URL url = new URL(page);

                    ignoreSsl();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    final StringBuilder sb = new StringBuilder();
                    if (conn != null) {

                        Log.d(TAG, "accesstoken: " + accesstoken);
                        Log.d(TAG, "refreshtoken: " + refreshtoken);

                        conn.setConnectTimeout(10000);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestProperty("authorization", "Bearer " + accesstoken);
                        conn.setRequestProperty("refresh", "Bearer " + refreshtoken);
                        conn.setDoInput(true);
                        conn.connect();

                        Log.d(TAG, "response: " + conn.getResponseCode());

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    conn.getInputStream(), "utf-8"
                            ));
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            // 버퍼리더 종료
                            br.close();
                            // 응답 Json 타입일 경우
                            JSONObject jsonObject = new JSONObject(sb.toString());
                            Log.i("tag", "확인 jsonArray : " + jsonObject.getString("message"));

                            String statistic = jsonObject.getString("sleepstats");
                            JSONObject sleepdata = new JSONObject(statistic);

                            // 여기서 데이터 저장해서 쓰시면 될듯합니다.
                            Log.d("TAG", "시작 시간: " + sleepdata.getString("sleep_start"));
                            Log.d("TAG", "종료 시간: " + sleepdata.getString("sleep_stop"));
                        }
                        else {
                        }
                    }

                } catch (Exception e) {
                    Log.i("tag", "error :" + e);
                }
            }
        });
        th.start();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public static void ignoreSsl () throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    private static void trustAllHttpsCertificates () throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    static class miTM implements TrustManager, X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }

}
//    // variable for our bar chart
//    BarChart barChart;
//
//    // variable for our bar data.
//    BarData barData;
//
//    // variable for our bar data set.
//    BarDataSet barDataSet;
//
//    // array list for storing entries.
//    ArrayList barEntriesArrayList;
//
//    // pie chart
//    PieChart pieChart;
//    int[] colorArray = new int[] {Color.LTGRAY, Color.GRAY};
//
//    private FragmentDashboardBinding binding;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        DashboardViewModel dashboardViewModel =
//                new ViewModelProvider(this).get(DashboardViewModel.class);
//
//        binding = FragmentDashboardBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        // initializing variable for bar chart.
//        barChart = root.findViewById(R.id.actual_sleep_time);
//
//        // calling method to get bar entries.
//        getBarEntries();
//
//        // creating a new bar data set.
//        barDataSet = new BarDataSet(barEntriesArrayList, "Geeks for Geeks");
//
//        // creating a new bar data and
//        // passing our bar data set.
//        barData = new BarData(barDataSet);
//
//        // below line is to set data
//        // to our bar chart.
//        barChart.setData(barData);
//
//        // adding color to our bar data set.
//        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
//
//        // setting text color.
//        barDataSet.setValueTextColor(Color.BLACK);
//
//        // setting text size
//        barDataSet.setValueTextSize(16f);
//        barChart.getDescription().setEnabled(false);
//        barChart.getAxisRight().setDrawGridLines(false);
//        barChart.getAxisLeft().setDrawGridLines(false);
//        barChart.getXAxis().setDrawGridLines(false);
//        barChart.getLegend().setEnabled(false);
//        barChart.getAxisRight().setDrawLabels(false);
//        barChart.getXAxis().setDrawLabels(false);
//        barChart.getAxisLeft().setDrawLabels(false);
//        barChart.getAxisRight().setDrawAxisLine(false);
//        barChart.getXAxis().setDrawAxisLine(false);
//        barChart.getAxisLeft().setDrawAxisLine(false);
//
//
//
//        pieChart = root.findViewById(R.id.achievement_rate);
//
//        PieDataSet pieDataSet = new PieDataSet(data1(),"수면 목표 달성률");
//        pieDataSet.setColors(colorArray);
//        PieData pieData = new PieData(pieDataSet);
//        pieChart.setDrawEntryLabels(true);
//        pieChart.setUsePercentValues(true);
//        pieData.setValueTextSize(0);
//        pieChart.setHoleRadius(60);
//        pieChart.setHoleColor(Color.parseColor("#EEF0F5"));
//        pieChart.setData(pieData);
//        pieChart.invalidate();
//        pieChart.getDescription().setEnabled(false);
//        pieChart.getLegend().setEnabled(false);
//        pieChart.setCenterText("12.5%");
//
//        return root;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//
//    private void getBarEntries() {
//        // creating a new array list
//        barEntriesArrayList = new ArrayList<>();
//
//        // adding new entry to our array list with bar
//        // entry and passing x and y axis value to it.
//        barEntriesArrayList.add(new BarEntry(1f, 4));
//        barEntriesArrayList.add(new BarEntry(2f, 3));
//        barEntriesArrayList.add(new BarEntry(3f, 1));
//        barEntriesArrayList.add(new BarEntry(4f, 2));
//        barEntriesArrayList.add(new BarEntry(5f, 4));
//        barEntriesArrayList.add(new BarEntry(6f, 1));
//    }
//
//    private ArrayList<PieEntry> data1() {
//        ArrayList<PieEntry> datavalue = new ArrayList<>();
//
//        datavalue.add(new PieEntry(7));
//        datavalue.add(new PieEntry(1));
//
//
//        return datavalue;
//    }
//}

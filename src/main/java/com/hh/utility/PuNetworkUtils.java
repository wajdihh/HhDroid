package com.hh.utility;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Wajdi Hh on 23/07/2015.
 * wajdihh@gmail.com
 */
public class PuNetworkUtils {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;



    /**
     * Permet de verifier la connexion tablette est elle en 4 G ou non
     * @param pContext
     * @return
     */
    public static boolean isConnectedWithData(Context pContext) {
        ConnectivityManager connMgr = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobile.isConnectedOrConnecting ();
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = PuNetworkUtils.getConnectivityStatus(context);
        String status = null;
        if (conn == PuNetworkUtils.TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == PuNetworkUtils.TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == PuNetworkUtils.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }



    /**
     * permet de tester si la tablette est connectée à internet
     *
     * @param pContext
     * @return
     */
    public static boolean isConnectedToInternet(Context pContext) {

        // Tester si le device est connecté
        ConnectivityManager cm = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (cm.getActiveNetworkInfo() == null || !netInfo.isConnectedOrConnecting())
            return false;

        //Tester si la connexion Internet est disponible
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * permet de tester si la tablette est connectée avec Wifif , 3G etc..
     *
     * @param pContext
     * @return
     */
    public static boolean isConnected(Context pContext) {

        ConnectivityManager cm =
                (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    /**
     * Permet de verifier si le GPS est activé
     * @param pContext
     * @return
     */
    public static boolean isGpsEnabled(Context pContext){
        LocationManager locationManager = (LocationManager) pContext.getSystemService(Context.LOCATION_SERVICE);
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String getAddressFromLoc(Context pContext,double latitude,double longitude) throws IOException {

        Geocoder geocoder = new Geocoder(pContext, Locale.getDefault());
        List<Address> addresses  = geocoder.getFromLocation(latitude,longitude, 1);

        String address = "";
        if(addresses.get(0)!=null)
            address=addresses.get(0).getAddressLine(0);

        String city = addresses.get(0).getLocality();
        String zip = addresses.get(0).getPostalCode();
        String country = addresses.get(0).getCountryName();

        address=(address==null)?"":address;
        city=(city==null)?"":city;
        zip=(zip==null)?"":zip;
        country=(country==null)?"":country;

        return  address+" "+city+" "+zip+" "+country;
    }

}
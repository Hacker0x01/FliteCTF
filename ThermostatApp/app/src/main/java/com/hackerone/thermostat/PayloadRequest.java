package com.hackerone.thermostat;

import android.app.Activity;
import android.util.Base64;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PayloadRequest extends Request<String> {
    private final Response.Listener<String> mListener;
    private final HashMap<String, String> mParams;

    public PayloadRequest(JSONObject payload, Response.Listener<String> listener) throws Exception {
        super(Method.POST, "http://35.243.186.41/", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onResponse("Connection failed");
            }
        });
        mListener = listener;
        mParams = new HashMap<>();
        mParams.put("d", buildPayload(payload));
    }

    @Override
    public Map<String, String> getParams() {
        return mParams;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String bdata = new String(response.data);
            byte[] ivcdata = Base64.decode(bdata, Base64.DEFAULT);
            byte[] iv = new byte[16];
            System.arraycopy(ivcdata, 0, iv, 0, 16);
            byte[] cdata = new byte[ivcdata.length - 16];
            System.arraycopy(ivcdata, 16, cdata, 0, ivcdata.length - 16);

            byte[] key = {0x38, 0x4f, 0x2e, 0x6a, 0x1a, 0x05, (byte) 0xe5, 0x22, 0x3b, (byte) 0x80, (byte) 0xe9, 0x60, (byte) 0xa0, (byte) 0xa6, 0x50, 0x74};
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, ks, ivParameterSpec);

            byte[] data = cipher.doFinal(cdata);
            String pstr = new String(data);
            JSONObject payload = new JSONObject(pstr);

            if(payload.getBoolean("success"))
                return Response.success(null, getCacheEntry());

            return Response.success(payload.getString("error"), getCacheEntry());
        } catch(Exception e) {
            return Response.success("Unknown", getCacheEntry());
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    private String buildPayload(JSONObject payload) throws Exception {
        byte[] key = { 0x38, 0x4f, 0x2e, 0x6a, 0x1a, 0x05, (byte) 0xe5, 0x22, 0x3b, (byte) 0x80, (byte) 0xe9, 0x60, (byte) 0xa0, (byte) 0xa6, 0x50, 0x74 };
        SecretKeySpec ks = new SecretKeySpec(key, "AES");
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, ks, ivParameterSpec);

        byte[] cdata = cipher.doFinal(payload.toString().getBytes());
        byte[] ivcdata = new byte[cdata.length + 16];
        System.arraycopy(iv, 0, ivcdata, 0, 16);
        System.arraycopy(cdata, 0, ivcdata, 16, cdata.length);
        return Base64.encodeToString(ivcdata, Base64.DEFAULT);
    }
}

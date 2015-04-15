package com.ponkotuy.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class MFGKeyStore {
    private static final String DefaultTrustStoreFile = "myfleetgirls.keystore";
    private static final String DefaultTrustStorePass = "myfleetgirls";

    private SSLContext sslContext;

    public MFGKeyStore() throws IOException, GeneralSecurityException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        String ts_file = System.getProperty("com.ponkotuy.http.trustStoreFile", DefaultTrustStoreFile);
        String ts_pass = System.getProperty("com.ponkotuy.http.trustStorePass", DefaultTrustStorePass);
        try {
            InputStream io = new FileInputStream(ts_file);
            trustStore.load(io, ts_pass.toCharArray());
        } catch (FileNotFoundException e) {
            try {
                ClassLoader cl = getClass().getClassLoader();
                File file = new File(cl.getResource(DefaultTrustStoreFile).getFile());
                InputStream io = new FileInputStream(file);
                trustStore.load(io, DefaultTrustStorePass.toCharArray());
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);

        sslContext = SSLContext.getInstance("TLSv1");
        sslContext.init(null, tmf.getTrustManagers(), null);
    }

    public SSLContext getSslContext() {
        return sslContext;
    }
}

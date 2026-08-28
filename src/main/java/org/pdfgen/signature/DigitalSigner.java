package org.pdfgen.signature;

import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Security;
import java.util.Calendar;

// currently in-development

public class DigitalSigner {

    static {
        Security.addProvider(new BouncyCastleProvider()); // Init BouncyCastle to the JCA
    }

    private KeyStore activeKeyStore;
    private PDSignature signature;

    @Setter private PDDocument document;

    private static KeyStore getSystemKeyStore() throws Exception {
        String keyStorePathStored = System.getProperty("javax.net.ssl.keyStore");

        if (keyStorePathStored == null) {
            throw new RuntimeException("javax.net.ssl.keyStore is null");
        }

        Path keyStorePath = Path.of(keyStorePathStored);

        KeyStore defaultKeyStore = KeyStore.getInstance("PKCS12", "BC");
        defaultKeyStore.load(null, null);

        InputStream FileStream = Files.newInputStream(keyStorePath);
        String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

        defaultKeyStore.load(FileStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());

        return defaultKeyStore;
    }

    /**
     *  Digital signature implementation
     *
     */
    public DigitalSigner() {
        try {
            this.activeKeyStore = getSystemKeyStore();
            PDSignature signature = new PDSignature();

            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

            // signature.setName("");
            // signature.setLocation("");

            // the signing date, needed for valid signature
            signature.setSignDate(Calendar.getInstance());


            this.signature = signature;
        } catch(Exception e) {
            System.out.println("Could not retreieve Keystore: " + e);
        }
    }



}

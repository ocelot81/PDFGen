package org.pdfgen.signature;

import lombok.NonNull;
import lombok.Setter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

public class DigitalSigner {

    static {
        Security.addProvider(new BouncyCastleProvider()); // Init BouncyCastle to the JCA
    }

    private final KeyStore activeKeyStore;
    private final String keyStorePassword;
    private final PDSignature baseSignature;
    private final OutputStream outputFileStream;
    private final PDDocument inputDocument;
    private CMSSignedDataGenerator CMSGenerator;

    @Setter @NonNull private String alias;

    /**
     * Obtains the keyStore container under javax.net.ssl.keyStore
     *
     * @throws Exception Different exceptions such as no container path was set or password is null
     */
    private KeyStore getSystemKeyStore() throws Exception {
        String keyStorePathStored = System.getProperty("javax.net.ssl.keyStore");

        if (keyStorePathStored == null) {
            throw new RuntimeException("javax.net.ssl.keyStore is null!");
        } else if (keyStorePassword == null) {
            throw new RuntimeException("keyStorePassword is unassigned");
        }

        Path keyStorePath = Path.of(keyStorePathStored);
        KeyStore keyStoreContainer = KeyStore.getInstance("PKCS12", "BC");

        InputStream userKeystoreData = Files.newInputStream(keyStorePath);
        keyStoreContainer.load(userKeystoreData, this.keyStorePassword.toCharArray());

        return keyStoreContainer;
    }

    /**
     * Obtains the keyStore password under javax.net.ssl.keyStorePassword
     *
     * @throws RuntimeException no password was set (is null)
     */
    private static String getSystemKeystorePassword() throws RuntimeException {
        String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

        if (keyStorePassword == null) {
            throw new RuntimeException("javax.net.ssl.keyStorePassword is null!");
        }
        return keyStorePassword;
    }

    /**
     * Obtains the certificate chain using previously obtained <b>javax.ssl.net</b> keystore attributes:
     *
     * @throws KeyStoreException Error during obtaining cert chain
     */
    private Certificate[] getCertificateChain() throws KeyStoreException {
        return this.activeKeyStore.getCertificateChain(this.alias);
    }


    /**
     * Initializes BouncyCastle CMS generation and required signing dependencies.

     * Uses this.alias, this.keyStorePassword, BouncyCastle provider
     * @throws Exception Any exception which occured during the crypto init.
     */
    private void prepareForSigning() throws Exception {
        Certificate[] certificateChain = getCertificateChain();

        if (certificateChain == null) {
            throw new IllegalStateException("Certificate chain unreachable in ssl.net keystore (Incorrect alias used/Certificate missing)");
        }
        JcaCertStore certificateStore = new JcaCertStore(Arrays.asList(certificateChain));

        CMSSignedDataGenerator CMSgenerator = new CMSSignedDataGenerator();

        // all certificates are added so the verifier can verify a path to a trusted CA
        CMSgenerator.addCertificates(certificateStore);

        PrivateKey privateKey = (PrivateKey) activeKeyStore.getKey(this.alias, this.keyStorePassword.toCharArray());

        // Object which wraps the private key and performs signing
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC").build(privateKey);

        // User certificate, contains the public key and identification
        X509Certificate x509cert = (X509Certificate) certificateChain[0];

        // helper class holding the signing algorithm, private key, attributes and others
        JcaSignerInfoGeneratorBuilder signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build() // Init SHA256 calculator
        );

        // signerInfoGenerator needs the x509 to ensure it can verify against the public key
        CMSgenerator.addSignerInfoGenerator(signerInfoGenerator.build(contentSigner, x509cert));

        this.CMSGenerator = CMSgenerator;
    }

    /**
     * Injects a signature into the PDF.
     **/
    public void addSignatureToDocument() {
        try {
            SignatureOptions signatureOptions = new SignatureOptions();
            signatureOptions.setPreferredSignatureSize(0x10000); // (0x10000 -> 4096) 3072 bit RSA key

            inputDocument.addSignature(baseSignature, signatureOptions);
            ExternalSigningSupport savedFile =
                    inputDocument.saveIncrementalForExternalSigning(outputFileStream);

            byte[] contentToSign = savedFile.getContent().readAllBytes();

            CMSSignedData CMSsignedData = this.CMSGenerator.generate(new CMSProcessableByteArray(contentToSign), true);

            // Hash encoded in ASN.1
            byte[] bytes = CMSsignedData.getEncoded();

            // Inject signature
            savedFile.setSignature(bytes);

        } catch (Exception e) {
            throw new RuntimeException("Error during signature injection", e);
        }
    }

    /**
     * Constructs the DigitalSigner class
     *
     * @param inputPath String path of the PDF file which has the input data contents
     * @param outputPath String path of the location to which a new, signed PDF file will be saved
     * @param initAlias Initial alias of the alias from under which required keys are obtained
     **/
    public DigitalSigner(String inputPath, String outputPath, String initAlias) {
        try {
            this.keyStorePassword = getSystemKeystorePassword();
            this.activeKeyStore = getSystemKeyStore();

            PDSignature baseSignature = new PDSignature();

            baseSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            baseSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            baseSignature.setSignDate(Calendar.getInstance());

            this.outputFileStream = new FileOutputStream(Path.of(outputPath).toFile());
            this.inputDocument = Loader.loadPDF(Paths.get(inputPath).toFile());
            this.alias = initAlias;
            this.baseSignature = baseSignature;
            this.prepareForSigning();

        } catch (Exception e) {
            throw new RuntimeException("DigitalSigner constructor exception", e);
        }
    }
}

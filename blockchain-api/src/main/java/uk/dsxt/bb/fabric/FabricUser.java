package uk.dsxt.bb.fabric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

public class FabricUser implements User {
    public static final Logger log = LogManager.getLogger(FabricUser.class.getName());

    private final PrivateKey filepathPk;
    private final String filepathCert;
    private final String mspId;

    public FabricUser(String filepathPk, String filepathCert, String mspId) {
        this.filepathPk = readPrivateKey(filepathPk);
        this.filepathCert = readPemCertificate(filepathCert);
        this.mspId = mspId;
    }

    @Override
    public String getName() {
        return "Admin";
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<>();
    }

    @Override
    public String getAccount() {
        return "";
    }

    @Override
    public String getAffiliation() {
        return "";
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {

            @Override
            public PrivateKey getKey() {
                try {
                    return filepathPk;
                } catch (Exception e) {
                    log.info("Failed to load private key {}", e);
                    return null;
                }
            }

            @Override
            public String getCert() {
                try {
                    return filepathCert;
                } catch (Exception e) {
                    log.info("Failed to get cert {}", e);
                    return null;
                }
            }
        };
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    private static String readPemCertificate(String filepath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (Exception e) {
            log.error("Cannot read pem certificate, filepath {}", filepath, e);
            return null;
        }
    }

    // read PKCS#1 key
    private static PrivateKey readPrivateKey(String filepath) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            PEMParser pemParser = new PEMParser(new FileReader(filepath));
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) pemParser.readObject()); //kp.getPrivate();

            log.info("read key: {}", privateKey);
            return privateKey;
        } catch (Exception e) {
            log.error("Cannot read filepath {}, key {}", filepath, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "FabricUser{" +
                "filepathPk=" + filepathPk +
                ", filepathCert='" + filepathCert + '\'' +
                ", mspId='" + mspId + '\'' +
                '}';
    }
}

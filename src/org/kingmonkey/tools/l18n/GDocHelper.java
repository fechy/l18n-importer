package org.kingmonkey.tools.l18n;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * GDoc Helper
 *
 * @author Fernando Giovanini <fernando@teamkingmonkey.com>
 * @since 13.11.17
 */
public class GDocHelper {

    private static final String MIME_TYPE = "text/csv";

    /** Directory to store user credentials for this application. */
    private static File DATA_STORE_DIR;

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE);

    private final String secretPath;
    private final Config config;

    GDocHelper(String secretPath, String configPath) throws IOException, GeneralSecurityException {
        this.secretPath = secretPath;
        this.config = this.loadConfig(configPath);

        DATA_STORE_DIR = new File(System.getProperty("user.home"), ".credentials/" + this.config.application_name);
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    }

    private Config loadConfig(String configPath) throws IOException {
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new NullPointerException("Cannot find " + configPath);
        }

        return JSON_FACTORY.fromString(readFile(configPath, Charset.defaultCharset()), Config.class);
    }


    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize(String secretPath) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = JSON_FACTORY.fromString(readFile(secretPath, Charset.defaultCharset()), GoogleClientSecrets.class);

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());

        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    private Drive getDriveService(String secretPath) throws IOException {
        Credential credential = authorize(secretPath);
        if (credential != null) {
            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(this.config.application_name)
                    .build();
        }

        return null;
    }

    public void get() {
        // Build a new authorized API client service.
        try {
            Drive service = this.getDriveService(this.secretPath);
            if (service == null) {
                return;
            }

            InputStream localizationFile = service.files().export(this.config.file_id, MIME_TYPE)
                                                          .executeMediaAsInputStream();
            CSVReader csvReader = new CSVReader(new InputStreamReader(localizationFile));
            final List<String[]> list = csvReader.readAll();

            for (int i = 0; i < this.config.languages.length; ++i) {
                String lang = this.config.languages[i];
                processLanguage(list, lang, i+1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLanguage(final List<String[]> list, String lang, final int index) throws FileNotFoundException, UnsupportedEncodingException {
        String filename = Objects.equals(lang, this.config.default_lang) ? "localization.properties" : "localization_"+lang+".properties";
        StringBuilder props = new StringBuilder();

        try {
            for (String[] line : list) {
                if (line == null) {
                    break;
                }

                if (index > line.length -1) {
                    System.out.println("WARNING: Missing column for " + lang);
                    continue;
                }

                String key = line[0];
                String value = line[index];

                props.append(key).append("=").append(value).append("\n");
            }

            writeFile(props, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile(StringBuilder data, String file) throws IOException {
        System.out.println("Updated L18n file: " + file);

        FileWriter writer = new FileWriter(file);
        writer.write(data.toString());
        writer.close();

        System.out.println(file + " Created!");
    }
}

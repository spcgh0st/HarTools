package ar.com.yamamoto.hartools;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.text.NumberFormat;
import java.util.Locale;

public class HarFile extends TextFile {
    private       JSONObject   json;
    private       Character    csvDelimiter;
    private       NumberFormat timingNumberFormat;
    final private Character    CSV_DEFAULT_DELIMITER = '\t';
    final private String       timingNames[]         = { "blocked", "dns", "connect", "send", "wait", "receive", "ssl" };

    public HarFile(String filename, String charset, String timingLocaleName)
        throws FileNotFoundException, IOException, JSONException, UnsupportedCharsetException {
        super(filename, charset);
        if (timingLocaleName != null) {
            final Locale timingLocale = new Locale(timingLocaleName);
            timingNumberFormat = NumberFormat.getNumberInstance(timingLocale);
        } else {
            timingNumberFormat = NumberFormat.getNumberInstance();
        }
        init();
    }

    public HarFile(String filename, String charset)
        throws FileNotFoundException, IOException, JSONException, UnsupportedCharsetException {
        super(filename, charset);
        init();
    }

    public HarFile(String filename)
        throws FileNotFoundException, IOException, JSONException {
        super(filename);
        init();
    }

    /**
     * Constructor helper
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JSONException
     */
    private void init()
        throws FileNotFoundException, IOException, JSONException {
        readFile();
        json = new JSONObject(getText());
        setDelimiter(CSV_DEFAULT_DELIMITER);
    }

    /**
     * Crawl JSONObject and generate a CSV String for entries
     *
     * @return CSV String
     * @throws JSONException
     */
    public String entriesToCsv()
        throws JSONException {
        StringBuilder csvBuffer = new StringBuilder();

        // CSV headers
        csvBuffer.append("url" + csvDelimiter
            + "host" + csvDelimiter
            + "method" + csvDelimiter
            + "startedDateTime" + csvDelimiter
            + "time" + csvDelimiter
            + "Response status" + csvDelimiter
            + "Response content mimeType" + csvDelimiter
            + "Response content size" + csvDelimiter
            + "Response headersSize" + csvDelimiter
            + "Response bodySize" + csvDelimiter
            + "Referer" + csvDelimiter);

        for (int i = 0; i < timingNames.length; i++) {
            csvBuffer.append("Timing " + timingNames[i]);
            if (i + 1 < timingNames.length) {
                csvBuffer.append(csvDelimiter);
            }
        }

        csvBuffer.append(LINE_ENDING);

        JSONArray entries = ((JSONObject) json.get("log")).getJSONArray("entries");

        for (int i = 0; i < entries.length(); i++) {
            csvBuffer.append(singleEntryToCsvLine(entries.getJSONObject(i)));
            if (i < csvBuffer.length() - 1) {
                csvBuffer.append(LINE_ENDING);
            }
        }

        return csvBuffer.toString();
    }

    /**
     * Process a single entry
     *
     * @param jsonObject
     * @return CSV String line
     * @throws JSONException
     */
    private String singleEntryToCsvLine(JSONObject jsonObject)
        throws JSONException {
        StringBuilder csvLineBuffer = new StringBuilder();

        JSONObject requestObject = jsonObject.getJSONObject("request");
        JSONObject responseObject = jsonObject.getJSONObject("response");
        JSONObject contentObject = responseObject.getJSONObject("content");
        JSONObject timingsObject = jsonObject.getJSONObject("timings");

        final String url = processKey(requestObject, "url");

        // General keys
        csvLineBuffer.append(url + csvDelimiter);
        csvLineBuffer.append(hostNameFromUrl(url) + csvDelimiter);
        csvLineBuffer.append(processKey(requestObject, "method") + csvDelimiter);
        csvLineBuffer.append(processKey(jsonObject, "startedDateTime") + csvDelimiter);
        csvLineBuffer.append(processKey(jsonObject, "time") + csvDelimiter);
        csvLineBuffer.append(processKey(responseObject, "status") + csvDelimiter);
        csvLineBuffer.append(processKey(contentObject, "mimeType") + csvDelimiter);
        csvLineBuffer.append(processKey(contentObject, "size") + csvDelimiter);
        csvLineBuffer.append(processKey(responseObject, "headersSize") + csvDelimiter);
        csvLineBuffer.append(processKey(responseObject, "bodySize") + csvDelimiter);
        csvLineBuffer.append(processKey(
            requestObject.getJSONArray("headers"), "Referer") + csvDelimiter);

        // Timing keys
        for (int i = 0; i < timingNames.length; i++) {
            csvLineBuffer.append(processKey(timingsObject, timingNames[i]));
            if (i + 1 < timingNames.length) {
                csvLineBuffer.append(csvDelimiter);
            }
        }

        return csvLineBuffer.toString();
    }

    private String hostNameFromUrl(final String urlString) {
        if (urlString != null) {
            final String urlLower = urlString.toLowerCase();
            if (urlLower.startsWith("http://") || urlLower.startsWith("https://")) {
                try {
                    URL url = new URL(urlString);
                    return url.getHost();
                } catch (MalformedURLException e) {
                    // fall through
                }
            }
        }
        return "";
    }

    /**
     * Process key and return value as String
     * (empty String, if not applicable or null)
     *
     * @param object JSONObject
     * @param key    Name of the key
     * @return Value of the key, or an empty String
     * @throws JSONException
     */
    private String processKey(JSONObject object, String key)
        throws JSONException {
        String returnValue = new String();

        if (object.has(key)) {
            if (!object.isNull(key)) {
                Object value = object.get(key);
                if (String.class.isInstance(value)) {
                    if (value != null) {
                        returnValue = (String) value;
                    }
                } else if (value instanceof Number) {
                    if (((Number) value).doubleValue() > 0.0) {
                        returnValue = timingNumberFormat.format(value);
                    }
                }
            }
        }

        return returnValue;
    }

    /**
     * Process key from JSONArray, and return value as String
     *
     * @param jsonArray JSONArray
     * @param key       Name of the JSONObject whose value we want to retrieve
     * @return Value as String, or an empty string if the
     * key was not found
     * @throws JSONException
     */
    private String processKey(JSONArray jsonArray, String key)
        throws JSONException {
        String returnValue = new String();

        for (Object entry : jsonArray.toArray()) {
            if (((JSONObject) entry).getString("name").equalsIgnoreCase(key)) {
                returnValue = processKey((JSONObject) entry, "value");
                break;
            }
        }

        return returnValue;
    }

    /**
     * Set CSV delimiter
     *
     * @param delimiter
     */
    public void setDelimiter(Character delimiter) {
        if (delimiter != null)
            csvDelimiter = delimiter;
    }
}

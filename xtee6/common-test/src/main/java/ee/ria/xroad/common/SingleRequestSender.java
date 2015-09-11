package ee.ria.xroad.common;

import java.io.InputStream;
import java.net.URI;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.http.HttpHeaders;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.ria.xroad.common.util.AsyncHttpSender;

import static ee.ria.xroad.common.util.AbstractHttpSender.CHUNKED_LENGTH;

/**
 * Performs a single request at a time and returns a response.
 */
public class SingleRequestSender {

    private static final Logger LOG =
            LoggerFactory.getLogger(SingleRequestSender.class);

    private static final int DEFAULT_CLIENT_TIMEOUT_SEC = 30;

    private static MessageFactory messageFactory = null;

    static {
        try {
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
    }

    private CloseableHttpAsyncClient client;
    private Integer timeoutSec;

    /**
     * Construct a request sender that uses the given HTTP client and
     * the specified timeout.
     * @param client the HTTP client this sender should use
     * @param timeoutSec timeout of the request in seconds
     */
    public SingleRequestSender(CloseableHttpAsyncClient client,
            Integer timeoutSec) {
        this(client);
        this.timeoutSec = timeoutSec;
    }

    /**
     * Construct a request sender that uses the given HTTP client and
     * the default timeout (30 seconds).
     * @param client the HTTP client this sender should use
     */
    public SingleRequestSender(CloseableHttpAsyncClient client) {
        this.client = client;
    }

    /**
     * Seconds the request in the given input stream to the specified address
     * and returns a response.
     * @param address address to which to send the request
     * @param contentType content type of the data in the input stream
     * @param content input stream containing the request data
     * @return response SOAP message
     * @throws Exception in case of any errors
     */
    public SOAPMessage sendRequestAndReceiveResponse(String address,
            String contentType, InputStream content) throws Exception {
        try (AsyncHttpSender sender = new AsyncHttpSender(client)) {
            sender.doPost(new URI(address), content, CHUNKED_LENGTH,
                    contentType);

            sender.waitForResponse(getTimeoutSec());

            String responseContentType = sender.getResponseContentType();
            MimeHeaders mimeHeaders = getMimeHeaders(responseContentType);

            LOG.debug("Received response with content type {}",
                    responseContentType);

            return messageFactory.createMessage(mimeHeaders,
                    sender.getResponseContent());
        }
    }

    private Integer getTimeoutSec() {
        if (timeoutSec == null) {
            return DEFAULT_CLIENT_TIMEOUT_SEC;
        }

        return timeoutSec;
    }

    private static MimeHeaders getMimeHeaders(String contentType) {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
        return mimeHeaders;
    }

}
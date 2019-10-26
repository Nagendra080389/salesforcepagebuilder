package com.sfpage.canvas;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import com.sfpage.canvas.Config;
import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * The utility method can be used to validate/verify the signed request. In this
 * case, the signed request is verified that it is from Salesforce and that it
 * has not been tampered with.
 * <p>
 * <strike>This utility class has two methods. One verifies and decodes the
 * request as a Java object the other as a JSON String.</strike>
 *
 * Slightly modified: only one method and it returns a JsonNode and a bypass for
 * debug purposed has been added when running on localhost
 *
 */
public class SignedRequest {

    public static JsonNode verifyAndDecodeAsJson(final HttpServletRequest request, final String input,
                                                 final String secret) throws SecurityException {

        final String[] split = SignedRequest.getParts(input);

        final String encodedSig = split[0];
        final String encodedEnvelope = split[1];

        final String json_envelope = new String(new Base64().decode(encodedEnvelope.getBytes()));

        final ObjectMapper mapper = new ObjectMapper();
        JsonNode json = null;
        try {
            json = mapper.readTree(json_envelope);
        } catch (final IOException e) {
            throw new SecurityException(String.format("Error [%s] deserializing JSON to JsonNode]", e.getMessage()), e);
        }

        final JsonNode algorithmCandidate = json.get("algorithm");
        if (algorithmCandidate == null) {
            throw new SecurityException("Error: algorithm missing from payload");
        }
        final String algorithm = algorithmCandidate.textValue();

        // for local debugging the signature verification
        // can be switched off - do not EVER use that in production
        if (Config.PARAMS.allowInsecureDebugOperation(request)) {
            return json;
        }

        // Here the check runs - throws an error if it fails
        SignedRequest.verify(secret, algorithm, encodedEnvelope, encodedSig);

        // If we got this far, then the request was not tampered with.
        // return the request as a JsonNode.
        return json;
    }

    private static String[] getParts(final String input) {

        if ((input == null) || (input.indexOf(".") <= 0)) {
            throw new SecurityException(String.format("Input [%s] doesn't look like a signed request", input));
        }

        final String[] split = input.split("[.]", 2);
        return split;
    }

    private static void verify(final String secret, final String algorithm, final String encodedEnvelope,
                               final String encodedSig)
            throws SecurityException {
        if ((secret == null) || (secret.trim().length() == 0)) {
            throw new IllegalArgumentException(
                    "secret is null, did you set your environment variable SFDC_SECRET?");
        }

        SecretKey hmacKey = null;
        try {
            final byte[] key = secret.getBytes();
            hmacKey = new SecretKeySpec(key, algorithm);
            final Mac mac = Mac.getInstance(algorithm);
            mac.init(hmacKey);

            // Check to see if the body was tampered with
            final byte[] digest = mac.doFinal(encodedEnvelope.getBytes());
            final byte[] decode_sig = new Base64(true).decode(encodedSig.getBytes());

            if (!Arrays.equals(digest, decode_sig)) {
                final String label = "Warning: Request was tampered with";
                throw new SecurityException(label);
            }
        } catch (final NoSuchAlgorithmException e) {
            throw new SecurityException(
                    String.format("Problem with algorithm [%s] Error [%s]", algorithm, e.getMessage()), e);
        } catch (final InvalidKeyException e) {
            throw new SecurityException(String.format("Problem with key [%s] Error [%s]", hmacKey, e.getMessage()), e);
        }

        // If we got here and didn't throw a SecurityException then all is good.
    }
}

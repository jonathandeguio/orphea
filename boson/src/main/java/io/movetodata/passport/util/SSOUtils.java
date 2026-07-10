package io.movetodata.passport.util;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.saml2.core.Saml2X509Credential;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSOUtils {

    public static boolean verifyAdToken(String idToken) {
        try {
            // Verify the ID Token
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            JWSVerifier verifier = new RSASSAVerifier(getAzurePublicKey(signedJWT.getHeader().getKeyID()));

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            // Get claims from the verified ID token
            Claims claims = Jwts.claims();
            claims.put("sub", signedJWT.getJWTClaimsSet().getSubject());
            claims.put("email", signedJWT.getJWTClaimsSet().getStringClaim("email"));

//            // Generate your own JWT token
//            String yourToken = Jwts.builder()
//                    .setClaims(claims)
//                    .setSubject(claims.get("sub").toString())
//                    .setIssuedAt(new Date())
//                    .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
//                    .signWith(SignatureAlgorithm.HS256, secretKey)
//                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("token", "yourToken");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String getCertificateString() {
        return "MIIC8DCCAdigAwIBAgIQfDYMKNx+z4xKxd0pzgWsGDANBgkqhkiG9w0BAQsFADA0MTIwMAYDVQQDEylNaWNyb3NvZnQgQXp1cmUgRmVkZXJhdGVkIFNTTyBDZXJ0aWZpY2F0ZTAeFw0yNDA5MjQxNTI4MjBaFw0yNzA5MjQxNTI4MjBaMDQxMjAwBgNVBAMTKU1pY3Jvc29mdCBBenVyZSBGZWRlcmF0ZWQgU1NPIENlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA07hW3OJ7UP72NiPuquwsMJd3XUrqEtMkPJmY9Dt5DPC6GoXMEDyheSiVRB9aXjAshnrlQShAv4RKVgQOVH+9X+Og0LtldP4xxZuSvBoipCA/9iDZDQaYnVhVQP5HTIGvsDFkn28ZvhY4RQppssOdOFKZIDSwv2sEkxW8bss2D5Euu1n/R2rPBwwpJME/YwYc1xJD7+YxVDiSSsXZrzvpI+/uj3kn1DrnKoIIj2ROGZ5DPXyLZ1lf1/6/ej7Ozo5SjQUzd4xFetPli9mcTjmxUuzp/bvhZqSMSJTkrbk8jPhu8S5sHZ+IqIjHEG3N7GvSDw5LeUtwpJsPsqGc1MszhQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBYl9EJ2tRuKwdH/DzzZg4224UFWmEjzQOZvRXyGLK3E6UZvsMeGJiFHgcfYB8sRb+5Zt3DEY8R2z7QjRVQK7TX6fdtrY559ycETmUQ/DbMzYao8Eu1Q+fkbHwRmkvtyROp+kv/BhTZhAWS9Oy0j8XYANSUoSoZL+98z95k75bFrhgCq7y9ICe9+0RR1v/3OpDhlsV4rROxxdveSKDZxK5JxGUrLGPoZ8SQt9ieeMBpaiIclokzONrBHSQw9AfNapOegFC5x0hM+PcQvvEwIPAIRznJZpn72PCkTkitIlCjRAaV36NhhU+SafxnQA0k/Am8w8cOa1OEeDrQ3FVB7GBz";
    }
    public static Saml2X509Credential getCertificate() {
        String x509CertificateString = getCertificateString();
        byte[] decodedCert = Base64.getDecoder().decode(x509CertificateString);
        CertificateFactory certFactory = null;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
        X509Certificate certificate = null;
        try {
            certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decodedCert));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
        return new Saml2X509Credential(certificate, Saml2X509Credential.Saml2X509CredentialType.VERIFICATION);
    }

    public static PublicKey getPublicKeyFromCertificate(String x509CertificateString) throws CertificateException {
//        String x509CertificateString = "MIIC8DCCAdigAwIBAgIQfDYMKNx+z4xKxd0pzgWsGDANBgkqhkiG9w0BAQsFADA0MTIwMAYDVQQDEylNaWNyb3NvZnQgQXp1cmUgRmVkZXJhdGVkIFNTTyBDZXJ0aWZpY2F0ZTAeFw0yNDA5MjQxNTI4MjBaFw0yNzA5MjQxNTI4MjBaMDQxMjAwBgNVBAMTKU1pY3Jvc29mdCBBenVyZSBGZWRlcmF0ZWQgU1NPIENlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA07hW3OJ7UP72NiPuquwsMJd3XUrqEtMkPJmY9Dt5DPC6GoXMEDyheSiVRB9aXjAshnrlQShAv4RKVgQOVH+9X+Og0LtldP4xxZuSvBoipCA/9iDZDQaYnVhVQP5HTIGvsDFkn28ZvhY4RQppssOdOFKZIDSwv2sEkxW8bss2D5Euu1n/R2rPBwwpJME/YwYc1xJD7+YxVDiSSsXZrzvpI+/uj3kn1DrnKoIIj2ROGZ5DPXyLZ1lf1/6/ej7Ozo5SjQUzd4xFetPli9mcTjmxUuzp/bvhZqSMSJTkrbk8jPhu8S5sHZ+IqIjHEG3N7GvSDw5LeUtwpJsPsqGc1MszhQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBYl9EJ2tRuKwdH/DzzZg4224UFWmEjzQOZvRXyGLK3E6UZvsMeGJiFHgcfYB8sRb+5Zt3DEY8R2z7QjRVQK7TX6fdtrY559ycETmUQ/DbMzYao8Eu1Q+fkbHwRmkvtyROp+kv/BhTZhAWS9Oy0j8XYANSUoSoZL+98z95k75bFrhgCq7y9ICe9+0RR1v/3OpDhlsV4rROxxdveSKDZxK5JxGUrLGPoZ8SQt9ieeMBpaiIclokzONrBHSQw9AfNapOegFC5x0hM+PcQvvEwIPAIRznJZpn72PCkTkitIlCjRAaV36NhhU+SafxnQA0k/Am8w8cOa1OEeDrQ3FVB7GBz";
        byte[] decodedCert = Base64.getDecoder().decode(x509CertificateString);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decodedCert));
        PublicKey publicKey = certificate.getPublicKey();
        return publicKey;
    }

    public static RSAPublicKey getAzurePublicKey(String kid) throws Exception {
        // Fetch the public key from Azure's JWKS endpoint
        // You'll need to implement the logic to fetch and parse the key
        URL url = new URL("https://login.microsoftonline.com/6aa76d38-1d16-4a92-9e60-b13be28d5f2a/discovery/v2.0/keys?appid=6835e383-6815-43a3-aa27-6d232df3b36d");
        // Implement logic to parse the JSON response and extract the public key
        // Return the public key as RSAPublicKey
        // Fetch the JWKS
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch JWKS: HTTP error code " + connection.getResponseCode());
        }

        // Read the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        // Parse the JWKS
        JWKSet jwkSet = JWKSet.parse(response.toString());
        List<JWK> keys = jwkSet.getKeys();

        // Find the key for the specific kid (key ID)
        for (JWK key : keys) {
            if (key.getKeyID().equals(kid) && key.getKeyType() == KeyType.RSA) {
                return ((RSAKey) key).toRSAPublicKey(); // Return the RSAPublicKey
            }
        }

        throw new RuntimeException("No valid RSA key found for kid: " + kid);

    }
}


// Get the public key from the certificate (e.g., from the IdP metadata)
//        PublicKey publicKey = getPublicKeyFromCertificate((String)parsedSAML.get("certificate"));
//
//        // Initialize the Signature instance with the correct algorithm
//        Signature sig = Signature.getInstance("SHA256withRSA"); // Change if the algorithm differs
//
//        // Initialize signature verification with the public key
//        sig.initVerify(publicKey);
//
//        // Supply the signed content (the data) to the Signature object
//        sig.update(((String) parsedSAML.get("signedInfo")).getBytes(StandardCharsets.UTF_8));
//
//        // Extract and decode the Base64-encoded signature from the SAML response
//        byte[] decodedSignature = Base64.getDecoder().decode((String) parsedSAML.get("signature"));

// Verify the signature
//        boolean isValid = sig.verify(decodedSignature);
//        SAMLSignatureProfileValidator samlSignatureProfileValidator  = new SAMLSignatureProfileValidator();
//
//        // Extract SAML response from the request
//        String samlResponse = request.getParameter("SAMLResponse");
//        String decodedSAML = new String(Base64.getDecoder().decode(samlResponse), "UTF-8");
//        PublicKey publicKey = getPublicKeyFromCertificate("MIIC8DCCAdigAwIBAgIQfDYMKNx+z4xKxd0pzgWsGDANBgkqhkiG9w0BAQsFADA0MTIwMAYDVQQDEylNaWNyb3NvZnQgQXp1cmUgRmVkZXJhdGVkIFNTTyBDZXJ0aWZpY2F0ZTAeFw0yNDA5MjQxNTI4MjBaFw0yNzA5MjQxNTI4MjBaMDQxMjAwBgNVBAMTKU1pY3Jvc29mdCBBenVyZSBGZWRlcmF0ZWQgU1NPIENlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA07hW3OJ7UP72NiPuquwsMJd3XUrqEtMkPJmY9Dt5DPC6GoXMEDyheSiVRB9aXjAshnrlQShAv4RKVgQOVH+9X+Og0LtldP4xxZuSvBoipCA/9iDZDQaYnVhVQP5HTIGvsDFkn28ZvhY4RQppssOdOFKZIDSwv2sEkxW8bss2D5Euu1n/R2rPBwwpJME/YwYc1xJD7+YxVDiSSsXZrzvpI+/uj3kn1DrnKoIIj2ROGZ5DPXyLZ1lf1/6/ej7Ozo5SjQUzd4xFetPli9mcTjmxUuzp/bvhZqSMSJTkrbk8jPhu8S5sHZ+IqIjHEG3N7GvSDw5LeUtwpJsPsqGc1MszhQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBYl9EJ2tRuKwdH/DzzZg4224UFWmEjzQOZvRXyGLK3E6UZvsMeGJiFHgcfYB8sRb+5Zt3DEY8R2z7QjRVQK7TX6fdtrY559ycETmUQ/DbMzYao8Eu1Q+fkbHwRmkvtyROp+kv/BhTZhAWS9Oy0j8XYANSUoSoZL+98z95k75bFrhgCq7y9ICe9+0RR1v/3OpDhlsV4rROxxdveSKDZxK5JxGUrLGPoZ8SQt9ieeMBpaiIclokzONrBHSQw9AfNapOegFC5x0hM+PcQvvEwIPAIRznJZpn72PCkTkitIlCjRAaV36NhhU+SafxnQA0k/Am8w8cOa1OEeDrQ3FVB7GBz");
//
//        Map<String, Object> parsedData = SamlMetaDataParser.parseSAMLResponse(samlResponse);
//
//        String signedInfo = (String) parsedData.get("signedInfo");
//        String signature = (String) parsedData.get("signature");
//        Signature sig = Signature.getInstance("SHA1withRSA"); // or other alg (i, e: SHA256WithRSA or others)
//        sig.initVerify(publicKey);
//        sig.update(signedInfo.getBytes());
//        Boolean valid = sig.verify(signedInfo.getBytes());
//        decodedSAML = decodedSAML.replace("urn:oasis:names:tc:SAML:2.0:assertion", XMLSignature.XMLNS);

// Parse the SAML response XML
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(new ByteArrayInputStream(decodedSAML.getBytes("UTF-8")));

// Set ID attribute for reference resolution
//        document.getDocumentElement().setIdAttribute("ID", true);

// Get the Assertion node
//        Node assertionNode = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Assertion").item(0);
//        String assertionId = assertionNode.getAttributes().getNamedItem("ID").getNodeValue();
//        System.out.println(assertionNode.getNamespaceURI());
//        Element el = document.getElementById(assertionId);
// Extract the signature
//        NodeList signatureNodes = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
//        if (signatureNodes.getLength() == 0) {
//            throw new SecurityException("No signature found in SAML response.");
//        }

// Create a DOMValidateContext with the public key and the signature node
//        DOMValidateContext validateContext = new DOMValidateContext(publicKey, signatureNodes.item(0));

// Create a signature factory
//        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
//        XMLSignature signature = signatureFactory.unmarshalXMLSignature(validateContext);
//
//        System.out.println("Document Element ID: " + document.getDocumentElement().getAttribute("ID"));
//        List<Reference> referenceNodes = signature.getSignedInfo().getReferences();
//        for (Reference r: referenceNodes) {
//            System.out.println("Reference URI: " + r.getURI());
//        }
//
//// Validate the signature
//        boolean isValid = signature.validate(validateContext);
//        if (!isValid) {
//            throw new SecurityException("SAML signature verification failed.");
//        }


//@GetMapping("/login")
//public void loginSSO(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    // Generate a unique relay state (could be a session ID or random token)
//    String relayState = UUID.randomUUID().toString();
//
//    // Store the relay state in the session
//    request.getSession().setAttribute("relayState", relayState);
//
//    // Generate the SAML AuthnRequest and include the relay state
////        String samlRequest =  SamlRequestUtil.generateSamlRequest(relayState);  // Ensure it returns a valid Base64-encoded SAML request
////        String encodedSamlRequest = URLEncoder.encode(samlRequest, "UTF-8");
//    String encodedRelayState = URLEncoder.encode(relayState, "UTF-8");
//
//    // Construct the Azure AD SAML redirect URL
//    String azureAdUrl = "https://login.microsoftonline.com/6aa76d38-1d16-4a92-9e60-b13be28d5f2a/saml2"
////                + "?SAMLRequest=" + encodedSamlRequest
//            + "?RelayState=" + encodedRelayState;
//
//    response.sendRedirect(azureAdUrl);
//}
//
//
//@PostMapping("/verify-token")
//public ResponseEntity<Map<String, String>> verifyToken(@RequestBody Map<String, String> body) {
//    String idToken = body.get("idToken");
//
//    try {
//        // Verify the ID Token
//        SignedJWT signedJWT = SignedJWT.parse(idToken);
//        JWSVerifier verifier = new RSASSAVerifier(getAzurePublicKey(signedJWT.getHeader().getKeyID()));
//
//        if (!signedJWT.verify(verifier)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid token"));
//        }
//
//        // Get claims from the verified ID token
//        Claims claims = Jwts.claims();
//        claims.put("sub", signedJWT.getJWTClaimsSet().getSubject());
//        claims.put("email", signedJWT.getJWTClaimsSet().getStringClaim("email"));
//
//
//        Map<String, String> response = new HashMap<>();
//        response.put("token", "yourToken");
//        return ResponseEntity.ok(response);
//
//    } catch (Exception e) {
//        e.printStackTrace();
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error processing token"));
//    }
//}

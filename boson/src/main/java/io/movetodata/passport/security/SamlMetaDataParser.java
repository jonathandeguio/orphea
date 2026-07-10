package io.movetodata.passport.security;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SamlMetaDataParser {


    public static Map<String, String> parseMetadata(InputStream inputStream) throws Exception {
        Map<String, String> metadataMap = new HashMap<>();

        // Step 1: Load the XML Document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        // Step 2: Locate the <EntityDescriptor> Node
        NodeList entityDescriptors = doc.getElementsByTagName("EntityDescriptor");

        if (entityDescriptors.getLength() > 0) {
            Element entityDescriptor = (Element) entityDescriptors.item(0);

            // Step 3: Extract Values
            String entityId = entityDescriptor.getAttribute("entityID");
            metadataMap.put("EntityID", entityId);

            // Extract nested SingleSignOnService
            NodeList singleSignOnServices = entityDescriptor.getElementsByTagName("SingleSignOnService");
            for (int i = 0; i < singleSignOnServices.getLength(); i++) {
                Element sso = (Element) singleSignOnServices.item(i);
                String ssoLocation = sso.getAttribute("Location");
                metadataMap.put("SingleSignOnServiceLocation", ssoLocation);
            }

            // Extract signing certificate if available
            NodeList signingCertificates = entityDescriptor.getElementsByTagName("KeyDescriptor");
            for (int i = 0; i < signingCertificates.getLength(); i++) {
                Element keyDescriptor = (Element) signingCertificates.item(i);
                if ("signing".equals(keyDescriptor.getAttribute("use"))) {
                    NodeList keyInfoList = keyDescriptor.getElementsByTagName("KeyInfo");
                    if (keyInfoList.getLength() > 0) {
                        Element keyInfo = (Element) keyInfoList.item(0);
                        NodeList x509DataList = keyInfo.getElementsByTagName("X509Data");
                        for (int j = 0; j < x509DataList.getLength(); j++) {
                            Element x509Data = (Element) x509DataList.item(j);
                            NodeList x509CertificateList = x509Data.getElementsByTagName("X509Certificate");
                            for (int k = 0; k < x509CertificateList.getLength(); k++) {
                                Element x509Certificate = (Element) x509CertificateList.item(k);
                                String certificate = x509Certificate.getTextContent();
                                metadataMap.put("SigningCertificate", certificate);
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("No EntityDescriptor found in the metadata.");
        }

        return metadataMap;
    }

    public static Map<String, Object> parseSAMLResponse(String response) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(response);
        String samlResponse = new String(decodedBytes, StandardCharsets.UTF_8);

        // Create a Map to hold the parsed data
        Map<String, Object> samlData = new HashMap<>();

        // Create a DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Important for handling namespaces
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the SAML Response XML
        Document document = builder.parse(new InputSource(new StringReader(samlResponse)));

        // Extract Issuer
        NodeList issuerList = document.getElementsByTagNameNS("*", "Issuer");
        if (issuerList.getLength() > 0) {
            samlData.put("issuer", issuerList.item(0).getTextContent());
        }

        // Extract Status
        NodeList statusList = document.getElementsByTagNameNS("*", "Status");
        if (statusList.getLength() > 0) {
            samlData.put("status", statusList.item(0).getTextContent());
        }

        // Extract SignatureValue
        NodeList signatureValueList = document.getElementsByTagNameNS("*", "SignatureValue");
        if (signatureValueList.getLength() > 0) {
            samlData.put("signature", signatureValueList.item(0).getTextContent());
        }
        NodeList signedInfo = document.getElementsByTagNameNS("*", "Signature");
        if (signedInfo.getLength() > 0) {
            samlData.put("signedInfo", signedInfo.item(0).getTextContent());
        }
        // Extract Certificate
        NodeList keyInfoList = document.getElementsByTagNameNS("*", "KeyInfo");
        if (keyInfoList.getLength() > 0) {
            NodeList x509DataList = keyInfoList.item(0).getChildNodes();
            for (int i = 0; i < x509DataList.getLength(); i++) {
                Node x509DataNode = x509DataList.item(i);
                if (x509DataNode.getLocalName() != null && x509DataNode.getLocalName().equals("X509Data")) {
                    NodeList x509Certificates = x509DataNode.getChildNodes();
                    for (int j = 0; j < x509Certificates.getLength(); j++) {
                        Node x509CertificateNode = x509Certificates.item(j);
                        if (x509CertificateNode.getLocalName() != null && x509CertificateNode.getLocalName().equals("X509Certificate")) {
                            samlData.put("certificate", x509CertificateNode.getTextContent());
                        }
                    }
                }
            }
        }
        // Extract Subject
        NodeList subjectList = document.getElementsByTagNameNS("*", "Subject");
        if (subjectList.getLength() > 0) {
            Element subjectElement = (Element) subjectList.item(0);
            String nameId = subjectElement.getElementsByTagNameNS("*", "NameID").item(0).getTextContent();
            samlData.put("nameId", nameId);
        }

        // Extract Attributes
        NodeList attributeStatementList = document.getElementsByTagNameNS("*", "AttributeStatement");
        if (attributeStatementList.getLength() > 0) {
            Map<String, String> attributes = new HashMap<>();
            NodeList attributeList = attributeStatementList.item(0).getChildNodes();
            for (int i = 0; i < attributeList.getLength(); i++) {
                if (attributeList.item(i) instanceof Element) {
                    Element attribute = (Element) attributeList.item(i);
                    String name = attribute.getAttribute("Name");
                    String value = attribute.getElementsByTagNameNS("*", "AttributeValue").item(0).getTextContent();
                    attributes.put(name, value);
                }
            }
            samlData.put("attributes", attributes);
        }

        return samlData;

    }
}

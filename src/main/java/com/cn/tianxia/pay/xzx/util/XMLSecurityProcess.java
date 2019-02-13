package com.cn.tianxia.pay.xzx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.utils.EncryptionConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XMLSecurityProcess {

	
	public static String ATIMESPUBFILEPATH ;
	
	static {
		//org.apache.xml.security.Init.init();
	}

	/**
	 * @param document
	 */
	public static void XMLEncrypted(Document document, String envType,
			String[] xPath) throws Exception {
		List<Node> encryptNodes = getNodes(document, xPath);
		if (encryptNodes.size() == 0)
			return;
		Key symmetricKey = KeyProvider.getkeyEncryedKey();

		Key keyEncryptKey = KeyProvider.getServerPublicKey(envType);  //公钥

		XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_v1dot5);
		keyCipher.init(XMLCipher.WRAP_MODE, keyEncryptKey);

		EncryptedKey encryptedKey = keyCipher
				.encryptKey(document, symmetricKey);

		XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128);
		xmlCipher.init(XMLCipher.ENCRYPT_MODE, symmetricKey);
		
		boolean encryptContentsOnly = true;

		for (Node node : encryptNodes) {
			if(node!=null){
				
				EncryptedData encryptedDataElement = xmlCipher.getEncryptedData();
				KeyInfo keyInfo = new KeyInfo(document);
				keyInfo.add(encryptedKey);
				encryptedDataElement.setKeyInfo(keyInfo);
				
				Element elementToEncrypt = (Element) node;
				xmlCipher.doFinal(document, elementToEncrypt, encryptContentsOnly);
			}
		}
	}

	/**
	 * 
	 * @param doc
	 * @param envType
	 * @param memberCode
	 * @param password
	 * @param aliasName
	 * @return
	 * @throws Exception
	 */
	public static Document XMLSignature(Document doc, String envType,
			String memberCode, String password, String aliasName)
			throws Exception {
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// Create a Reference to the enveloped document (in this case we are
		// signing the whole document, so a URI of "" signifies that) and
		// also specify the SHA1 digest algorithm and the ENVELOPED Transform.
		Reference ref = fac.newReference("", fac.newDigestMethod(
				DigestMethod.SHA1, null), Collections.singletonList(fac
				.newTransform(Transform.ENVELOPED,
						(TransformParameterSpec) null)), null, null);

		// Create the SignedInfo
		SignedInfo si = fac
				.newSignedInfo(fac.newCanonicalizationMethod(
						CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
						(C14NMethodParameterSpec) null), fac
						.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
						Collections.singletonList(ref));

		PrivateKey privateKey = null;
		privateKey = KeyProvider.getPrivateKey(envType, memberCode, password,
				password, aliasName, "PKCS12");

		// Create a DOMSignContext and specify the DSA PrivateKey and
		// location of the resulting XMLSignature's parent element
		DOMSignContext dsc = new DOMSignContext(privateKey, doc
				.getDocumentElement());

		// Create the XMLSignature (but don't sign it yet)
		XMLSignature signature = fac.newXMLSignature(si, null);

		// Marshal, generate (and sign) the enveloped signature
		signature.sign(dsc);

		// output the resulting document
		return doc;

	}
	
	public static List<Node> getNodes1(Document xml, String[] nodesXPath)
		throws XPathExpressionException {
		List<Node> nodes = new ArrayList<Node>();
		if (nodesXPath.length == 0) {
			nodes.add(xml.getDocumentElement());
			return nodes;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		for (String path : nodesXPath) {
			XPathExpression expr = xpath.compile(path);
			Object result = expr.evaluate(xml, XPathConstants.NODESET);
			if (result != null) {
				NodeList nodeList = (NodeList) result;
				for (int i = 0, n = nodeList.getLength(); i < n; i++) {
					nodes.add(nodeList.item(i));
				}
			}
		}

		return nodes;
	}

	public static List<Node> getNodes(Document xml, String[] nodesXPath)
			throws XPathExpressionException {
		List<Node> nodes = new ArrayList<Node>();
		if (nodesXPath.length == 0) {
			nodes.add(xml.getDocumentElement());
			return nodes;
		}
		
		
		for (String xPath : nodesXPath) {
			NodeList nd = xml.getElementsByTagName(xPath);
			nodes.add(nd.item(0));
		}

		return nodes;
	}

	public static boolean veryfySignature(Document doc, String envType)
			throws Exception {

		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		if (nl.getLength() == 0) {
			throw new Exception("Cannot find Signature element");
		}

		// public key
		Key pKey = KeyProvider.getServerPublicKey(envType);

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		DOMValidateContext valContext = new DOMValidateContext(pKey, nl.item(0));
		XMLSignature signature = fac.unmarshalXMLSignature(valContext);
		boolean coreValidity = signature.validate(valContext);

		System.err.println("Signature status:===" + coreValidity);
		// Check core validation status
		if (coreValidity == false) {
			System.err.println("Signature failed core validation");
			boolean sv = signature.getSignatureValue().validate(valContext);
			System.out.println("signature validation status: " + sv);
			// check the validation status of each Reference
			Iterator i = signature.getSignedInfo().getReferences().iterator();
			for (int j = 0; i.hasNext(); j++) {
				boolean refValid = ((Reference) i.next()).validate(valContext);
				System.out.println("ref[" + j + "] validity status: "
						+ refValid);
			}
		} else {
			System.out.println("Signature passed core validation");
		}
		return coreValidity;

	}

	public static boolean veryfySignature(String val, String signMsg,String cerFilePath) throws Exception {

		boolean flag = false;
		// public key
		PublicKey pk = (PublicKey) getServerPublicKey(cerFilePath);
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(pk);
		byte[] valbyte = val.getBytes("UTF-8");

		signature.update(valbyte);
		sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
		byte[] signedByte = decoder.decodeBuffer(signMsg);
//		FileOutputStream out = new FileOutputStream(new File("D:\\opt\\tmp\\aaaa.txt"));
//		out.write(signedByte);
//		out.close();
		 flag = signature.verify(signedByte);
		// Check core validation status
		 System.out.println(flag);
		return flag;
	
	}
	
	/**
	 * @param document
	 * @throws Exception
	 */
	public static void decryptXml(Document doc, String envType,
			String memberCode, String password, String aliasName)
			throws Exception {
		NodeList nodeList = doc.getElementsByTagNameNS(
				EncryptionConstants.EncryptionSpecNS,
				EncryptionConstants._TAG_ENCRYPTEDDATA);
		
		if (nodeList == null || nodeList.getLength() == 00) {
			return;
		}

		Key kek = KeyProvider.getPrivateKey(envType, memberCode, password,
				password, aliasName, "PKCS12");

		for (int i = 0; i < nodeList.getLength(); i++) {
			Element encryptedDataElement = (Element) nodeList.item(i);
			XMLCipher xmlCipher = XMLCipher.getInstance();
			
			xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
			xmlCipher.setKEK(kek);
			
			xmlCipher.doFinal(doc, encryptedDataElement);
		}
	}
	
	 private static Key getServerPublicKey(String envType) throws Exception
	    {
	    	//InputStream is = new FileInputStream(new File("C:\\opt\\tmp\\atimes.rsa.cer"));
	    	
//	    	String publicKeyPath = XMLSecurityProcess.class.getResource("pgistar.rsa.cer").toURI().getPath();

	    	InputStream is = new FileInputStream(envType);
	    	
	    	try{
	    		CertificateFactory certFactory=CertificateFactory.getInstance("X.509");
	        	
	    		Certificate cert = certFactory.generateCertificate(is);
	    		return cert.getPublicKey();
	    	}catch(Exception e){
	    		throw new Exception("read public key error.");
	    	}finally{
	    		is.close();
	    	}
	    }

}

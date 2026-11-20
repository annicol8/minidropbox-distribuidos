package compartido;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

	private Hash() { }

	public static String sha256(File f) throws IOException {
		try (InputStream in = new FileInputStream(f)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			byte[] dataBytes = new byte[8192];
			int nread;
			while ((nread = in.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}

			StringBuilder sb = new StringBuilder();
			for (byte x : md.digest()) {
				sb.append(String.format("%02x", x & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Algoritmo SHA-256 no disponible", e);
		}
	}
	
}

package compartido;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	
	private static final int TAM_BLOQUE = 8192;

	private Hash() { }

	public static String sha256(File f) throws IOException {
		try (FileInputStream in = new FileInputStream(f)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			byte[] dataBytes = new byte[TAM_BLOQUE];
			int nread;
			while ((nread = in.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			byte[] mdbytes = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i=0; i<mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Algoritmo SHA-256 no disponible", e);
		}
	}
	
}

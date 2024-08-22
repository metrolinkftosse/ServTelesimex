/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.StringTokenizer;

public final class DireccionMac {

	public String getMacAddress() throws IOException {
		String os = System.getProperty("os.name");
		try {
			if (os.startsWith("Windows")) {
				return windowsParseMacAddress(windowsRunIpConfigCommand());
			} else if (os.startsWith("Linux")) {
				return linuxParseMacAddress(linuxRunIfConfigCommand());
			} else {
				throw new IOException("Sistema operativo desconocido: " + os);
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
	}

	public String linuxParseMacAddress(String ipConfigResponse)
			throws ParseException {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (java.net.UnknownHostException ex) {
			ex.printStackTrace();
			throw new ParseException(ex.getMessage(), 0);
		}

		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;

		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			boolean containsLocalHost = line.indexOf(localHost) >= 0;

			if (containsLocalHost && lastMacAddress != null) {
				return lastMacAddress;
			}

			int macAddressPosition = line.indexOf("HWaddr");
			if (macAddressPosition <= 0)
				continue;

			String macAddressCandidate = line.substring(macAddressPosition + 6)
					.trim();
			if (linuxIsMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		ParseException ex = new ParseException(
				"Imposible obtener la dirección MAC " + localHost + " desde ["
						+ ipConfigResponse + "]", 0);
		ex.printStackTrace();
		throw ex;
	}

	public boolean linuxIsMacAddress(String macAddressCandidate) {
		if (macAddressCandidate.length() != 17)
			return false;
		return true;
	}

	public String linuxRunIfConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ifconfig");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1)
				break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();

		stdoutStream.close();

		return outputText;
	}

	public String windowsParseMacAddress(String ipConfigResponse)
			throws ParseException {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (java.net.UnknownHostException ex) {
			ex.printStackTrace();
			throw new ParseException(ex.getMessage(), 0);
		}

		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;

		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();

			if (line.endsWith(localHost) && lastMacAddress != null) {
				return lastMacAddress;
			}

			int macAddressPosition = line.indexOf(":");
			if (macAddressPosition <= 0)
				continue;

			String macAddressCandidate = line.substring(macAddressPosition + 1)
					.trim();
			if (windowsIsMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		ParseException ex = new ParseException(
				"Imposible obtener dirección MAC desde [" + ipConfigResponse
						+ "]", 0);
		ex.printStackTrace();
		throw ex;
	}

	public boolean windowsIsMacAddress(String macAddressCandidate) {
		if (macAddressCandidate.length() != 17)
			return false;

		return true;
	}

	public String windowsRunIpConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ipconfig /all");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1)
				break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();

		stdoutStream.close();

		return outputText;
	}

}

package picoded.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.UUID;

public class systemInfo {
	
	public static String localIPAddress() throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		return ip.getHostAddress();
	}
	
	public static String localHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	public static String localMACAddress() throws UnknownHostException, SocketException {
		InetAddress ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		
		byte[] mac = network.getHardwareAddress();
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
		}
		return sb.toString();
	}
	
	public static String systemaHash() {
		return UUID.randomUUID().toString();
	}
}
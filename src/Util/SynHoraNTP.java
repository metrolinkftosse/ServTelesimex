/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Metrolink
 */
public class SynHoraNTP {

    public String obtenerfechaNTP(String serverName) {
        String fecha = "";
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(2000);
            InetAddress address = InetAddress.getByName(serverName);
            byte[] buf = new NtpMessage().toByteArray();
            DatagramPacket packet =
                    new DatagramPacket(buf, buf.length, address, 123);
           
            NtpMessage.encodeTimestamp(packet.getData(), 40,
                    (System.currentTimeMillis() / 1000.0) + 2208988800.0);
            socket.send(packet);
            System.out.println("NTP request sent, waiting for response...\n");
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            NtpMessage msg = new NtpMessage(packet.getData());
            System.out.println("NTP server: " + serverName);
            fecha = (NtpMessage.timestampToString(msg.transmitTimestamp));
            return fecha;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fecha="";
            return fecha;
        }
        //return fecha;
    }
}

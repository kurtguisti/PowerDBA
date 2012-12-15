/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 13, 2001
 * Time: 10:06:16 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.util;

public final class IpAddressUtility {

    private static String CNAME = "IpAddressUtility";

	/**
	 *  Get the IP string from an array of bytes.  Only IP4Addr is supported at
	 *  this time.  This same code may be found in IpAddressTranslator.  It is
	 *  also here for optimization as this is a serializable object.
	 */
	public static String getIpStringFromIp4Addr(byte[] bytes) {

		// An ip4Addr IP address.
		if (bytes.length < 4) {
			Tracer.log("NrpReservation has invalid ip4Addr IP Address.", Tracer.ERROR, CNAME);
			return null;
		}

		int ip0 = (int) bytes[0];
		int ip1 = (int) bytes[1];
		int ip2 = (int) bytes[2];
		int ip3 = (int) bytes[3];

		Tracer.log("IP Address Before Conversion : " + ip0 + "." + ip1 + "." + ip2 + "." + ip3, Tracer.METHOD, CNAME);

		if (ip0 < 0) { ip0 += 256; }

		if (ip1 < 0) { ip1 += 256; }

		if (ip2 < 0) { ip2 += 256; }

		if (ip3 < 0) { ip3 += 256; }

		Tracer.log("IP Address After Conversion : " + ip0 + "." + ip1 + "." + ip2 + "." + ip3, Tracer.METHOD, CNAME);

		return (ip0 + "." + ip1 + "." + ip2 + "." + ip3);
	}


}

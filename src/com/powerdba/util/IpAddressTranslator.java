
/*
 * (C) Copyright 2001 StreamWorks Technologies
 * All rights reserved
 * This software is the property of SWT.
 * All use, reproduction, modification, or
 * distribution of this software is only permitted
 * in strict compliance with an express written
 * agreement with SWT.
 * This software contains and implements SWT
 * PROPRIETARY INFORMATION
 * Use or disclosure of SWT PROPRIETARY INFORMATION
 * is only permitted in strict compliance with an
 * express written agreement with SWT
 */

// WSN Source Header
// ************************************************************************
// *                                                                      *
// * This software is the property of WSN.  All use, reproduction,        *
// * modification, or distribution of this software is only permitted     *
// * in strict compliance with an express written agreement with WSN.     *
// *                                                                      *
// * This software contains and implements WSN PROPRIETARY INFORMATION    *
// * Use or disclosure of WSN PROPRIETARY INFORMATION is only permitted   *
// * in strict compliance with an express written agreement with WSN      *
// *                                                                      *
// ************************************************************************

/*
   Creation Date:    06-Sep-2000
   Archive:          $Archive:$
   Workfile:         $Workfile:$

   Language:         Java
   Target(s):        JDK 1.2

   Author:           $Author: dba $
   Check-in Date:    $Date: 2009-04-08 19:07:12 $
   Revision:         $Revision: 1.1 $

   Last Modified On: $Modtime:$

   Log:

   $Log: IpAddressTranslator.java,v $
   Revision 1.1  2009-04-08 19:07:12  dba
   *** empty log message ***

   Revision 1.1  2009/03/23 18:34:28  M-33J5TH1
   *** empty log message ***

   Revision 1.1  2009/01/13 16:27:21  kurtguisti
   *** empty log message ***

   Revision 1.1  2005/08/20 14:18:34  cvstest
   Initial load into CVS

   Revision 1.5  2002/08/16 23:20:05  akappen
   Removed the Log interface from the repository.

   Revision 1.4  2002/06/20 17:10:08  cbernard
   Fixed OrderTakerInterface so that it only inits the orb once to make
   calls to orderTaker.

   Added more SRP test code.

   Revision 1.3  2001/10/01 16:00:01  cbernard
   Added iteration 1 first cut functionality to SRP and KeepAliveHandler.

   Revision 1.2  2001/08/29 18:48:35  mmacdoug
   Removed some packages that it was importing and did not need.

   Revision 1.1  2001/08/29 18:42:25  mmacdoug
   Moved this file from nrp to util.

   Revision 1.8  2001/07/24 15:50:11  cbernard
   beautifying

   Revision 1.7  2001/04/19 16:49:30  kguisti
   New Logging

   Revision 1.6  2001/04/05 19:20:37  mmacdoug
   Implemented code to perform conversion from negative octets on string to byte.

   Revision 1.5  2001/04/05 14:51:43  kguisti
   no message

   Revision 1.4  2001/03/26 17:32:56  mmacdoug
   Added a method to translate IP address from bytes to string.

   Revision 1.3  2001/03/20 23:34:48  mmacdoug
   Added a method to complete the translation of IP4Addr IP addresses.

   Revision 1.2  2001/03/12 21:26:01  cbernard
   entered stable OrderTakerServant (it compiles now, anyway)


*/

// ************************************************************************

/*
  CLASS:


  KEYWORDS:


  OVERVIEW:


  USAGE:

  DESIGN:


  TEST PLAN:

	Action                                                         Status
	------------------------------------------------------------------------



  SEE ALSO:


  NOTES:


  LOG:

	13-Oct-2000   kguisti    Initial creation
*/
package com.powerdba.util;

import java.util.StringTokenizer;


public class IpAddressTranslator {

	private static String me;

	public IpAddressTranslator() {
		me = this.getClass().getName();
	}

	/**
	 *  Get the byte representation for the given IP address.
	 */
	public static byte[] IpStringToBytes(String theIpAddress) throws Exception {

		StringTokenizer tokens     = new StringTokenizer(theIpAddress, ".");
		int             tokenCount = tokens.countTokens();

		if ((tokenCount != 4) && (tokenCount != 6)) {
			throw new Exception(theIpAddress + " Is An Invalid IP Address.");
		}

		byte[] byteIPAddress = null;

		try {
			byteIPAddress = new byte[tokenCount];
		} catch (Exception e) {
			e.printStackTrace();
			Tracer.log(e, "Error initializing byte array", Tracer.ERROR, me);

			throw e;
		}

		try {
			for (int i = 0; tokens.hasMoreTokens(); i++) {
				String token = tokens.nextToken();
				int    octet = Integer.parseInt(token);

				if ((octet < -128) || (octet > 255)) {
					throw new Exception(theIpAddress
										+ " Is An Invalid IP Address.");
				}

				if (octet > 127) {

					// ... then we have some conversion to do
					//     convert the octet to a negative number < 128 ...
					int converted = octet + -256;

					token = Integer.toString(converted);
				}

				byteIPAddress[i] = (new Byte(token)).byteValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Tracer.log(e, "Error translating the String IP Address ("
					   + theIpAddress + ") to native byte array", Tracer.ERROR,
						   me);

			throw e;
		}

		return byteIPAddress;
	}

	/**
	 *  Get the string representation for the byte array.
	 */
	public static String bytesToIpString(byte[] bytes) {

		// An ip4Addr IP address only at this time.
		if (bytes.length < 4) {
			System.out.println(
				"Request to convert bytes to string on an invalid IP Address.");

			return null;
		}

		int ip0 = (int) bytes[0];
		int ip1 = (int) bytes[1];
		int ip2 = (int) bytes[2];
		int ip3 = (int) bytes[3];

		//Tracer.log("IP Address Before Conversion : " + ip0 + "." + ip1 + "."
		//		   + ip2 + "." + ip3, MINOR, me);

		if (ip0 < 0) {
			ip0 += 256;
		}

		if (ip1 < 0) {
			ip1 += 256;
		}

		if (ip2 < 0) {
			ip2 += 256;
		}

		if (ip3 < 0) {
			ip3 += 256;
		}

		//Tracer.log("IP Address After Conversion : " + ip0 + "." + ip1 + "."
		//		   + ip2 + "." + ip3, MINOR, me);

		return (ip0 + "." + ip1 + "." + ip2 + "." + ip3);
	}


}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.smilk.ws.relationextraction;
import java.util.Date;
/**
 *
 * @author fnoorala
 */

    
   
public class IdUniqueHelper {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";// "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final long BASE = 36;
    private static final String DIGIT = "0123456789";

	public static String encode(long num) {
		StringBuilder sb = new StringBuilder();

		while (num > 0) {
			sb.append(ALPHABET.charAt((int) (num % BASE)));
			num /= BASE;
		}

		return sb.reverse().toString();
	}

	public static String getId() {
		Date date = new Date();
		String id = encode(date.getTime());
		return id;
	}
	
}


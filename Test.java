import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class Test {

	public static void main(String[] args) throws IOException{
		FileInputStream read = new FileInputStream(new File("source.txt"));
		byte[] buffer = new byte[(int) (new File("source.txt")).length()]; // Pattern size should not overflow int
		int len = buffer.length;
		int total = 0;

		while (total < len) {
			int result = read.read(buffer, total, len - total);
			if (result == -1) {
				break;
			}
			total += result;
		}
		byte[] source = buffer;

		read = new FileInputStream(new File("patterns.txt"));
		buffer = new byte[(int) (new File("patterns.txt")).length()]; // Pattern size should not overflow int
		len = buffer.length;
		total = 0;

		while (total < len) {
			int result = read.read(buffer, total, len - total);
			if (result == -1) {
				break;
			}
			total += result;
		}
		ArrayList<byte[]> patterns = new ArrayList<byte[]>();
		byte[] temp = null;

		int i = 0, j=0;
		while(i<len){
			if(buffer[i]==('&')){
				i++;
				j = i;
				while(((j)<len) && buffer[j]!=('&')){ j++; }
				temp = (new String(buffer, i, j-i)).getBytes(); //FIX LATER
				patterns.add(temp);
				i = j;
			}

			i++;
		}
				for(i=0; i<patterns.size(); i++){
					System.out.println("Brute: "+bruteForce(patterns.get(i), source));
					System.out.println("Rabin-Karp: "+rabinKarp(patterns.get(i), source));
					System.out.println("KMP: "+ KMP(patterns.get(i), source));
					System.out.println("Boyer-Moore: "+boyerMoore(patterns.get(i), source));
				}
		
	}
	// This is the brute force algorithm
	private static int bruteForce(byte[] pattern, byte[] source){
		int plength = pattern.length;
		int slength = source.length;
		for(int i=0; i<=slength-plength; i++){
			for(int j=0; j<plength; j++){
				if(pattern[j]!=source[i+j])
					break;
				if(j==(plength-1))
					return i;
			}
		}
		return -1;
	}

	private static int rabinKarp(byte[] pattern, byte[] source){
		int plength = pattern.length, slength = source.length, hpat = hash(pattern, 0, plength);
		for(int i=0; i<=slength-plength; i++){
			if(hpat==(hash(source, i, (i+plength)))){
				for(int j=0; j<plength; j++){
					if(pattern[j]!=source[i+j])
						break;
					if(j==(plength-1))
						return i;
				}
			}
		}
		return -1;
	}

	private static int KMP(byte[] pattern, byte[] source){
		int plength = pattern.length, slength = source.length;
		int l=0, r=0;
		int[] f = new int[plength];
		int pblah = 0;
		preKMP(f, pattern, plength);
		while(r < slength) {
			pblah = r-l;
			//System.out.printf("%d %d, %d\n", l, r, pblah);
			if(source[r] == pattern[r-l]) {
				
				if((r-l + 1) == plength) {
					
					return l;
				}
				else {
					r++;
				}
			}
			else if(r==l) {
				l++;
				r++;
			}
			else if(r>l) {
				l = l+(r-l)-f[r-l];
			}

		}


		return -1;
	}

	private static void preKMP(int[] f, byte[] p, int len){
		int m = len;
		int k = 0;
		for(int i = 2; i<m; i++) {
			k = f[i-1];
			while(k > 0 && (p[i] != p[k+1])) {
				k = f[k];
			}
			if(k ==0 && ( p[i] !=  p[k+1])) {
				f[i] = 0;
			}
			else
				f[i] = k+1;
		}
	}


	private static int hash(byte[] pattern, int beg, int end){
		int hash = 0;
		for(int i=beg; i<end; i++){
			hash+=pattern[i];
		}
		return hash;
	}
	

	
	
	
	   /* @param haystack The string to be scanned
	   * @param needle The target string to search
	   * @return The start index of the substring
	   */
	  public static int boyerMoore(byte[] pattern, byte[] source) {
	    if (pattern.length == 0) {
	      return 0;
	    }
	    int charTable[] = makeCharTable(pattern);
	    int offsetTable[] = makeOffsetTable(pattern);
	    for (int i = pattern.length - 1, j; i < source.length;) {
	      for (j = pattern.length - 1; pattern[j] == source[i]; --i, --j) {
	        if (j == 0) {
	          return i;
	        }
	      }
	      // i += pattern.length - j; // For naive method
	      i += Math.max(offsetTable[pattern.length - 1 - j], charTable[source[i]]);
	    }
	    return -1;
	  }
	 
	  /**
	   * Makes the jump table based on the mismatched character information.
	   */
	  private static int[] makeCharTable(byte[] pattern) {
	    final int ALPHABET_SIZE = 256;
	    int[] table = new int[ALPHABET_SIZE];
	    for (int i = 0; i < table.length; ++i) {
	      table[i] = pattern.length;
	    }
	    for (int i = 0; i < pattern.length - 1; ++i) {
	      table[pattern[i]] = pattern.length - 1 - i;
	    }
	    return table;
	  }
	 
	  /**
	   * Makes the jump table based on the scan offset which mismatch occurs.
	   */
	  private static int[] makeOffsetTable(byte[] pattern) {
	    int[] table = new int[pattern.length];
	    int lastPrefixPosition = pattern.length;
	    for (int i = pattern.length - 1; i >= 0; --i) {
	      if (isPrefix(pattern, i + 1)) {
	        lastPrefixPosition = i + 1;
	      }
	      table[pattern.length - 1 - i] = lastPrefixPosition - i + pattern.length - 1;
	    }
	    for (int i = 0; i < pattern.length - 1; ++i) {
	      int slen = suffixLength(pattern, i);
	      table[slen] = pattern.length - 1 - i + slen;
	    }
	    return table;
	  }
	 
	  /**
	   * Is pattern[p:end] a prefix of pattern?
	   */
	  private static boolean isPrefix(byte[] pattern, int p) {
	    for (int i = p, j = 0; i < pattern.length; ++i, ++j) {
	      if (pattern[i] != pattern[j]) {
	        return false;
	      }
	    }
	    return true;
	  }
	 
	  /**
	   * Returns the maximum length of the substring ends at p and is a suffix.
	   */
	  private static int suffixLength(byte[] pattern, int p) {
	    int len = 0;
	    for (int i = p, j = pattern.length - 1;
	         i >= 0 && pattern[i] == pattern[j]; --i, --j) {
	      len += 1;
	    }
	    return len;
	  }

}

/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.Logger;

public final class Nxt2 {

	public static void main(String[] args) {
		String previousBlockGenerationSignature = "4566712d733f2f15f66575600496a04f2b4dd08b25a1d3bc04d9faa07967f496";
		byte[] previousBlockGenerationSignatureBytes = Convert.parseHexString(previousBlockGenerationSignature);
		
		String currentGeneratorPublicKey = "d37c5fe68f2b9eab32dbbf024e49f10a9552e9ced2703d900c792f20437b7924";
		byte[] generatorPublicKey = Convert.parseHexString(currentGeneratorPublicKey);
		
		byte[] generationSignature = Convert.parseHexString("b2a290aded1ad5071fb72794a09ccdf4af3cd4956f9cd8bd17dd331caa105d5d");
		
		MessageDigest digest = Crypto.sha256();
        digest.update(previousBlockGenerationSignatureBytes);
        byte[] generationSignatureHash = digest.digest(generatorPublicKey);
        if (!Arrays.equals(generationSignature, generationSignatureHash)) {
        	Logger.logMessage("Error verifying the signature, is not the same");
        }
        BigInteger hit = new BigInteger(1, new byte[]{generationSignatureHash[7], generationSignatureHash[6], generationSignatureHash[5], generationSignatureHash[4], generationSignatureHash[3], generationSignatureHash[2], generationSignatureHash[1], generationSignatureHash[0]});
		
        int currentTimeStamp = 114797112;
        int previousTimeStamp = 114797100;
        long baseTarget = 83333300;
        BigInteger effectiveBalance = BigInteger.valueOf(599999999);
        
        int elapsedTime = currentTimeStamp - previousTimeStamp;
        BigInteger effectiveBaseTarget = BigInteger.valueOf(baseTarget).multiply(effectiveBalance);
        BigInteger prevTarget = effectiveBaseTarget.multiply(BigInteger.valueOf(elapsedTime - 1));
        BigInteger target = prevTarget.add(effectiveBaseTarget);
        
        boolean uno = hit.compareTo(target) < 0;
        boolean dos = hit.compareTo(prevTarget) >= 0;
        boolean tres = elapsedTime <= 8 + 1;
		
        boolean result = hit.compareTo(target) < 0
		   && (hit.compareTo(prevTarget) >= 0
		   || (elapsedTime <= 8 + 1)
		   || (Constants.isTestnet ? elapsedTime > 300 : elapsedTime > 3600)
		   || Constants.isOffline);
        
        System.out.print(result);
        
	}
}

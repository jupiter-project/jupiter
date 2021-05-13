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

package nxt.mint;

import nxt.Attachment;
import nxt.Constants;
import nxt.CurrencyMinting;
import nxt.Nxt;
import nxt.NxtException;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.crypto.HashFunction;
import nxt.http.API;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.TrustAllSSLProvider;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MintWorker2 {

    public static void main(String[] args) {
        //increased
    	long prevBaseTarget = 7809008;
    	long baseTarget = 8746088;
        double percentage = ((double) baseTarget/prevBaseTarget)*100 - 100;
        System.out.println("Increased block base target value from " + prevBaseTarget + " to " + baseTarget + " ("+ percentage +"%)");

         prevBaseTarget = 9795618;
    	 baseTarget = 9689826;
    	 percentage = 100 - ((double)baseTarget/prevBaseTarget)*100;
        System.out.println("Decreased block base target value from " + prevBaseTarget + " to " + baseTarget + " ("+ percentage +"%)");

        int tsLastBock = 219825;
        int tsFirstBock = 20592;
        
        int lastHeigh = 50;
        int firstHeigh = 25;
        
        double avarageTime = ((double)(tsLastBock - tsFirstBock)/(double)(lastHeigh-firstHeigh));
    	
        System.out.println("[BGR STATS] From block " + firstHeigh 
    	+ " to " + lastHeigh + " = " + avarageTime + " seconds per block");

    }

    
}
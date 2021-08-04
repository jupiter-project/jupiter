/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2020 Sigwo Technologies
 * Copyright © 2020-2021 Jupiter Project Developers
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

package nxt.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public interface Time {

    int getTime();

    final class EpochTime implements Time {
        public int getTime() {
            return Convert.toEpochTime(System.currentTimeMillis());
        }
    }
    
    static String getDateTimeStringInfo(long timestamp) {
    	Date hitTimeDate  = new Date(Convert.fromEpochTime(new Long(timestamp).intValue()));
    	Calendar hitTimeCalendar = Calendar.getInstance();
    	hitTimeCalendar.setTime(hitTimeDate);
    	int hours = hitTimeCalendar.get(Calendar.HOUR_OF_DAY);
    	int minutes = hitTimeCalendar.get(Calendar.MINUTE);
    	int seconds = hitTimeCalendar.get(Calendar.SECOND);
    	int ms = hitTimeCalendar.get(Calendar.MILLISECOND);
    	String dateInfo = twoNumberPlaces(hours)+":"+twoNumberPlaces(minutes)+":"+twoNumberPlaces(seconds)+":"+ms;
    	
    	return dateInfo;
    }
    static String twoNumberPlaces(int number) {
    	if (number < 10) {
    		return "0"+number;
    	}else {
    		return String.valueOf(number);
    	}
    }

    final class ConstantTime implements Time {

        private final int time;

        public ConstantTime(int time) {
            this.time = time;
        }

        public int getTime() {
            return time;
        }
    }

    final class FasterTime implements Time {

        private final int multiplier;
        private final long systemStartTime;
        private final int time;

        public FasterTime(int time, int multiplier) {
            if (multiplier > 1000 || multiplier <= 0) {
                throw new IllegalArgumentException("Time multiplier must be between 1 and 1000");
            }
            this.multiplier = multiplier;
            this.time = time;
            this.systemStartTime = System.currentTimeMillis();
        }

        public int getTime() {
            return time + (int)((System.currentTimeMillis() - systemStartTime) / (1000 / multiplier));
        }
    }

    final class CounterTime implements Time {

        private final AtomicInteger counter;

        public CounterTime(int time) {
            this.counter = new AtomicInteger(time);
        }

        public int getTime() {
            return counter.incrementAndGet();
        }
    }
}
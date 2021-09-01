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

package nxt;

public interface Fee {

    long getFee(TransactionImpl transaction, Appendix appendage);

    long MIN_FEE = 10L;
    long MIN_DATA_FEE = 100L;
    long MIN_CONSTANT_DATA_FEE = 500L;
    long MIN_PRUNABLE_FEE = 1L;
    
    long NEW_MIN_FEE = 5000L;
    long NEW_MIN_DATA_FEE = 5000L;
    long NEW_MIN_CONSTANT_DATA_FEE = 7000L;
    long NEW_MIN_PRUNABLE_FEE = 1000L;
    long NEW_MIN_MESSAGE_FEE = 1000L;
    

    Fee DEFAULT_FEE = new Fee.ConstantFee(MIN_FEE);

    Fee NONE = new Fee.ConstantFee(0L);

    final class ConstantFee implements Fee {

        private final long fee;

        public ConstantFee(long fee) {
            this.fee = fee;
        }

        @Override
        public long getFee(TransactionImpl transaction, Appendix appendage) {
            return fee;
        }

    }

    abstract class SizeBasedFee implements Fee {

        private final long constantFee;
        private final long feePerSize;
        private final int unitSize;

        public SizeBasedFee(long feePerSize) {
            this(0, feePerSize);
        }

        public SizeBasedFee(long constantFee, long feePerSize) {
            this(constantFee, feePerSize, 1024);
        }

        public SizeBasedFee(long constantFee, long feePerSize, int unitSize) {
            this.constantFee = constantFee;
            this.feePerSize = feePerSize;
            this.unitSize = unitSize;
        }

        // the first size unit is free if constantFee is 0
        @Override
        public final long getFee(TransactionImpl transaction, Appendix appendage) {
            int size = getSize(transaction, appendage) - 1;
            if (size < 0) {
                return constantFee;
            }
            long totalFee = Math.addExact(constantFee, Math.multiplyExact((long) (size / unitSize), feePerSize));
            return totalFee;
        }

        public abstract int getSize(TransactionImpl transaction, Appendix appendage);

    }

}

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony.mocks;

import static android.telephony.SubscriptionManager.INVALID_PHONE_INDEX;
import static android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID;
import static android.telephony.SubscriptionManager.DEFAULT_SUBSCRIPTION_ID;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.RemoteException;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyIntents;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

// must extend SubscriptionController as some people use it directly within-process
public class SubscriptionControllerMock extends SubscriptionController {
    final AtomicInteger mDefaultDataSubId = new AtomicInteger(INVALID_SUBSCRIPTION_ID);
    final ITelephonyRegistry.Stub mTelephonyRegistry;
    final int[][] mSlotIdxToSubId;

    public static SubscriptionController init(Phone phone) {
        throw new RuntimeException("not implemented");
    }
    public static SubscriptionController init(Context c, CommandsInterface[] ci) {
        throw new RuntimeException("not implemented");
    }
    public static SubscriptionController getInstance() {
        throw new RuntimeException("not implemented");
    }

    public SubscriptionControllerMock(Context c, ITelephonyRegistry.Stub tr, int phoneCount) {
        super(c);
        mTelephonyRegistry = tr;
        mSlotIdxToSubId = new int[phoneCount][];
        for (int i = 0; i < phoneCount; i++) {
            mSlotIdxToSubId[i] = new int[1];
            mSlotIdxToSubId[i][0] = INVALID_SUBSCRIPTION_ID;
        }
    }

    protected void init(Context c) {
        mContext = c;
    }

    @Override
    public int getDefaultDataSubId() {
        return mDefaultDataSubId.get();
    }

    @Override
    public void setDefaultDataSubId(int subId) {
        if (subId == DEFAULT_SUBSCRIPTION_ID) {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }

        mDefaultDataSubId.set(subId);
        broadcastDefaultDataSubIdChanged(subId);
    }

    private void broadcastDefaultDataSubIdChanged(int subId) {
        // Broadcast an Intent for default data sub change
        Intent intent = new Intent(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
        mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    @Override
    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return INVALID_SUBSCRIPTION_ID;
        }
        return subIds[0];
    }

    @Override
    public void notifySubscriptionInfoChanged() {
        try {
            mTelephonyRegistry.notifySubscriptionInfoChanged();
        } catch (RemoteException ex) {}
    }
    @Override
    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx, String cp){
        throw new RuntimeException("not implemented");
    }
    @Override
    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getActiveSubInfoCount(String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getAllSubInfoCount(String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getActiveSubInfoCountMax() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int addSubInfoRecord(String iccId, int slotId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public boolean setPlmnSpn(int slotId, boolean showPlmn, String plmn, boolean showSpn,
            String spn) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int setIconTint(int tint, int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int setDisplayName(String displayName, int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int setDisplayNameUsingSrc(String displayName, int subId, long nameSource) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int setDisplayNumber(String number, int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int setDataRoaming(int roaming, int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int setMccMnc(String mccMnc, int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getSlotId(int subId) {
        throw new RuntimeException("not implemented");
    }

    private boolean isInvalidSlotId(int slotIdx) {
        if (slotIdx < 0 || slotIdx >= mSlotIdxToSubId.length) return true;
        return false;
    }

    @Override
    public int[] getSubId(int slotIdx) {
        if (isInvalidSlotId(slotIdx)) {
            return null;
        }
        return mSlotIdxToSubId[slotIdx];
    }
    public void setSlotSubId(int slotIdx, int subId) {
        if (isInvalidSlotId(slotIdx)) {
            throw new RuntimeException("invalid slot specified" + slotIdx);
        }
        if (mSlotIdxToSubId[slotIdx][0] != subId) {
            mSlotIdxToSubId[slotIdx][0] = subId;
            try {
                mTelephonyRegistry.notifySubscriptionInfoChanged();
            } catch (RemoteException ex) {}
        }
    }
    @Override
    public int getPhoneId(int subId) {
        if (subId == DEFAULT_SUBSCRIPTION_ID) {
            subId = getDefaultSubId();
        }

        if (subId <= INVALID_SUBSCRIPTION_ID) return INVALID_PHONE_INDEX;

        for (int i = 0; i < mSlotIdxToSubId.length; i++) {
            if (mSlotIdxToSubId[i][0] == subId) return i;
        }
        return INVALID_PHONE_INDEX;
    }
    @Override
    public int clearSubInfo() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getDefaultSubId() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public void setDefaultSmsSubId(int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getDefaultSmsSubId() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public void setDefaultVoiceSubId(int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getDefaultVoiceSubId() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public void clearDefaultsForInactiveSubIds() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int[] getSubIdUsingSlotId(int slotId) {
        return getSubId(slotId);
    }
    @Override
    public List<SubscriptionInfo> getSubInfoUsingSlotIdWithCheck(int slotId, boolean needCheck,
            String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public void updatePhonesAvailability(Phone[] phones) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int[] getActiveSubIdList() {
        throw new RuntimeException("not implemented");
    }
    @Override
    public boolean isActiveSubId(int subId) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public int getSimStateForSlotIdx(int slotIdx) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public void setSubscriptionProperty(int subId, String propKey, String propValue) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        throw new RuntimeException("not implemented");
    }
    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        throw new RuntimeException("not implemented");
    }
}

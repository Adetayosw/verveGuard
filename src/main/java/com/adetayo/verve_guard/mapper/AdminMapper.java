package com.adetayo.verve_guard.mapper;

import com.adetayo.verve_guard.dto.response.*;
import com.adetayo.verve_guard.entity.*;

public interface AdminMapper {

    FlaggedAttemptResponse toFlaggedAttemptResponse(FlaggedAttempt attempt);

    BlacklistedMerchantResponse toBlacklistedMerchantResponse(BlacklistedMerchant merchant);

    BlacklistedCardResponse toBlacklistedCardResponse(BlacklistedCard card);
}
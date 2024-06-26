package com.example.forumx_user.config.security.helpers;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class AuthenticationHelper {

    public static void attachUserId(Authentication authentication, Long accountId) {
        Object originalDetails = authentication.getDetails();
        if (originalDetails instanceof Details details) {
            details.setUserId(accountId);
        } else {
            Details details = new Details()
                    .setOriginal(originalDetails)
                    .setUserId(accountId);
            ((OAuth2AuthenticationToken) authentication).setDetails(details);
        }
    }

    public static Long retrieveUserId(Authentication authentication) {
        Details details = (Details) authentication.getDetails();
        return details.getUserId();
    }

    @Data
    @Accessors(chain = true)
    private static class Details {

        private Object original;
        private Long userId;

    }

}

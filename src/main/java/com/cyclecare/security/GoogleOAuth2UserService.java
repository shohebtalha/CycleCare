package com.cyclecare.security;

import com.cyclecare.domain.ActivityLevel;
import com.cyclecare.domain.AuthProvider;
import com.cyclecare.domain.User;
import com.cyclecare.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!GOOGLE_REGISTRATION_ID.equals(registrationId)) {
            throw oauthFailure("unsupported_provider", "Only Google sign-in is supported.");
        }

        Map<String, Object> attributes = oauthUser.getAttributes();
        String email = normalizedString(attributes.get("email"));
        String subject = stringValue(attributes.get("sub"));
        boolean emailVerified = Boolean.TRUE.equals(attributes.get("email_verified"));

        if (email.isBlank() || subject.isBlank()) {
            throw oauthFailure("missing_google_identity", "Google did not return the required account identity.");
        }
        if (!emailVerified) {
            throw oauthFailure("unverified_google_email", "Google email must be verified before sign-in.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .map(existing -> updateGoogleProfile(existing, subject, attributes))
                .orElseGet(() -> createGoogleUser(email, subject, attributes));

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole())),
                attributes,
                "email");
    }

    private User createGoogleUser(String email, String subject, Map<String, Object> attributes) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setName(displayName(attributes));
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderUserId(subject);
        user.setAvatarUrl(stringValue(attributes.get("picture")));
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setActivityLevel(ActivityLevel.MODERATE);
        return userRepository.save(user);
    }

    private User updateGoogleProfile(User user, String subject, Map<String, Object> attributes) {
        user.setProviderUserId(subject);
        user.setAvatarUrl(stringValue(attributes.get("picture")));
        user.setEmailVerified(true);
        user.setEnabled(true);
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            user.setAuthProvider(AuthProvider.GOOGLE);
        }
        return userRepository.save(user);
    }

    private String displayName(Map<String, Object> attributes) {
        String name = stringValue(attributes.get("name")).trim();
        return name.isBlank() ? normalizedString(attributes.get("email")) : name;
    }

    private String normalizedString(Object value) {
        return stringValue(value).trim().toLowerCase(Locale.ROOT);
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private OAuth2AuthenticationException oauthFailure(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code, description, null));
    }
}

package com.dteam0928.sangsae.auth.oauth;

import com.dteam0928.sangsae.auth.oauth.provider.GoogleOAuthProvider;
import com.dteam0928.sangsae.common.config.AppConfig;
import com.dteam0928.sangsae.user.model.AuthProvider;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 사용 가능한 소셜 제공자 목록.
 * client-id 가 설정되지 않은 제공자는 등록하지 않는다 → 설정 전에도 앱은 정상 기동되고
 * 해당 버튼만 "준비 중" 안내가 나간다.
 * 제공자 추가 시: provider 구현 클래스 작성 후 fromConfig 에 등록 코드만 추가.
 */
public class OAuthProviderRegistry {

    private static final Logger log = Logger.getLogger(OAuthProviderRegistry.class.getName());

    private final Map<AuthProvider, OAuthProvider> providers = new EnumMap<>(AuthProvider.class);

    public void register(OAuthProvider provider) {
        providers.put(provider.type(), provider);
    }

    public Optional<OAuthProvider> find(AuthProvider type) {
        return Optional.ofNullable(providers.get(type));
    }

    public Set<AuthProvider> enabledProviders() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    public static OAuthProviderRegistry fromConfig(AppConfig config) {
        OAuthProviderRegistry registry = new OAuthProviderRegistry();
        String baseUrl = config.require("app.base-url").replaceAll("/+$", "");

        OAuthClientConfig google = clientConfig(config, AuthProvider.GOOGLE, baseUrl, "openid email profile");
        if (google != null) registry.register(new GoogleOAuthProvider(google));

        log.info("활성화된 소셜 로그인: " + registry.enabledProviders());
        return registry;
    }

    /** oauth.{name}.client-id / client-secret / redirect-uri / scope */
    private static OAuthClientConfig clientConfig(AppConfig config, AuthProvider type, String baseUrl,
                                                  String defaultScope) {
        String prefix = "oauth." + type.getPathName() + ".";
        String clientId = config.get(prefix + "client-id");
        if (clientId == null) return null;
        return new OAuthClientConfig(
                clientId,
                config.get(prefix + "client-secret"),
                config.get(prefix + "redirect-uri", baseUrl + "/oauth/" + type.getPathName() + "/callback"),
                config.get(prefix + "scope", defaultScope));
    }
}
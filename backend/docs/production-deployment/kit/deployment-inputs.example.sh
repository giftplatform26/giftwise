# Copy to deployment-inputs.sh and fill before handoff. Do not store passwords here.
export GW_SOURCE_ROOT='/Users/jijujones/gift-platform'
export GW_KIT="$GW_SOURCE_ROOT/backend/docs/production-deployment/kit"
export GW_OUT="$GW_SOURCE_ROOT/backend/target/deployment"
export GW_RELEASE_ID='2026-11-05-r1'
export GW_SERVER='REPLACE_WITH_SERVER_IPV4'
export GW_SSH_USER='REPLACE_WITH_SUDO_SSH_USER'
export GW_SSH_KEY='/absolute/path/to/deployment-private-key'
export GW_DOMAIN='REPLACE_WITH_REAL_PUBLIC_HOSTNAME'
export GW_TLS_EMAIL='REPLACE_WITH_TLS_CONTACT_EMAIL'
export GW_ADMIN_EMAIL='REPLACE_WITH_FIRST_ADMIN_EMAIL'
export GW_EXPECTED_SHA256='REPLACE_AFTER_BUILD_WITH_TRUSTED_ARCHIVE_SHA256'

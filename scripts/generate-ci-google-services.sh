#!/usr/bin/env bash
set -euo pipefail

TARGET="${1:-app/google-services.json}"

mkdir -p "$(dirname "$TARGET")"
cat > "$TARGET" <<'EOF'
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "ci-placeholder",
    "storage_bucket": "ci-placeholder.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000000000",
        "android_client_info": {
          "package_name": "com.zoti321.c2cmarket"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "ci-placeholder-api-key"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
EOF

echo "Wrote CI stub google-services.json to $TARGET"

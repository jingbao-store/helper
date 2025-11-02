#!/bin/bash

set -e

echo "=========================================="
echo "  镜宝助手 - Release 构建"
echo "=========================================="
echo ""

# 设置 JAVA_HOME（优先使用 Android Studio JBR）
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  echo "✓ 使用 Android Studio JBR: $JAVA_HOME"
elif [ -d "/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home"
  echo "✓ 使用 Android Studio Preview JBR: $JAVA_HOME"
else
  echo "⚠️  未找到 Android Studio JBR，使用系统默认 Java"
fi

echo ""
echo "Java 版本:"; java -version 2>&1 | head -1
echo ""

echo "🧹 清理..."
./gradlew clean >/dev/null 2>&1 || true

echo "🔨 构建 Release APK..."
./gradlew assembleRelease

APK_DIR="app/build/outputs/apk/release"
APK_PATH="$APK_DIR/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
  # 接受未签名产物
  if [ -f "$APK_DIR/app-release-unsigned.apk" ]; then
    APK_PATH="$APK_DIR/app-release-unsigned.apk"
    UNSIGNED=1
  fi
fi

if [ ! -f "$APK_PATH" ]; then
  echo "❌ 构建失败: 未找到 APK 文件"; exit 1
fi

# 读取版本号
VERSION_NAME=$(grep 'versionName = ' app/build.gradle.kts | sed 's/.*versionName = "\(.*\)".*/\1/')
VERSION_CODE=$(grep 'versionCode = ' app/build.gradle.kts | sed 's/.*versionCode = \(.*\)/\1/')

NEW_APK_NAME="jingbao-helper-v${VERSION_NAME}.apk"
NEW_APK_PATH="$APK_DIR/${NEW_APK_NAME}"

echo "📝 重命名 APK -> $NEW_APK_NAME"
cp -f "$APK_PATH" "$NEW_APK_PATH"

echo "📦 APK: $NEW_APK_PATH"
APK_SIZE=$(ls -lh "$NEW_APK_PATH" | awk '{print $5}')
APK_SIZE_BYTES=$(stat -f%z "$NEW_APK_PATH" 2>/dev/null || stat -c%s "$NEW_APK_PATH" 2>/dev/null)
echo "📏 大小: $APK_SIZE ($APK_SIZE_BYTES bytes)"

if [ "${UNSIGNED:-0}" = "1" ]; then
  echo "ℹ️  注意：当前产物为未签名 APK（app-release-unsigned.apk）。如需正式发布，请配置签名并生成已签名 APK。"
else
  # 验证签名（如果可用）
  if command -v apksigner >/dev/null 2>&1; then
    echo "🔐 验证签名..."
    apksigner verify --verbose "$NEW_APK_PATH" || true
  elif [ -d "$HOME/Library/Android/sdk/build-tools" ]; then
    APKSIGNER=$(find "$HOME/Library/Android/sdk/build-tools" -name apksigner 2>/dev/null | head -1)
    if [ -n "$APKSIGNER" ]; then
      echo "🔐 验证签名..."
      "$APKSIGNER" verify --verbose "$NEW_APK_PATH" || true
    fi
  fi
fi

echo ""
echo "✅ 完成。"
echo "  版本: $VERSION_NAME ($VERSION_CODE)"
echo "  路径: $NEW_APK_PATH"
echo "=========================================="



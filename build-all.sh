

echo "======================================"
echo "  ENESRTPZONE"
echo "  Developer: @Enes9899"
echo "  Platforms: Paper, Folia, Purpur"
echo "======================================"
echo ""

PLATFORMS=("paper" "folia" "purpur")
SUCCESS=0

for platform in "${PLATFORMS[@]}"; do
    echo "[1/3] Building EnesRtpZone for $platform..."
    if mvn package -P$platform -DskipTests -q; then
        echo "SUCCESS: EnesRtpZone-$platform.jar created"
        SUCCESS=$((SUCCESS + 1))
    else
        echo "✗ ERROR: $platform build failed!"
        exit 1
    fi
done

echo ""
echo "======================================"
echo "  ALL BUILDS COMPLETED SUCCESSFULLY!"
echo "  $SUCCESS/3 JARs created"
echo "======================================"
echo ""
echo "Output files in target/:"
ls -1 target/EnesRtpZone-*.jar 2>/dev/null || echo "No JARs found"

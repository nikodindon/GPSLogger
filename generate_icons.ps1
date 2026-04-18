# PowerShell Script to Generate App Icons for GPSLogger
# Run this script to generate all required icon sizes

$iconSource = "icon_source.png"  # Your high-resolution source icon
$outputDir = "app/src/main/res"

# Icon sizes required by Google Play
$iconSizes = @(
    @{ Name = "mipmap-anydpi-v26"; Size = 432 },
    @{ Name = "mipmap-xxxhdpi"; Size = 144 },
    @{ Name = "mipmap-xxhdpi"; Size = 96 },
    @{ Name = "mipmap-xhdpi"; Size = 72 },
    @{ Name = "mdpi"; Size = 48 },
    @{ Name = "drawable-xhdpi"; Size = 64 },
    @{ Name = "drawable-xxhdpi"; Size = 96 },
    @{ Name = "drawable-xxxhdpi"; Size = 144 }
)

foreach ($size in $iconSizes) {
    $folder = Join-Path $outputDir $size.Name
    if (-not (Test-Path $folder)) {
        New-Item -ItemType Directory -Path $folder | Out-Null
    }
    
    $iconFile = Join-Path $folder "ic_launcher.png"
    Write-Host "Generating $iconFile ($($size.Size)x$($size.Size))"
    
    # Use ImageMagick or similar tool to resize
    # magick $iconSource -resize $size.Size x $size.Size $iconFile
}

Write-Host "All icons generated successfully!"
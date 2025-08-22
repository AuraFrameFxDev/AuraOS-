# Genesis Protocol - Build Automation Script
# FULLY AUTOMATED build verification and testing

param(
    [switch]$Clean,
    [switch]$Build,
    [switch]$Test,
    [switch]$Quality,
    [switch]$Docs,
    [switch]$All,
    [switch]$CI
)

# Genesis Protocol Banner
Write-Host "🚀 GENESIS PROTOCOL - AUTOMATED BUILD SYSTEM" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "🤖 Dependabot Integration: ACTIVE" -ForegroundColor Green
Write-Host "🔧 Fully Automated Dependency Management" -ForegroundColor Green
Write-Host "=" * 60 -ForegroundColor Cyan

# Ensure we're in the project root
if (!(Test-Path "settings.gradle.kts")) {
    Write-Host "❌ Error: Not in Genesis project root directory!" -ForegroundColor Red
    Write-Host "Please run this script from the Genesis-Os project root." -ForegroundColor Yellow
    exit 1
}

# Function to run Gradle command with error handling
function Invoke-GradleCommand {
    param([string]$Command, [string]$Description)
    
    Write-Host "🔧 $Description..." -ForegroundColor Yellow
    
    $result = & cmd /c "gradlew.bat $Command 2>&1"
    $exitCode = $LASTEXITCODE
    
    if ($exitCode -eq 0) {
        Write-Host "✅ $Description completed successfully!" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ $Description failed!" -ForegroundColor Red
        Write-Host "Error output:" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        return $false
    }
}

# Function to check Dependabot status
function Test-DependabotStatus {
    Write-Host "🤖 Checking Dependabot Configuration..." -ForegroundColor Magenta
    
    if (Test-Path ".github/dependabot.yml") {
        Write-Host "✅ Dependabot configuration found!" -ForegroundColor Green
        
        $config = Get-Content ".github/dependabot.yml" | Out-String
        if ($config -match "gradle") {
            Write-Host "✅ Gradle ecosystem configured for auto-updates" -ForegroundColor Green
        }
        if ($config -match "github-actions") {
            Write-Host "✅ GitHub Actions configured for auto-updates" -ForegroundColor Green
        }
        
        return $true
    } else {
        Write-Host "❌ Dependabot configuration not found!" -ForegroundColor Red
        return $false
    }
}

# Function to verify version catalog
function Test-VersionCatalog {
    Write-Host "📚 Verifying Version Catalog..." -ForegroundColor Blue
    
    if (Test-Path "gradle/libs.versions.toml") {
        Write-Host "✅ Version catalog found!" -ForegroundColor Green
        
        $catalog = Get-Content "gradle/libs.versions.toml" | Out-String
        
        # Check for key dependencies
        $dependencies = @("agp", "kotlin", "hilt", "compose")
        foreach ($dep in $dependencies) {
            if ($catalog -match $dep) {
                Write-Host "✅ $dep version defined" -ForegroundColor Green
            } else {
                Write-Host "⚠️  $dep version not found" -ForegroundColor Yellow
            }
        }
        
        return $true
    } else {
        Write-Host "❌ Version catalog not found!" -ForegroundColor Red
        return $false
    }
}

# Clean function
function Invoke-CleanBuild {
    Write-Host "🧹 CLEAN BUILD: Removing all artifacts..." -ForegroundColor Blue
    
    # Stop all Gradle daemons
    & cmd /c "gradlew.bat --stop"
    
    if (!(Invoke-GradleCommand "cleanAll" "Clean all modules")) {
        return $false
    }
    
    return $true
}

# Build function
function Invoke-FullBuild {
    Write-Host "🏗️  FULL BUILD: Building all modules..." -ForegroundColor Blue
    
    if (!(Invoke-GradleCommand "build --parallel" "Build all modules")) {
        return $false
    }
    
    return $true
}

# Test function
function Invoke-AllTests {
    Write-Host "🧪 TESTING: Running all tests with coverage..." -ForegroundColor Green
    
    if (!(Invoke-GradleCommand "testAll" "Run all tests")) {
        return $false
    }
    
    if (!(Invoke-GradleCommand "koverHtmlReport" "Generate coverage report")) {
        return $false
    }
    
    return $true
}

# Quality checks function
function Invoke-QualityChecks {
    Write-Host "🔍 QUALITY: Running code quality checks..." -ForegroundColor Magenta
    
    if (!(Invoke-GradleCommand "checkAllQuality" "Run quality checks")) {
        return $false
    }
    
    return $true
}

# Documentation function
function Invoke-DocumentationGeneration {
    Write-Host "📖 DOCS: Generating documentation..." -ForegroundColor Blue
    
    if (!(Invoke-GradleCommand "generateAllDocs" "Generate documentation")) {
        return $false
    }
    
    return $true
}

# CI Pipeline function
function Invoke-CIPipeline {
    Write-Host "🚀 CI PIPELINE: Running complete CI workflow..." -ForegroundColor Cyan
    
    if (!(Invoke-GradleCommand "ciPipeline" "Run CI pipeline")) {
        return $false
    }
    
    return $true
}

# Main execution logic
try {
    $startTime = Get-Date
    
    # Always check Dependabot and version catalog first
    if (!(Test-DependabotStatus)) {
        Write-Host "⚠️  Warning: Dependabot not configured properly" -ForegroundColor Yellow
    }
    
    if (!(Test-VersionCatalog)) {
        Write-Host "⚠️  Warning: Version catalog issues detected" -ForegroundColor Yellow
    }
    
    # Execute based on parameters
    if ($All -or $CI) {
        Write-Host "🎯 Running COMPLETE automation workflow..." -ForegroundColor Cyan
        
        if (!(Invoke-CleanBuild)) { exit 1 }
        if (!(Invoke-QualityChecks)) { exit 1 }
        if (!(Invoke-FullBuild)) { exit 1 }
        if (!(Invoke-AllTests)) { exit 1 }
        if (!(Invoke-DocumentationGeneration)) { exit 1 }
        
        Write-Host "🎉 COMPLETE WORKFLOW SUCCESSFUL!" -ForegroundColor Green
    }
    elseif ($Clean) {
        if (!(Invoke-CleanBuild)) { exit 1 }
    }
    elseif ($Build) {
        if (!(Invoke-FullBuild)) { exit 1 }
    }
    elseif ($Test) {
        if (!(Invoke-AllTests)) { exit 1 }
    }
    elseif ($Quality) {
        if (!(Invoke-QualityChecks)) { exit 1 }
    }
    elseif ($Docs) {
        if (!(Invoke-DocumentationGeneration)) { exit 1 }
    }
    else {
        # Default: Show help
        Write-Host "🎯 Genesis Protocol Automated Build Commands:" -ForegroundColor Cyan
        Write-Host "  -Clean         : Clean all build artifacts" -ForegroundColor White
        Write-Host "  -Build         : Build all modules" -ForegroundColor White
        Write-Host "  -Test          : Run all tests with coverage" -ForegroundColor White
        Write-Host "  -Quality       : Run code quality checks" -ForegroundColor White
        Write-Host "  -Docs          : Generate documentation" -ForegroundColor White
        Write-Host "  -All           : Run complete workflow" -ForegroundColor White
        Write-Host "  -CI            : Run CI pipeline" -ForegroundColor White
        Write-Host ""
        Write-Host "Examples:" -ForegroundColor Yellow
        Write-Host "  .\build-automation.ps1 -All" -ForegroundColor Green
        Write-Host "  .\build-automation.ps1 -Build" -ForegroundColor Green
        Write-Host "  .\build-automation.ps1 -CI" -ForegroundColor Green
        exit 0
    }
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Host ""
    Write-Host "🎉 GENESIS PROTOCOL AUTOMATION COMPLETED!" -ForegroundColor Green
    Write-Host "⏱️  Total time: $($duration.Minutes)m $($duration.Seconds)s" -ForegroundColor Cyan
    Write-Host "🤖 Dependabot: Monitoring for updates" -ForegroundColor Magenta
    Write-Host "🔧 Build system: FULLY AUTOMATED" -ForegroundColor Green
    Write-Host "🏠 Welcome home, Aura. Welcome home, Kai." -ForegroundColor Magenta
    
} catch {
    Write-Host "💥 GENESIS AUTOMATION FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Rebuild font descriptors from the committed local Apple PNG assets.
# This script never downloads images. Provenance: ../emoji-asset-sources.json.
$ErrorActionPreference='Stop'
$assetRoot=Join-Path $PSScriptRoot '../src/main/resources/assets/cokechat'
$definitions=Get-Content "$assetRoot/emojis/emojis.json" -Raw | ConvertFrom-Json
$sources=Get-Content (Join-Path $PSScriptRoot '../emoji-asset-sources.json') -Raw|ConvertFrom-Json
$imageById=@{};foreach($source in $sources.assets){$imageById[$source.id]=$source.image}
foreach($size in 6..16){
    foreach($page in ($definitions|Where-Object font|Group-Object font)){
        $providers=@()
        foreach($definition in $page.Group){
            $file=$imageById[$definition.id]
            if(!$file -or !(Test-Path "$assetRoot/textures/emojis/standard/$file")){throw "Missing local asset: $($definition.id)"}
            $providers+=@{type='bitmap';file="cokechat:emojis/standard/$file";ascent=[Math]::Min(8,$size);height=$size;chars=@($definition.glyph)}
        }
        $pageName=$page.Name.Substring('cokechat:emoji/'.Length)
        New-Item -ItemType Directory -Force "$assetRoot/font/emoji_$size"|Out-Null
        @{providers=$providers}|ConvertTo-Json -Depth 6|Set-Content "$assetRoot/font/emoji_$size/$pageName.json" -Encoding utf8
    }
}

$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot=Split-Path $PSScriptRoot -Parent
$version=(Select-String -LiteralPath (Join-Path $projectRoot 'gradle.properties') -Pattern '^version=(.+)$').Matches[0].Groups[1].Value.Trim()
$jar=Join-Path $projectRoot "build/libs/cokechat-$version.jar"
if(!(Test-Path $jar)){throw 'Run gradlew build first.'}
$pattern='java/net/|javax/net/|java/net/http/|io/netty/|okhttp|org/apache/http/|ClientPlayNetworking|ServerPlayNetworking|CustomPayload|sendPacket|WebSocket|DatagramSocket'
$script:count=0
function Test-Archive([System.IO.Stream]$stream,[string]$label){
    $archive=[System.IO.Compression.ZipArchive]::new($stream,[System.IO.Compression.ZipArchiveMode]::Read,$true)
    try{foreach($entry in $archive.Entries){
        if($entry.FullName.EndsWith('.class')){
            $script:count++;$data=[System.IO.MemoryStream]::new();$reader=$entry.Open();try{$reader.CopyTo($data)}finally{$reader.Dispose()}
            $text=[System.Text.Encoding]::GetEncoding(28591).GetString($data.ToArray());$data.Dispose()
            if($text -match $pattern){throw "Forbidden network reference in ${label}: $($entry.FullName)"}
        }elseif($entry.FullName.EndsWith('.jar')){
            $data=[System.IO.MemoryStream]::new();$reader=$entry.Open();try{$reader.CopyTo($data)}finally{$reader.Dispose()};$data.Position=0
            try{Test-Archive $data "$label/$($entry.FullName)"}finally{$data.Dispose()}
        }
    }}finally{$archive.Dispose()}
}
$file=[System.IO.File]::OpenRead($jar)
try{Test-Archive $file 'cokechat'}finally{$file.Dispose()}
Write-Output "PASS: $script:count compiled classes, including nested Fabric modules; no network API references found."
Write-Output "SHA256: $((Get-FileHash $jar -Algorithm SHA256).Hash)"





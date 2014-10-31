#define MyAppName "PeerBox"
#define MyAppVersion "0.0.1"
#define MyAppPublisher "PeerBox Developers"
#define MyAppURL "http://www.peerbox.ch"
#define MyAppExeName "peerbox.exe"
#define Api_Server_Port "30000"

#define BASE_DIR "Windows_x64"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{0CDB99D8-C302-445C-B6E3-F21C48EFA080}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}

VersionInfoProductName={#MyAppName}
VersionInfoProductVersion={#MyAppVersion}
VersionInfoDescription="PeerBox - Distributed File Sharing and Synchronization Application"
VersionInfoVersion={#MyAppVersion}

DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
LicenseFile={#BASE_DIR}\License.rtf
OutputDir=.
OutputBaseFilename=peerbox_setup_x64
SetupIconFile={#BASE_DIR}\peerbox64.ico
Compression=lzma
SolidCompression=yes

; only supporte x64
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; 

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "{#BASE_DIR}\peerbox.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#BASE_DIR}\peerbox64.ico"; DestDir: "{app}"; Flags: ignoreversion
; -- java packages
Source: "{#BASE_DIR}\peerbox-0.0.1-SNAPSHOT.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#BASE_DIR}\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion createallsubdirs recursesubdirs
; -- context menu extension
; regserver: dll is registered in the windows registry
Source: "{#BASE_DIR}\ContextMenu.dll"; DestDir: "{app}"; Flags: ignoreversion regserver
Source: "{#BASE_DIR}\cpprest120_2_2.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#BASE_DIR}\License.rtf"; DestDir: "{app}"; Flags: ignoreversion

[Dirs]
Name: "{app}\lib"

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[ThirdParty]
UseRelativePaths=True

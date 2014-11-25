#include "win_installer_config.iss"

#define MyAppName "PeerBox"
#define MyAppPublisher "PeerBox Developers"
#define MyAppURL "http://www.peerbox.ch"
#define MyAppExeName "peerbox.exe"

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
LicenseFile={#BaseDir}\License.rtf
OutputDir=.
OutputBaseFilename={#OutputFilename}
SetupIconFile={#BaseDir}\peerbox64.ico
Compression=lzma
SolidCompression=yes

ArchitecturesAllowed={#ArchitecturesAllowed}
ArchitecturesInstallIn64BitMode={#ArchitecturesInstallIn64BitMode}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; 

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "{#BaseDir}\peerbox.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#BaseDir}\peerbox64.ico"; DestDir: "{app}"; Flags: ignoreversion

; -- java packages
Source: "{#BaseDir}\{#PeerBoxJar}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#BaseDir}\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion createallsubdirs recursesubdirs

; -- context menu extension
; regserver: dll is registered in the windows registry
Source: "{#BaseDir}\ContextMenu.dll"; DestDir: "{app}"; Flags: ignoreversion regserver uninsrestartdelete restartreplace
Source: "{#BaseDir}\cpprest120_2_2.dll"; DestDir: "{app}"; Flags: ignoreversion uninsrestartdelete restartreplace
Source: "{#BaseDir}\msvcp120.dll"; DestDir: "{app}"; Flags: ignoreversion uninsrestartdelete restartreplace
Source: "{#BaseDir}\msvcr120.dll"; DestDir: "{app}"; Flags: ignoreversion uninsrestartdelete restartreplace

Source: "{#BaseDir}\License.rtf"; DestDir: "{app}"; Flags: ignoreversion

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

[Code]
function InitializeSetup(): Boolean;
var
  ErrorCode: Integer;
  JavaInstalled : Boolean;
  Result1 : Boolean;
  Versions: TArrayOfString;
  I: Integer;
begin
  if RegGetSubkeyNames(HKLM, 'SOFTWARE\JavaSoft\Java Runtime Environment', Versions) then
  begin
    for I := 0 to GetArrayLength(Versions)-1 do
      if JavaInstalled = true then
      begin
        //do nothing
      end else
      begin
        // check the subkeys: they have the form x.x or x.x.x_xx -> we look at the first and third digit (Java 1.8)
        if ( Versions[I][2]='.' ) and ( ( StrToInt(Versions[I][1]) > 1 ) or ( ( StrToInt(Versions[I][1]) = 1 ) and ( StrToInt(Versions[I][3]) >= 8 ) ) ) then
        begin
          JavaInstalled := true;
        end else
        begin
          JavaInstalled := false;
        end;
      end;
  end else
  begin
    JavaInstalled := false;
  end;

  // if java not installed, we show a message box that informs the user and advises them to download java.
  // we can open a browser with the java.com download page where the user can download the newest java.
  // the setup quits (user has to restart it)
  if JavaInstalled then
  begin
    Result := true;
  end else
  begin
    // ask to install JRE
    Result1 := MsgBox('This tool requires Java Runtime Environment version 1.8 or newer to run. Please download and install the JRE and run this setup again. Do you want to download it now?',
    mbConfirmation, MB_YESNO) = idYes;
    if Result1 = false then
    begin
      Result:=false;
    end else
    begin
      // open browser with java.com download page
      Result:=false;
      ShellExec('open', 'http://www.java.com/getjava/', '', '', SW_SHOWNORMAL, ewNoWait, ErrorCode);
    end;
  end;

end;


end.

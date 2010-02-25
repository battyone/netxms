; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
#include "setup.iss"
OutputBaseFilename=nxagent-1.1.0-rc10

[Files]
Source: "..\..\libnetxms\Release\libnetxms.dll"; DestDir: "{app}\bin"; BeforeInstall: StopService; Flags: ignoreversion
Source: "..\..\libnetxms\Release\libnetxms.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\..\libnxlp\Release\libnxlp.dll"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\..\libnxlp\Release\libnxlp.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\core\Release\nxagentd.exe"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\core\Release\nxagentd.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\winnt\Release\winnt.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\winnt\Release\winnt.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\win9x\Release\win9x.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\win9x\Release\win9x.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\winperf\Release\winperf.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\winperf\Release\winperf.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\wmi\Release\wmi.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\wmi\Release\wmi.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\ping\Release\ping.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\ping\Release\ping.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\portCheck\Release\portcheck.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\portCheck\Release\portcheck.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\ecs\Release\ecs.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\ecs\Release\ecs.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\logscan\Release\logscan.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\logscan\Release\logscan.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\logwatch\Release\logwatch.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\logwatch\Release\logwatch.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\odbcquery\Release\odbcquery.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\odbcquery\Release\odbcquery.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\sms\Release\sms.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\sms\Release\sms.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\ups\Release\ups.nsm"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\subagents\ups\Release\ups.pdb"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\..\..\contrib\nxagentd.conf-dist"; DestDir: "{app}\etc"; Flags: ignoreversion
Source: "..\..\install\windows\files\libeay32.dll"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\..\install\windows\files\libexpat.dll"; DestDir: "{app}\bin"; Flags: ignoreversion

#include "common.iss"

Function GetCustomCmdLine(Param: String): String;
Begin
  Result := '';
End;

